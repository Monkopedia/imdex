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

import com.ccfraser.muirwik.components.mThemeProvider
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.styles.Theme
import com.monkopedia.kpages.INSTANCE
import com.monkopedia.kpages.Navigator
import kotlinx.browser.window
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.a
import react.dom.b
import react.dom.br
import react.dom.del
import react.dom.h1
import react.dom.h2
import react.dom.h3
import react.dom.h4
import react.dom.h5
import react.dom.h6
import styled.css
import styled.styledCode

actual data class Spanned(val element: RBuilder.() -> Unit)

actual enum class Gravity {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
}

actual class RenderingState(
    val codeTheme: Theme,
    actual val content: String,
    actual val linkContext: LinkContext
) {

    actual fun buildSpan(node: ImdexNode): Spanned {
        return Spanned {
            mTypography {
                node.spans.forEach { span ->
                    val types = span.type.toMutableList()
                    var str =
                        if (types.contains(SpanType.USE_DATA)) span.data!!
                        else content.substring(span.s!!, span.e!!)
                    appendSpanned(str, types, span.data)
                }
            }
        }
    }

    private fun RBuilder.appendSpanned(str: String, types: MutableList<SpanType>, data: String?) {
        if (types.isEmpty()) {
            +str
            return
        }
        when (types.removeLast()) {
            SpanType.HEADER1 -> h1 { appendSpanned(str, types, data) }
            SpanType.HEADER2 -> h2 { appendSpanned(str, types, data) }
            SpanType.HEADER3 -> h3 { appendSpanned(str, types, data) }
            SpanType.HEADER4 -> h4 { appendSpanned(str, types, data) }
            SpanType.HEADER5 -> h5 { appendSpanned(str, types, data) }
            SpanType.HEADER6 -> h6 { appendSpanned(str, types, data) }
            SpanType.HEADER7 -> h6 { appendSpanned(str, types, data) }
            SpanType.CODE_STYLE -> mThemeProvider(codeTheme) {
                styledCode {
                    css {
                        specific {
                            backgroundColor = Color(codeTheme.palette.background.default)
                            color = Color(codeTheme.palette.text.secondary)
                        }
                    }
                    codeStyle(codeTheme)
                    appendSpanned(str, types, data)
                }
            }
            SpanType.HARD_BREAK -> {
                +"\n\n"
            }
            SpanType.SOFT_BREAK -> {
                +" "
            }
            SpanType.CROSSED_OUT -> del {
                appendSpanned(str, types, data)
            }
            SpanType.BOLD -> b {
                appendSpanned(str, types, data)
            }
            SpanType.UNDERLINE -> {
                // TODO: Underline
                appendSpanned(str, types, data)
            }
            SpanType.URL -> createAction(linkContext, data!!) {
                appendSpanned(str, types, data)
            }
            SpanType.PATH -> createAction(linkContext, data!!) {
                appendSpanned(str, types, data)
            }
            SpanType.USE_DATA -> appendSpanned(str, types, data)
            SpanType.BR -> br {}
            SpanType.SUPERSCRIPT -> appendSpanned(str, types, data)
        }
    }
}

private fun RBuilder.createAction(
    linkContext: LinkContext,
    url: String,
    children: RBuilder.() -> Unit
) {
    a {
        attrs {
            href = url
            onClickFunction = with(url) {
                when {
                    startsWith("http://") || startsWith("https://") -> urlAction(url)
                    contains("#") && startsWith("/") -> {
                        val (path, scrollPosition) = splitPath()
                        pathAction(linkContext, path, scrollPosition)
                    }
                    contains("#") -> {
                        val (path, scrollPosition) = splitPath()
                        pathAction(linkContext, path, scrollPosition)
                    }
                    startsWith("/") -> pathAction(linkContext, this, null)
                    isNotEmpty() -> pathAction(linkContext, this, null)
                    else -> throw IllegalArgumentException("Can't parse link $url")
                }
            }
        }
        children()
    }
}

private fun String.splitPath(): Pair<String, String> {
    val index = indexOf("#")
    val path = substring(0, index)
    val scrollPosition = substring(index + 1)
    return Pair(path, scrollPosition)
}

fun urlAction(url: String): (Event) -> Unit = {
    it.stopPropagation()
    it.preventDefault()
    window.open(url, "_blank")
}

fun pathAction(
    linkContext: LinkContext,
    path: String,
    scrollTo: String? = null
): (Event) -> Unit = {
    it.stopPropagation()
    it.preventDefault()
    linkContext.launchLink(Navigator.INSTANCE, path, scrollTo)
}
