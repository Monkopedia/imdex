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
package com.monkopedia.markdown.handlers

import com.monkopedia.markdown.NodeHandler
import com.monkopedia.markdown.ParsingState
import com.monkopedia.markdown.SpanBuilder
import com.monkopedia.markdown.SpanType.BR
import com.monkopedia.markdown.SpanType.CODE_STYLE
import com.vladsch.flexmark.ast.HtmlEntity
import com.vladsch.flexmark.ast.HtmlInline

object HtmlInlineHandler : NodeHandler<HtmlInline> {
    override suspend fun onNode(
        node: HtmlInline,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val text = node.chars.toString()
        handleHtml(text, state)
    }
}

object HtmlEntityHandler : NodeHandler<HtmlEntity> {
    override suspend fun onNode(
        node: HtmlEntity,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val text = node.chars.toString()
        handleHtml(text, state)
    }
}

private fun handleHtml(text: String, state: ParsingState) {
    when (text.trim()) {
        "<br>" -> {
            state.span.append(BR)
        }
        "<code>" -> {
            val element = SpanBuilder()
            state.spans.add(element)
        }
        "</code>" -> {
            val spanned = state.spans.removeLastOrNull() ?: return
            state.span.append(spanned, CODE_STYLE)
        }
    }
}
