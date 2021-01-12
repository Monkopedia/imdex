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

import com.monkopedia.kpages.LifecycleComponent
import com.monkopedia.kpages.Mutable
import kotlinx.css.LinearDimension
import kotlinx.css.height
import kotlinx.css.padding
import kotlinx.css.px
import react.RBuilder
import react.RProps
import react.RState
import react.dom.h1
import react.setState
import styled.css
import styled.styledDiv


external interface MarkdownProps : RProps {
    var title: Mutable<CharSequence>?
    var content: Korpus.Document?
    var document: Korpus.DocumentContent?
    var nodes: ImdexNode?
}

private val MarkdownProps.id: String
    get() = (document?.document?.path ?: content?.path)!!

external interface MarkdownState : RState {
    var id: String?
    var content: String?
    var nodes: ImdexNode?
    var document: Korpus.Document?
}

class MarkdownScreen(props: MarkdownProps) :
    LifecycleComponent<MarkdownProps, MarkdownState>(props) {

    val queue = CoroutineQueue()

    override fun componentWillUnmount() {
        queue.parent.cancel()
    }

    private fun launchLoad(props: MarkdownProps) {
        queue.run {
            if (state.id == props.id) {
                return@run
            }
            props.document?.let { doc ->
                val nodes = props.nodes!!
                setState {
                    id = doc.document.path
                    content = doc.content
                    this.document = doc.document
                    this.nodes = nodes
                }
                return@run
            }
            val document = props.content!!
            val loadedContent = ImdexApp.INSTANCE.imdex.fetch(document)
            val parsedContent = ImdexApp.INSTANCE.imdex.parse(document)
            console.log("Fetch $document")
            console.log(parsedContent.toLongString())
            props.title?.value = loadedContent.metadata.label
            setState {
                try {
                    id = loadedContent.document.path
                    this.document = loadedContent.document
                    content = loadedContent.content
                    nodes = parsedContent
                } catch (e: Throwable) {
                    throw RuntimeException("Problem setting state $loadedContent", e)
                }
            }
        }
    }

    override fun RBuilder.render() {
        if (state.id != props.id) {
            launchLoad(props)
        }
        val content = state.content
        if (content != null) {
            val renderer = ReactImdexRenderer()
            val rstate = RenderingState(invertedTheme, content, MdContext(state.document!!))
            // styledDiv {
            //     css {
            //         overflowX = Overflow.hidden
            //         overflowY = Overflow.scroll
            //     }
            styledDiv {
                css {
                    padding = 16.px.toString()
                    height = LinearDimension.auto
                }
                renderer.bindNode(this@styledDiv, state.nodes!!, rstate)
            }

            // }
        } else {
            styledDiv {
                css {
                    padding = 16.px.toString()
                    height = LinearDimension.auto
                }
                h1 {
                    +"Loading ${props.document}"
                }
            }
        }
    }
}
