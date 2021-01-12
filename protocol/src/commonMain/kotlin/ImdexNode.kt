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

import com.monkopedia.imdex.SpanType.USE_DATA
import kotlinx.serialization.Serializable

enum class ImdexNodeType {
    ROOT,
    HEADING,
    BLOCK_QUOTE,
    FENCED_CODE,
    IMAGE,
    BULLET,
    PARAGRAPH,
    TABLE_CONTAINER,
    TABLE_DIVIDER_ROW,
    TABLE_ROW,
    TABLE_CELL,
    THEMATIC_BREAK,
    BULLET_CONTAINER,
    NUMBERED,
    NUMBERED_CONTAINER
}

@Serializable
data class ImdexNode(
    var t: Int,
    var s: Int,
    var e: Int,
    var children: MutableList<ImdexNode> = mutableListOf(),
    var spans: MutableList<ImdexSpan> = mutableListOf(),
) {
    constructor(
        type: ImdexNodeType,
        start: Int,
        end: Int,
        childNodes: MutableList<ImdexNode> = mutableListOf(),
        text: MutableList<ImdexSpan> = mutableListOf(),
    ) : this(t = type.ordinal, start, end, childNodes, text)

    var type: ImdexNodeType
        get() = ImdexNodeType.values()[t]
        set(value) {
            t = value.ordinal
        }

    fun toLongString(): String {
        return toString() + "\n    " + children.joinToString("\n    ") {
            it.toLongString().replace("\n", "\n    ")
        }
    }

    override fun toString(): String {
        return "$type [$s-$e] (${children.size} children) ${spans.joinToString(", ")}"
    }
}

inline fun <T : Enum<T>> List<T>.asBitfield(): Long {
    if (isEmpty()) return 0L
    return map { 1L shl it.ordinal }.reduce { a, b ->
        a or b
    }
}

inline fun <reified T : Enum<T>> Long.fromBitfield(): List<T> {
    val field = this
    return enumValues<T>().filter {
        (field and (1L shl it.ordinal)) != 0L
    }
}

enum class SpanType {
    HEADER1,
    HEADER2,
    HEADER3,
    HEADER4,
    HEADER5,
    HEADER6,
    HEADER7,
    CODE_STYLE,
    HARD_BREAK,
    SOFT_BREAK,
    CROSSED_OUT,
    BOLD,
    UNDERLINE,
    URL,
    PATH,
    USE_DATA,
    BR,
    SUPERSCRIPT,
}

@Serializable
data class ImdexSpan(
    var t: Long,
    var s: Int? = null,
    var e: Int? = null,
    var data: String? = null
) {
    fun isEmpty(): Boolean {
        if (type.contains(USE_DATA) && !data.isNullOrEmpty()) {
            return true
        }
        s?.let { s ->
            e?.let { e ->
                if (e > s) {
                    return true
                }
            }
        }
        return false
    }

    constructor(type: List<SpanType>, start: Int? = null, end: Int? = null, data: String? = null) :
        this(type.asBitfield(), start, end, data)

    var type: List<SpanType>
        get() = t.fromBitfield()
        set(value) {
            t = value.asBitfield()
        }

    override fun toString(): String {
        return "Span(${type.joinToString(",")},$s,$e,$data)"
    }
}
