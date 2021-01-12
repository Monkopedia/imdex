package com.monkopedia.imdex

import com.monkopedia.kpages.KPagesApp
import com.monkopedia.kpages.Navigator
import com.monkopedia.kpages.RouteBuilder
import com.monkopedia.kpages.ViewControllerFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

expect val themeDemoFactory: ViewControllerFactory
expect val rootSettingsFactory: ViewControllerFactory
expect val defaultFactory: ViewControllerFactory
expect val errorFactory: ViewControllerFactory

class ImdexApp(val imdex: Imdex) : KPagesApp() {
    lateinit var navigator: Navigator
    val cache: ContentCache = ContentCache()

    init {
        INSTANCE = this
    }

    override fun RouteBuilder.routes() {
        prefixRoute("/error", errorFactory)
        route("/theme_demo", themeDemoFactory)
        route("/settings", rootSettingsFactory)

        prefixRoute("/loading/", defaultFactory)
        prefixRoute("/", defaultFactory)
        prefixRoute("", defaultFactory)
    }

    companion object {
        lateinit var INSTANCE: ImdexApp
            private set
    }
}

enum class Error(val title: String, val description: String) {
    MISSING_CACHE(
        "Internal error",
        "An internal error has occured in the caching system that could not be recovered from"
    ),
    BROKEN_LINK(
        "Broken link",
        "The content this link points at either doesn't exist or is otherwise broken"
    )
}

fun Navigator.error(error: Error): Nothing {
    GlobalScope.launch {
        delay(1)
        push("/error/${error.name}")
    }
    throw RuntimeException("Error found: $error")
}