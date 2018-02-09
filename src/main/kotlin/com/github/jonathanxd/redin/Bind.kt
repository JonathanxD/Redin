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

import java.util.*

typealias InjectionProvider<T> = (target: InjectionTargetDescriptor) -> T

/**
 * Describes a binding to a [target]
 *
 * @property target Target of binding.
 * @property scope Scope of binding.
 * @property provider Provider of value to bind.
 */
data class Bind<out T : Any>(
    val target: BindTarget,
    val scope: BindScope,
    val provider: InjectionProvider<T>
) {

    override fun equals(other: Any?): Boolean =
        other is Bind<*> && other.target == this.target && other.scope == this.scope

    override fun hashCode(): Int = Objects.hash(this.target, this.scope)
}

/**
 * Determines which [InjectionTargets][InjectionTarget] receives [injection][InjectionTarget.inject]
 * from the [Bind].
 */
abstract class BindTarget {
    /**
     * Returns whether [target] matches the [Bind].
     */
    abstract fun match(target: InjectionTarget): Boolean
}
