package com.monkopedia.imdex

import com.monkopedia.kpages.Navigator

data class Content(
    val nodes: ImdexNode,
    val content: Korpus.DocumentContent
)

class ContentCache {
    private val cache = mutableMapOf<String, Content>()

    fun insert(path: String, content: Content) {
        cache[path] = content
    }

    fun get(path: String): Content? {
        return cache[path]
    }

    fun get(path: String, navigator: Navigator): Content {
        return cache[path] ?: navigator.error(Error.MISSING_CACHE)
    }

    private fun clearCache(path: String) {
        // TODO: Literally anything else.
        cache.remove(path)
    }

    fun wrapped(navigator: Navigator): Navigator {
        return object : Navigator {
            override val path: String
                get() = navigator.path

            override suspend fun goBack() {
                clearCache(path)
                navigator.goBack()
            }

            override suspend fun push(path: String) {
                navigator.push(path)
            }
        }
    }
}

