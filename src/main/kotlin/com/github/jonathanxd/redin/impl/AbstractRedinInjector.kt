/**
 *      Redin - Dependency injection built on top of Kores
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2021 JonathanxD <https://github.com/JonathanxD/Redin>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.redin.impl

import com.github.jonathanxd.iutils.`object`.LateInit
import com.github.jonathanxd.iutils.annotation.Named
import com.github.jonathanxd.iutils.box.MutableBox
import com.github.jonathanxd.iutils.string.ToStringHelper
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.jonathanxd.kores.util.isKotlin
import com.github.jonathanxd.redin.*
import java.lang.InstantiationException
import java.lang.reflect.*
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaConstructor

abstract class AbstractRedinInjector : Injector, BindListModifier {

    protected abstract val binds: List<Bind<*>>

    protected abstract val mutableBinds: MutableList<Bind<*>>

    protected abstract fun addScopedBinding(bind: Bind<*>, instance: Any)
    protected abstract fun addLateTarget(target: InjectionTarget)
    protected abstract fun addHotSwappableTarget(target: Injector.HotSwappable)

    protected abstract fun removeLateTargetsThat(predicate: (InjectionTarget) -> Boolean)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(klass: Class<T>, scope: BindScope): T {
        return createBinding(klass, scope).provide(InjectionTargetType(klass)) as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> injectMembers(instance: T, scope: BindScope): T {
        val ctx = InjectionContext()
        ctx.instance = instance
        val type = instance::class.java

        return createBinding(type, scope, ctx, ignoreConstructors = true).provide(
                InjectionTargetType(type)
        ) as T
    }

    override fun bind() {
        this.hotswap(this.binds)
    }

    override fun bind(ctx: BindContext.() -> Unit) {
        ctx(BindContext(this, this))

        this.bind()
    }

    override fun getAllBinds(): List<Bind<*>> =
            this.binds.toList()

    override fun getAllBinds(matcher: BindMatcher): List<Bind<*>> =
            this.binds.filter { matcher.match(it) }.toList()

    override fun <T : Any> provide(
            typeInfo: TypeInfo<T>,
            qualifiers: List<AnnotationContainer>,
            scope: BindScope
    ): () -> T {
        val matcher = GenericCommonBindMatcher(typeInfo, qualifiers, scope)
        val descriptor = InjectionTargetDescriptor(TypeUtil.toType(typeInfo), qualifiers)
        return {
            @Suppress("UNCHECKED_CAST")
            this.getRequiredBind(matcher).provide(descriptor) as T
        }
    }

    override fun <T : Any> provideLazyBind(
            typeInfo: TypeInfo<T>,
            qualifiers: List<AnnotationContainer>,
            scope: BindScope,
            safe: LazyThreadSafetyMode
    ): () -> T {
        val bind: Bind<*> by lazy(safe) {
            this.getRequiredBind(
                    GenericCommonBindMatcher(typeInfo, qualifiers, scope)
            )
        }
        val descriptor = InjectionTargetDescriptor(TypeUtil.toType(typeInfo), qualifiers)
        return {
            @Suppress("UNCHECKED_CAST")
            bind.provider(descriptor) as T
        }
    }

    override fun createChild(binds: MutableList<Bind<*>>): Injector =
            ChildRedinInjector(this, binds)

    abstract override fun newInjector(keepScoped: Boolean): AbstractRedinInjector

    private fun hotswap(bindings: List<Bind<*>>) {
        this.removeLateTargetsThat { it.doInjection(bindings) }

        this.hotSwappableTargets.forEach { swappable ->
            swappable.target.bindings(bindings).lastOrNull()?.let {
                if (swappable.target.lazy || swappable.target.typeClass.isLazy()) {
                    swappable.setter(swappable.target.lazily(it))
                } else {
                    swappable.setter(it.provider(swappable.target.descriptor))
                }
            }
        }
    }

    private fun InjectionTarget.bindings(bindings: List<Bind<*>>) =
            bindings.filter { it.target.match(this) }

    private fun InjectionTarget.doInjection(bindings: List<Bind<*>>): Boolean {
        val found = this.bindings(bindings)

        val typeClass = this.typeClass

        if (typeClass.isProvider()) {
            this.inject(Provider<Any?> {
                if (this.lazy) {
                    this.lazily()
                } else {
                    this.bindings(bindings).last().provide(this)
                }
            })
            return true
        }

        if (found.isEmpty()) {
            if (this.late) {
                if (this.lazy)
                    throw IllegalArgumentException("@Lazy and @Late cannot be used together. InjectionTarget: $this.")

                if (this !in this@AbstractRedinInjector.lateTargets) {
                    addLateTarget(if (typeClass.isLate() && this !is InjectionTargetLate) {
                        val late = typeClass.createLate(this, this.lazy)
                        this.inject(late)
                        InjectionTargetLate(this, late)
                    } else {
                        this
                    })
                    return false
                }
            }
        }

        if ((this.lazy || this.late) && typeClass.isProvider())
            throw IllegalArgumentException("@Lazy or @Late cannot be used in Provided dependencies.")

        if ((this.lazy || (this.hotSwappable && !typeClass.isHot())) && Modifier.isFinal(this.type.toClass().modifiers)) {
            throw IllegalArgumentException(
                    "@Lazy and @HotSwappable is not applicable to final types. You can either use @Lazy with" +
                            " Lazy<T> type and @HotSwappable with Hot<T> (or specialized type)."
            )
        }

        val toInject: Any = if (this.lazy) {
            this.lazily()
        } else {
            if (found.isEmpty()) {
                throw BindingMissingException(this)
            }

            found.last().provide(this)
        }

        val finalInject = if (this.hotSwappable) {
            if (typeClass.isHot()) {
                val hot = typeClass.createHotWithValue(toInject)
                addHotSwappableTarget(Injector.HotSwappable(this, hot))
                hot
            } else {
                val box = MutableBox(toInject)
                addHotSwappableTarget(Injector.HotSwappable(this, box))
                createHotSwappable(this.type.toClass(), box)
            }
        } else if (typeClass.isLate() && this !is InjectionTargetLate) {
            val late = typeClass.createLate(this, this.lazy)
            val target = InjectionTargetLate(this, late)
            target.inject(toInject)
            late
        } else toInject

        this.inject(finalInject)

        return true
    }

    private fun Bind<*>.provide(target: InjectionTarget): Any =
            this.provide(target.descriptor)

    private fun Bind<*>.provide(target: InjectionTargetDescriptor): Any {
        if (this.scope != BindScope.NO_SCOPE) {
            scopedBindings[this]?.let {
                return@provide it
            }

            val value = this.provider(target)
            addScopedBinding(this, value)
            return value
        } else {
            return this.provider(target)
        }
    }

    private fun InjectionTarget.lazily(): Any {
        val lz = lazy(LazyThreadSafetyMode.NONE) {
            val targetBind = getBind(this) ?: throw BindingMissingException(this)

            targetBind.provide(this)
        }

        return if (typeClass.isLazy())
            lz
        else
            createLazy(this.type.toClass(), lz)
    }

    private fun InjectionTarget.lazily(bind: Bind<*>): Any {
        val lz = lazy(LazyThreadSafetyMode.NONE) {
            bind.provider(this.descriptor)
        }

        return if (typeClass.isLazy())
            lz
        else
            createLazy(this.type.toClass(), lz)
    }

    private fun List<InjectionTarget>.validateInjectionTargets() {
        this.forEach { target ->
            target.validateInjectionTarget()
        }
    }

    private fun List<InjectionTarget>.doInjection(bindings: List<Bind<*>>) {
        this.forEach { target ->
            target.doInjection(bindings)
        }
    }

    private fun Class<*>.defaultCtr(ctx: InjectionContext) {
        val single = this.constructors.singleOrNull { it.parameterCount == 0 }
                ?: throw InstantiationException("At least an empty constructor is required for injection. Class: $this")
        try {
            ctx.instance = single.newInstance()
        } catch (t: Throwable) {
            throw InstantiationException("Exception occurred while trying to instantiate the class '${this.canonicalName}'", t)
        }
    }

    private fun Class<*>.getInjectionTargets(
            ctx: InjectionContext,
            ignoreConstructors: Boolean
    ): List<InjectionTarget> {
        val targets = mutableListOf<InjectionTarget>()

        for (field in this.declaredFields) {
            if (field.isInjectAnnotationPresent()) {
                targets += InjectionTargetField(ctx, field)
            }
        }

        if (!ignoreConstructors) {
            val injectOnType = this.isInjectAnnotationPresent()

            if (injectOnType && this.declaredConstructors.size != 1)
                throw IllegalArgumentException("The injection target '${this.canonicalName}' must have only one constructor.")

            val kConstructors = if (this.isKotlin) {
                this.kotlin.constructors
            } else {
                emptyList()
            }

            for (constructor in this.declaredConstructors) {
                if (constructor.isInjectAnnotationPresent() || injectOnType) {
                    val kConstructor =
                        if (kConstructors.isEmpty()) null
                        else kConstructors.first { it.javaConstructor == constructor }

                    if (constructor.parameterCount == 0) {
                        ctx.instance = constructor.newInstance()
                    } else {
                        val args = arrayOfNulls<Any>(constructor.parameterCount)
                        val parameters = kConstructor?.parameters.orEmpty()

                        constructor.parameters.forEachIndexed { index, parameter ->
                            val kParameter = if (parameters.isEmpty()) null else parameters[index]
                            targets += ParameterInjectionTarget(args, index, parameter, kParameter, constructor, index == args.size - 1, ctx)
                        }
                    }
                }
            }
        }

        for (method in this.methods) {
            if (method.isInjectAnnotationPresent() && !method.isSynthetic) {
                if (method.parameterCount != 1)
                    throw IllegalArgumentException("Injection target '$method' must be a setter method with a single parameter!")

                targets += InjectionTargetMethod(ctx, method)
            }
        }

        return targets
    }

    private data class ParameterInjectionTarget(
            val args: Array<Any?>,
            val count: Int,
            val parameter: Parameter,
            val kParameter: KParameter?,
            val constructor: Constructor<*>,
            val isLast: Boolean,
            val context: InjectionContext
    ) :
            InjectionTargetAnnotated(parameter) {
        override val type: Type
            get() = this.parameter.parameterizedType

        override val name: String
            get() = this.kParameter?.name
                ?: this.parameter.getDeclaredAnnotation(Named::class.java)?.value
                ?: this.parameter.name

        override fun injectValue(value: Any?) {
            this.args[count] = value

            if (isLast) {
                this.context.instance = constructor.newInstance(*this.args)
            }
        }

        override fun validateInjectionTarget() {
            if (this.late && !this.parameter.type.isLate())
                throw InvalidInjectionTargetException("Late Injection parameter must be of LateInit type", this)
        }

        override fun equals(other: Any?): Boolean =
                (other as? ParameterInjectionTarget)?.parameter == this.parameter && other.context == this.context

        override fun hashCode(): Int =
                Objects.hash(this.parameter, this.context)

        override fun stringHelper(): ToStringHelper =
                super.stringHelper()
                        .add("ctx", this.context)
                        .add("parameter", this.parameter)
                        .add("pos", this.count)
                        .add("constructor", this.constructor)

        override fun toString(): String = this.stringHelper().toString()

        override fun formatToReadable(): String {
            val formattedArgs = createFormattedConstructorArgs(
                    this.count + 1,
                    this.args.size,
                    "${this.type.toReadable()} ${this.resolveNameOrOriginal()}"
            )
            return "${this.qualifiersToReadableSpaced()}$formattedArgs (Argument pos: ${count + 1})"
        }

        private fun createFormattedConstructorArgs(count: Int, max: Int, value: String) =
                this.constructor.declaringClass.simpleName +
                StringJoiner(", ", "(", ")").also { sb ->
                    repeat(count - 1) {
                        sb.add("?")
                    }

                    sb.add(value)

                    repeat(max - count) {
                        sb.add("?")
                    }
                }


    }

    private fun AnnotatedElement.isInjectAnnotationPresent() =
            this.isAnnotationPresent(RedinInject::class.java)
                    || this.isAnnotationPresent(Inject::class.java)
                    || this.isAnnotationPresent(HotSwappable::class.java)
                    || this.isAnnotationPresent(Late::class.java)
                    || this.isAnnotationPresent(LazyDep::class.java)

    private data class InjectionTargetLate(val target: InjectionTarget, val lateObj: LateInit) :
            InjectionTarget() {
        override val late: Boolean = true
        override val lazy: Boolean = this.target.lazy
        override val hotSwappable: Boolean = this.target.hotSwappable
        override val type: Type = this.target.type
        override val name: String = this.target.name
        override val qualifiers: List<AnnotationContainer> = this.target.qualifiers

        override fun inject(value: Any?) {
            @Suppress("UNCHECKED_CAST")
            when (this.lateObj) {
                is LateInit.Ref<*> -> (this.lateObj as LateInit.Ref<Any?>).init(value)
                is LateInit.Bool -> lateObj.init(value as Boolean)
                is LateInit.Char -> lateObj.init(value as Char)
                is LateInit.Short -> lateObj.init(value as Short)
                is LateInit.Byte -> lateObj.init(value as Byte)
                is LateInit.Int -> lateObj.init(value as Int)
                is LateInit.Long -> lateObj.init(value as Long)
                is LateInit.Double -> lateObj.init(value as Double)
                is LateInit.Float -> lateObj.init(value as Float)
                is LateInit.LLazy<*> -> (this.lateObj as LateInit.LLazy<Any?>).init(JwLazy.lazy { (value as Lazy<Any?>).value })
                else -> (this.lateObj as LateInit.Ref<Any?>).init(value)
            }
        }

    }

    private fun createBinding(
            klass: Class<*>, scope: BindScope,
            ctx: InjectionContext = InjectionContext(),
            ignoreConstructors: Boolean = false
    ): Bind<*> =
            Bind(
                    TypeBindingTarget(klass),
                    if (scope != BindScope.NO_SCOPE) scope
                    else klass.getScope()?.let { AnnotationBindScope(it) } ?: BindScope.NO_SCOPE
            ) {
                val targets = klass.getInjectionTargets(ctx, ignoreConstructors)

                if (targets.none { it is ParameterInjectionTarget } && !ignoreConstructors && ctx.instance == null)
                    klass.defaultCtr(ctx)

                targets.validateInjectionTargets()
                targets.doInjection(this.binds)

                ctx.instance!!
            }

    private data class TypeBindingTarget(val klass: Class<*>) : BindTarget() {
        override fun match(target: InjectionTarget): Boolean =
                (target as? InjectionTargetType)?.klass == klass
    }

    private data class InjectionTargetType(val klass: Class<*>) : InjectionTargetAnnotated(klass) {
        override val type: Type = klass
        override val name: String = klass.canonicalName ?: klass.name

        override fun injectValue(value: Any?) {}
    }


}