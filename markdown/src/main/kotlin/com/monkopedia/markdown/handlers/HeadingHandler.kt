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

import com.monkopedia.markdown.ImdexNodeType.HEADING
import com.monkopedia.markdown.NodeHandler
import com.monkopedia.markdown.ParsingState
import com.monkopedia.markdown.SpanBuilder
import com.monkopedia.markdown.SpanType.HEADER1
import com.monkopedia.markdown.SpanType.HEADER2
import com.monkopedia.markdown.SpanType.HEADER3
import com.monkopedia.markdown.SpanType.HEADER4
import com.monkopedia.markdown.SpanType.HEADER5
import com.monkopedia.markdown.SpanType.HEADER6
import com.monkopedia.markdown.SpanType.HEADER7
import com.vladsch.flexmark.ast.Heading

object HeadingHandler : NodeHandler<Heading> {
    override suspend fun onNode(
        node: Heading,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val base = SpanBuilder()
        state.withSpan(base, handleChildren)
        val wrapping = SpanBuilder()
        wrapping.append(
            base,
            when (node.level) {
                1 -> HEADER1
                2 -> HEADER2
                3 -> HEADER3
                4 -> HEADER4
                5 -> HEADER5
                6 -> HEADER6
                else -> HEADER7
            }
        )
        state.handler.children.add(wrapping.build(HEADING))
    }
}
