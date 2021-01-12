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
package com.monkopedia.imdex.handlers

import com.monkopedia.imdex.NodeHandler
import com.monkopedia.imdex.ParsingState
import com.monkopedia.imdex.SpanBuilder
import com.monkopedia.imdex.SpanType.PATH
import com.monkopedia.imdex.SpanType.URL
import com.vladsch.flexmark.ast.AutoLink
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.LinkNodeBase
import com.vladsch.flexmark.ast.LinkRef

sealed class BaseLinkHandler<T : LinkNodeBase> : NodeHandler<T> {
    override suspend fun onNode(node: T, state: ParsingState, handleChildren: suspend () -> Unit) {
        val base = SpanBuilder()
        state.withSpan(base, handleChildren)
        if (base.isEmpty()) {
            base.append(node.url)
        }
        state.span.createAction(base, node.url)
    }
}

object AutoLinkHandler : BaseLinkHandler<AutoLink>()
object LinkHandler : BaseLinkHandler<Link>()

object LinkRefHandler : NodeHandler<LinkRef> {
    override suspend fun onNode(
        node: LinkRef,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val base = SpanBuilder()
        state.withSpan(base, handleChildren)
        state.span.createAction(base, node.reference)
    }
}

private fun SpanBuilder.createAction(base: SpanBuilder, url: CharSequence) {
    return with(url.toString()) {
        when {
            startsWith("http://") || startsWith("https://") -> append(base, url.toString(), URL)
            (contains("#") && startsWith("/")) ||
                contains("#") ||
                startsWith("/") ||
                isNotEmpty() -> append(base, url.toString(), PATH)
            else -> append(base)
        }
    }
}
