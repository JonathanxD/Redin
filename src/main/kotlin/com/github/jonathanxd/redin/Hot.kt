/**
 *      Redin - Dependency injection built on top of Kores
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2020 JonathanxD <https://github.com/JonathanxD/Redin>
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

sealed class HotSwappableData

/**
 * Hot [lazy][Lazy].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class HotLazy<T>(var value: Lazy<T>) : HotSwappableData()

/**
 * Hot [reference][Any].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class Hot<T>(var value: T) : HotSwappableData()

/**
 * Hot [Boolean].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class HotBoolean(var value: Boolean) : HotSwappableData()

/**
 * Hot [Char].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class HotChar(var value: Char) : HotSwappableData()

/**
 * Hot [Byte].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class HotByte(var value: Byte) : HotSwappableData()

/**
 * Hot [Short].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class HotShort(var value: Short) : HotSwappableData()

/**
 * Hot [Int].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class HotInt(var value: Int) : HotSwappableData()

/**
 * Hot [Long].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class HotLong(var value: Long) : HotSwappableData()

/**
 * Hot [Double].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class HotDouble(var value: Double) : HotSwappableData()

/**
 * Hot [Float].
 *
 * The [value] can be swapped at any time, if you want to pass the value forward and keep
 * the behavior, use this instance instead of [value].
 */
data class HotFloat(var value: Float) : HotSwappableData()