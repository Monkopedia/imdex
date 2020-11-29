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
package com.monkopedia.kindex

import com.monkopedia.Log
import com.monkopedia.debug
import com.monkopedia.imdex.Korpus
import com.monkopedia.imdex.Scriptorium.KorpusInfo
import com.monkopedia.kindex.KindexApp.Companion.ARTIFACTS_URL_KEYS
import com.monkopedia.kindex.KindexApp.Companion.MAVEN_URL_KEYS
import com.monkopedia.scriptorium.FileIndexWorker
import com.monkopedia.scriptorium.Main
import com.monkopedia.scriptorium.ProgressBar
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

val KorpusInfo.homeFile: File
    get() = File(config[Korpus.DATA_PATH_KEY]).also {
        it.mkdirs()
    }

private val stringListSerializer = ListSerializer(String.serializer())

val KorpusInfo.mavenUrls: List<String>
    get() = config[MAVEN_URL_KEYS]?.let { Json.decodeFromString(stringListSerializer, it) }
        ?: emptyList()
val KorpusInfo.includedArtifacts: List<MavenArtifact>
    get() = (
        config[ARTIFACTS_URL_KEYS]?.let { Json.decodeFromString(stringListSerializer, it) }
            ?: emptyList()
        ).map {
        MavenArtifact.from(it)
    }

class ArtifactIndexWorker(val config: KorpusInfo, val database: Database) {

    private val executor = Executors.newFixedThreadPool(4)
    private val scope = CoroutineScope(executor.asCoroutineDispatcher() + Log.exceptionHandler)

    suspend fun launchIndexing(korpus: Korpus) {
        withContext(scope.coroutineContext) {
            val states = transaction { ArtifactStateDao.all().toList() }
            val existingState = states.toMutableList()
            val mavenClient = MavenClient.forUrls(*config.mavenUrls.toTypedArray())
            val downloader = Downloader(config, mavenClient)
            var anyChanges = false

            val includedArtifacts = config.includedArtifacts
            println("Updating ${includedArtifacts.size} artifacts...")
            val downloaders = includedArtifacts.map { artifact ->
                val oldState = existingState.find { it.artifact == artifact.shortString() }?.also {
                    existingState.remove(it)
                }
                launchTransaction {
                    val state = oldState ?: ArtifactStateDao.new {
                        this.artifact = artifact.shortString()
                        this.specVersion = artifact.version ?: ""
                        this.version = ""
                        this.state = ArtifactState.BLANK
                    }
                    Log.debug("Resolving $artifact")
                    val resolvedArtifact = mavenClient.resolve(artifact)
                    Log.debug("Downloading $resolvedArtifact")
                    if (downloader.ensureUpdated(state, resolvedArtifact)) {
                        anyChanges = true
                    }
                }
            }
            val removers = if (existingState.isNotEmpty()) launchTransaction {
                anyChanges = true
                existingState.map {
                    it.delete()
                }
            } else launch {}
            Log.debug("Joining update jobs")

            withContext(Main) {
                val allJobs = downloaders + removers
                val total = allJobs.size
                var complete = allJobs.count { it.isCompleted }
                val progressBar = ProgressBar()
                while (complete != total) {
                    progressBar.update(complete, total)
                    delay(20)
                    complete = allJobs.count { it.isCompleted }
                }
            }
            downloaders.forEach { it.join() }
            removers?.join()
            Log.debug("Joined downloaders")
            if (anyChanges || states.any { it.state < ArtifactState.DOKKAD }) {
                println("Running dokka...")
                runDokka(config)
                transaction {
                    ArtifactStateDao.all().forEach {
                        it.state = ArtifactState.DOKKAD
                    }
                }
            } else {
                println("No changes, skipping dokka + indexing")
                return@withContext
            }
            println("Indexing files...")
            val imdexer = FileIndexWorker(database)
            imdexer.launchIndexing(korpus, File(config.homeFile, "root"))
        }
        executor.shutdown()
    }

    suspend inline fun CoroutineScope.launchTransaction(
        noinline code: suspend Transaction.() -> Unit
    ): Job = launch {
        newSuspendedTransaction(db = database, statement = code)
    }
}
