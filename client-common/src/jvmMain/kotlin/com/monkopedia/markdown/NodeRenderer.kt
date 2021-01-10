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
package com.monkopedia.mdview

import com.monkopedia.lanterna.WeightedPanelHolder
import com.monkopedia.markdown.ImdexNode
import com.monkopedia.markdown.ImdexRenderer
import com.monkopedia.markdown.LanternaImdexRenderer
import com.monkopedia.markdown.LinkContext
import com.monkopedia.markdown.RenderingState

suspend fun WeightedPanelHolder.markdown(node: ImdexNode, str: String, file: LinkContext) {
    val renderer = LanternaImdexRenderer()
    renderer.markdown(this, str, node, file)
}

suspend fun <T> ImdexRenderer<T>.markdown(
    parent: T,
    content: String,
    node: ImdexNode,
    file: LinkContext
) {
    val state = RenderingState(content, file)
    bindNode(parent, node, state)
}
