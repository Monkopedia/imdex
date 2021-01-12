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
package com.monkopedia.imdex

import com.googlecode.lanterna.SGR.BOLD
import com.monkopedia.dynamiclayout.DynamicVerticalLinearLayout
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Padding
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.dynamiclayout.asDynamicLayout
import com.monkopedia.lanterna.Lanterna
import com.monkopedia.lanterna.LinearPanelHolder
import com.monkopedia.lanterna.WeightedPanelHolder
import com.monkopedia.lanterna.asciiLabel
import com.monkopedia.lanterna.frame
import com.monkopedia.lanterna.grid
import com.monkopedia.lanterna.hdiv
import com.monkopedia.lanterna.horizontal
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.space
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.lanterna.spannable.ThemeSpan
import com.monkopedia.lanterna.vertical
import com.monkopedia.imdex.SpanType.HEADER1
import com.monkopedia.imdex.SpanType.HEADER2
import com.monkopedia.imdex.SpanType.HEADER3
import com.monkopedia.imdex.SpanType.HEADER4
import com.monkopedia.imdex.SpanType.HEADER5
import com.monkopedia.imdex.SpanType.HEADER6
import com.monkopedia.imdex.SpanType.HEADER7
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LanternaImdexRenderer : ImdexRenderer<WeightedPanelHolder> {

    override fun onBlockNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        vertical {
            (panel.asDynamicLayout as DynamicVerticalLinearLayout).padding = Padding(8, 0, 8, 0)
            handleChildren()
        }.layoutParams(Fill, Wrap)
    }

    override fun onFencedCodeNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        frame {
            layout.padding = Padding(2, 0, 2, 0)
            vertical {
                layout.renderer.setFillAreaBeforeDrawingComponents(true)
                panel.fillColorOverride = Style.codeStyle.background
                val label = label("")
                var base = Spanned()
                val spanned = state.buildSpan(node)
                base.append(spanned, ThemeSpan(Style.codeStyle))
                if (base.endsWith('\n')) {
                    base = base.subSequence(0, base.length - 1)
                }
                label.setText(base)
            }.layoutParams(Fill, Wrap)
        }.layoutParams(Fill, Wrap)
        space(1)
    }

    override fun Collection<CharSequence>.joinToSpanned(separator: CharSequence): CharSequence {
        return Spanned().also {
            for ((i, item) in withIndex()) {
                if (item.isEmpty()) continue
                if (i != 0) {
                    it.append(separator)
                }
                it.append(item)
            }
        }
    }

    override fun onHeadingNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        val level = node.spans.flatMap { it.type }.filter {
            it in listOf(
                HEADER1,
                HEADER2,
                HEADER3,
                HEADER4,
                HEADER5,
                HEADER6,
                HEADER7,
            )
        }.minByOrNull { it.ordinal }
        val spanned = state.buildSpan(node)

        if (Style.asciiFontsEnabled && level in listOf(HEADER1, HEADER2)) {
            frame {
                GlobalScope.launch {
                    val font = Lanterna.fontLoader.get(if (level == HEADER1) "h1" else "h2")
                    val label = this@frame.asciiLabel(font, "")
                    label.text = if (level == HEADER2) spanned.toUpperCase() else spanned
                }
            }
        } else {
            val label = label("")
            space(1)
            label.setText(spanned)
        }
    }

    override fun onImageNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        val l = label("")
        l.setText(state.buildSpan(node))
    }

    override fun onBulletNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        horizontal {
            label(" - ")
            vertical {
                handleChildren()
            }.layoutParams(Fill, Wrap)
        }.layoutParams(Fill, Wrap)
    }

    override fun onBulletContainerNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ) {
        p.handleChildren()
    }

    override fun onNumberedNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        horizontal {
            label(" - ")
            vertical {
                handleChildren()
            }.layoutParams(Fill, Wrap)
        }.layoutParams(Fill, Wrap)
    }

    override fun onNumberedContainerNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ) {
        p.handleChildren()
    }

// object OrderedListItemHandler : NodeHandler<OrderedListItem> {
//    override override fun onNode(node: OrderedListItem, state: ParsingState, handleChildren: () -> Unit) {
//        state.handler.apply {
//            horizontal {
//                label(" - ")
//                vertical {
//                    state.withHolder(this, handleChildren)
//                }.layoutParams(Fill, Wrap)
//            }.layoutParams(Fill, Wrap)
//        }
//    }
// }

    override fun onParagraphNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        val label = label("").layoutParams(Fill, Wrap)
        label.setText(state.buildSpan(node))
        space(1)
    }

    override fun onTableContainerNode(
        p: WeightedPanelHolder,
        rows: Int,
        cols: Int,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        grid(cols) {
            handleChildren()
        }
    }

    override fun onTableRowNode(
        p: WeightedPanelHolder,
        row: Int,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        handleChildren()
    }

    override fun onTableCellNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        inHeader: Boolean,
        gravity: Gravity
    ): Unit = with(p as LinearPanelHolder<*>) {
        val base = state.buildSpan(node)
        val label = label("")
        if (inHeader) {
            base.applySpans(0, base.length, EnableSGRSpan(BOLD))
        }
        label.setText(base)
        label.layoutParams(Wrap, Wrap, gravity = gravity)
    }

    override fun onThemeBreakNode(
        p: WeightedPanelHolder,
        node: ImdexNode,
        state: RenderingState,
        handleChildren: WeightedPanelHolder.() -> Unit
    ): Unit = with(p) {
        space(3)
        hdiv()
    }
}
