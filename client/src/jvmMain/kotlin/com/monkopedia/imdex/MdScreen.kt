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
package com.monkopedia.imdex

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.Container
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.input.KeyType.Enter
import com.googlecode.lanterna.input.KeyType.ReverseTab
import com.googlecode.lanterna.input.KeyType.Tab
import com.monkopedia.Log
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Gravity
import com.monkopedia.dynamiclayout.MeasureSpec
import com.monkopedia.dynamiclayout.ScrollComponent
import com.monkopedia.dynamiclayout.ScrollListener
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.dynamiclayout.asDynamicLayout
import com.monkopedia.dynamiclayout.debugLayout
import com.monkopedia.dynamiclayout.plus
import com.monkopedia.info
import com.monkopedia.lanterna.ComponentHolder
import com.monkopedia.lanterna.EventMatcher.Companion.matcher
import com.monkopedia.lanterna.GUI
import com.monkopedia.lanterna.Lanterna
import com.monkopedia.lanterna.Selectable
import com.monkopedia.lanterna.SelectableContainer
import com.monkopedia.lanterna.SelectableListener
import com.monkopedia.lanterna.SelectionManager
import com.monkopedia.lanterna.SharedFocus
import com.monkopedia.lanterna.buildViews
import com.monkopedia.lanterna.frame
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.navigation.registerBackspaceAsBack
import com.monkopedia.lanterna.navigation.registerEscapeAsBack
import com.monkopedia.lanterna.scroll
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

private const val DEBUG_LAYOUT: Boolean = false

class MdScreen(
    private val document: ImdexNode,
    private val content: String,
    private val scrollTo: String?,
    private val linkContext: LinkContext
) : Screen("mdscreen") {
    private lateinit var frame: Panel
    private lateinit var scroll: ScrollComponent
    private var scrollPosition: Int = 0

    private val scrollListener = ScrollListener {
        scrollPosition = it
        updateActiveActions()
    }

    private val actionMap =
        mutableMapOf<SelectableContainer, List<Pair<TerminalPosition, Selectable>>>()
    private val actionListener = SelectableListener { label, actions ->
        val offset = label.relativeTo(scroll.component)
        actionMap[label] = actions.map {
            (it.first + offset) to it.second
        }
        updateActionList()
    }
    private var activeActions = mapOf<Int, List<Selectable>>()

    private val selectionManager by lazy {
        SelectionManager(
            navigation,
            Enter.matcher(),
            Tab.matcher(),
            ReverseTab.matcher()
        )
    }

    private fun updateActionList() {
        activeActions = actionMap.values.flatten().groupBy {
            it.first.row
        }.mapValues {
            it.value.sortedBy { item ->
                item.first.column
            }.map { (_, v) ->
                v
            }
        }
        updateActiveActions()
    }

    private fun updateActiveActions() {
        selectionManager.selectables =
            (scrollPosition until (scrollPosition + scroll.size.rows)).flatMap {
                activeActions[it] ?: emptyList()
            }
    }

    override fun ComponentHolder.createWindow() {
        frame = frame {
            label("Rendering...").layoutParams(Wrap, Wrap, Gravity.CENTER)
        }
    }

    override suspend fun onCreate() {
        window.fullscreen = true
        launch(Dispatchers.IO) {
            scroll = buildViews {
                scroll {
                    vertical {
                        layout.renderer.setFillAreaBeforeDrawingComponents(true)
                        markdown(document, content, linkContext)
                    }
                }.layoutParams(Fill, Wrap)
            }.first() as ScrollComponent
            scroll.scrollListeners.add(scrollListener)
            focusManager.defaultHandler = SharedFocus(selectionManager)
            registerEscapeAsBack()
            registerBackspaceAsBack()
            scroll.registerCommands(focusManager.keymap)

            if (DEBUG_LAYOUT) {
                window.scope.launch(Dispatchers.GUI) {
                    delay(1000)
                    window.component.asDynamicLayout.measure(
                        MeasureSpec.atMost(Lanterna.screen.terminalSize.columns - 4),
                        MeasureSpec.atMost(Lanterna.screen.terminalSize.rows - 4)
                    )
                    Log.info(window.component.debugLayout())
                }
            }
            scroll.findChildren<SpannableLabel>().forEach {
                it.selectableListeners.add(actionListener)
            }
            withContext(Dispatchers.GUI) {
                frame.removeAllComponents()
                frame.addComponent(scroll)
            }
        }
    }
}

fun Component.relativeTo(component: Component): TerminalPosition = when {
    parent == component -> position
    parent != null -> position + parent.relativeTo(component)
    else -> throw IllegalArgumentException("Can't find parent $component")
}

inline fun <reified T : Component> Container.findChildren(): Collection<T> {
    return mutableListOf<T>().also {
        findChildren(it, T::class)
    }
}

fun <T : Component> Container.findChildren(mutableList: MutableList<T>, cls: KClass<T>) {
    for (c in children) {
        if (cls.isInstance(c)) {
            mutableList.add(c as T)
        }
        if (c is Container) {
            c.findChildren(mutableList, cls)
        }
    }
}
