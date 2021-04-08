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

import com.github.jonathanxd.iutils.`object`.LateInit
import com.github.jonathanxd.redin.*
import org.junit.Assert
import org.junit.Test
import javax.inject.Singleton

class LateInjectionOnConstructor {

    @RedinInject
    class Node(val value: Any,
               @Late val parent: Node?)

    @Test(expected = InvalidInjectionTargetException::class)
    fun selfInjectionTestFail() {
        val injector = Redin {
            bind<Any>() toValue "Hello"
        }

        val node: Node = injector.get()
        injector.bind {
            bind<Node>() toValue node
        }

        Assert.assertTrue(node === node.parent)
    }

    @RedinInject
    class Node2(val value: Any) {
        @set:Late
        lateinit var parent: Node2
    }

    @Test
    fun selfInjectionTestRight() {
        val injector = Redin {
            bind<Any>() toValue "Hello"
        }

        val node: Node2 = injector.get()
        injector.bind {
            bind<Node2>() toValue node
        }

        Assert.assertTrue(node === node.parent)
    }

    @RedinInject
    class Node3(val value: Any,
                @Late val parent: LateInit.Ref<Node3>)

    @Test
    fun selfPreLateInjectionTest() {
        val injector = Redin {
            bind<Any>() toValue "Hello"
            val instance: Node3 = injector.get()
            bind<Node3>() toValue instance
        }

        val node: Node3 = injector.get()

        Assert.assertTrue(node !== node.parent.value)
    }

    @RedinInject
    class Node4(val value: Any,
                @Late @Singleton val parent: LateInit.Ref<Node4>)

    @Test
    fun selfPreLateInjectionSingletonTest() {
        val injector = Redin {
            bind<Any>() toValue "Hello"
            val instance: Node4 = injector.get(scope = SINGLETON)
            bind<Node4>() inScope SINGLETON toValue instance
        }

        val node: Node4 = injector.get(scope = SINGLETON)

        Assert.assertTrue(node === node.parent.value)
    }

}