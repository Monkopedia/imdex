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
package com.monkopedia.fileindexer

import MdFileTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.monkopedia.FileLogger
import com.monkopedia.Log
import com.monkopedia.StdoutLogger
import com.monkopedia.imdex.DATA_PATH_KEY
import com.monkopedia.imdex.DataType.PATH
import com.monkopedia.imdex.KorpusInfo
import com.monkopedia.imdex.KorpusKeyInfo
import com.monkopedia.imdex.KorpusKeyInfo.Companion.LABEL
import com.monkopedia.imdex.KorpusType
import com.monkopedia.imdex.Scriptorium
import com.monkopedia.imdex.ensureConfig
import com.monkopedia.ksrpc.channels.ChannelClient
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.ktor.asConnection
import com.monkopedia.ksrpc.ktor.websocket.asWebsocketConnection
import com.monkopedia.ksrpc.sockets.asConnection
import com.monkopedia.ksrpc.toStub
import com.monkopedia.scriptorium.FileIndexWorker
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import java.io.File
import java.net.Socket
import java.sql.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) = FileIndexerApp().main(args)

class FileIndexerApp : CliktCommand() {
    private val log by option("-l", "--log", help = "Path to log to, or stdout")
        .default("/tmp/file_indexer.txt")
    private val imdexPath by option("-i", "--imdex", help = "Ksrpc uri to imdex server")
        .default("ksrpc://localhost:14038")

    override fun run() {
        if (log == "stdout") {
            Log.init(StdoutLogger)
        } else {
            Log.init(FileLogger(File(log)))
        }
        runBlocking {
            val imdex = imdexPath.asConnection().defaultChannel().toStub<Scriptorium, String>()
            imdex.korpusManager(Unit).ensureConfig(fileIndexerType)
            val configs = imdex.getKorpii(Unit).filter {
                it.type == fileIndexerType.type
            }
            for (c in configs) {
                println("Running indexing on ${c.id}")
                val korpus = imdex.korpus(c.id)
                val target = c.config[TARGET_PATH] ?: continue
                val targetFile = File(target)
                if (!targetFile.exists()) {
                    println("$targetFile doesn't exist")
                    continue
                }
                val db = dbFor(config = c)
                val imdexer = FileIndexWorker(db)
                imdexer.launchIndexing(korpus, targetFile)
            }
        }
    }

    private fun dbFor(config: KorpusInfo): Database {
        val db = File(File(config.config[DATA_PATH_KEY]), "state.db")
        if (!db.exists()) {
            db.parentFile.mkdirs()
        }
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${db.absolutePath}"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 1
        }
        val dataSource = HikariDataSource(config)

        return Database.connect(dataSource).also {
            TransactionManager.managerFor(it)?.defaultIsolationLevel =
                Connection.TRANSACTION_SERIALIZABLE
            transaction(db = it) {
                SchemaUtils.createMissingTablesAndColumns(MdFileTable)
            }
        }
    }

    companion object {
        const val TARGET_PATH = "folderindexer.path"
        val fileIndexerType = KorpusType(
            "File index",
            "com.monkopedia.imdex.korpus.files",
            listOf(
                LABEL,
                KorpusKeyInfo(
                    "Path",
                    PATH.type,
                    TARGET_PATH,
                ),
            ),
        )
    }
}

private suspend fun String.asConnection(): ChannelClient<String> = withContext(Dispatchers.IO) {
    val ksrpcEnv = ksrpcEnvironment { }
    return@withContext when {
        startsWith("ksrpc://") -> {
            val (host, port) = substring("ksrpc://".length).split(":")
            val socket = Socket(host, port.toInt())
            (socket.getInputStream() to socket.getOutputStream()).asConnection(ksrpcEnv)
        }

        startsWith("http://") || startsWith("https://") -> {
            HttpClient {}.asConnection(this@asConnection, ksrpcEnv)
        }

        startsWith("ws://") -> {
            HttpClient {
                install(WebSockets)
            }.asWebsocketConnection(this@asConnection, ksrpcEnv)
        }

        else -> error("Unsupported connection format $this")
    }
}
