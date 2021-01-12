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

import com.monkopedia.imdex.ImdexNodeType.PARAGRAPH
import com.monkopedia.imdex.NodeHandler
import com.monkopedia.imdex.ParsingState
import com.monkopedia.imdex.SpanBuilder
import com.vladsch.flexmark.ast.Paragraph

object ParagraphHandler : NodeHandler<Paragraph> {
    override suspend fun onNode(
        node: Paragraph,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val span = SpanBuilder(
            node.startOffset,
            node.endOffset
        )
        state.invalidateChildren {
            state.withSpan(span, handleChildren)
        }
        state.handler.children.add(span.build(PARAGRAPH))
    }
}
