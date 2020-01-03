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

import com.github.jonathanxd.iutils.`object`.LateInit
import com.github.jonathanxd.redin.*
import org.junit.Assert
import org.junit.Test

class ChildInjection {

    @RedinInject
    class Bank(val accountService: AccountService, val obj: Obj)

    @Test
    fun inject() {
        val injector = Redin {
            bind<AccountService>().inScope(SINGLETON).toImplementation<MapAccountService>()
            bind<Obj>().inScope(SINGLETON).toProvider { Obj() }
        }

        val bank = injector[Bank::class.java]

        val obj = bank.obj
        Assert.assertEquals(1L, bank.accountService.deposit(Account("Redin"), 1L))
        Assert.assertEquals(2L, bank.accountService.deposit(Account("Redin"), 1L))
        Assert.assertEquals(12L, bank.accountService.deposit(Account("Redin"), 10L))
        Assert.assertEquals(2L, bank.accountService.withdraw(Account("Redin"), 10L))
        Assert.assertEquals(0L, bank.accountService.withdraw(Account("Redin"), 2L))
        Assert.assertEquals(10L, bank.accountService.deposit(Account("Redin"), 10L))
        Assert.assertEquals(-12L, bank.accountService.withdraw(Account("Redin"), 12L)) // Not enough money
        Assert.assertEquals(0L, bank.accountService.withdraw(Account("Redin"), 10L)) // Not enough money

        val child = injector.child {
            bind<AccountService>().inScope(SINGLETON).toImplementation<MapAccountServiceSelf>()
        }

        child.inheritParentScopedBindingsCache()

        val bank2 = child[Bank::class.java]

        Assert.assertTrue(obj == bank2.obj)
        Assert.assertEquals(1L, bank2.accountService.deposit(Account("Redin"), 1L))
        Assert.assertEquals(1L, bank2.accountService.deposit(Account("Redin"), 1L))
        Assert.assertEquals(10L, bank2.accountService.deposit(Account("Redin"), 10L))
        Assert.assertEquals(-10L, bank2.accountService.withdraw(Account("Redin"), 10L))
        Assert.assertEquals(-2L, bank2.accountService.withdraw(Account("Redin"), 2L))
        Assert.assertEquals(10L, bank2.accountService.deposit(Account("Redin"), 10L))
        Assert.assertEquals(0, bank2.accountService.withdraw(Account("Redin"), 12L)) // Not enough money

        val bank3 = injector[Bank::class.java] // Base injector
        Assert.assertTrue(obj == bank3.obj)
        Assert.assertEquals(-2L, bank3.accountService.withdraw(Account("Redin"), 2L))

    }

    class Obj

    class MapAccountService : AccountService {
        private val map = mutableMapOf<Account, Long>()

        override fun withdraw(account: Account, amount: Long): Long {
            if (amount < 0)
                return this.deposit(account, amount)

            val money = this.map[account]
            return if (money == null) {
                -amount
            } else {
                if (money < amount) {
                    -amount
                } else {
                    this.map[account] = money - amount
                    money - amount
                }
            }
        }

        override fun deposit(account: Account, amount: Long): Long {
            if (amount < 0)
                return this.withdraw(account, amount)

            val money = this.map.computeIfAbsent(account) { 0 }
            this.map[account] = money + amount
            return money + amount
        }
    }

    class MapAccountServiceSelf : AccountService {
        private val map = mutableMapOf<Account, Long>()

        override fun withdraw(account: Account, amount: Long): Long {
            if (amount < 0)
                return this.deposit(account, amount)

            val money = this.map[account]
            return if (money == null) {
                0
            } else {
                if (money < amount) {
                    0
                } else {
                    this.map[account] = money - amount
                    -amount
                }
            }
        }

        override fun deposit(account: Account, amount: Long): Long {
            if (amount < 0)
                return this.withdraw(account, amount)

            val money = this.map.computeIfAbsent(account) { 0 }
            this.map[account] = money + amount
            return amount
        }
    }

    interface AccountService {
        fun withdraw(account: Account, amount: Long): Long
        fun deposit(account: Account, amount: Long): Long
    }

    data class Account(val name: String)
}