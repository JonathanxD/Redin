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

import com.github.jonathanxd.iutils.array.ArrayUtils
import com.github.jonathanxd.kores.type.`is`
import com.github.jonathanxd.kores.type.hash
import com.github.jonathanxd.kores.type.simpleName
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.*

/**
 * Container that stores [annotation type][type] and [annotation properties][properties]. Nested
 * annotations are stored as [AnnotationContainer].
 */
data class AnnotationContainer constructor(val type: Type, val properties: Map<String, Any>) {

    /**
     * Gets the value of [annotation property][key] or return null if annotation does not have requested property.
     */
    fun getOrNull(key: String) = this.properties[key]

    /**
     * Gets required value of [annotation property][key].
     *
     * This throws [IllegalArgumentException] if value is not found.
     */
    operator fun get(key: String) = this.properties[key]
            ?: throw IllegalArgumentException("$key not found in properties of $type. Properties: $properties.")

    /**
     * Gets required value of [annotation property][key] as [T].
     *
     * This throws [IllegalArgumentException] if value is not found or value is not instance of [type].
     */
    fun <T> getAs(key: String, type: Class<T>): T =
        this[key].let { value ->
            if (!type.isInstance(value))
                throw IllegalArgumentException("Value for key '$key' is not of type ${type.simpleName}. Value: $value.")
            @Suppress("UNCHECKED_CAST")
            value as T
        }

    /**
     * Gets value of [annotation property][key] as [T] or return null if either annotation does not have requested property
     * or the value is not instance of [type].
     */
    fun <T> getAsOrNull(key: String, type: Class<T>): T? =
        this.getOrNull(key)?.let { value ->
            if (!type.isInstance(value))
                null
            else
                @Suppress("UNCHECKED_CAST")
                value as T
        }

    /**
     * Gets required value of [annotation property][key] as [T].
     *
     * This throws [IllegalArgumentException] if value is not found or value is not instance of [T].
     */
    inline fun <reified T> getAs(key: String): T =
        this[key].let { value ->
            (value as? T)
                    ?: throw IllegalArgumentException("Value for key '$key' is not of type ${T::class.java.simpleName}. Value: $value.")
        }

    /**
     * Gets value of [annotation property][key] as [T] or return null if either annotation does not have requested property
     * or the value is not instance of [T].
     */
    inline fun <reified T> getAsOrNull(key: String): T? =
        this.getOrNull(key) as? T

    override fun toString(): String =
        "${type.simpleName}(${properties.toString().let { it.subSequence(1, it.length - 1) }})"

    override fun hashCode(): Int =
        Objects.hash(type.hash(), properties)

    override fun equals(other: Any?): Boolean =
        (other as? AnnotationContainer)?.type?.`is`(this.type) == true && this.properties == other.properties
}

inline fun <reified T: Annotation> AnnotationContainer(properties: Map<String, Any>) =
        AnnotationContainer(T::class.java, properties)

/**
 * Creates [AnnotationContainer] from annotation [T].
 */
fun <T : Annotation> T.toContainer(): AnnotationContainer {
    val properties = mutableMapOf<String, Any>()

    this.annotationClass.java.declaredMethods
        .filter {
            Modifier.isPublic(it.modifiers)
                    && it.parameterCount == 0
        }
        .map {
            properties[it.name] = it.invoke(this).annotationValue()
        }

    return AnnotationContainer(this.annotationClass.java, properties)
}

/**
 * Converts [Any] to a value compatible with [AnnotationContainer]
 */
private fun Any.annotationValue(): Any =
    when (this) {
        is Boolean, is Byte, is Char, is Short,
        is Int, is Float, is Long, is Double, is String,
        is Class<*>, is Enum<*> -> this
        is Annotation -> this.toContainer()
        else -> {
            if (this::class.java.isArray) {
                val arr = ArrayUtils.toObjectArray(this)

                arr.map { it.annotationValue() }.toList()
            } else throw IllegalArgumentException("Cannot convert $this to annotation value.")
        }
    }
