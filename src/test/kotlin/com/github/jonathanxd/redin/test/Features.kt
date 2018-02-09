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
package com.github.jonathanxd.redin.test

import com.github.jonathanxd.iutils.`object`.LateInit
import com.github.jonathanxd.redin.*
import org.junit.Assert
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

class Features {
    private var fetched = false
    private var fetched2 = false
    private var fetched3 = false
    private var fetched4 = false

    class My @Inject constructor(
        @Named("amount") @Late val amount: LateInit.Int,
        @Named("url") val url: String,
        @Named("url2") val url2: String,
        @Named("numbers") val numbers: List<Int>,
        @Named("numbers") val numbers2: List<String>,
        @Named("expensiveValue") @LazyDep val data: RemoteData,
        @Named("expensiveValue2") @HotSwappable @LazyDep val data2: RemoteData,
        @Named("expensiveValue4") @LazyDep val data4: Lazy<RemoteData>
    )

    @Test
    fun features() {
        val injector = Redin {
            bind<String>() qualifiedWith Name("url") toValue "http://test"
            bind<String>() qualifiedWith Name("url2") toValue "http://test2"
            bindReified<List<Int>>() qualifiedWith Name("numbers") toValue listOf(1, 2, 3)
            bindReified<List<String>>() qualifiedWith Name("numbers") toValue listOf("1", "2", "3")
            bind<RemoteData>() qualifiedWith Name("expensiveValue") toProvider { fetchRemoteData() }
            bind<RemoteData>() qualifiedWith Name("expensiveValue2") toProvider { fetchRemoteData2() }
            bind<RemoteData>() qualifiedWith Name("expensiveValue4") toProvider { fetchRemoteData4() }
        }

        val my = injector.get<My>()

        Assert.assertEquals("http://test", my.url)
        Assert.assertEquals("http://test2", my.url2)
        Assert.assertEquals(listOf(1, 2, 3), my.numbers)
        Assert.assertEquals(listOf("1", "2", "3"), my.numbers2)
        Assert.assertFalse(my.amount.isInitialized)

        injector.bind {
            bind<Int>() qualifiedWith Name("amount") toValue 10
        }

        Assert.assertTrue(my.amount.isInitialized)
        Assert.assertEquals(10, my.amount.value)

        Assert.assertFalse(fetched)
        Assert.assertEquals(listOf("x"), my.data.data)
        Assert.assertTrue(fetched)

        Assert.assertFalse(fetched2)
        Assert.assertEquals(listOf("x2"), my.data2.data)
        Assert.assertTrue(fetched2)

        injector.bind {
            bind<RemoteData>() qualifiedWith Name("expensiveValue2") toProvider { fetchRemoteData3() }
        }

        Assert.assertFalse(fetched3)
        Assert.assertEquals(listOf("x3"), my.data2.data)
        Assert.assertTrue(fetched3)

        Assert.assertFalse(fetched4)
        Assert.assertEquals(listOf("x4"), my.data4.value.data)
        Assert.assertTrue(fetched3)
    }

    fun fetchRemoteData(): RemoteData {
        Thread.sleep(100)
        fetched = true
        return object : RemoteData {
            override val data: List<String> = listOf("x")
        }
    }

    fun fetchRemoteData2(): RemoteData {
        Thread.sleep(100)
        fetched2 = true
        return object : RemoteData {
            override val data: List<String> = listOf("x2")
        }
    }

    fun fetchRemoteData3(): RemoteData {
        Thread.sleep(100)
        fetched3 = true
        return object : RemoteData {
            override val data: List<String> = listOf("x3")
        }
    }

    fun fetchRemoteData4(): RemoteData {
        Thread.sleep(100)
        fetched4 = true
        return object : RemoteData {
            override val data: List<String> = listOf("x4")
        }
    }

    interface RemoteData {
        val data: List<String>
    }
}