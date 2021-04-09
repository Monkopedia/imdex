package com.monkopedia.imdex

import com.monkopedia.imdex.browser.iMDexService

actual suspend fun getScriptorium(): Scriptorium {
    return iMDexService.scriptorium()
}