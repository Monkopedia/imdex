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
package com.monkopedia.imdex.lucene

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.gui2.Window
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Gravity
import com.monkopedia.dynamiclayout.GravityLayoutParams
import com.monkopedia.dynamiclayout.SizeSpec.Companion.specify
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.lanterna.ComponentHolder
import com.monkopedia.lanterna.Lanterna.fontLoader
import com.monkopedia.lanterna.asciiLabel
import com.monkopedia.lanterna.buildUi
import com.monkopedia.lanterna.frame
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.space
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.StaticSpan
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchStatusScreen(
    private val onComplete: suspend () -> Unit
) : Screen("search_status") {
    private lateinit var labelFrame: CachingPanel

    override fun ComponentHolder.createWindow() {
        vertical {
            space(1)
            labelFrame = frame {
            }.layoutParams(Fill, specify(4))
            space(1)
        }
        window.setHints(listOf(Window.Hint.CENTERED))
        window.fullscreen = false
    }

    override suspend fun onCreate() {
        labelFrame.buildUi {
            asciiLabel(
                fontLoader.get("h2"),
                StaticSpan("IMDEXing...", listOf(EnableSGRSpan(SGR.BOLD)))
            )
                .layoutData = GravityLayoutParams(Wrap, Wrap, Gravity.CENTER)
        }
        launch {
            delay(1000)
            onComplete()
        }
    }
}
