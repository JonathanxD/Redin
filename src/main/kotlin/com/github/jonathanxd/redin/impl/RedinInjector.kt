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
package com.github.jonathanxd.redin.impl

import com.github.jonathanxd.redin.Bind
import com.github.jonathanxd.redin.BindListModifier
import com.github.jonathanxd.redin.InjectionTarget
import com.github.jonathanxd.redin.Injector
import java.util.*
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.collections.set
import kotlin.collections.toMutableList

typealias JwLazy<T> = com.github.jonathanxd.iutils.`object`.Lazy<T>

class RedinInjector(override val mutableBinds: MutableList<Bind<*>>) : AbstractRedinInjector(), BindListModifier {
    override val binds: List<Bind<*>>
        get() = this.mutableBinds

    private val _scopedBindings: MutableMap<Bind<*>, Any> = mutableMapOf()
    override val scopedBindings: Map<Bind<*>, Any> = Collections.unmodifiableMap(this._scopedBindings)

    private val _lateTargets: MutableSet<InjectionTarget> = mutableSetOf()
    override val lateTargets: Set<InjectionTarget> = Collections.unmodifiableSet(this._lateTargets)

    private val _hotSwappableTargets: MutableSet<Injector.HotSwappable> = mutableSetOf()
    override val hotSwappableTargets: Set<Injector.HotSwappable> = Collections.unmodifiableSet(this._hotSwappableTargets)

    init {
        bind {
            bind<Injector>() toValue this@RedinInjector
        }
    }

    override fun addBind(bind: Bind<*>) {
        this.mutableBinds.add(bind)
    }

    override fun removeBind(bind: Bind<*>) {
        this.mutableBinds.remove(bind)
    }

    override fun removeBindIf(predicate: (Bind<*>) -> Boolean) {
        this.mutableBinds.removeIf(predicate)
    }

    override fun addScopedBinding(bind: Bind<*>, instance: Any) {
        this._scopedBindings[bind] = instance
    }

    override fun addLateTarget(target: InjectionTarget) {
        this._lateTargets.add(target)
    }

    override fun addHotSwappableTarget(target: Injector.HotSwappable) {
        this._hotSwappableTargets.add(target)
    }

    override fun removeLateTargetsThat(predicate: (InjectionTarget) -> Boolean) {
        this._lateTargets.removeIf(predicate)
    }

    override fun newInjector(keepScoped: Boolean): RedinInjector {
        val newInjector = RedinInjector(this.binds.toMutableList())
        if (keepScoped) newInjector._scopedBindings += this.scopedBindings
        return newInjector
    }

}

