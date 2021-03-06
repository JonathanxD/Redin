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

import com.github.jonathanxd.redin.*
import org.junit.Assert
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

class ModuleReadmeTest {

    @RedinInject
    class Example(val service: LoggingService)

    @Inject
    lateinit var service: LoggingService

    @Test
    fun readmeTest() {
        val injector = Redin {
            bind<LoggingService>() inScope SINGLETON toProvider { ListLoggingService() }
            module(MyModule())
        }

        val example = injector.get<Example>()

        injector.injectMembers(this)

        Assert.assertEquals(this.service, example.service)
    }

    class MyModule {
        @Provides
        @Singleton
        fun provideLoggingService(): LoggingService {
            return ListLoggingService()
        }
    }

    class ListLoggingService : LoggingService {
        private val _back = mutableListOf<String>()

        val list: List<String>
            get() = this._back

        override fun log(message: String) {
            this._back.add(message)
        }
    }

    interface LoggingService {
        fun log(message: String)
    }
}