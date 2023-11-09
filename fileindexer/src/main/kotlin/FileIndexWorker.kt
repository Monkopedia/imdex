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
package com.monkopedia.scriptorium

import MdFileDao
import com.monkopedia.imdex.Document
import com.monkopedia.imdex.DocumentContent
import com.monkopedia.imdex.DocumentMetadata
import com.monkopedia.imdex.DocumentType.MARKDOWN
import com.monkopedia.imdex.Korpus
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.Executors
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

val Main = newSingleThreadContext("Main")

class FileIndexWorker(private val database: Database) {

    private val executor = Executors.newCachedThreadPool()
    private val running = Semaphore(4)
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        LoggerFactory.getLogger(FileIndexWorker::class.java).error(throwable.stackTraceToString())
        exitProcess(0)
    }
    private val scope = CoroutineScope(executor.asCoroutineDispatcher() + exceptionHandler)

    suspend fun launchIndexing(korpus: Korpus, baseDir: File) {
        withContext(scope.coroutineContext) {
            val rootDoc = korpus.rootDocument(Unit)
            val mdFiles = flow {
                baseDir.listFiles().forEach { f -> listFiles(f) }
            }.map {
                (Document(rootDoc, it.toRelativeString(baseDir)) to it)
            }.toCollection(mutableListOf())
            val mdState = transaction(db = database) {
                MdFileDao.all().map {
                    it.file to it
                }
            }.toMap().toMutableMap()

            val updateJobs = mdFiles.map { (doc, file) ->
                val state = mdState.remove(doc.path)
                if (state != null) {
                    launch {
                        running.withPermit {
                            // Update
                            val md5sum = file.md5sum()
                            if (md5sum != state.hash) {
                                korpus.updateDocument(
                                    DocumentContent(
                                        doc,
                                        DocumentMetadata(
                                            MARKDOWN,
                                            "",
                                            file.name
                                        ),
                                        file.readText()
                                    )
                                )
                                newSuspendedTransaction(db = database) {
                                    state.hash = md5sum
                                }
                            }
                        }
                    }
                } else {
                    launch {
                        running.withPermit {
                            val md5sum = file.md5sum()
                            korpus.createDocument(
                                DocumentContent(
                                    doc,
                                    DocumentMetadata(
                                        MARKDOWN,
                                        "",
                                        file.name
                                    ),
                                    if (file.isDirectory) "" else file.readText()
                                )
                            )
                            newSuspendedTransaction(db = database) {
                                MdFileDao.new {
                                    this.file = doc.path
                                    this.hash = md5sum
                                }
                            }
                        }
                    }
                }
            }
            val newJobs = mdState.map { (path, state) ->
                // Delete
                launch {
                    running.withPermit {
                        korpus.deleteDocument(Document(path))
                        newSuspendedTransaction(db = database) {
                            state.delete()
                        }
                    }
                }
            }
            withContext(Main) {
                val allJobs = updateJobs + newJobs
                val total = allJobs.size
                var complete = allJobs.count { it.isCompleted }
                var lastCommit = 0
                val progressBar = ProgressBar()
                while (complete != total) {
                    if (complete - lastCommit > 15) {
                        korpus.updateIndex(Unit)
                        lastCommit = complete
                    }
                    progressBar.update(complete, total)
                    delay(20)
                    complete = allJobs.count { it.isCompleted }
                }
                korpus.updateIndex(Unit)
            }
        }
        executor.shutdown()
    }
}

private suspend fun FlowCollector<File>.listFiles(file: File) {
    if (file.extension == "md") {
        emit(file)
    }
    if (file.isDirectory) {
        file.listFiles().forEach { f -> listFiles(f) }
    }
}

class ProgressBar {
    private var progress: StringBuilder = StringBuilder(60)

    fun update(amount: Int, total: Int) {
        var done = amount
        val workchars = charArrayOf('|', '/', '-', '\\')
        val format = "\r%3d%% %s %c"
        val percent = ++done * 100 / total
        var extrachars = percent / 2 - progress.length
        while (extrachars-- > 0) {
            progress!!.append('#')
        }
        System.out.printf(format, percent, progress, workchars[done % workchars.size])
        if (done == total) {
            System.out.flush()
            println("")
            System.out.flush()
        }
    }
}

private fun File.listMarkdown(): List<File> {
    if (isDirectory) return listFiles().flatMap {
        it.listMarkdown()
    } + this
    return if (extension == "md") listOf(this) else emptyList()
}

fun File.md5sum(): String {
    if (isDirectory) return ""
    val bytes = MessageDigest
        .getInstance("MD5")
        .digest(readBytes())
    return bytes.toHex()
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
