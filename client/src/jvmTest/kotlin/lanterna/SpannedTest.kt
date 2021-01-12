/*
 * Copyright 2020 Jason Monk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lanterna

import StdoutLogger
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.DefaultMutableThemeStyle
import com.googlecode.lanterna.gui2.Container
import com.googlecode.lanterna.gui2.Panel
import com.monkopedia.Log
import com.monkopedia.info
import com.monkopedia.lanterna.spannable.LinkSpan
import com.monkopedia.lanterna.spannable.Span
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.lanterna.spannable.ThemeSpan
import com.monkopedia.imdex.parseMarkdown
import com.monkopedia.lanterna.Lanterna
import com.monkopedia.lanterna.ThemeData
import com.monkopedia.lanterna.ThemeDefinitionData
import com.monkopedia.lanterna.ThemeStyleData
import com.monkopedia.lanterna.buildViews
import com.monkopedia.lanterna.vertical
import com.monkopedia.mdview.markdown
import java.io.File
import java.util.Properties
import junit.framework.Assert
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SpannedTest {
    class TestSpan : Span<TestSpan> {
        override fun copy() = TestSpan()
    }

    @Test
    fun `static span splitting`() {
        Log.init(StdoutLogger)
        val span = Spanned()
        span.append("Test ")
        span.append("other text", TestSpan())
        span.applySpans(5, 10, TestSpan())

        val staticSpans = span.toStaticSpans()
        Assert.assertEquals(listOf("Test ", "other", " text"), staticSpans.map { "$it" })
        Assert.assertEquals(2, staticSpans[1].spans.size)
        Assert.assertEquals(1, staticSpans[2].spans.size)
    }

    @Test
    fun `link surrounding code`() {
        val style = DefaultMutableThemeStyle(TextColor.ANSI.BLACK, TextColor.ANSI.BLACK)
        val span = Spanned()
        val linkSpan = Spanned()
        linkSpan.append("This is some ") // 1
        val code = Spanned()
        code.append("code") // 2
        code.applySpans(0, code.length, ThemeSpan(style))
        linkSpan.append(code)
        linkSpan.append(" ") // 3

        span.append("base text. ") // 0
        span.append(linkSpan, LinkSpan({ }, style, style))
        linkSpan.append("")

        Assert.assertEquals(
            listOf("base text. ", "This is some ", "code", " "),
            span.toStaticSpans().map { "$it" }
        )
    }

    private val testString = """
        |  Name|  Summary| 
        |---|---|
        | [offer](offer.md)| Brief description  <br>Immediately adds the specified [element]() to this channel, if this doesn't violate its capacity restrictions, and returns <code>true</code>. Otherwise, just returns <code>false</code>. This is a synchronous variant of [send](send.md) which backs off in situations when <code>send</code> suspends.Throws an exception if the channel [is closed for <code>send</code> ](index.md#kotlinx.coroutines.channels/SendChannel/isClosedForSend/#/PointingToDeclaration/) (see [close](close.md) for details).  <br>Content  <br>abstract fun [offer](offer.md)(element: [E](index.md)): [Boolean](../../kotlin/-boolean/index.md)  <br><br><br>
    """.trimIndent()
    val themeStyle = ThemeStyleData("#ffffff", "#000000", emptyList(), emptyMap())
    val theme =
        ThemeData(ThemeDefinitionData(themeStyle, themeStyle, themeStyle, themeStyle, themeStyle))

    @Test
    fun `parsed spannables`() {
        runBlocking {
            val nodes = parseMarkdown(testString)
            Log.init(StdoutLogger)
            Lanterna.init(File("/tmp"), theme, Properties())
            val panel = buildViews {
                vertical {
                    markdown(nodes) { _, _, _ ->
                        // Nothing
                    }
                }
            }.first() as Panel
            val textChild = (panel.childrenList[0] as Container).childrenList[3] as SpannableLabel
            val text = textChild.getCharSequence() as Spanned
            Log.info("Panel: ${text::class} $text")
            text.spans.forEach {
                Log.info("Span: $it ${text.toString().substring(it.start, it.end)}")
            }
            text.children.forEach {
                println("Child: ${it::class} $it")
            }
            text.toStaticSpans().forEach {
                println("Span: ${it.length} ${it.spans} $it")
            }
        }
    }
}
