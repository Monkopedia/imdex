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

import com.googlecode.lanterna.gui2.AbstractBorder
import com.googlecode.lanterna.gui2.Border
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.dynamiclayout.DynamicFrameLayout
import com.monkopedia.dynamiclayout.DynamicVerticalLinearLayout
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Gravity
import com.monkopedia.dynamiclayout.Padding
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.imdex.Korpus
import com.monkopedia.imdex.MdContext
import com.monkopedia.imdex.markdown
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.FocusResult
import com.monkopedia.lanterna.GUI
import com.monkopedia.lanterna.Lanterna.gui
import com.monkopedia.lanterna.LinearPanelHolder
import com.monkopedia.lanterna.Selectable
import com.monkopedia.lanterna.ThemeDataImpl
import com.monkopedia.lanterna.border
import com.monkopedia.lanterna.buildUi
import com.monkopedia.lanterna.buildViews
import com.monkopedia.lanterna.copyWithMutation
import com.monkopedia.lanterna.frame
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.mutate
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.space
import com.monkopedia.lanterna.vertical
import com.monkopedia.lanterna.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchResults(var onSelectCallback: (Korpus.Document) -> Unit) : CachingPanel() {
    private val layout = DynamicVerticalLinearLayout(this)
    private var layoutBuilder = LinearPanelHolder(this, layout)

    init {
        layoutManager = layout
    }

    var results: List<Korpus.DocumentSectionContent> = emptyList()
        set(value) {
            field = value.toList()
            bindResults(results)
            layout.requestLayout()
        }

    val selectables: List<Selectable>
        get() = childrenList.filterIsInstance<Selectable>()

    private fun bindResults(results: List<Korpus.DocumentSectionContent>): List<Selectable> {
        removeAllComponents()
        if (results.isNotEmpty()) {
            layoutBuilder.apply {
                for (location in results) {
                    space(1)
                    val searchResultItem = SearchResultItem {
                        onSelectCallback(location.documentSection.document)
                    }
                    addComponent(searchResultItem)
                    searchResultItem.layoutParams(Fill, Wrap)
                    searchResultItem.location = location
                }
            }
            layout.requestLayout()
        } else {
            layoutBuilder.apply {
                space(2)
                addComponent(
                    SearchResultItem {
                        // Do nothing.
                    }.also {
                        it.location = null
                        it.layoutParams(Fill, Wrap)
                    }
                )
            }
        }
        return selectables
    }
}

suspend fun errorMessage(s: String) {
    val window = gui.window(centered = true) { label(s) }
    gui.addWindow(window)
}

class SearchResultItem(private val fireCallback: () -> Unit) : CachingPanel(), Selectable {
    private val layout = DynamicFrameLayout(this)
    private var lastBorder: Border? = null
        set(value) {
            field = value
            field?.updateTheme()
        }

    override var selected: Boolean = false
        set(value) {
            field = value
            lastBorder?.updateTheme()
        }

    init {
        layoutManager = layout
        updateUi(null)
    }

    var location: Korpus.DocumentSectionContent? = null
        set(value) {
            field = value
            updateUi(value)
            layout.requestLayout()
        }

    private fun updateUi(location: Korpus.DocumentSectionContent?) {
        removeAllComponents()
        if (location == null) {
            buildUi {
                lastBorder = border {
                    frame {
                        layout.padding = Padding(0, 1, 0, 1)
                        label("No results").layoutParams(Wrap, Wrap, Gravity.CENTER)
                    }.layoutParams(Fill, Wrap)
                }.layoutParams(Fill, Wrap)
            }
        } else {
            buildUi {
                lastBorder = border {
                    frame {
                        layout.padding = Padding(0, 3, 0, 3)
                        label("Loading result...").layoutParams(Wrap, Wrap, Gravity.CENTER)
                    }.layoutParams(Fill, Wrap)
                }.layoutParams(Fill, Wrap)
            }
            GlobalScope.launch(Dispatchers.IO) {
                val markdownViews = buildViews {
                    lastBorder = border(location.documentSection.document.path) {
                        vertical {
                            if (location.parsed != null) {
                                markdown(
                                    location.parsed!!,
                                    location.content,
                                    MdContext(location.documentSection.document)
                                )
                            } else {
                                label(location.content)
                            }
                        }.layoutParams(Fill, Wrap)
                    }.layoutParams(Fill, Wrap)
                }
                withContext(Dispatchers.GUI) {
                    removeAllComponents()
                    for (component in markdownViews) {
                        addComponent(component)
                    }
                    delay(500)
                }
            }
        }
    }

    fun Border.updateTheme() {
        val borderTheme = AbstractBorder::class.qualifiedName!!
        val defaultBorderTheme = gui.theme.getDefinition(AbstractBorder::class.java)
        theme = if (selected) {
            (gui.theme as ThemeDataImpl).copyWithMutation {
                mutate(borderTheme) {
                    copy(
                        normal = (normal ?: default.normal)!!.copy(
                            foreground = (
                                defaultBorderTheme.normal as
                                    ThemeDataImpl.ThemeStyleImpl
                                ).backgroundColor,
                            background = (
                                defaultBorderTheme.normal as
                                    ThemeDataImpl.ThemeStyleImpl
                                ).foregroundColor
                        )
                    )
                }
            }
        } else {
            gui.theme
        }
    }

    override fun onFire(navigation: Navigation): FocusResult {
        fireCallback()
        return ConsumeEvent
    }
}
