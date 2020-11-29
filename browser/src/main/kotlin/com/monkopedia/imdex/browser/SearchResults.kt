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

import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.card.mCard
import com.ccfraser.muirwik.components.mDivider
import com.ccfraser.muirwik.components.mTypography
import com.monkopedia.imdex.Korpus
import kotlinx.browser.window
import kotlinx.css.LinearDimension
import kotlinx.css.marginTop
import kotlinx.css.padding
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyUpFunction
import kotlinx.html.tabIndex
import kotlinx.html.unsafe
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.h1
import styled.StyleSheet
import styled.css

external interface SearchResultProps : RProps {
    var results: List<Korpus.DocumentSectionContent>
}

class SearchResults : RComponent<SearchResultProps, RState>() {

    private object ComponentStyles : StyleSheet("searchresults", isStatic = true) {
        val resultCard by css {
            marginTop = LinearDimension("8px")
            padding(LinearDimension("8px"))
        }
    }

    val converter = Showdown().also {
        it.setOption("tables", true)
    }

    override fun RBuilder.render() {
        if (props.results?.isEmpty() ?: true) {
            h1 {
                +"No results"
            }
        } else {
            for (item in props.results) {
                mCard(raised = true) {
                    css(ComponentStyles.resultCard)
                    div {
                        attrs {
                            tabIndex = "0"
                        }
                        mTypography(
                            item.documentSection.document.path,
                            variant = MTypographyVariant.caption
                        )
                        mDivider {
                        }
                        div {
                            attrs.unsafe {
                                +converter.makeHtml(item.content)
                            }
                        }
                        attrs.onClickFunction = {
                            it.preventDefault()
                            it.stopPropagation()
                            Navigation.open(item.documentSection.document)
                        }
                        attrs.onKeyUpFunction = {
                            if (it.asDynamic().key == "Enter") {
                                it.preventDefault()
                                it.stopPropagation()
                                Navigation.open(item.documentSection.document)
                            }
                        }
                    }
                }
            }
        }
    }
}

object Navigation {
    fun open(document: Korpus.Document) {
        window.location.pathname = document.path
    }
}

external fun decodeURIComponent(encodedURI: String): String
external fun encodeURIComponent(encodedURI: String): String
