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

import com.monkopedia.markdown.ImdexNode
import com.monkopedia.markdown.ImdexNodeType
import com.monkopedia.markdown.ImdexNodeType.BULLET
import com.monkopedia.markdown.ImdexNodeType.NUMBERED
import com.monkopedia.markdown.NodeHandler
import com.monkopedia.markdown.ParsingState
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem

object BulletListHandler : NodeHandler<BulletList> {
    override suspend fun onNode(
        node: BulletList,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val container = ImdexNode(
            ImdexNodeType.BULLET_CONTAINER,
            node.startOffset,
            node.endOffset
        )
        state.withHolder(container, handleChildren)
        state.handler.children.add(container)
    }
}

object BulletListItemHandler : NodeHandler<BulletListItem> {
    override suspend fun onNode(
        node: BulletListItem,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val bullet = ImdexNode(
            BULLET,
            node.startOffset,
            node.endOffset
        )
        state.withHolder(bullet, handleChildren)
        state.handler.children.add(bullet)
    }
}

object OrderedListHandler :
    NodeHandler<OrderedList> {
    override suspend fun onNode(
        node: OrderedList,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val container = ImdexNode(
            ImdexNodeType.NUMBERED_CONTAINER,
            node.startOffset,
            node.endOffset
        )
        state.withHolder(container, handleChildren)
        state.handler.children.add(container)
    }
}

object OrderedListItemHandler : NodeHandler<OrderedListItem> {
    override suspend fun onNode(
        node: OrderedListItem,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val bullet = ImdexNode(
            NUMBERED,
            node.startOffset,
            node.endOffset
        )
        state.withHolder(bullet, handleChildren)
        state.handler.children.add(bullet)
    }
}
