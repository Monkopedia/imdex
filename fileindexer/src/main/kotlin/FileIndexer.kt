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
import com.monkopedia.imdex.Korpus
import com.monkopedia.imdex.KorpusManager.DataType
import com.monkopedia.imdex.KorpusManager.KorpusKeyInfo
import com.monkopedia.imdex.KorpusManager.KorpusType
import com.monkopedia.imdex.Scriptorium
import com.monkopedia.imdex.Scriptorium.KorpusInfo
import com.monkopedia.imdex.ensureConfig
import com.monkopedia.ksrpc.connect
import com.monkopedia.ksrpc.deserialized
import com.monkopedia.ksrpc.toKsrpcUri
import com.monkopedia.scriptorium.FileIndexWorker
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.sql.Connection
import kotlinx.coroutines.runBlocking
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
            val imdex = Scriptorium.wrap(imdexPath.toKsrpcUri().connect().deserialized())
            imdex.korpusManager(Unit).ensureConfig(fileIndexerType)
            val configs = imdex.getKorpii(Unit).filter {
                it.type == fileIndexerType.type
            }
            for (c in configs) {
                println("Running indexing on ${c.id}")
                val korpus = imdex.korpus(c.id)
                val target = c.config[TARGET_PATH] ?: continue.also {
                    println("Warning no target")
                }
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
        val db = File(File(config.config[Korpus.DATA_PATH_KEY]), "state.db")
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
            "com.monkopedia.imdex.korpus.files",
            listOf(
                KorpusKeyInfo(
                    "Path",
                    DataType.PATH.type,
                    TARGET_PATH
                )
            ),
        )
    }
}
