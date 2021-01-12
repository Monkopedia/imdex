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

import com.monkopedia.imdex.ImdexNodeType.FENCED_CODE
import com.monkopedia.imdex.NodeHandler
import com.monkopedia.imdex.ParsingState
import com.monkopedia.imdex.SpanBuilder
import com.monkopedia.imdex.SpanType
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.IndentedCodeBlock

object CodeHandler : NodeHandler<Code> {
    override suspend fun onNode(
        node: Code,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val base = SpanBuilder()
        state.withSpan(base, handleChildren)
        state.span.append(base, SpanType.CODE_STYLE)
    }
}

object FencedCodeBlockHandler : NodeHandler<FencedCodeBlock> {
    override suspend fun onNode(
        node: FencedCodeBlock,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val builder = SpanBuilder(
            node.startOffset,
            node.endOffset
        )
        state.invalidateChildren {
            state.withSpan(builder, handleChildren)
        }
        state.handler.children.add(builder.build(FENCED_CODE))
    }
}

object IndentedCodeBlockHandler : NodeHandler<IndentedCodeBlock> {
    override suspend fun onNode(
        node: IndentedCodeBlock,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val builder = SpanBuilder(
            node.startOffset,
            node.endOffset
        )
        state.invalidateChildren {
            state.withSpan(builder, handleChildren)
        }
        node.contentLines.forEachIndexed { i, s ->
            // if (i != 0) {
            //     builder.append(SpanType.BR)
            // }
            builder.append(s)
        }
        state.handler.children.add(builder.build(FENCED_CODE))
    }
}
