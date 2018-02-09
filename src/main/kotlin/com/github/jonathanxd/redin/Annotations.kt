/**
 *      Redin - Dependency injection built on top of Kores
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 JonathanxD <https://github.com/JonathanxD/Redin>
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

typealias RedinInject = Inject


/**
 * Dependency injection annotation.
 *
 * This annotation is recommended only to be used over [javax.inject.Inject] in special cases, like:
 *
 * - Annotating classes
 *
 * When applied to classes, the class must have only one constructor. The injection will be applied to this single constructor.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.CLASS
)
annotation class Inject

/**
 * Object will be injected lazily (at first request). The provider does not need to be available
 * at the time of the injection, but must be present at the time of first use.
 *
 * This only works for non-final injection target types.
 *
 * Can be used with [Lazy] (recommended for types that does not behave correctly with proxies
 * or final types).
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class LazyDep

/**
 * Object will be inject as soon as available. The provider does need to available at the time of injection,
 * but must be provided before the object is used, otherwise a [NullPointerException] may be throw.
 *
 * Can be used with [LateInit] types (specialized types are supported).
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class Late

/**
 * Object can be hot-swapped, this means that the object can be re-injected at any moment, this is made through
 * a Proxy Pattern, so the reference still the same but the wrapping object is changed.
 *
 * **Obs: this is not a deep operation, only injected object will be wrapped into a Proxy, but the objects inside
 * this object will not (unless in effect of this type of injection)**
 *
 * Can be used with [Hot] types (specialized types are supported/recommended for types that does not behave correctly with proxies
 * or final types).
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class HotSwappable

/**
 * Provides dependency.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION
)
annotation class Provides