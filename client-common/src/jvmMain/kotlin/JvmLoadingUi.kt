package com.monkopedia.markdown

import com.monkopedia.imdex.Korpus
import com.monkopedia.kpages.Navigator
import com.monkopedia.lanterna.GUI
import com.monkopedia.lanterna.Lanterna
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun Navigator.showLoadingUi(document: Korpus.Document): Closeable {
    val window = Lanterna.gui.window(centered = true) {
        label("Loading ${document.path}")
    }
    Lanterna.gui.addWindow(window)
    return object : Closeable {
        override suspend fun close() {
            withContext(Dispatchers.GUI) {
                Lanterna.gui.removeWindow(window)
            }
        }
    }
}