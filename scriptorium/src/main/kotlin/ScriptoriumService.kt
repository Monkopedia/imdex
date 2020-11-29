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

import com.monkopedia.imdex.Imdex
import com.monkopedia.imdex.Korpus
import com.monkopedia.imdex.Korpus.Document
import com.monkopedia.imdex.Korpus.DocumentMetadata
import com.monkopedia.imdex.KorpusManager
import com.monkopedia.imdex.ProfileManager
import com.monkopedia.imdex.Scriptorium
import com.monkopedia.imdex.Scriptorium.KorpusInfo
import com.monkopedia.ksrpc.Service
import java.io.File
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ScriptoriumService(private val config: Config) : Service(), Scriptorium {

    val target = File(config.homeFile, "content")

    init {
        StateDatabase.init(config)
    }

    private val korpusManager by lazy {
        KorpusManagerService(config, this)
    }
    private val profileManager by lazy {
        ProfileManagerService(this)
    }

    override suspend fun korpusManager(u: Unit): KorpusManager = korpusManager
    override suspend fun profileManager(u: Unit): ProfileManager = profileManager

    override suspend fun imdex(profile: Int): Imdex {
        return ImdexService(config, profileManager, profile)
    }

    override suspend fun getKorpii(u: Unit): List<KorpusInfo> {
        return newSuspendedTransaction(db = StateDatabase.database) {
            KorpusDao.all().map {
                KorpusInfo(
                    it.config.id,
                    it.config.type,
                    it.config.config
                )
            }
        }
    }

    override suspend fun korpus(id: String): Korpus = newSuspendedTransaction {
        val k = KorpusDao.findById(id.toLong())
            ?: throw IllegalArgumentException("Can't find $id")
        val kInfo = KorpusInfo(
            k.config.id,
            k.config.type,
            k.config.config
        )
        return@newSuspendedTransaction KorpusService(config, kInfo)
    }

    val Document.file: File
        get() = File(target, path)

    var Document.metadata: DocumentMetadata
        get() = file.metadata
        set(value) {
            file.metadata = value
        }
}

fun File.readFolder(): String {
    return (listOf(".", "..") + list().filter { !it.endsWith(".imdex") }).joinToString("\n")
}

val File.metadataFile: File
    get() = File(parentFile, "$name.imdex").also {
        if (isDirectory && !it.exists()) {
            it.writeText(
                Json.encodeToString(
                    DocumentMetadata.serializer(),
                    DocumentMetadata(Korpus.DocumentType.FOLDER, "", it.name)
                )
            )
        }
    }

var File.metadata: DocumentMetadata
    get() = Json.decodeFromString(DocumentMetadata.serializer(), metadataFile.readText())
    set(value) {
        metadataFile.writeText(
            Json.encodeToString(DocumentMetadata.serializer(), value)
        )
    }
