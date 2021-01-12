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
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ast.TextBase
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacter

object TextHandler : NodeHandler<Text> {
    override suspend fun onNode(
        node: Text,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        state.span.append(node)
        state.invalidateChildren(handleChildren)
    }
}
object TextBaseHandler : NodeHandler<TextBase> {
    override suspend fun onNode(
        node: TextBase,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        state.span.append(node)
        state.invalidateChildren(handleChildren)
    }
}
object EscapedCharacterHandler : NodeHandler<EscapedCharacter> {
    override suspend fun onNode(
        node: EscapedCharacter,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        // Do nothing?
        state.invalidateChildren(handleChildren)
    }
}
