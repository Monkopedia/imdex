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
package com.monkopedia.imdex.browser

import com.monkopedia.imdex.Korpus
import com.monkopedia.markdown.ImdexNode
import com.monkopedia.markdown.ReactImdexRenderer
import com.monkopedia.markdown.RenderingState
import kotlinx.browser.window
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
    var id: String
    var label: String
}

external interface MarkdownState : RState {
    var id: String?
    var content: String?
    var nodes: ImdexNode?
}

class MarkdownScreen(props: MarkdownProps) :
    LifecycleComponent<MarkdownProps, MarkdownState>(props) {

    val queue = CoroutineQueue()

    init {
        globalKey("e") {
            window.location.search =
                if (window.location.search.isNotEmpty()) "${window.location.search}&edit=true"
                else "edit=true"
        }
    }

    override fun componentWillUnmount() {
        queue.parent.cancel()
    }

    private fun launchLoad() {
        queue.run {
            if (state.id == props.id) {
                return@run
            }
            val document =
                if (props.id.isEmpty()) Korpus.Document.Companion.ROOT else Korpus.Document(
                    Korpus.Document.ROOT,
                    props.id
                )
            val loadedContent = iMDexService.get().fetch(document)
            val parsedContent = iMDexService.get().parse(document)
            console.log("Fetch $document")
            console.log(parsedContent.toLongString())
            setState {
                try {
                    id = loadedContent.document.path.trimStart('/')
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
            launchLoad()
        }
        val content = state.content
        if (content != null) {
            val renderer = ReactImdexRenderer()
            val rstate = RenderingState(invertedTheme, content) { n, path, scrollPosition ->
                queue.run {
                    val document = Korpus.Document(props.id)
                    val request = Korpus.ResolveLinkRequest(document, path, scrollPosition)
                    val resolved = iMDexService.get().resolveLink(request)
                    if (resolved != null) {
                        Navigation.open(resolved.document)
                    } else {
                        console.error("Can't resolve $path $scrollPosition")
                    }
                }
            }
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
                    +"Loading ${props.id}"
                }
            }
        }
    }
}
