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
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.redin.*
import org.junit.Test
import java.util.logging.Logger
import javax.inject.Named
import javax.inject.Singleton

class Doc {

    data class Account(val id: Long)

    interface AccountService {
        fun deposit(account: Account, amount: Long)
    }


    class AccountServiceImpl : AccountService {
        override fun deposit(account: Account, amount: Long) {
            TODO("Not yet implemented")
        }
    }

    class BankService @Inject constructor(val accountService: AccountService)

    @Test
    fun inject() {
        val injector = Redin {
            bind<AccountService>() inScope SINGLETON toImplementation(AccountServiceImpl::class.java)
            bind<BankService>() inScope SINGLETON toImplementation(BankService::class.java)
        }

        val accountService = injector.provide<AccountService>(scope = SINGLETON)
        val bankService = injector.provide<BankService>(scope = SINGLETON)


    }

    @Test
    fun inject2() {
        val injector = Redin {
            bind<AccountService>() inScope SINGLETON toImplementation(AccountServiceImpl::class.java)
        }

        val accountService = injector.provide<AccountService>(scope = SINGLETON)
        injector.bind {
            bind<BankService>() inScope SINGLETON toImplementation(BankService::class.java)
        }

        val bankService = injector.provide<BankService>(scope = SINGLETON)
    }

    @Test
    fun inject3() {
        data class Project(val name: String)
        abstract class Plugin {
            abstract fun init()
        }
        class PluginA @Inject constructor(val logger: Logger, val project: Project, val injector: Injector): Plugin() {
            override fun init() {
                logger.info("Plugin A initialized for project ‘${this.project.name}’!")
            }
        }
        class PluginB @Inject constructor(val logger: Logger, val project: Project, val injector: Injector): Plugin() {
            override fun init() {
                logger.info("Plugin B initialized for project ‘${this.project.name}’!")
            }
        }

        val injector = Redin {
            bind<Project>() inScope SINGLETON toValue Project("Test")
        }

        val pluginClasses = listOf(PluginA::class.java, PluginB::class.java)
        val pluginInstances = mutableListOf<Plugin>()

        for (pluginClass in pluginClasses) {
            val pluginInjector = injector.child {
                bind<Logger>().inSingletonScope().toValue(Logger.getLogger(pluginClass.name))
            }

            pluginInstances.add(pluginInjector[pluginClass])
        }


        pluginInstances.forEach(Plugin::init)

    }

    @Test
    fun inject4List() {
        data class Project(val name: String)
        abstract class Plugin {
            abstract val id: String
            abstract fun init()
        }
        class PluginA @Inject constructor(val logger: Logger, val project: Project, val injector: Injector): Plugin() {
            override val id: String = "com.example.PluginA"

            override fun init() {
                logger.info("Plugin A initialized for project ‘${this.project.name}’!")
            }
        }
        class PluginB @Inject constructor(val logger: Logger, val project: Project, val injector: Injector): Plugin() {
            override val id: String = "com.example.PluginB"

            override fun init() {
                logger.info("Plugin B initialized for project ‘${this.project.name}’!")
            }
        }
        class PluginC @Inject constructor(val logger: Logger,
                                          val project: Project,
                                          @Named("com.example.PluginB") val pluginB: Plugin,
                                          val injector: Injector): Plugin() {
            override val id: String = "com.example.PluginC"

            override fun init() {
                logger.info("Plugin C initialized for project ‘${this.project.name}’!")
            }
        }

        val injector = Redin {
            bind<Project>() inScope SINGLETON toValue Project("Test")
        }

        val pluginClasses = listOf(PluginA::class.java, PluginB::class.java, PluginC::class.java)
        val pluginInstances = mutableListOf<Plugin>()

        injector.bind {
            bindReified<List<Plugin>>().inSingletonScope().toProvider {
                println("called provider")
                pluginInstances
            }
            bind(TypeInfo.builderOf(List::class.java).of(Plugin::class.java).buildGeneric())
        }

        for (pluginClass in pluginClasses) {
            val pluginInjector = injector.child {
                bind<Logger>().inSingletonScope().toValue(Logger.getLogger(pluginClass.name))
            }

            val pluginInstance = pluginInjector[pluginClass]
            pluginInstances.add(pluginInjector[pluginClass])

            injector.bind {
                bind<Plugin>().inSingletonScope().qualifiedWith(Name(pluginInstance.id)).toValue(pluginInstance)
            }
        }


        pluginInstances.forEach(Plugin::init)

        class PluginSystem @Inject constructor(@Singleton val plugins: List<Plugin>)

        injector.bind {
            bind<PluginSystem>().inSingletonScope().toImplementation<PluginSystem>()
        }

        val system = injector.provide<PluginSystem>(scope = SINGLETON)()

    }



}