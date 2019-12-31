/**
 *      Redin - Dependency injection built on top of Kores
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2019 JonathanxD <https://github.com/JonathanxD/Redin>
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
package com.github.jonathanxd.redin

import com.github.jonathanxd.iutils.box.MutableBox
import com.github.jonathanxd.iutils.kt.typeInfo
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.jonathanxd.redin.impl.AbstractRedinInjector
import java.lang.reflect.Type

/**
 * Injector of dependencies. To construct an injector uses [Redin].
 */
interface Injector {

    /**
     * Map of bindings to injected instance. This map may be populated lazily.
     */
    val scopedBindings: Map<Bind<*>, Any>

    /**
     * Gets a set of late injection targets.
     */
    val lateTargets: Set<InjectionTarget>

    /**
     * Gets a set of hot swappable targets.
     */
    val hotSwappableTargets: Set<HotSwappable>

    /**
     * Injects dependencies in [klass] (in specified [scope]) and returns the [instance][T].
     */
    operator fun <T : Any> get(klass: Class<T>, scope: BindScope): T

    /**
     * Injects dependencies in [class][TypeInfo.getTypeClass] (in specified [scope]) and returns the [instance][T].
     */
    operator fun <T : Any> get(type: TypeInfo<T>, scope: BindScope): T =
        this[type.typeClass, scope]

    /**
     * Injects dependencies in [klass] and returns the [instance][T].
     */
    operator fun <T : Any> get(klass: Class<T>): T =
        this[klass, BindScope.NO_SCOPE]

    /**
     * Injects dependencies in [class][TypeInfo.getTypeClass] and returns the [instance][T].
     */
    operator fun <T : Any> get(type: TypeInfo<T>): T =
        this[type.typeClass]

    /**
     * Inject dependency in members of existing instance.
     */
    fun <T : Any> injectMembers(instance: T, scope: BindScope): T

    /**
     * Inject dependency in members of existing instance.
     */
    fun <T : Any> injectMembers(instance: T): T = this.injectMembers(instance, BindScope.NO_SCOPE)

    /**
     * Add more bindings to this injector.
     */
    fun bind(ctx: BindContext.() -> Unit)

    /**
     * Make this injector to inherit *scoped bindings instance cache* of parent one.
     *
     * Not calling this method will cause new instances to be created for *scoped bindings*
     * instead of reusing already created ones, which is the default behavior of child injectors.
     *
     * *Does not have any effect if there is no parent injector.*
     */
    fun inheritParentScopedBindingsCache(): Injector = this

    /**
     * Gets all [binds][bind].
     */
    fun getAllBinds(): List<Bind<*>>

    /**
     * Gets all [binds][bind] that matches [matcher].
     */
    fun getAllBinds(matcher: BindMatcher): List<Bind<*>>

    /**
     * Gets single [bind] that matches [matcher]. If more than one matches, throw an exception.
     */
    fun getBind(matcher: BindMatcher): Bind<*>? =
        this.getAllBinds(matcher).lastOrNull()

    /**
     * Gets [bind] for [target].
     */
    fun getBind(target: InjectionTarget): Bind<*>? =
        this.getBind(BindMatcher { it.target.match(target) })

    /**
     * Returns a function that resolves the [Bind] and retrieves the provided [dependency][T] at each invocation.
     */
    fun <T : Any> provide(
        typeInfo: TypeInfo<T>,
        qualifiers: List<AnnotationContainer> = emptyList(),
        scope: BindScope = BindScope.NO_SCOPE
    ): () -> T

    /**
     * Returns a function that resolve the [Bind] lazily and retrieves [dependency][T] at each invocation.
     *
     * This function reuses the lazily resolved [Bind] to provide the dependency, instead of resolving it at each invocation,
     * like the function returned by [provide].
     */
    fun <T : Any> provideLazyBind(
        typeInfo: TypeInfo<T>,
        qualifiers: List<AnnotationContainer> = emptyList(),
        scope: BindScope = BindScope.NO_SCOPE,
        safe: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE
    ): () -> T

