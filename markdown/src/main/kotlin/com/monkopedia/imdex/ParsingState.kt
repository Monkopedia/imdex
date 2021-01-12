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

data class ParsingState(
    val handlers: MutableList<ImdexNode> = mutableListOf(),
    val spans: MutableList<SpanBuilder> = mutableListOf(),
    val customStates: MutableList<Any> = mutableListOf()
) {
    suspend inline fun withHolder(holder: ImdexNode, handleChildren: suspend () -> Unit) {
        handlers.add(holder)
        handleChildren()
        require(handlers.removeLast() == holder) {
            "Invalid handler stack, not $holder"
        }
    }

    suspend inline fun withSpan(span: SpanBuilder, handleChildren: suspend () -> Unit) {
        spans.add(span)
        handleChildren()
        require(spans.removeLast() == span) {
            "Invalid span stack, not $span"
        }
    }

    suspend inline fun withState(customState: Any, handleChildren: suspend () -> Unit) {
        customStates.add(customState)
        handleChildren()
        require(customStates.removeLast() == customState) {
            "Invalid span stack, not $customState"
        }
    }

    suspend inline fun invalidateChildren(handleChildren: suspend () -> Unit) {
        isInvalid = true
        handleChildren()
        isInvalid = false
    }

    inline fun <reified T> state(): T? {
        return customStates.last() as? T
    }

    var isInvalid: Boolean = false
    val handler get() =
        if (isInvalid) throw IllegalStateException("Children not allowed here") else handlers.last()
    val span get() = spans.last()
}
