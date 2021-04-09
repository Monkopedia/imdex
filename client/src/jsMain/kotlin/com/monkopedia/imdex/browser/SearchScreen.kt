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

import com.monkopedia.imdex.Imdex
import com.monkopedia.imdex.Korpus
import com.monkopedia.imdex.ProfileManager.Companion.GLOBAL
import com.monkopedia.imdex.Scriptorium
import com.monkopedia.ksrpc.KsrpcType
import com.monkopedia.ksrpc.KsrpcUri
import com.monkopedia.ksrpc.connect
import com.monkopedia.ksrpc.deserialized
import com.monkopedia.imdex.CoroutineQueue
import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.h1
import react.setState

external interface SearchQueryState : RState {
    var results: List<Korpus.DocumentSectionContent>
}

object iMDexService {

    private var serviceImpl: Imdex? = null
    private var scriptoriumImpl: Scriptorium? = null

    suspend fun scriptorium() = scriptoriumImpl ?: Scriptorium.wrap(
        KsrpcUri(
            KsrpcType.HTTP,
            ("http://localhost:8080/scriptorium").also {
//            (window.location.protocol + "//" + window.location.hostname + "${window.location.port?.toIntOrNull()?.let { ":$it" } ?: ""}" + "/scriptorium").also {
                println("Connecting to $it")
            }
        ).connect().deserialized()
    ).also {
        scriptoriumImpl = it
    }

    suspend fun get() = serviceImpl ?: scriptorium().imdex(GLOBAL).also {
        serviceImpl = it
    }
}

class SearchScreen : RComponent<RProps, SearchQueryState>() {
    private val searchQueue = CoroutineQueue()

    private fun onQueryChanged(str: String) {
        println("Query: $str")
        searchQueue.run {
            val results = iMDexService.get().query(Imdex.Query(str)).results
            setState {
                this.results = results
            }
        }
    }

    override fun RBuilder.render() {
        child(SearchBox::class) {
            attrs.onQueryChanged = this@SearchScreen::onQueryChanged
        }
        child(SearchResults::class) {
            attrs.results = state.results
        }
    }
}

class ClickedApp : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        h1 {
            +"Clickd Hello, React+Kotlin/JS Component!"
        }
    }
}