    /**
     * Creates a new injectors with same binds as this.
     *
     * @param keepScoped Whether to keep cached scoped dependencies or not.
     */
    fun newInjector(keepScoped: Boolean = true): Injector

    /**
     * Creates a child injector.
     */
    fun createChild(binds: MutableList<Bind<*>> = mutableListOf()): Injector

    data class HotSwappable(val target: InjectionTarget, internal val setter: (a: Any?) -> Unit) {
        constructor(target: InjectionTarget, box: MutableBox<Any?>) : this(target, box::set)
        constructor(target: InjectionTarget, hot: HotSwappableData) : this(target, {
            @Suppress("UNCHECKED_CAST")
            when (hot) {
                is Hot<*> -> (hot as Hot<Any?>).value = it
                is HotLazy<*> -> (hot as HotLazy<Any?>).value = it as Lazy<Any?>
                is HotBoolean -> hot.value = it as Boolean
                is HotChar -> hot.value = it as Char
                is HotByte -> hot.value = it as Byte
                is HotShort -> hot.value = it as Short
                is HotInt -> hot.value = it as Int
                is HotLong -> hot.value = it as Long
                is HotFloat -> hot.value = it as Float
                is HotDouble -> hot.value = it as Double
            }
        })
    }
}

fun Injector.getRequiredBind(matcher: BindMatcher): Bind<*> =
    this.getBind(matcher) ?: throw IllegalStateException("Bind that matches '$matcher' not found!")

/**
 * Injects dependency in [T] and returns the [instance][T].
 */
inline fun <reified T : Any> Injector.get() = this[typeInfo<T>()]

/**
 * Returns a function that resolves the [Bind] and retrieves the provided [dependency][T] at each invocation.
 */
inline fun <reified T : Any> Injector.provide(
    qualifiers: List<AnnotationContainer> = emptyList(),
    scope: BindScope = BindScope.NO_SCOPE
): () -> T =
    this.provide(typeInfo(), qualifiers, scope)

/**
 * Returns a function that resolve the [Bind] lazily and retrieves [dependency][T] at each invocation.
 *
 * This function reuses the lazily resolved [Bind] to provide the dependency, instead of resolving it at each invocation,
 * like the function returned by [provode].
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> Injector.provideLazyBind(
    qualifiers: List<AnnotationContainer> = emptyList(),
    scope: BindScope = BindScope.NO_SCOPE,
    safe: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE
): () -> T =
    this.provideLazyBind(typeInfo(), qualifiers, scope, safe)


/**
 * Matcher of bindings
 */
inline fun BindMatcher(crossinline func: (bind: Bind<*>) -> Boolean): BindMatcher =
    object : BindMatcher {
        override fun match(bind: Bind<*>): Boolean = func(bind)
    }

/**
 * Matcher of bindings
 */
interface BindMatcher {
    fun match(bind: Bind<*>): Boolean
}

/**
 * Match binds by type and qualifiers
 */
data class CommonBindMatcher(
    private val type: Type,
    private val scope: BindScope,
    private val qualifiers: List<AnnotationContainer>
) : BindMatcher {
    override fun match(bind: Bind<*>): Boolean =
        (bind.target as? TypedQualifiedBindTarget)?.let {
            it.qualifiers.size == this.qualifiers.size
                    && it.qualifiers.all { q -> this.qualifiers.any { q.matches(it) } }
                    && TypeUtil.toTypeInfo(it.type) == TypeUtil.toTypeInfo(this.type)
                    && this.scope == bind.scope
        } == true
}

/**
 * Match binds by type and qualifiers
 */
data class GenericCommonBindMatcher<T>(
    private val typeInfo: TypeInfo<T>,
    private val qualifiers: List<AnnotationContainer>,
    private val scope: BindScope
) : BindMatcher {
    override fun match(bind: Bind<*>): Boolean =
        (bind.target as? TypedQualifiedBindTarget)?.let {
            it.qualifiers.size == this.qualifiers.size
                    && it.qualifiers.all { q -> this.qualifiers.any { q.matches(it) } }
                    && TypeUtil.toTypeInfo(it.type) == this.typeInfo
                    && bind.scope == this.scope
        } == true
}