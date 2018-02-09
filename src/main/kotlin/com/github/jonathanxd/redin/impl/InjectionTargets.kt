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
package com.github.jonathanxd.redin.impl

import com.github.jonathanxd.iutils.string.ToStringHelper
import com.github.jonathanxd.redin.*
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.util.*


abstract class InjectionTargetAnnotated(annotatedElement: AnnotatedElement) :
    AbstractInjectionTarget() {
    override val late: Boolean = annotatedElement.isAnnotationPresent(Late::class.java)
    override val lazy: Boolean = annotatedElement.isAnnotationPresent(LazyDep::class.java)
    override val hotSwappable: Boolean =
        annotatedElement.isAnnotationPresent(HotSwappable::class.java)

    final override val qualifiers: List<AnnotationContainer> = annotatedElement.getQualifiers()

    protected open fun stringHelper() =
        ToStringHelper.helper(this::class.java.simpleName, ", ", "(", ")")
            .add("late", this.late)
            .add("lazy", this.lazy)
            .add("hotSwappable", this.hotSwappable)
            .add("type", this.type)
            .add("injectionType", this.injectionType)
            .add("qualifiers", this.qualifiers)

    override fun toString(): String = this.stringHelper().toString()
}

data class InjectionTargetField(val ctx: InjectionContext, val field: Field) :
    InjectionTargetAnnotated(field) {
    override val type: Type
        get() = this.field.genericType
    override val name: String
        get() = this.field.name

    override fun injectValue(value: Any) {
        this.field.isAccessible = true
        this.field.set(ctx.instance, value)
    }

    override fun equals(other: Any?): Boolean =
        (other as? InjectionTargetField)?.ctx == this.ctx && other.field == this.field

    override fun hashCode(): Int =
        Objects.hash(this.field, this.ctx)

    override fun stringHelper(): ToStringHelper =
        super.stringHelper()
            .add("ctx", this.ctx)
            .add("field", this.field)

    override fun toString(): String = this.stringHelper().toString()
}

data class InjectionTargetMethod(val ctx: InjectionContext, val method: Method) :
    InjectionTargetAnnotated(method) {
    override val name: String
        get() = if (this.method.name.startsWith("set")) this.method.name.substring(3) else this.method.name
    override val type: Type
        get() = this.method.genericParameterTypes[0]

    override fun injectValue(value: Any) {
        this.method.invoke(ctx.instance, value)
    }

    override fun equals(other: Any?): Boolean =
        (other as? InjectionTargetMethod)?.ctx == this.ctx && other.method == this.method

    override fun hashCode(): Int =
        Objects.hash(this.method, this.ctx)

    override fun stringHelper(): ToStringHelper =
        super.stringHelper()
            .add("ctx", this.ctx)
            .add("method", this.method)

    override fun toString(): String = this.stringHelper().toString()
}

class InjectionContext {
    var instance: Any? = null
        set(value) {
            if (field != null)
                throw IllegalStateException("Already initialized")
            field = value
        }

    override fun toString(): String =
        "InjectionContext[$instance]"
}
