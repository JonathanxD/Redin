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
package com.github.jonathanxd.redin

import com.github.jonathanxd.iutils.`object`.NonNullLazy
import com.github.jonathanxd.iutils.kt.typeInfo
import com.github.jonathanxd.iutils.reflection.Reflection
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.iutils.type.TypeInfoBuilder
import com.github.jonathanxd.iutils.type.TypeParameterProvider
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.jonathanxd.redin.impl.ChildRedinInjector
import com.github.jonathanxd.redin.impl.RedinInjector
import java.util.function.Predicate

@DslMarker
@Retention(AnnotationRetention.SOURCE)
annotation class RedinDsl

/**
 * Creates injector.
 */
inline fun Redin(ctx: BindContext.() -> Unit): Injector {
    val bindings = mutableListOf<Bind<*>>()
    val redin = RedinInjector(bindings)
    ctx(BindContext(redin, redin))
    redin.bind()
    return redin
}

/**
 * Creates injector.
 */
inline fun ChildRedin(parent: Injector, crossinline ctx: BindContext.() -> Unit): Injector {
    val bindings = mutableListOf<Bind<*>>()
    val redin = ChildRedinInjector(parent, bindings)
    ctx(BindContext(redin, redin))
    redin.bind()
    return redin
}

/**
 * Creates injector.
 */
inline fun Injector.child(ctx: BindContext.() -> Unit): Injector {
    val bindings = mutableListOf<Bind<*>>()
    val redin = ChildRedinInjector(this, bindings)
    ctx(BindContext(redin, redin))
    redin.bind()
    return redin
}


@RedinDsl
class BindContext(private val bindListModifier: BindListModifier, val injector: Injector) {

    /**
     * Adds all binds of [module].
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun module(module: Any): BindContext {
        module.getBinds().forEach {
            this.addBind(it)
        }
        return this
    }


    /**
     * Bind [type].
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T : Any> bind(type: Class<T>): ProgressBinding<T> =
        ProgressBinding({ TypedQualifiedBindTarget(type, it) }, this)

    /**
     * Bind [generic type][type].
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T : Any> bind(type: TypeInfo<T>): ProgressBinding<T> =
        ProgressBinding({ TypedQualifiedBindTarget(TypeUtil.toType(type), it) }, this)

    /**
     * Bind [generic type][type].
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T : Any> bind(type: TypeInfoBuilder<T>): ProgressBinding<T> =
        this.bind(type.build())

    /**
     * Bind [type][T].
     */
    inline fun <reified T : Any> bind(bind: ProgressBinding<T>.() -> Unit) =
        bind(bind())

    /**
     * Bind [generic type][T].
     */
    inline fun <reified T : Any> bindReified(bind: ProgressBinding<T>.() -> Unit) =
        bind(bindReified())

    /**
     * Bind [type][T].
     */
    inline fun <reified T : Any> bind(): ProgressBinding<T> =
        ProgressBinding({ TypedQualifiedBindTarget(T::class.java, it) }, this)

    /**
     * Bind [generic type][T].
     */
    inline fun <reified T : Any> bindReified(): ProgressBinding<T> =
        ProgressBinding({
            TypedQualifiedBindTarget(
                object : TypeParameterProvider<T>() {}.type,
                it
            )
        }, this)

    /**
     * Adds [bind] to this bind list.
     */
    fun addBind(bind: Bind<*>): BindContext {
        this.bindListModifier.addBind(bind)
        return this
    }

    /**
     * Removes [bind] from this bind list.
     */
    fun removeBind(bind: Bind<*>): BindContext {
        this.bindListModifier.removeBind(bind)
        return this
    }

    /**
     * Removes [bind] from this bind list.
     */
    fun removeBindIf(predicate: Predicate<in Bind<*>>): BindContext {
        this.bindListModifier.removeBindIf(predicate)
        return this
    }
}

