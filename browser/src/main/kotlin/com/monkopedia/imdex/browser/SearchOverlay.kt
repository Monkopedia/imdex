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

import com.ccfraser.muirwik.components.mBackdrop
import com.monkopedia.imdex.Imdex
import com.monkopedia.imdex.Korpus
import com.monkopedia.markdown.CoroutineQueue
import kotlinx.browser.window
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.css.left
import kotlinx.css.padding
import kotlinx.css.paddingTop
import kotlinx.css.position
import kotlinx.css.top
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

external interface SearchOverlayProps : RProps
external interface SearchOverlayState : RState {
    var query: String?
    var results: List<Korpus.DocumentSectionContent>
}

class SearchOverlay : RComponent<SearchOverlayProps, SearchOverlayState>() {
    private val searchQueue = CoroutineQueue()

    private fun onQueryChanged(str: String) {
        println("Query: $str")
        searchQueue.run {
            val results = iMDexService.get().query(Imdex.Query(str)).results
            setState {
                this.results = results
            }
            window.scrollTo(0.0, 0.0)
        }
    }

    override fun RBuilder.render() {
        child(AppBarComponent::class) {
            attrs.onSearchChanged = {
                if (it != state.query) {
                    setState {
                        query = it
                        if (it != null) {
                            onQueryChanged(it)
                        } else {
                            results = emptyList()
                        }
                    }
                }
            }
        }
        mBackdrop(state.query != null)
        if (state.query != null) {
            styledDiv {
                css {
                    position = Position.absolute
                    top = LinearDimension("64px")
                    left = LinearDimension.none
                    padding = "64px"
                    paddingTop = LinearDimension("16px")
                }
                child(SearchResults::class) {
                    attrs.results = state.results
                }
            }
        }
    }
}
