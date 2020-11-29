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

import com.ccfraser.muirwik.components.mThemeProvider
import com.monkopedia.kpages.preferences.PreferenceDemoScreen
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.height
import kotlinx.css.marginTop
import kotlinx.css.overflowY
import kotlinx.css.px
import kotlinx.css.vh
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.router.dom.browserRouter
import react.router.dom.route
import react.router.dom.switch
import styled.css
import styled.styledDiv

external interface RootState : RState {
    var search: String?
}

class ImdexRoot : RComponent<RProps, RootState>() {
    override fun RBuilder.render() {
        kotlinext.js.require("codemirror/lib/codemirror.css")
        kotlinext.js.require("codemirror/mode/gfm/gfm.js")
        kotlinext.js.require("codemirror/keymap/vim.js")
        kotlinext.js.require("codemirror/addon/dialog/dialog.js")
        kotlinext.js.require("codemirror/addon/dialog/dialog.css")
        mThemeProvider(theme = theme) {
            styledDiv {
                css {
                    marginTop = LinearDimension("70px")
                    height = 100.vh - 70.px
                    overflowY = Overflow.auto
                }

                browserRouter {
                    switch {
                        // route("/", SearchScreen::class, exact = true)
                        route<RProps>("/theme_demo", exact = false) { props ->
                            div {
                                child(ThemeDemo::class) {
                                }
                            }
                        }
                        route("/settings", SettingsScreen::class, exact = true)
                        route("/preference_demo", exact = true) {
                            PreferenceDemoScreen.componentFactory(this)
                        }
                        route<MarkdownProps>("/", exact = false) { props ->
                            div {
                                val edit = URLSearchParams(props.location.search).get("edit")
                                console.log("Get edit ${props.location.search} $edit")
                                if (edit?.toBoolean() == true) {
                                    child(EditScreen::class) {
                                        attrs.id = props.location.pathname.trimStart('/')
                                    }
                                } else {
                                    child(MarkdownScreen::class) {
                                        attrs.id = props.location.pathname.trimStart('/')
                                    }
                                }
                            }
                        }
                    }
                }
            }
            child(SearchOverlay::class) { }
        }
    }
}

object Keymap {
    private val listeners = mutableMapOf<Int, () -> Unit>()
    private val keyListeners = mutableMapOf<String, () -> Unit>()

    fun addGlobalKey(c: Char, listener: () -> Unit) {
        listeners[c.toInt()] = listener
    }

    fun addGlobalKey(key: String, listener: () -> Unit) {
        keyListeners[key] = listener
    }

    fun removeGlobalKey(c: Char) {
        listeners.remove(c.toInt())
    }

    fun removeGlobalKey(key: String) {
        keyListeners.remove(key)
    }

    fun handle(charCode: Int?, key: String?): Boolean {
        println("Handle $key")
        listeners[charCode ?: return false]?.invoke()?.also {
            return true
        }
        keyListeners[key ?: return false]?.invoke()?.also {
            return true
        }
        return false
    }
}
