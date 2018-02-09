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
import java.lang.reflect.Type
import javax.inject.Named

/**
 * Implements a basic logic for [qualified][BindQualifier] [binding targets][BindTarget].
 */
abstract class QualifiedBindTarget : BindTarget() {
    abstract val qualifiers: List<BindQualifier>
}

/**
 * Qualified bind target with type.
 */
data class TypedQualifiedBindTarget(val type: Type, override val qualifiers: List<BindQualifier>) :
    QualifiedBindTarget() {
    override fun match(target: InjectionTarget): Boolean =
        this.type.equalsTo(target.injectionType) && this.qualifiers.allQualifiersMatch(target.qualifiers)

}

/**
 * Specification of a qualifier of [BindTarget].
 */
interface BindQualifier {
    /**
     * Returns whether [annotationContainer] of a [InjectionTarget] matches `this` injection qualifier.
     */
    fun matches(annotationContainer: AnnotationContainer): Boolean
}

/**
 * Matches targets annotated with [Named] with the specified [name].
 */
data class Name(val name: String) : BindQualifier {
    override fun matches(annotationContainer: AnnotationContainer): Boolean =
        annotationContainer.type.`is`(Named::class.java)
                && annotationContainer.getAsOrNull<String>("value") == name
}

/**
 * Creates a [Named] [AnnotationContainer].
 */
fun nameContainer(name: String) = AnnotationContainer<Named>(mapOf("value" to name))

/**
 * Matches targets annotated with [annotations].
 */
data class AnnotatedWith(val annotations: List<Class<out Annotation>>) : BindQualifier {

    constructor(annotation: Class<out Annotation>) : this(listOf(annotation))

    override fun matches(annotationContainer: AnnotationContainer): Boolean =
        this.annotations.any { it.`is`(annotationContainer.type) }

}

/**
 * Matches targets annotated with annotation [T].
 */
inline fun <reified T : Annotation> AnnotatedWith() = AnnotatedWith(T::class.java)

fun AnnotatedElement.getQualifiers(): List<AnnotationContainer> =
    this.annotations
        .filter { it.annotationClass.java.hasQualifierAnnotation() }
        .map { it.toContainer() }

fun List<BindQualifier>.allQualifiersMatch(qualifiers: List<AnnotationContainer>) =
    qualifiers.size == this.size
            && this.all { q -> qualifiers.any { q.matches(it) } }