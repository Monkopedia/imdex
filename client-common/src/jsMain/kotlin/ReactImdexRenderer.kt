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

import com.ccfraser.muirwik.components.card.mCard
import com.ccfraser.muirwik.components.mDivider
import com.ccfraser.muirwik.components.mThemeProvider
import com.ccfraser.muirwik.components.styles.Theme
import com.ccfraser.muirwik.components.table.mTable
import com.ccfraser.muirwik.components.table.mTableBody
import com.ccfraser.muirwik.components.table.mTableCell
import com.ccfraser.muirwik.components.table.mTableRow
import kotlinx.css.Align
import kotlinx.css.LinearDimension
import kotlinx.css.alignContent
import kotlinx.css.background
import kotlinx.css.fontFamily
import kotlinx.css.paddingLeft
import kotlinx.css.paddingRight
import react.RBuilder
import react.dom.blockQuote
import react.dom.code
import react.dom.li
import react.dom.ol
import react.dom.p
import react.dom.ul
import styled.StyledDOMBuilder
import styled.css
import styled.styledPre

class ReactImdexRenderer : ImdexRenderer<RBuilder> {
    private var inBullet: Boolean = false

    override fun Collection<CharSequence>.joinToSpanned(separator: CharSequence): CharSequence {
        return joinToString(separator)
    }

    override fun onBlockNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        p.mThemeProvider(state.codeTheme) {
            blockQuote {
                mCard(raised = false) {
                    css {
                        paddingLeft = LinearDimension("8px")
                        paddingRight = LinearDimension("8px")
                        specific {
                            background = state.codeTheme.palette.background.default
                        }
                    }
                    val span = state.buildSpan(node).element
                    span()
                    handleChildren()
                }
            }
        }
    }

    override fun onFencedCodeNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        p.mThemeProvider(state.codeTheme) {
            mCard(raised = false) {
                styledPre {
                    codeStyle(state.codeTheme)
                    css {
                        paddingLeft = LinearDimension("16px")
                        paddingRight = LinearDimension("16px")
                    }
                    code {
                        val span = state.buildSpan(node).element
                        span()
                        handleChildren()
                    }
                }
            }
        }
    }

    override fun onHeadingNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        val span = state.buildSpan(node).element
        p.span()
        p.handleChildren()
    }

    override fun onImageNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        val span = state.buildSpan(node).element
        p.span()
        p.handleChildren()
    }

    override fun onBulletNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        p.li {
            val span = state.buildSpan(node).element
            span()
            inBullet = true
            handleChildren()
            inBullet = false
        }
    }

    override fun onBulletContainerNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        p.ul {
            handleChildren()
        }
    }

    override fun onNumberedNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        p.li {
            val span = state.buildSpan(node).element
            span()
            inBullet = true
            handleChildren()
            inBullet = false
        }
    }

    override fun onNumberedContainerNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        p.ol {
            handleChildren()
        }
    }

    override fun onParagraphNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        if (inBullet) {
            val span = state.buildSpan(node).element
            p.span()
        } else {
            p.p {
                val span = state.buildSpan(node).element
                span()
            }
        }
    }

    override fun onTableContainerNode(
        p: RBuilder,
        rows: Int,
        cols: Int,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        p.mTable {
            p.mTableBody {
                handleChildren()
            }
        }
    }

    override fun onTableRowNode(
        p: RBuilder,
        row: Int,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        p.mTableRow {
            handleChildren()
        }
    }

    override fun onTableCellNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        inHeader: Boolean,
        gravity: Gravity
    ) {
        p.mTableCell {
            css {
                alignContent = when (gravity) {
                    Gravity.TOP_LEFT -> Align.start
                    Gravity.TOP_CENTER -> Align.center
                    Gravity.TOP_RIGHT -> Align.end
                }
            }
            val span = state.buildSpan(node).element
            span()
        }
    }

    override fun onThemeBreakNode(
        p: RBuilder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: RBuilder.() -> Unit
    ) {
        p.mDivider {
        }
        p.handleChildren()
    }
}

fun StyledDOMBuilder<*>.codeStyle(codeTheme: Theme) {
    css {
        fontFamily = "\"Courier New\", Courier, monospace"
        // backgroundColor = Color(codeTheme.palette.background.default)
        // backgroundColor = Color("#0000007D")
    }
}
