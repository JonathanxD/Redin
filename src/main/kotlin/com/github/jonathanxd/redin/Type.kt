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

import com.github.jonathanxd.iutils.type.Primitive
import com.github.jonathanxd.iutils.type.TypeUtil
import java.lang.reflect.Type

fun Type.toClass(): Class<*> = TypeUtil.toTypeInfo(this).typeClass

fun Type.equalsTo(other: Type): Boolean =
    if (this is Class<*> && other is Class<*>) Primitive.typeEquals(this, other)
    else if (this == other) true
    else TypeUtil.toTypeInfo(this)?.let { typeInfo ->
        TypeUtil.toTypeInfo(other).let { otherInfo ->
            if (typeInfo.typeParameters.size == 0 && otherInfo.typeParameters.size == 0)
                Primitive.typeEquals(typeInfo.typeClass, otherInfo.typeClass)
            else
                typeInfo == otherInfo
        }
    } == true