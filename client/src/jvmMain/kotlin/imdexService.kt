package com.monkopedia.imdex

actual suspend fun getScriptorium(): Scriptorium {
    return App.scriptoriumService
}