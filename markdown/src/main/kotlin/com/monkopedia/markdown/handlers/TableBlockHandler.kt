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
import com.monkopedia.markdown.ImdexNodeType.TABLE_CELL
import com.monkopedia.markdown.ImdexNodeType.TABLE_CONTAINER
import com.monkopedia.markdown.ImdexNodeType.TABLE_DIVIDER_ROW
import com.monkopedia.markdown.ImdexNodeType.TABLE_ROW
import com.monkopedia.markdown.NodeHandler
import com.monkopedia.markdown.ParsingState
import com.monkopedia.markdown.SpanBuilder
import com.monkopedia.markdown.SpanType.BOLD
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.ext.tables.TableBody
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TableHead
import com.vladsch.flexmark.ext.tables.TableRow
import com.vladsch.flexmark.ext.tables.TableSeparator

object TableBlockHandler : NodeHandler<TableBlock> {
    override suspend fun onNode(
        node: TableBlock,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val table = ImdexNode(
            TABLE_CONTAINER,
            node.startOffset,
            node.endOffset
        )
        state.withHolder(table, handleChildren)
        state.handler.children.add(table)
    }
}

object TableBodyHandler : NodeHandler<TableBody> {
    override suspend fun onNode(
        node: TableBody,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        handleChildren()
    }
}

object TableCellHandler : NodeHandler<TableCell> {
    override suspend fun onNode(
        node: TableCell,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        var base = SpanBuilder()
        state.invalidateChildren {
            state.withSpan(base, handleChildren)
        }
        if (state.customStates.contains("Header")) {
            base = SpanBuilder().apply { append(base, BOLD) }
        }
        state.handler.children.add(base.build(TABLE_CELL))
    }
}

object TableHeadHandler : NodeHandler<TableHead> {
    override suspend fun onNode(
        node: TableHead,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        state.withState("Header") {
            handleChildren()
        }
    }
}

object TableRowHandler : NodeHandler<TableRow> {
    override suspend fun onNode(
        node: TableRow,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        if (state.handler.type == TABLE_DIVIDER_ROW) {
            // Don't need a row holder in divider state.
            handleChildren()
            return
        }
        val table = ImdexNode(
            TABLE_ROW,
            node.startOffset,
            node.endOffset
        )
        state.withHolder(table, handleChildren)
        state.handler.children.add(table)
    }
}

object TableSeparatorHandler : NodeHandler<TableSeparator> {
    override suspend fun onNode(
        node: TableSeparator,
        state: ParsingState,
        handleChildren: suspend () -> Unit
    ) {
        val table = ImdexNode(
            TABLE_DIVIDER_ROW,
            node.startOffset,
            node.endOffset
        )
        state.withHolder(table, handleChildren)
        state.handler.children.add(table)
    }
}
