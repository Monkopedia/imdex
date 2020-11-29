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

import com.monkopedia.markdown.SpanType.USE_DATA
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ast.TextBase
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.util.sequence.builder.tree.SegmentTree

class SpanBuilder(val start: Int? = null, val end: Int? = null) {
    val segments = mutableListOf<ImdexSpan>()

    fun append(base: SpanBuilder, vararg spans: SpanType) {
        segments.addAll(
            base.segments.map {
                ImdexSpan(
                    it.type + spans,
                    it.s,
                    it.e,
                    it.data
                )
            }
        )
    }

    fun append(base: SpanBuilder, data: String, vararg spans: SpanType) {
        segments.addAll(
            base.segments.map {
                if (it.data != null) {
                    throw IllegalArgumentException("Conflicting data $data ${it.data}")
                }
                ImdexSpan(
                    it.type + spans,
                    it.s,
                    it.e,
                    data
                )
            }
        )
    }

    fun append(node: Text) {
        val tree = node.chars.segmentTree
        var base = node.chars
        while (base.baseSequence != null && base.baseSequence != base) {
            base = base.baseSequence
        }
        appendSegments(tree, base)
    }

    private fun appendSegments(tree: SegmentTree, base: BasedSequence, vararg spans: SpanType) {
        for (i in 0 until tree.size()) {
            val segment = tree.getSegment(i, base)
//            if (segment.isText) {
            val start = segment.startOffset
            val end = segment.endOffset
            segments.add(ImdexSpan(spans.toList(), start, end))
//            }
        }
    }

    fun append(node: TextBase) {
        val tree = node.chars.segmentTree
        var base = node.chars
        while (base.baseSequence != null && base.baseSequence != base) {
            base = base.baseSequence
        }
        appendSegments(tree, base)
    }

    fun append(base: BasedSequence, vararg spans: SpanType) {
        var base = base
        val tree = base.segmentTree
        while (base.baseSequence != null && base.baseSequence != base) {
            base = base.baseSequence
        }
        appendSegments(tree, base, *spans)
    }

    fun append(base: String, vararg spans: SpanType) {
        segments.add(ImdexSpan((spans.toList() + USE_DATA), data = base))
    }

    fun append(vararg spans: SpanType) {
        segments.add(ImdexSpan(spans.toList(), 0, 0))
    }

    fun build(type: ImdexNodeType): ImdexNode {
        val start = start ?: segments.minOfOrNull { it.s ?: Int.MAX_VALUE } ?: -1
        val end = end ?: segments.minOfOrNull { it.e ?: Int.MAX_VALUE } ?: -1

        return ImdexNode(
            type,
            start,
            end,
            text = segments
        )
    }

    fun isEmpty(): Boolean {
        return segments.any { !it.isEmpty() }
    }
}
