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
package org.monksanctum.imdex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.monkopedia.FileLogger
import com.monkopedia.Log
import com.monkopedia.imdex.Imdex
import com.monkopedia.imdex.KorpusManager
import com.monkopedia.imdex.ProfileManager
import com.monkopedia.imdex.Scriptorium
import com.monkopedia.info
import com.monkopedia.ksrpc.connect
import com.monkopedia.ksrpc.toKsrpcUri
import com.monkopedia.lanterna.Lanterna
import com.monkopedia.lanterna.Lanterna.gui
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.runGuiThread
import com.monkopedia.mdview.loadMd
import java.io.File
import java.util.Properties
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import org.monksanctum.imdex.lucene.SearchStatusScreen
import org.monksanctum.imdex.search.SearchScreen

object App : CliktCommand() {
    lateinit var scriptoriumService: Scriptorium
    lateinit var imdexService: Imdex
    val configFile by option("-c", "--config", help = "Path to json configuration file").file()
        .defaultLazy {
            File(defaultKindexHome, "config.json").also {
                if (!it.exists()) {
                    it.writeJson(Config())
                }
            }
        }

    val file by argument("md file", help = "Optional md to load").optional()
    val imdex by option("-i", "--imdex", help = "Ksrpc path to iMDex server")
        .default("ksrpc://localhost:14038")
    val profile by option("-p", "--profile", help = "The profile to use")
        .int()
        .default(ProfileManager.DEFAULT_CMD)

    override fun run() = runBlocking {
        val config = configFile.readJson<Config>()
        val logFile = File(config.homeFile, "log.txt")
        if (logFile.exists()) {
            logFile.delete()
        }
        Log.init(FileLogger(logFile))
        val channel = imdex.toKsrpcUri().connect()
        scriptoriumService = Scriptorium.wrap(channel)
        imdexService = scriptoriumService.imdex(profile)
        Lanterna.init(
            config.homeFile,
            config.themeFile.readJson(),
            config.fontFile.properties
        )

        val navigation = Navigation(gui)

        if (scriptoriumService.getKorpii(Unit).isEmpty()) {
            Log.info("No korpii, creating one")
            scriptoriumService.korpusManager(Unit).createKorpus(
                KorpusManager.CreateKorpus(
                    "imdex.korpus.files",
                    mapOf(
                        "folderindexer.path" to "/home/jmonk/dnd_content"
                    )
                )
            )
        }
        Log.info("Indexing...")
        navigation.open(
            SearchStatusScreen {
                Log.info("Searching...")
                val launchFile = file
                if (launchFile != null) {
                    navigation.loadMd(File(launchFile))
                } else {
                    navigation.replace(SearchScreen())
                }
            }
        )
        runGuiThread(true)
        Log.info("Shutting down main thread")
        navigation.destroy()
        Lanterna.screen.close()
        Lanterna.terminal.close()
        exitProcess(1)
    }
}

val File.properties: Properties
    get() = reader().use { theme ->
        Properties().also { it.load(theme) }
    }

fun main(args: Array<String>) = App.main(args)
