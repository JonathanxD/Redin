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
package com.github.jonathanxd.redin.test

import com.github.jonathanxd.kores.type.`is`
import com.github.jonathanxd.redin.*
import org.junit.Test
import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MyQualifier(val name: String)

fun myQualifier(name: String) =
    AnnotationContainer<MyQualifier>(mapOf("name" to name))


data class MyBindQualifier(val name: String) : BindQualifier {
    override fun matches(annotationContainer: AnnotationContainer): Boolean =
        annotationContainer.type.`is`(MyQualifier::class.java)
                && annotationContainer["name"] == name

}

class DocMyQualifier {

    class ToInject
    class QualifierTest @Inject constructor(@MyQualifier("test") val inject: ToInject)

    @Test
    fun myQualifier() {
        val injector = Redin {
            bind<ToInject>() inScope SINGLETON qualifiedWith MyBindQualifier("test") toValue ToInject()
            bindToImplementation<QualifierTest>(scope = SINGLETON)
        }

        val qualifierTest = injector.provide<QualifierTest>(scope = SINGLETON)()
        val toInject = injector.provide<ToInject>(scope = SINGLETON, qualifiers = listOf(myQualifier("test")))()
    }



}