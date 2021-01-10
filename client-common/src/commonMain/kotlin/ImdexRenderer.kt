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
package com.monkopedia.markdown

import com.monkopedia.kpages.Navigator
import com.monkopedia.markdown.ImdexNodeType.BLOCK_QUOTE
import com.monkopedia.markdown.ImdexNodeType.BULLET
import com.monkopedia.markdown.ImdexNodeType.FENCED_CODE
import com.monkopedia.markdown.ImdexNodeType.HEADING
import com.monkopedia.markdown.ImdexNodeType.IMAGE
import com.monkopedia.markdown.ImdexNodeType.PARAGRAPH
import com.monkopedia.markdown.ImdexNodeType.ROOT
import com.monkopedia.markdown.ImdexNodeType.TABLE_CELL
import com.monkopedia.markdown.ImdexNodeType.TABLE_CONTAINER
import com.monkopedia.markdown.ImdexNodeType.TABLE_DIVIDER_ROW
import com.monkopedia.markdown.ImdexNodeType.TABLE_ROW
import com.monkopedia.markdown.ImdexNodeType.THEMATIC_BREAK

expect class Spanned

expect class RenderingState {
    val content: String
    val linkContext: LinkContext
    fun buildSpan(node: ImdexNode): Spanned
}

fun interface LinkContext {
    fun launchLink(navigation: Navigator, path: String, position: String?)
}

expect enum class Gravity {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
}

fun ImdexNode?.alignmentFor(renderingState: RenderingState, i: Int): Gravity {
    if (this == null || i >= children.size) {
        return Gravity.TOP_LEFT
    }
    val colText = renderingState.buildSpan(children[i]).toString().trim()
    return when {
        colText.startsWith(":") && colText.endsWith(":") -> Gravity.TOP_CENTER
        colText.startsWith(":") -> Gravity.TOP_LEFT
        colText.endsWith(":") -> Gravity.TOP_RIGHT
        else -> Gravity.TOP_LEFT
    }
}

interface ImdexRenderer<ViewParent> {

    fun Collection<CharSequence>.joinToSpanned(separator: CharSequence): CharSequence

    fun onBlockNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onFencedCodeNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onHeadingNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onImageNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onBulletNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onBulletContainerNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onNumberedNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onNumberedContainerNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onParagraphNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onTableContainerNode(
        p: ViewParent,
        rows: Int,
        cols: Int,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun onTableRowNode(
        p: ViewParent,
        row: Int,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    ): Unit

    fun onTableCellNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        inHeader: Boolean,
        gravity: Gravity
    )

    fun onThemeBreakNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    )

    fun bindNode(p: ViewParent, node: ImdexNode, state: RenderingState) {
        return when (node.type) {
            ROOT -> handleChildren(p, node, state)
            BLOCK_QUOTE -> onBlockNode(p, node, state) { handleChildren(this, node, state) }
            FENCED_CODE -> onFencedCodeNode(p, node, state) { handleChildren(this, node, state) }
            IMAGE -> onImageNode(p, node, state) { handleChildren(this, node, state) }
            BULLET -> onBulletNode(p, node, state) { handleChildren(this, node, state) }
            PARAGRAPH -> onParagraphNode(p, node, state) { handleChildren(this, node, state) }
            TABLE_CONTAINER -> renderTableContainerNode(p, node, state) {
                handleChildren(this, node, state)
            }
            TABLE_DIVIDER_ROW -> {
                throw IllegalStateException("Trying to bind table content")
            }
            TABLE_ROW -> {
                throw IllegalStateException("Trying to bind table content")
            }
            TABLE_CELL -> {
                throw IllegalStateException("Trying to bind table content")
            }
            THEMATIC_BREAK -> onThemeBreakNode(p, node, state) { handleChildren(this, node, state) }
            HEADING -> onHeadingNode(p, node, state) { handleChildren(this, node, state) }
            ImdexNodeType.BULLET_CONTAINER -> onBulletContainerNode(p, node, state) {
                handleChildren(
                    this,
                    node,
                    state
                )
            }
            ImdexNodeType.NUMBERED -> onNumberedNode(p, node, state) {
                handleChildren(
                    this,
                    node,
                    state
                )
            }
            ImdexNodeType.NUMBERED_CONTAINER -> onNumberedContainerNode(p, node, state) {
                handleChildren(
                    this,
                    node,
                    state
                )
            }
        }
    }

    fun renderTableContainerNode(
        p: ViewParent,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: ViewParent.() -> Unit
    ) {
        val rows = node.children.filter { it.type === TABLE_ROW }
        val divider = node.children.find { it.type == TABLE_DIVIDER_ROW }
        val cols = rows.maxOfOrNull { it.children.filter { it.type == TABLE_CELL }.size } ?: return
        if (rows.isEmpty()) {
            throw RuntimeException("No rows")
        }
        if (cols == 0) return

        onTableContainerNode(p, rows.size, cols, node, state) {
            for ((rowIndex, row) in rows.withIndex()) {
                onTableRowNode(this, rowIndex, row, state) {
                    for (i in 0 until cols) {
                        val component = row.children.getOrNull(i) ?: continue
                        require(component.type == TABLE_CELL && component.children.isEmpty()) {
                            "Expected table cell instead of $component"
                        }

                        onTableCellNode(
                            this,
                            component,
                            state,
                            rowIndex == 0,
                            if (rowIndex == 0) Gravity.TOP_CENTER
                            else divider.alignmentFor(state, i)
                        )
                    }
                }
            }
        }
    }

    private fun handleChildren(p: ViewParent, node: ImdexNode, state: RenderingState) {
        node.children.forEach {
            bindNode(p, it, state)
        }
    }
}
