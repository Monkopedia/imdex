package com.monkopedia.imdex

import com.googlecode.lanterna.input.KeyType
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Gravity
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.imdex.search.SearchScreen
import com.monkopedia.kpages.Mutable
import com.monkopedia.kpages.Navigator
import com.monkopedia.kpages.ViewControllerFactory
import com.monkopedia.lanterna.ComponentHolder
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.EventMatcher.Companion.matcher
import com.monkopedia.lanterna.EventMatcher.Companion.or
import com.monkopedia.lanterna.border
import com.monkopedia.lanterna.frame
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.on
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.launch

actual val themeDemoFactory: ViewControllerFactory = object : ViewControllerFactory() {
    override fun create(
        navigation: Navigator,
        path: String,
        title: Mutable<CharSequence>
    ): Screen {
        title.value = "Theme demo"
        return object : Screen("missing") {
            override fun ComponentHolder.createWindow() {
                frame {
                    label("Not implemented yet").layoutParams(Wrap, Wrap, Gravity.CENTER)
                }
            }
        }
    }
}

actual val rootSettingsFactory: ViewControllerFactory = object : ViewControllerFactory() {
    override fun create(
        navigation: Navigator,
        path: String,
        title: Mutable<CharSequence>
    ): Screen {
        title.value = "Settings"
        return object : Screen("missing") {
            override fun ComponentHolder.createWindow() {
                frame {
                    label("Not implemented yet").layoutParams(Wrap, Wrap, Gravity.CENTER)
                }
            }
        }
    }
}

actual val searchFactory: ViewControllerFactory = object : ViewControllerFactory() {
    override fun create(
        navigation: Navigator,
        path: String,
        title: Mutable<CharSequence>
    ): Screen {
        title.value = "Search"
        return SearchScreen(navigation)
    }
}

actual val defaultFactory: ViewControllerFactory = object : ViewControllerFactory() {
    override fun create(
        navigation: Navigator,
        path: String,
        title: Mutable<CharSequence>
    ): Screen {
        val (path, scroll) = if (path.contains("?scroll=")) path.split("?scroll=")
        else listOf(path, "")
        val data = ImdexApp.INSTANCE.cache.get(path, navigation)
        title.value = data.content.metadata.label
        return MdScreen(navigation, data.nodes, data.content.content, scroll, MdContext(data.content.document))
    }
}

actual val errorFactory: ViewControllerFactory = ViewControllerFactory { navigator, path ->
    val error = enumValueOf<Error>(path.substring("/error/".length))
    ErrorScreen(navigator, error)
}


class ErrorScreen(private val navigator: Navigator, private val error: Error) :
    Screen("error_screen") {
    override fun ComponentHolder.createWindow() {
        frame {
            vertical {
                border(error.title) {
                    label(error.description)
                }
            }.layoutParams(Wrap, Wrap, Gravity.CENTER)
        }.layoutParams(Fill, Fill)
    }

    override suspend fun onCreate() {
        super.onCreate()
        focusManager.keymap.create("Back") {
            launch {
                navigator.goBack()
            }
            ConsumeEvent
        } on (KeyType.Escape.matcher() or KeyType.Backspace.matcher())
    }
}