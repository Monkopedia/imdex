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
import com.monkopedia.imdex.SpanType.BOLD
import com.monkopedia.imdex.SpanType.CROSSED_OUT
import com.monkopedia.imdex.SpanType.SUPERSCRIPT
import com.monkopedia.imdex.SpanType.UNDERLINE
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ext.definition.DefinitionList
import com.vladsch.flexmark.ext.footnotes.Footnote
import com.vladsch.flexmark.ext.footnotes.FootnoteBlock
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.superscript.Superscript

object StrikethroughHandler : NodeHandler<Strikethrough> {
    override suspend fun onNode(
        node: Strikethrough,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val base = SpanBuilder()
        state.withSpan(base, handleChildren)
        state.span.append(base, CROSSED_OUT)
    }
}

object StrongEmphasisHandler : NodeHandler<StrongEmphasis> {
    override suspend fun onNode(
        node: StrongEmphasis,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val base = SpanBuilder()
        state.withSpan(base, handleChildren)
        state.span.append(base, BOLD, UNDERLINE)
    }
}

object EmphasisHandler : NodeHandler<Emphasis> {
    override suspend fun onNode(
        node: Emphasis,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val base = SpanBuilder()
        state.withSpan(base, handleChildren)
        state.span.append(base, BOLD)
    }
}

object SuperscriptHandler : NodeHandler<Superscript> {
    override suspend fun onNode(
        node: Superscript,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val base = SpanBuilder()
        state.withSpan(base, handleChildren)
        state.span.append(base, SUPERSCRIPT)
    }
}

object FootnoteHandler : NodeHandler<Footnote> {
    override suspend fun onNode(
        node: Footnote,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        // Do nothing
    }
}

object FootnoteBlockHandler : NodeHandler<FootnoteBlock> {
    override suspend fun onNode(
        node: FootnoteBlock,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        // Do nothing
    }
}

object DefinitionListHandler : NodeHandler<DefinitionList> {
    override suspend fun onNode(
        node: DefinitionList,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        // Do nothing
    }
}
