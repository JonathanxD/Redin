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

import com.github.jonathanxd.kores.type.`is`
import java.lang.reflect.AnnotatedElement
import javax.inject.Singleton
import javax.inject.Scope

/**
 * Specification of scope of [Bind].
 */
interface BindScope {
    /**
     * Returns whether this scope specification matches the specification of [scope].
     */
    fun match(scope: AnnotationContainer): Boolean

    companion object {
        /**
         * No scope.
         */
        val NO_SCOPE = object : BindScope {
            override fun match(scope: AnnotationContainer): Boolean = false
        }
    }
}

/**
 * [Annotation type][annotation] scope.
 */
data class AnnotationTypeBindScope(val annotation: Class<out Annotation>): BindScope {
    override fun match(scope: AnnotationContainer): Boolean =
            scope.type.`is`(this.annotation)
}

/**
 * [Annotation instance][annotation] scope.
 */
data class AnnotationBindScope(val annotationContainer: AnnotationContainer): BindScope {
    override fun match(scope: AnnotationContainer): Boolean =
        scope == annotationContainer
}

/**
 * Singleton scope.
 */
val SINGLETON = object : BindScope {
    override fun match(scope: AnnotationContainer): Boolean =
        scope.type.`is`(Singleton::class.java)

}

/**
 * [Annotation type][T] scope.
 */
inline fun <reified T: Annotation> AnnotationTypeScope() = AnnotationTypeBindScope(T::class.java)

fun AnnotatedElement.getScope(): AnnotationContainer? {
    val scopes =
        this.declaredAnnotations.filter { it.annotationClass.java.hasScopeAnnotation() }
    if (scopes.size > 1)
        throw IllegalArgumentException(
            "Target '$this' is annotated with more than one scope annotation." +
                    " Scope annotations: $scopes"
        )

    return scopes.singleOrNull()?.let { it.toContainer() }
}