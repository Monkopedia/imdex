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

import com.monkopedia.imdex.Korpus
import com.monkopedia.imdex.Korpus.Document
import com.monkopedia.imdex.KorpusManager
import com.monkopedia.imdex.KorpusManager.DataType.ARTIFACT
import com.monkopedia.imdex.KorpusManager.DataType.BOOLEAN
import com.monkopedia.imdex.KorpusManager.DataType.INT
import com.monkopedia.imdex.KorpusManager.DataType.PATH
import com.monkopedia.imdex.KorpusManager.DataType.STRING
import com.monkopedia.imdex.KorpusManager.KorpusDataType
import com.monkopedia.imdex.KorpusManager.KorpusKeyInfo
import com.monkopedia.imdex.KorpusManager.KorpusType
import com.monkopedia.imdex.Scriptorium.KorpusInfo
import com.monkopedia.ksrpc.Service
import java.io.File
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class KorpusManagerService(
    private val config: Config,
    private val scriptorium: ScriptoriumService
) : Service(), KorpusManager {

    override suspend fun createType(type: KorpusType): Unit = newSuspendedTransaction {
        KorpusTypeDao.new {
            this.type = type.type
            this.config = type
        }
    }

    override suspend fun updateType(type: KorpusType): Unit = newSuspendedTransaction {
        KorpusDao.all().filter {
            it.config.type == type.type
        }.forEach {
            try {
                type.validate(it.config)
            } catch (t: IllegalArgumentException) {
                throw IllegalArgumentException("${it.config.id} is not compatible with new config")
            }
        }
        val dao = KorpusTypeDao.find {
            TypeTable.type eq type.type
        }.singleOrNull() ?: throw IllegalArgumentException("Can't find type ${type.type}")
        dao.config = type
    }

    override suspend fun getKorpusTypes(u: Unit): List<KorpusType> = newSuspendedTransaction {
        KorpusTypeDao.all().map {
            it.config
        }
    }

    override suspend fun getKorpusType(type: String): KorpusType? = newSuspendedTransaction {
        KorpusTypeDao.find {
            TypeTable.type eq type
        }.singleOrNull()?.config
    }

    override suspend fun createKorpus(korpus: KorpusManager.CreateKorpus): String {
        return newSuspendedTransaction(db = StateDatabase.database) {
            val newItem = KorpusDao.new { }
            commit()
            val id = newItem.id.value.toString()
            newItem.config = KorpusInfo(
                id,
                korpus.type,
                korpus.config
            ).ensureValid()
            id
        }.also {
            scriptorium.korpus(it).rootDocument(Unit).file.mkdirs()
        }
    }

    override suspend fun updateKorpus(korpus: KorpusManager.UpdateKorpus): String {
        return newSuspendedTransaction(db = StateDatabase.database) {
            val k = KorpusDao.findById(korpus.id.toLong())
                ?: throw IllegalArgumentException("Can't find ${korpus.id}")
            k.config = k.config.copy(config = korpus.config).ensureValid()

            k
        }.id.toString()
    }

    override suspend fun deleteKorpus(id: String): String {
        newSuspendedTransaction(db = StateDatabase.database) {
            KorpusDao.findById(id.toLong())?.delete()
                ?: throw IllegalArgumentException("Can't find $id")
        }
        return id
    }

    private fun KorpusInfo.ensureValid(): KorpusInfo {
        var ret = this
        if (!ret.config.containsKey(Korpus.ID_KEY)) {
            ret = ret.copy(
                config = ret.config.toMutableMap().also {
                    it[Korpus.ID_KEY] = id
                }
            )
        }
        if (!ret.config.containsKey(Korpus.DATA_PATH_KEY)) {
            ret = ret.copy(
                config = ret.config.toMutableMap().also {
                    it[Korpus.DATA_PATH_KEY] =
                        File(this@KorpusManagerService.config.homeFile, "korpus/$id").absolutePath
                }
            )
        }
        val dao = KorpusTypeDao.find {
            TypeTable.type eq type
        }.singleOrNull() ?: throw IllegalArgumentException("Can't find type $type")
        dao.config.validate(this)
        return ret
    }

    val Document.file: File
        get() = File(scriptorium.target, path)
}

fun KorpusType.validate(info: KorpusInfo) {
    if (type != info.type) {
        throw IllegalArgumentException("${info.id}'s type ${info.type} does not match type $type")
    }
    for ((k, v) in info.config) {
        val typeInfo = findTypeInfo(k)
            ?: throw IllegalArgumentException("$k is not configured for type $type")
        try {
            typeInfo.type.validate(v)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("${info.id}:$k is not compatible with config $info", e)
        }
    }
}

private val deserializer = ListSerializer(String.serializer())

private fun KorpusDataType.validate(v: String) {
    type?.let { t ->
        when (t) {
            PATH -> {
                // Nothing to check here yet.
            }
            ARTIFACT -> {
                require(v.split(":").size in 2..3) {
                    "$v is not a valid artifact"
                }
            }
            STRING -> {
                // Nothing to check here.
            }
            INT -> {
                v.toIntOrNull()
                    ?: throw IllegalArgumentException("$v cannot be interpretted as an Int")
            }
            BOOLEAN -> {
                require(v.toLowerCase() == "false" || v.toLowerCase() == "true") {
                    "$v cannot be interpretted as a Bool"
                }
            }
            else -> throw IllegalArgumentException("Unsupported type $t")
        }
    }
    listOf?.let { listType ->
        val list = Json.decodeFromString(deserializer, v)
        list.forEach {
            listType.validate(it)
        }
    }
}

fun KorpusType.findTypeInfo(key: String): KorpusKeyInfo? {
    return keys.find { it.key == key }
}
