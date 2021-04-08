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

import com.github.jonathanxd.iutils.`object`.LateInit
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.jonathanxd.kores.type.`is`
import java.lang.reflect.Type
import javax.inject.Qualifier
import javax.inject.Scope

/**
 * Descriptor of the [InjectionTarget]
 */
data class InjectionTargetDescriptor(
    val injectionType: Type,
    val qualifiers: List<AnnotationContainer>
) {
    /**
     * Returns whether this target descriptor has a annotation qualifier of [annotationType].
     */
    fun hasQualifier(annotationType: Class<out Annotation>): Boolean =
        this.qualifiers.any { it.type.`is`(annotationType) }

    /**
     * Returns all annotation qualifiers of the [annotationType].
     */
    @Suppress("UNCHECKED_CAST")
    fun qualifier(annotationType: Class<out Annotation>): List<AnnotationContainer> =
        this.qualifiers
            .filter { it.type.`is`(annotationType) }

}

/**
 * Subject of injection. This is the wrapper of target which directly injects to backed
 * instance through [inject].
 */
abstract class InjectionTarget {
    /**
     * Whether the value can be injected later or not. The value will be injected immediately after
     * a binding matching this target is provided to [Injector], also if a binding is already available,
     * the value will be immediately injected.
     *
     * If the [type] is a [LateInit] (or primitive variant), an [uninitialized LateInit][LateInit] will be injected
     * and initialized when the rules above are met.
     */
    abstract val late: Boolean

    /**
     * Whether this target is lazy and the value can be injected lazily. The value is resolved from [Injector] at the
     * first request and reused.
     *
     * Until the lazy is evaluated, another binding can overwrite the current target of evaluation,
     * but after evaluation, the dependency can not be hot-swapped unless this target is [hotSwappable] too.

     * If the [type] is [Lazy], then a [Lazy] instance is injected, otherwise a `Lazy Proxy` is injected (but if
     * [type] is final, an exception is thrown).
     *
     * A Lazy proxy is a proxy that implements [type] and uses a [Lazy] that calls [Injector.getBind] to resolve the binding
     * to use to inject the value.
     */
    abstract val lazy: Boolean

    /**
     * Whether this target can be hot-swapped by new bindings. A binding must be immediately available at injection-time
     * (unless this target is either [late] or [lazy] too).
     *
     * If the [type] is [Hot] (or primitive variant), then a [Hot] instance is injected and updated at each binding (if an applicable
     * binding is found), otherwise a `HotSwappable Proxy` is injected (but if [type] is final, an exception is thrown).
     *
     * A HotSwappable Proxy is a proxy which the backing instance can be changed at any time.
     */
    abstract val hotSwappable: Boolean

    /**
     * The parameterized type of this injection target.
     */
    abstract val type: Type

    /**
     * The name of this injection target (this is not the name provided by `Named`).
     */
    abstract val name: String

    /**
     * Qualifiers of this target (such as `Named`).
     */
    abstract val qualifiers: List<AnnotationContainer>

    /**
     * Type of the dependency to inject.
     */
    val injectionType: Type
        get() = TypeUtil.toTypeInfo(this.type).let {
            if (!this.isSpecial()) TypeUtil.toType(it)
            else it.specialType()
                    ?: throw IllegalStateException("Invalid special: $this.")
        }

    val descriptor: InjectionTargetDescriptor
        get() = InjectionTargetDescriptor(this.injectionType, this.qualifiers)

    /**
     * Injects the [value].
     */
    abstract fun inject(value: Any?)

    /**
     * Validates the injection target.
     *
     * @throws InvalidInjectionTargetException
     */
    @Throws(InvalidInjectionTargetException::class)
    open fun validateInjectionTarget() {}

    /**
     * Formats injection target into readable string.
     */
    open fun formatToReadable(): String =
        "${this.qualifiersToReadableSpaced()}${this.type.toReadable()} ${this.resolveNameOrOriginal()}"

}

/**
 * Implements a type checking for [inject] method.
 */
abstract class AbstractInjectionTarget : InjectionTarget() {

    final override fun inject(value: Any?) {
        if (value != null) {
            (this.type as? Class<*>)?.let {
                if (!it.isInstance(value))
                    throw IllegalArgumentException("Value $value is not assignable to ${it.canonicalName}.")
            } ?: TypeUtil.toTypeInfo(this.type)?.let {
                if (!it.typeClass.isInstance(value))
                    throw IllegalArgumentException("Value $value is not assignable to $it.")
            }
        }

        this.injectValue(value)
    }

    /**
     * Injects the [value].
     */
    abstract fun injectValue(value: Any?)
}

/**
 * Returns whether this [InjectionTargetDescriptor] has the qualifier [T].
 */
inline fun <reified T : Annotation> InjectionTargetDescriptor.hasQualifier(): Boolean =
    this.hasQualifier(T::class.java)

/**
 * Gets all qualifiers [T] of this [InjectionTargetDescriptor].
 */
inline fun <reified T : Annotation> InjectionTargetDescriptor.qualifier(): List<AnnotationContainer> =
    this.qualifier(T::class.java)

val InjectionTarget.typeClass: Class<*>
    get() = this.type.toClass()

fun InjectionTarget.isSpecial() =
    (this.late || this.lazy || this.hotSwappable || this.typeClass.isProvider()) && this.typeClass.isValidSpecial()

fun Class<*>.hasQualifierAnnotation() = this.isAnnotationPresent(Qualifier::class.java)
fun Class<*>.hasScopeAnnotation() = this.isAnnotationPresent(Scope::class.java)
