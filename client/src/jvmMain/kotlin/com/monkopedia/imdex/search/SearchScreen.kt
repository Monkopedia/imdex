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
package com.monkopedia.imdex.search

import com.googlecode.lanterna.input.KeyType.ArrowDown
import com.googlecode.lanterna.input.KeyType.ArrowUp
import com.googlecode.lanterna.input.KeyType.Enter
import com.monkopedia.Log
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.imdex.Imdex
import com.monkopedia.info
import com.monkopedia.kpages.Navigator
import com.monkopedia.lanterna.ComponentHolder
import com.monkopedia.lanterna.EventMatcher
import com.monkopedia.lanterna.GUI
import com.monkopedia.lanterna.Lanterna
import com.monkopedia.lanterna.SelectionManager
import com.monkopedia.lanterna.TextInput
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.vertical
import com.monkopedia.imdex.App
import com.monkopedia.imdex.loadDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SearchScreen(private val navigator: Navigator) : Screen("search") {
    private val searchResults = SearchResults {
        navigator.loadDocument(it)
    }
    private val textInput = TextInput().apply {
        hint = "Enter query"
        onTextChangedListener = {
            launchUpdate()
        }
    }

    override fun ComponentHolder.createWindow() {
        vertical {
            addComponent(textInput)
            textInput.layoutParams(Fill, Wrap)
            addComponent(searchResults)
            searchResults.layoutParams(Fill, Fill)
        }.layoutParams(Fill, Fill)
    }

    override suspend fun onCreate() {
        window.theme = Lanterna.gui.theme
        window.fullscreen = true
    }

    private var pendingUpdate = false
    private val updateLock = Mutex()
    private var latestText: String = ""
    private val selection by lazy {
        SelectionManager(
            navigation,
            EventMatcher.keyType(Enter),
            EventMatcher.keyType(ArrowDown),
            EventMatcher.keyType(ArrowUp)
        )
    }

    /**
     * Handles queries when many characters come in quickly.
     * In the case of 'a', 'b' and 'c' being typed in less time than a search
     * can be done, this will most likely launch a search on 'a', then a search
     * on "abc", but queueing it as soon as
     */
    private fun launchUpdate() {
        latestText = textInput.text?.toString() ?: ""
        if (pendingUpdate) return
        pendingUpdate = true
        launch(Dispatchers.IO) {
            Log.info("Query $latestText")
            val results = updateLock.withLock {
                val text = latestText
                pendingUpdate = false
                App.imdexService.query(Imdex.Query(text)).results.also {
                    it.forEach {
                        Log.info("Document: $it")
                    }
                }
            }
            Log.info("Found ${results.size}")
            withContext(Dispatchers.GUI) {
                searchResults.results = results
                selection.selectables = listOf(textInput.asSelectable) + searchResults.selectables
            }
        }
    }

    override suspend fun onShowing() {
        Log.info("$this onShowing")
        searchResults.results = emptyList()
        launchUpdate()

        selection.selectables = listOf(textInput.asSelectable) + searchResults.selectables
        focusManager.defaultHandler = selection
        launch(Dispatchers.IO) {
            delay(1000)
            withContext(Dispatchers.GUI) {
                // Log.info(window.component.debugLayout())
            }
        }
    }
}
