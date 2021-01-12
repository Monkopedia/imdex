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

import com.ccfraser.muirwik.components.MCircularProgressColor
import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.MIconEdge
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.form.MFormControlMargin
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.mAppBar
import com.ccfraser.muirwik.components.mCircularProgress
import com.ccfraser.muirwik.components.mMuiThemeProvider
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.mToolbar
import com.ccfraser.muirwik.components.mToolbarTitle
import com.ccfraser.muirwik.components.targetInputValue
import com.monkopedia.kpages.LifecycleComponent
import com.monkopedia.kpages.Mutable
import com.monkopedia.kpages.onValue
import com.monkopedia.imdex.invertedTheme
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.LinearDimension
import kotlinx.css.flex
import kotlinx.css.height
import kotlinx.css.marginRight
import kotlinx.css.px
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.StyleSheet
import styled.createGlobalStyle
import styled.css

external interface AppBarState : RState {
    var title: String?
    var mutableTitle: Mutable<CharSequence>?
    var search: String?
    var showingSearch: Boolean?
    var saving: Boolean?
}

external interface AppBarProps : RProps {
    var title: Mutable<CharSequence>?
    var onSearchChanged: ((String?) -> Unit)?
}

class AppBarComponent : LifecycleComponent<AppBarProps, AppBarState>() {
    private object ComponentStyles : StyleSheet("AppBar", isStatic = true) {
        val textField by css {
            height = LinearDimension("48px")
            marginRight = LinearDimension("32px")
            flex(1.0)
        }

        val appBar by css {
            height = LinearDimension("64px")
        }
    }
    private val holder = lifecycleHolder()

    init {
        listen(Events.Save) {
            startSave()
        }
        globalKey("s") {
            setState {
                showingSearch = true
            }
        }
    }

    private fun startSave() {
        setState {
            saving = true
        }
        GlobalScope.launch {
            setState {
                saving = false
            }
            val searchParams = URLSearchParams(window.location.search)
            searchParams.set("edit", "false")
            window.location.search = searchParams.toString()
        }
    }

    override fun RBuilder.render() {
        holder.lifecycle = props.title?.onValue {
            if (state.title != it.toString()) {
                setState {
                    println("App bar title: $it")
                    this.title = it.toString()
                }
            }
        }
        mAppBar {
            css(ComponentStyles.appBar)
            createGlobalStyle
            mToolbar {
                if (state.showingSearch == true) {
                    mMuiThemeProvider(invertedTheme) {
                        mTextField(
                            "Search",
                            variant = MFormControlVariant.outlined,
                            margin = MFormControlMargin.dense,
                            onChange = { e ->
                                props.onSearchChanged?.invoke(e.targetInputValue)
                            }
                        ) {
                            css(ComponentStyles.textField)
                            attrs {
                                this.autoFocus = true
                                this.onKeyUp = {
                                    if (it.asDynamic().key == "Escape") {
                                        setState {
                                            showingSearch = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                    mIconButton(
                        "close",
                        edge = MIconEdge.end,
                        color = MColor.inherit,
                        onClick = {
                            setState {
                                showingSearch = false
                            }
                        }
                    )
                } else {
                    props.onSearchChanged?.invoke(null)
                    mToolbarTitle(state.title ?: "iMDex")
                    val searchParams = URLSearchParams(window.location.search)
                    val isEditing = searchParams.get("edit")?.toBoolean() ?: false
                    if (state.saving == true) {
                        mCircularProgress(color = MCircularProgressColor.inherit, size = 24.px) {
                        }
                    } else {
                        mIconButton(
                            if (isEditing) "save" else "edit",
                            edge = MIconEdge.end,
                            color = MColor.inherit,
                            onClick = {
                                if (isEditing) {
                                    startSave()
                                } else {
                                    searchParams.set("edit", "${!isEditing}")
                                    window.location.search = searchParams.toString()
                                }
                            }
                        )
                    }
                    mIconButton(
                        "search",
                        edge = MIconEdge.end,
                        color = MColor.inherit,
                        onClick = {
                            setState {
                                showingSearch = true
                            }
                        }
                    )
                }
            }
        }
    }
}
