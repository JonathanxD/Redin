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

import com.github.jonathanxd.iutils.kt.Try
import com.github.jonathanxd.redin.*
import org.junit.Assert
import org.junit.Test
import java.util.*
import javax.inject.Inject

class ContextProviderTest {

    class My @Inject constructor(
        @Access("users", Mode.READ_ONLY) val accessed: Map<String, String>
    )

    @Test
    fun context() {
        val service = MyServiceManager()

        service.registerServiceData("users", mutableMapOf("example" to "@89513"))

        val injector = Redin {
            bindReified<Map<String, String>>() qualifiedWith AnnotatedWith<Access>() toProvider {
                val access = it.qualifier<Access>().last()
                service.getServiceData(access.getAs("name")).let {
                    if (access.getAs<Mode>("mode") == Mode.READ_ONLY) Collections.unmodifiableMap(it)
                    else it
                }
            }
        }

        val my = injector.get<My>()

        val tryModify = Try<Unit, java.lang.UnsupportedOperationException> { (my.accessed as MutableMap<String, String>)["A"] = "B" }

        Assert.assertTrue(tryModify.isError)
        Assert.assertTrue(tryModify.errorOrNull() is UnsupportedOperationException)
        Assert.assertEquals(mapOf("example" to "@89513"), my.accessed)

    }

    class MyServiceManager {
        private val serviceDataMap = mutableMapOf<String, Map<String, String>>()

        fun registerServiceData(name: String, map: Map<String, String>) {
            this.serviceDataMap[name] = map
        }

        fun getServiceData(name: String): Map<String, String> {
            return this.serviceDataMap[name]!!
        }
    }

}