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
import com.monkopedia.markdown.SpanType.HARD_BREAK
import com.monkopedia.markdown.SpanType.SOFT_BREAK
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.SoftLineBreak

object HardLineBreakHandler : NodeHandler<HardLineBreak> {
    override suspend fun onNode(
        node: HardLineBreak,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        state.span.append(HARD_BREAK)
        state.invalidateChildren(handleChildren)
    }
}

object SoftLineBreakHandler : NodeHandler<SoftLineBreak> {
    override suspend fun onNode(
        node: SoftLineBreak,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        state.span.append(SOFT_BREAK)
        state.invalidateChildren(handleChildren)
    }
}