@RedinDsl
class ProgressBinding<T : Any>(
    private val builder: (qualifiers: List<BindQualifier>) -> BindTarget,
    private val qualifiers: List<BindQualifier>,
    private val scope: BindScope,
    private val ctx: BindContext
) {

    constructor(builder: (qualifiers: List<BindQualifier>) -> BindTarget, ctx: BindContext) :
            this(builder, emptyList(), BindScope.NO_SCOPE, ctx)

    private fun build() = builder(this.qualifiers.toList())

    /**
     * Specifies [injectionQualifier] for this bind.
     */
    infix fun qualifiedWith(injectionQualifier: BindQualifier): ProgressBinding<T> {
        return ProgressBinding(
            this.builder,
            this.qualifiers + injectionQualifier,
            this.scope,
            this.ctx
        )
    }

    /**
     * Specifies [injectionQualifiers] for this bind.
     */
    infix fun qualifiedWith(injectionQualifiers: List<BindQualifier>): ProgressBinding<T> {
        return ProgressBinding(
            this.builder,
            this.qualifiers + injectionQualifiers,
            this.scope,
            this.ctx
        )
    }

    /**
     * Specifies the [scope] of this bind.
     */
    fun inSingletonScope(): ProgressBinding<T> {
        return this inScope SINGLETON
    }

    /**
     * Specifies the [scope] of this bind.
     */
    infix fun inScope(scope: BindScope): ProgressBinding<T> {
        return ProgressBinding(this.builder, this.qualifiers, scope, this.ctx)
    }

    /**
     * Specifies the [scope][scopeAnnotation] of this bind.
     */
    infix fun inScope(scopeAnnotation: Class<out Annotation>): ProgressBinding<T> {
        return this inScope AnnotationTypeBindScope(scopeAnnotation)
    }

    /**
     * Defines a provider of the dependency and finalizes this bind construction.
     */
    infix fun toProvider(provider: InjectionProvider<T>): BindContext {
        this.ctx.addBind(Bind(this.build(), this.scope, provider))
        return ctx
    }

    /**
     * Defines the value of dependency and finalizes the bind construction.
     */
    infix fun toProvider(provider: Class<out InjectionProvider<T>>): BindContext {
        return this toProvider (try {
            provider.getDeclaredConstructor().newInstance()
        } catch (n: NoSuchMethodException) {
            Reflection.getInstance(provider)
        })
    }

    /**
     * Defines the value of dependency and finalizes the bind construction.
     */
    infix fun toValue(value: T): BindContext {
        return this toProvider { value }
    }

    /**
     * Defines the lazy provided of dependency and finalizes the bind construction.
     */
    infix fun toLazy(lazy: Lazy<T>): BindContext {
        return this toProvider { lazy.value }
    }

    /**
     * Defines the lazy provided of dependency and finalizes the bind construction.
     */
    infix fun toLazy(lazy: NonNullLazy<T>): BindContext {
        return this toProvider { lazy.get() }
    }

    /**
     * Defines an implementation for dependency and finalizes the bind construction.
     *
     * Dependencies are injected in [V].
     */
    inline fun <reified V: T> toImplementation(): BindContext {
        return this toImplementation V::class.java
    }

    /**
     * Defines an implementation for dependency and finalizes the bind construction.
     *
     * Dependencies are injected in [V].
     */
    inline fun <reified V: T> toImplementationReified(): BindContext {
        return this toImplementation object : TypeParameterProvider<T>() {}.createTypeInfo()
    }

    /**
     * Defines an implementation for dependency and finalizes the bind construction.
     *
     * Dependencies are injected in [klass].
     */
    infix fun toImplementation(klass: Class<out T>): BindContext {
        return this toProvider { this.ctx.injector[klass, this.scope] }
    }

    /**
     * Defines an implementation for dependency and finalizes the bind construction.
     *
     * Dependencies are injected in [type].
     */
    infix fun toImplementation(type: TypeInfo<out T>): BindContext {
        return this toProvider { this.ctx.injector[type, this.scope] }
    }

}

/**
 * Returns a function that provides a dependency instance.
 */
inline fun <reified T : Any> provide(
    injector: Injector,
    provide: ProgressProvide<T>.() -> ProgressProvide<T>
): () -> T =
    provide(ProgressProvide(typeInfo(), BindScope.NO_SCOPE, false, emptyList())).let {
        if (it.lazy)
            injector.provideLazyBind(it.type, it.qualifiers, it.scope)
        else
            injector.provide(it.type, it.qualifiers, it.scope)
    }

@RedinDsl
class ProgressProvide<T : Any>(
    val type: TypeInfo<T>,
    val scope: BindScope,
    val lazy: Boolean,
    val qualifiers: List<AnnotationContainer>
) {
    /**
     * Add qualifier
     */
    infix fun qualifiedWith(container: AnnotationContainer): ProgressProvide<T> =
        ProgressProvide(this.type, this.scope, this.lazy, this.qualifiers + container)

    /**
     * Add qualifiers
     */
    infix fun qualifiedWith(containers: List<AnnotationContainer>): ProgressProvide<T> =
        ProgressProvide(this.type, this.scope, this.lazy, this.qualifiers + containers)

    /**
     * Defines the scope of dependency.
     */
    infix fun inScope(scope: BindScope): ProgressProvide<T> =
        ProgressProvide(this.type, scope, this.lazy, this.qualifiers)

    /**
     * Whether to provide with lazy [Bind] ([Injector.provideLazyBind]) or not ([Injector.provide]).
     */
    infix fun lazily(lazy: Boolean): ProgressProvide<T> =
        ProgressProvide(this.type, scope, lazy, this.qualifiers)
}