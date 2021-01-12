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

import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.targetInputValue
import kotlinx.css.LinearDimension
import kotlinx.css.height
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

external interface SearchState : RState {
    var currentString: String?
}

external interface SearchProps : RProps {
    var onQueryChanged: ((String) -> Unit)?
}

class SearchBox : RComponent<SearchProps, SearchState>() {

    private fun updateSearch(str: String) {
        setState {
            currentString = str
        }
        props.onQueryChanged?.invoke(str)
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                height = LinearDimension("64px")
            }
        }
        mTextField(
            "Search",
            value = state.currentString,
            variant = MFormControlVariant.outlined,
            onChange = { e ->
                updateSearch(e.targetInputValue)
            }
        )
    }
}
