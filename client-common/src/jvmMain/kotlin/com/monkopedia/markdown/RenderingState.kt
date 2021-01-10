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
package com.monkopedia.markdown

import com.googlecode.lanterna.SGR
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.spannable.Action
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.LinkSpan
import com.monkopedia.lanterna.spannable.Span
import com.monkopedia.lanterna.spannable.ThemeSpan
import com.monkopedia.markdown.SpanType.BOLD
import com.monkopedia.markdown.SpanType.BR
import com.monkopedia.markdown.SpanType.CODE_STYLE
import com.monkopedia.markdown.SpanType.CROSSED_OUT
import com.monkopedia.markdown.SpanType.HARD_BREAK
import com.monkopedia.markdown.SpanType.HEADER1
import com.monkopedia.markdown.SpanType.HEADER2
import com.monkopedia.markdown.SpanType.HEADER3
import com.monkopedia.markdown.SpanType.HEADER4
import com.monkopedia.markdown.SpanType.HEADER5
import com.monkopedia.markdown.SpanType.HEADER6
import com.monkopedia.markdown.SpanType.HEADER7
import com.monkopedia.markdown.SpanType.PATH
import com.monkopedia.markdown.SpanType.SOFT_BREAK
import com.monkopedia.markdown.SpanType.SUPERSCRIPT
import com.monkopedia.markdown.SpanType.UNDERLINE
import com.monkopedia.markdown.SpanType.URL
import com.monkopedia.markdown.SpanType.USE_DATA
import java.awt.Desktop
import java.net.URI

actual typealias Spanned = com.monkopedia.lanterna.spannable.Spanned
actual typealias Gravity = com.monkopedia.dynamiclayout.Gravity

fun createAction(linkContext: LinkContext, url: CharSequence): Action? {
    return with(url.toString()) {
        when {
            startsWith("http://") || startsWith("https://") -> UrlAction(url)
            contains("#") && startsWith("/") -> {
                val (path, scrollPosition) = splitPath()
                PathAction(linkContext, path, scrollPosition)
            }
            contains("#") -> {
                val (path, scrollPosition) = splitPath()
                PathAction(linkContext, path, scrollPosition)
            }
            startsWith("/") -> PathAction(linkContext, this, null)
            isNotEmpty() -> PathAction(linkContext, this, null)
            else -> null
        }
    }
}
private fun String.splitPath(): Pair<String, String> {
    val index = indexOf("#")
    val path = substring(0, index)
    val scrollPosition = substring(index + 1)
    return Pair(path, scrollPosition)
}

class UrlAction(private val url: CharSequence) : Action {
    override fun invoke(navigation: Navigation) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url.toString()))
        } else {
            Runtime.getRuntime().exec("xdg-open $url")
        }
    }
}

class PathAction(
    private val linkContext: LinkContext,
    private val path: String,
    private val scrollTo: String? = null
) : Action {
    override fun invoke(navigation: Navigation) {
        linkContext.launchLink(ImdexApp.INSTANCE.navigator, path, scrollTo)
    }
}

actual class RenderingState(actual val content: String, actual val linkContext: LinkContext) {
    val levelHandlers by lazy {
        if (Style.asciiFontsEnabled) {
            listOf(
                null,
                null,
                Style.headerPrimaryStyle,
                Style.headerSecondaryStyle,
            )
        } else {
            listOf(
                Style.headerPrimaryStyle,
                Style.headerSecondaryStyle,
            )
        }
    }

    actual fun buildSpan(node: ImdexNode): Spanned {
        return Spanned().apply {
            node.spans.forEach { span ->
                val types = span.type
                var str = if (types.contains(USE_DATA)) span.data!! else content.substring(
                    span.s!!,
                    span.e!!
                )

                val spans = types.mapNotNull<SpanType, Span<*>> {
                    when (it) {
                        HEADER1 -> levelHandlers[1.coerceAtMost(levelHandlers.size - 1)]?.let { t ->
                            ThemeSpan(
                                t
                            )
                        }
                        HEADER2 -> levelHandlers[2.coerceAtMost(levelHandlers.size - 1)]?.let { t ->
                            ThemeSpan(
                                t
                            )
                        }
                        HEADER3 -> levelHandlers[3.coerceAtMost(levelHandlers.size - 1)]?.let { t ->
                            ThemeSpan(
                                t
                            )
                        }
                        HEADER4 -> levelHandlers[4.coerceAtMost(levelHandlers.size - 1)]?.let { t ->
                            ThemeSpan(
                                t
                            )
                        }
                        HEADER5 -> levelHandlers[5.coerceAtMost(levelHandlers.size - 1)]?.let { t ->
                            ThemeSpan(
                                t
                            )
                        }
                        HEADER6 -> levelHandlers[6.coerceAtMost(levelHandlers.size - 1)]?.let { t ->
                            ThemeSpan(
                                t
                            )
                        }
                        HEADER7 -> levelHandlers[7.coerceAtMost(levelHandlers.size - 1)]?.let { t ->
                            ThemeSpan(
                                t
                            )
                        }
                        CODE_STYLE -> ThemeSpan(Style.codeStyle)
                        HARD_BREAK -> {
                            str += "\n\n"
                            null
                        }
                        SOFT_BREAK -> {
                            str += " "
                            null
                        }
                        CROSSED_OUT -> EnableSGRSpan(SGR.CROSSED_OUT)
                        BOLD -> EnableSGRSpan(SGR.BOLD)
                        UNDERLINE -> EnableSGRSpan(SGR.UNDERLINE)
                        URL -> createAction(linkContext, span.data!!)?.let { it1 ->
                            LinkSpan(
                                it1,
                                Style.selectedLinkStyle,
                                Style.linkStyle
                            )
                        }
                        PATH -> createAction(linkContext, span.data!!)?.let { it1 ->
                            LinkSpan(
                                it1,
                                Style.selectedLinkStyle,
                                Style.linkStyle
                            )
                        }
                        USE_DATA -> null
                        BR -> null
                        SUPERSCRIPT -> null
                    }
                }.toList()
                append(str, *(spans.toTypedArray()))
                if (types.contains(BR) && !endsWith("\n")) {
                    append("\n")
                }
            }
        }
    }
}
