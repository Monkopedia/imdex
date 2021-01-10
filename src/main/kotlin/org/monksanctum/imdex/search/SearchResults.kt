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
package org.monksanctum.imdex.search

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
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.FocusResult
import com.monkopedia.lanterna.GUI
import com.monkopedia.lanterna.Lanterna
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
import com.monkopedia.markdown.LinkContext
import com.monkopedia.markdown.parseMarkdown
import com.monkopedia.mdview.MdScreen
import com.monkopedia.mdview.markdown
import com.vladsch.flexmark.util.ast.Node
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.monksanctum.imdex.App

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

fun Navigation.loadDocument(document: Korpus.Document) {
    val window = Lanterna.gui.window(centered = true) {
        label("Loading ${document.path}")
    }
    Lanterna.gui.addWindow(window)
    GlobalScope.launch(Dispatchers.IO) {
        val content = App.imdexService.fetch(document)
        val node: Node = parseMarkdown(content.content)
        withContext(Dispatchers.GUI) {
            Lanterna.gui.removeWindow(window)
        }
        open(MdScreen(node, null, MdContext(document)))
    }
}

class MdContext(private val document: Korpus.Document) : LinkContext {
    override fun launchLink(
        navigation: Navigation,
        path: String,
        position: String?
    ) {
        val window = gui.window(centered = true) {
            label("Loading ${document.path}")
        }
        gui.addWindow(window)
        GlobalScope.launch(Dispatchers.IO) {
            val link = App.imdexService.resolveLink(
                Korpus.ResolveLinkRequest(
                    document,
                    path,
                    position
                )
            ) ?: return@launch errorMessage("Broken link")
            val content = App.imdexService.fetch(link.document)
            val document: Node = parseMarkdown(content.content)
            withContext(Dispatchers.GUI) {
                gui.removeWindow(window)
            }
            navigation.open(MdScreen(document, position, MdContext(link.document)))
        }
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
                val document = parseMarkdown(location.content)
                val markdownViews = buildViews {
                    lastBorder = border(location.documentSection.document.path) {
                        vertical {
                            markdown(document, MdContext(location.documentSection.document))
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