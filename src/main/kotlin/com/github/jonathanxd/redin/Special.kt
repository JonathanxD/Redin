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
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.iutils.type.TypeUtil
import java.lang.reflect.Type
import javax.inject.Provider

fun Class<*>.isValidSpecial() = this.isLate() || this.isLazy() || this.isHot() || this.isProvider()
fun Class<*>.isLate() = LateInit::class.java.isAssignableFrom(this)
fun Class<*>.isLazy() = Lazy::class.java == this
fun Class<*>.isHot() = HotSwappableData::class.java.isAssignableFrom(this)
fun Class<*>.isProvider() = Provider::class.java == this

fun TypeInfo<*>.specialType(): Type? =
    when {
        this.typeClass.isLate() -> when (this.typeClass) {
            LateInit.Ref::class.java, LateInit.LLazy::class.java -> null
            LateInit.Bool::class.java -> java.lang.Boolean.TYPE
            LateInit.Char::class.java -> java.lang.Character.TYPE
            LateInit.Byte::class.java -> java.lang.Byte.TYPE
            LateInit.Short::class.java -> java.lang.Short.TYPE
            LateInit.Int::class.java -> java.lang.Integer.TYPE
            LateInit.Long::class.java -> java.lang.Long.TYPE
            LateInit.Float::class.java -> java.lang.Float.TYPE
            LateInit.Double::class.java -> java.lang.Double.TYPE
            else -> null
        }
        this.typeClass.isHot() -> when (this.typeClass) {
            Hot::class.java -> null
            HotLazy::class.java -> null
            HotBoolean::class.java -> java.lang.Boolean.TYPE
            HotChar::class.java -> java.lang.Character.TYPE
            HotByte::class.java -> java.lang.Byte.TYPE
            HotShort::class.java -> java.lang.Short.TYPE
            HotInt::class.java -> java.lang.Integer.TYPE
            HotLong::class.java -> java.lang.Long.TYPE
            HotFloat::class.java -> java.lang.Float.TYPE
            HotDouble::class.java -> java.lang.Double.TYPE
            else -> null
        }
        else -> null
    }.let {
        if (it == null && this.typeParameters.size == 1) TypeUtil.toType(this.getTypeParameter(0))
        else it
    }


fun Class<*>.createLate(target: InjectionTarget, lazy: Boolean): LateInit {
    val name = "${target.qualifiersToReadableSpaced()}${target.type.typeName} ${target.name}"

    return if (lazy) {
        LateInit.lateLazy<Any?>(name)
    } else when (this) {
        LateInit.Bool::class.java -> LateInit.lateBool(name)
        LateInit.Char::class.java -> LateInit.lateChar(name)
        LateInit.Byte::class.java -> LateInit.lateByte(name)
        LateInit.Short::class.java -> LateInit.lateShort(name)
        LateInit.Int::class.java -> LateInit.lateInt(name)
        LateInit.Long::class.java -> LateInit.lateLong(name)
        LateInit.Float::class.java -> LateInit.lateFloat(name)
        LateInit.Double::class.java -> LateInit.lateDouble(name)
        else -> LateInit.lateRef<Any?>(name)
    }
}

@Suppress("UNCHECKED_CAST")
fun Class<*>.createHotWithValue(value: Any?) =
    when (this) {
        Hot::class.java -> Hot(value)
        HotLazy::class.java -> HotLazy(value as Lazy<Any?>)
        HotBoolean::class.java -> HotBoolean(value as Boolean)
        HotChar::class.java -> HotChar(value as Char)
        HotByte::class.java -> HotByte(value as Byte)
        HotShort::class.java -> HotShort(value as Short)
        HotInt::class.java -> HotInt(value as Int)
        HotLong::class.java -> HotLong(value as Long)
        HotFloat::class.java -> HotFloat(value as Float)
        HotDouble::class.java -> HotDouble(value as Double)
        else -> Hot(value)
    }