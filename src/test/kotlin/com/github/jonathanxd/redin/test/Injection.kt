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

import com.github.jonathanxd.iutils.kt.textOf
import com.github.jonathanxd.iutils.localization.Locale
import com.github.jonathanxd.iutils.localization.Locales
import com.github.jonathanxd.iutils.localization.MapLocaleManager
import com.github.jonathanxd.iutils.text.localizer.FastTextLocalizer
import com.github.jonathanxd.iutils.text.localizer.TextLocalizer
import com.github.jonathanxd.redin.Redin
import com.github.jonathanxd.redin.get
import org.junit.Assert
import org.junit.Test

class Injection {
    @Test
    fun injectionTest() {
        val mapLocaleManager = MapLocaleManager()
        val localeA = Locales.create("a")
        val localeB = Locales.create("b")

        localeA.localizationManager.registerLocalization("gui.title", textOf("GuiA"))
        localeB.localizationManager.registerLocalization("gui.title", textOf("GuiB"))

        val injector = Redin {
            bind<TextLocalizer>() toValue FastTextLocalizer(mapLocaleManager, localeA)
            bind<Locale>() toValue localeA
        }

        val gui = injector.get<Gui>()

        Assert.assertEquals("GuiA", gui.title)

        injector.bind {
            bind<Locale>() toValue localeB
        }

        Assert.assertEquals("GuiB", gui.title)
    }
}