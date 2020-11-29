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

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@NodeDsl
class ImdexNodeBuilder(val base: String, val node: ImdexNode) {

    fun indexed(str: String): IndexedText {
        require(base.contains(str)) {
            "Can't index string if it doesn't exist"
        }
        val start = base.indexOf(str)
        require(start >= -1)
        val end = start + str.length
        require(end >= -1)
        return IndexedText(start, end)
    }

    fun splitText(separator: String) = base.split(separator).map { indexed(it) }

    fun IndexedText.get(): String {
        return this@ImdexNodeBuilder.base.substring(start, end)
    }
}

@DslMarker
annotation class NodeDsl

@NodeDsl
@OptIn(ExperimentalContracts::class)
inline fun root(base: String, action: ImdexNodeBuilder.() -> Unit): ImdexNode {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return ImdexNodeBuilder(base, ImdexNode(ImdexNodeType.ROOT, 0, 0)).apply(action).node
}

@NodeDsl
@OptIn(ExperimentalContracts::class)
inline fun ImdexNodeBuilder.header(level: Int = 1, action: SpanNodeBuilder.() -> Unit): ImdexNode {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    val text = SpanNodeBuilder()
    text.append(SpanNodeBuilder().apply(action), level.toHeader())
    val node = text.build(ImdexNodeType.HEADING)
    this.node.children.add(node)
    return node
}

@NodeDsl
@OptIn(ExperimentalContracts::class)
inline fun ImdexNodeBuilder.paragraph(action: SpanNodeBuilder.() -> Unit): ImdexNode {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    val text = SpanNodeBuilder().apply(action)
    val node = text.build(ImdexNodeType.PARAGRAPH)
    this.node.children.add(node)
    return node
}

@NodeDsl
@OptIn(ExperimentalContracts::class)
inline fun ImdexNodeBuilder.table(action: ImdexNodeBuilder.() -> Unit): ImdexNode {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    val node =
        ImdexNodeBuilder(base, ImdexNode(ImdexNodeType.TABLE_CONTAINER, 0, 0)).apply(action).node
    this.node.children.add(node)
    return node
}

@NodeDsl
@OptIn(ExperimentalContracts::class)
inline fun ImdexNodeBuilder.row(action: ImdexNodeBuilder.() -> Unit): ImdexNode {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    val node =
        ImdexNodeBuilder(base, ImdexNode(ImdexNodeType.TABLE_ROW, 0, 0)).apply(action).node
    this.node.children.add(node)
    return node
}

@NodeDsl
@OptIn(ExperimentalContracts::class)
inline fun ImdexNodeBuilder.cell(action: SpanNodeBuilder.() -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    val text = SpanNodeBuilder().apply(action)
    node.children.add(text.build(ImdexNodeType.TABLE_CELL))
}

@NodeDsl
@OptIn(ExperimentalContracts::class)
inline fun SpanNodeBuilder.bold(action: SpanNodeBuilder.() -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    val subText = SpanNodeBuilder().apply(action)
    append(subText, SpanType.BOLD)
}

@NodeDsl
@OptIn(ExperimentalContracts::class)
inline fun SpanNodeBuilder.link(url: String, action: SpanNodeBuilder.() -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    val subText = SpanNodeBuilder().apply(action)
    append(subText, url, SpanType.PATH)
}

fun Int.toHeader(): SpanType = when (this) {
    1 -> SpanType.HEADER1
    2 -> SpanType.HEADER2
    3 -> SpanType.HEADER3
    4 -> SpanType.HEADER4
    5 -> SpanType.HEADER5
    6 -> SpanType.HEADER6
    else -> SpanType.HEADER7
}

@NodeDsl
inline fun ImdexNodeBuilder.h1(action: SpanNodeBuilder.() -> Unit): ImdexNode = header(1, action)

@NodeDsl
inline fun ImdexNodeBuilder.h2(action: SpanNodeBuilder.() -> Unit): ImdexNode = header(2, action)

@NodeDsl
inline fun ImdexNodeBuilder.h3(action: SpanNodeBuilder.() -> Unit): ImdexNode = header(3, action)

@NodeDsl
inline fun ImdexNodeBuilder.h4(action: SpanNodeBuilder.() -> Unit): ImdexNode = header(4, action)

@NodeDsl
inline fun ImdexNodeBuilder.h5(action: SpanNodeBuilder.() -> Unit): ImdexNode = header(5, action)

@NodeDsl
inline fun ImdexNodeBuilder.h6(action: SpanNodeBuilder.() -> Unit): ImdexNode = header(6, action)

@NodeDsl
inline fun ImdexNodeBuilder.h7(action: SpanNodeBuilder.() -> Unit): ImdexNode = header(7, action)

@NodeDsl
data class IndexedText(val start: Int, val end: Int)

@NodeDsl
class SpanNodeBuilder(val start: Int? = null, val end: Int? = null) {
    val segments = mutableListOf<ImdexSpan>()

    fun append(base: SpanNodeBuilder, vararg spans: SpanType) {
        segments.addAll(
            base.segments.map {
                require(it.type.contains(SpanType.USE_DATA) || (it.s!! >= 0 && it.e!! >= 0))
                ImdexSpan(
                    it.type + spans,
                    it.s,
                    it.e,
                    it.data
                )
            }
        )
    }

    fun append(base: SpanNodeBuilder, data: String, vararg spans: SpanType) {
        segments.addAll(
            base.segments.map {
                if (it.data != null) {
                    throw IllegalArgumentException("Conflicting data $data ${it.data}")
                }
                require(it.type.contains(SpanType.USE_DATA) || (it.s!! >= 0 && it.e!! >= 0))
                ImdexSpan(
                    it.type + spans,
                    it.s,
                    it.e,
                    data
                )
            }
        )
    }

    fun append(node: IndexedText, vararg spans: SpanType) {
        segments.add(ImdexSpan(spans.toList(), node.start, node.end))
    }

    fun append(base: String, vararg spans: SpanType) {
        segments.add(ImdexSpan((spans.toList() + SpanType.USE_DATA), data = base))
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

    @NodeDsl
    inline operator fun String.unaryPlus() {
        append(this)
    }

    @NodeDsl
    inline operator fun IndexedText.unaryPlus() {
        this@SpanNodeBuilder.append(this)
    }
}
