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

import com.github.jonathanxd.redin.Redin
import com.github.jonathanxd.redin.SINGLETON
import com.github.jonathanxd.redin.get
import org.junit.Assert
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

class ScopeTest {

    class MyTest @Inject constructor(val messager: Messager)

    @Singleton
    class MyTest2 @Inject constructor(val messager: Messager)

    @Test
    fun scopeTest() {
        val injector = Redin {
            bind<Messager> { toProvider { MyMessager() } }
        }

        val test1 = injector.get<MyTest>()
        val test2 = injector.get<MyTest>()

        Assert.assertTrue(test1.messager !== test2.messager)

        val injector2 = Redin {
            bind<Messager>() inScope SINGLETON toProvider { MyMessager() }
        }

        val test3 = injector2.get<MyTest>()
        val test4 = injector2.get<MyTest>()

        Assert.assertTrue(test3.messager === test4.messager)

        val injector3 = Redin {
            bind<Messager>() inScope SINGLETON toProvider { MyMessager() }
        }

        val testSingA = injector3.get<MyTest2>()
        val testSingB = injector3.get<MyTest2>()

        Assert.assertTrue(testSingA === testSingB)
    }

    class MyMessager : Messager {
        override fun sendMessage(message: String) {
        }
    }

    interface Messager {
        fun sendMessage(message: String)
    }
}