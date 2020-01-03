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

import com.github.jonathanxd.iutils.kt.textOf
import com.github.jonathanxd.iutils.localization.Locales
import com.github.jonathanxd.iutils.localization.MapLocaleManager
import com.github.jonathanxd.iutils.text.Text
import com.github.jonathanxd.iutils.text.localizer.DefaultTextLocalizer
import com.github.jonathanxd.iutils.text.localizer.FastTextLocalizer
import com.github.jonathanxd.iutils.text.localizer.TextLocalizer
import com.github.jonathanxd.kores.type.`is`
import com.github.jonathanxd.redin.*
import org.junit.Assert
import org.junit.Test
import javax.inject.Inject
import javax.inject.Qualifier

class CustomQualifiers {

    class My @Inject constructor(
        @Localizer(LocalizerType.FAST) val fastLocalizer: TextLocalizer,
        @Localizer(LocalizerType.NORMAL) val normalLocalizer: TextLocalizer
    )

    @Test
    fun customQualifiers() {
        val mapLocaleManager = MapLocaleManager()
        val localeA = Locales.create("a")

        localeA.localizationManager.registerLocalization("test.a", textOf("A"))
        val fast = FastTextLocalizer(mapLocaleManager, localeA)
        val normal = DefaultTextLocalizer(mapLocaleManager, localeA)

        val injector = Redin {
            bind<TextLocalizer>() qualifiedWith LocalizerQualifier.FAST toValue fast
            bind<TextLocalizer>() qualifiedWith LocalizerQualifier.NORMAL toValue normal
        }

        val my = injector.get<My>()

        Assert.assertTrue(my.fastLocalizer is FastTextLocalizer)
        Assert.assertTrue(my.normalLocalizer is DefaultTextLocalizer)
        Assert.assertEquals("A", my.fastLocalizer.localize(Text.localizable("test.a")))
    }

    enum class LocalizerType {
        FAST,
        NORMAL
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class Localizer(val value: LocalizerType)

    data class LocalizerQualifier(val type: LocalizerType): BindQualifier {
        override fun matches(annotationContainer: AnnotationContainer): Boolean =
            annotationContainer.type.`is`(Localizer::class.java)
                    && annotationContainer.getAsOrNull<LocalizerType>("value") == this.type

        companion object {
            val FAST = LocalizerQualifier(LocalizerType.FAST)
            val NORMAL = LocalizerQualifier(LocalizerType.NORMAL)
        }
    }
}