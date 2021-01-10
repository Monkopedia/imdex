package com.monkopedia.markdown

import com.monkopedia.imdex.Korpus
import com.monkopedia.kpages.Navigator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface Closeable {
    suspend fun close()
}

expect fun Navigator.showLoadingUi(document: Korpus.Document): Closeable

fun Navigator.loadDocument(document: Korpus.Document) {
    val ui = showLoadingUi(document)
    GlobalScope.launch {
        val content = ImdexApp.INSTANCE.imdex.fetch(document)
        val nodes = ImdexApp.INSTANCE.imdex.parse(document)

        ui.close()

        ImdexApp.INSTANCE.cache.insert(document.path, Content(nodes, content))
        push(document.path)
//        open(MdScreen(node, str, null, MdContext(document)))
    }
}

class MdContext(private val document: Korpus.Document) : LinkContext {
    override fun launchLink(
        navigation: Navigator,
        path: String,
        position: String?
    ) {
        val ui = navigation.showLoadingUi(document)
        GlobalScope.launch {
            val link = ImdexApp.INSTANCE.imdex.resolveLink(
                Korpus.ResolveLinkRequest(
                    document,
                    path,
                    position
                )
            ) ?: return@launch navigation.error(Error.BROKEN_LINK)
            val str = ImdexApp.INSTANCE.imdex.fetch(link.document)
            val nodes = ImdexApp.INSTANCE.imdex.parse(link.document)

            ui.close()

            val path = "${link.document.path}?scroll=${link.scrollPosition}"
            ImdexApp.INSTANCE.cache.insert(document.path, Content(nodes, str))
            navigation.push(document.path)
        }
    }
}