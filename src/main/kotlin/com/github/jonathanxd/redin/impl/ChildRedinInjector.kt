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
package com.github.jonathanxd.redin.impl

import com.github.jonathanxd.redin.*

class ChildRedinInjector(private val parent: Injector,
                         override val mutableBinds: MutableList<Bind<*>>) : AbstractRedinInjector(), BindListModifier {

    private var inheritedScopedBindings: Map<Bind<*>, Any> = emptyMap()

    override val binds: List<Bind<*>>
        get() = this.parent.getAllBinds() + this.mutableBinds

    override val scopedBindings: Map<Bind<*>, Any>
        get() = this.inheritedScopedBindings
                .filterKeys { !this.mutableBinds.contains(it) } + this.mutableScopedBindings

    override val lateTargets: Set<InjectionTarget>
        get() = this.parent.lateTargets + this.mutableLateTargets

    override val hotSwappableTargets: Set<Injector.HotSwappable>
        get() = this.parent.hotSwappableTargets + this.mutableHotSwappableTargets

    private val mutableScopedBindings = mutableMapOf<Bind<*>, Any>()
    private val mutableLateTargets = mutableSetOf<InjectionTarget>()
    private val mutableHotSwappableTargets = mutableSetOf<Injector.HotSwappable>()

    init {
        bind {
            bind<Injector>() toValue this@ChildRedinInjector
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

    override fun inheritParentScopedBindingsCache(): ChildRedinInjector {
        this.inheritedScopedBindings = this.parent.scopedBindings
        return this
    }

    override fun addScopedBinding(bind: Bind<*>, instance: Any) {
        this.mutableScopedBindings[bind] = instance
    }

    override fun addLateTarget(target: InjectionTarget) {
        this.mutableLateTargets.add(target)
    }

    override fun addHotSwappableTarget(target: Injector.HotSwappable) {
        this.mutableHotSwappableTargets.add(target)
    }

    override fun removeLateTargetsThat(predicate: (InjectionTarget) -> Boolean) {
        this.mutableLateTargets.removeIf(predicate)
    }

    override fun newInjector(keepScoped: Boolean): ChildRedinInjector {
        val newInjector = ChildRedinInjector(this.parent.newInjector(keepScoped), this.binds.toMutableList())
        if (keepScoped) newInjector.mutableScopedBindings += this.scopedBindings
        return newInjector
    }


}