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
import com.monkopedia.markdown.CoroutineQueue
import com.monkopedia.markdown.ImdexNode
import kotlinext.js.js
import kotlinx.css.Position
import kotlinx.css.position
import kotlinx.html.TEXTAREA
import react.RBuilder
import react.RProps
import react.RState
import react.createRef
import react.dom.defaultValue
import react.dom.h1
import react.setState
import styled.css
import styled.styledDiv
import styled.styledForm
import styled.styledTextArea

external interface EditProps : RProps {
    var id: String
    var label: String
}

external interface EditState : RState {
    var id: String?
    var content: String?
    var nodes: ImdexNode?
}

class EditScreen(props: EditProps) : LifecycleComponent<EditProps, EditState>(props) {

    val queue = CoroutineQueue()

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
            styledDiv {
                css {
                    // overflow = Overflow.scroll
                    // height = LinearDimension("100vh")
                }
                styledForm {
                    css {
                        position = Position.relative
                    }
                    child(CodeMirrorScreen::class) {
                        attrs {
                            this.content = content
                        }
                    }
                }
            }
        } else {
            h1 {
                +"Loading ${props.id}"
            }
        }
    }
}

external interface CodeMirrorProps : RProps {
    var content: String?
}

class CodeMirrorScreen(props: CodeMirrorProps) :
    LifecycleComponent<CodeMirrorProps, RState>(props) {
    val textAreaRef = createRef<TEXTAREA>()
    override fun RBuilder.render() {
        styledTextArea {
            attrs {
                this.defaultValue = props.content.toString()
            }
            css {
            }
            this.ref = textAreaRef
        }
    }

    override fun componentDidMount() {
        val onSave: () -> Unit = this::onSave
        commands.save = onSave
        fromTextArea(
            textAreaRef.current!!,
            js {
                mode = js {
                    name = "gfm"
                }
                keyMap = "vim"
                lineNumbers = true
                this.theme = "default"
                this.showCorsorWhenSelecting = true
            }
        )
    }

    private fun onSave() {
        Events.Save.dispatch(Unit)
    }
}
// @JsName("CodeMirror")
// external object CodeMirror {
//     @JsName("fromTextArea")
//     fun fromTextArea(element: dynamic, config: dynamic)
// }
