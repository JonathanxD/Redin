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

import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Type

fun Any.getBinds(): List<Bind<*>> {
    val instance = this
    val type = this::class.java
    return (type.declaredMethods + type.methods)
        .filter { it.isProvidesPresent() }
        .filter { it.parameterCount == 0 || (it.parameterCount == 1 && it.parameterTypes[0] == InjectionTargetDescriptor::class.java) }
        .map {
            val scope = it.getScope()?.let { ReflectiveScope(it) } ?: BindScope.NO_SCOPE
            val qualifiers = it.getQualifiers()

            Bind(ReflectiveTarget(it.genericReturnType, qualifiers), scope, { t ->
                if (it.parameterCount == 1 && it.parameterTypes[0] == InjectionTargetDescriptor::class.java)
                    it.invoke(instance, t)
                else
                    it.invoke(instance)
            })
        }
}

private data class ReflectiveScope(val annotationContainer: AnnotationContainer) : BindScope {
    override fun match(scope: AnnotationContainer): Boolean =
        scope == annotationContainer

}

private data class ReflectiveTarget(val type: Type, val qualifiers: List<AnnotationContainer>) :
    BindTarget() {
    override fun match(target: InjectionTarget): Boolean {
        return this.type.equalsTo(target.injectionType) && target.qualifiers == this.qualifiers
    }

}

fun AnnotatedElement.isProvidesPresent() = this.isAnnotationPresent(Provides::class.java)