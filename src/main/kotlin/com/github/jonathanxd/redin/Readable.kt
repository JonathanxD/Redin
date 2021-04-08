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

import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.jonathanxd.kores.type.`is`
import java.lang.reflect.Type
import javax.inject.Named


fun List<AnnotationContainer>.toReadable(): String =
        this.joinToString(separator = " ") { it.toString() }

fun List<AnnotationContainer>.toReadableSpaced(): String =
        if (this.isEmpty()) ""
        else
            "${this.toReadable()} "

fun InjectionTarget.qualifiersToReadable(): String =
        this.qualifiers.toReadable()

fun InjectionTarget.qualifiersToReadableSpaced(): String =
        this.qualifiers.toReadableSpaced()

fun Type.toReadable(): String =
        if (this is Class<*>) this.simpleName else TypeUtil.toTypeInfo(this).toString()

fun InjectionTarget.toReadableExtended(): String =
        "${this.formatToReadable()} ($this)"

fun InjectionTarget.resolveNameOrOriginal(): String =
        this.qualifiers.find {
            it.type.`is`(Named::class.java)
                    && it.getOrNull("value") != null
        }?.get("value") as? String
                ?: this.name