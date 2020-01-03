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
package com.github.jonathanxd.redin.test

import com.github.jonathanxd.redin.*
import org.junit.Assert
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

class DirectTest {

    @Test
    fun test() {
        var x = 0
        val injector = Redin {
            bind<Int>() toProvider { x++ }
        }

        val number = injector.provide<Int>()
        Assert.assertEquals(0, number.invoke())
        Assert.assertEquals(1, number.invoke())

        injector.bind {
            bind<Int>() qualifiedWith Name("y") toValue 0
        }

        Assert.assertEquals(2, number.invoke())

        val y = injector.provide<Int>(listOf(AnnotationContainer<Named>(mapOf("value" to "y"))))

        Assert.assertEquals(0, y.invoke())

        var x2 = 0

        injector.bind {
            bind<Int>() inScope SINGLETON qualifiedWith Name("h") toProvider { x2++ }
        }

        val b = provide<Int>(injector) {
            this qualifiedWith nameContainer("h") inScope SINGLETON
        }

        Assert.assertEquals(0, b.invoke())
        Assert.assertEquals(0, b.invoke())
    }
}