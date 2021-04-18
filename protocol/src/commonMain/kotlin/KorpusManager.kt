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
package com.monkopedia.imdex

import com.monkopedia.imdex.KorpusManager.KorpusType
import com.monkopedia.ksrpc.RpcObject
import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.RpcServiceChannel
import com.monkopedia.ksrpc.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.json.Json

interface KorpusManager : RpcService {

    enum class DataType {
        PATH,
        ARTIFACT,
        STRING,
        INT,
        BOOLEAN;

        val type: KorpusDataType
            get() = KorpusDataType(this)
    }

    @Serializable
    data class KorpusDataType(
        val type: DataType? = null,
        val listOf: KorpusDataType? = null
    ) {
        init {
            require(type != null || listOf != null) {
                "Invalid type $type $listOf"
            }
        }
    }

    @Serializable
    data class KorpusKeyInfo(
        val displayName: String,
        val type: KorpusDataType,
        val key: String
    ) {
        companion object {
            val LABEL = KorpusKeyInfo(
                "Label",
                DataType.STRING.type,
                Scriptorium.KorpusInfo.LABEL
            )
            val EDITABLE = KorpusKeyInfo(
                "Editable",
                DataType.BOOLEAN.type,
                "imdex.mutable"
            )
        }
    }

    @Serializable
    data class KorpusType(
        val label: String = "Generic type",
        val type: String,
        val keys: List<KorpusKeyInfo>
    )

    @Serializable
    data class CreateKorpus(
        val type: String,
        val config: Map<String, String>
    ) {
        companion object {
            const val DEFAULT_TYPE = "imdex.korpus.default"
        }
    }

    suspend fun createType(type: KorpusType): Unit = map("/createType", type)
    suspend fun updateType(type: KorpusType): Unit = map("/updateType", type)

    suspend fun getKorpusTypes(u: Unit): List<KorpusType> = map("/types", u)

    suspend fun getKorpusType(type: String): KorpusType? = map("/type", type)

    suspend fun createKorpus(korpus: CreateKorpus): String = map("/create", korpus)

    @Serializable
    data class UpdateKorpus(
        val id: String,
        val config: Map<String, String>
    )

    suspend fun updateKorpus(korpus: UpdateKorpus): String = map("/update", korpus)

    suspend fun deleteKorpus(id: String): String = map("/delete", id)

    private class KorpusManagerStub(private val channel: RpcServiceChannel) :
        KorpusManager, RpcService by channel

    companion object : RpcObject<KorpusManager>(KorpusManager::class, ::KorpusManagerStub)
}

suspend fun KorpusManager.ensureConfig(type: KorpusType) {
    val existing = getKorpusType(type.type)
    if (existing != null) {
        if (existing != type) {
            updateType(type)
        }
    } else {
        createType(type)
    }
}

inline val Scriptorium.KorpusInfo.label get() = config[Scriptorium.KorpusInfo.LABEL] ?: id

inline fun Scriptorium.KorpusInfo.boolean(key: KorpusManager.KorpusKeyInfo): Boolean {
    require (key.type.type == KorpusManager.DataType.BOOLEAN) {
        "$key is not a boolean"
    }
    return config[key.key].toBoolean()
}

inline fun Scriptorium.KorpusInfo.int(key: KorpusManager.KorpusKeyInfo): Int {
    require (key.type.type == KorpusManager.DataType.INT) {
        "$key is not a boolean"
    }
    return config[key.key]?.toIntOrNull() ?: 0
}

inline fun Scriptorium.KorpusInfo.string(key: KorpusManager.KorpusKeyInfo): String {
    require (key.type.type == KorpusManager.DataType.STRING) {
        "$key is not a boolean"
    }
    return config[key.key] ?: ""
}

inline fun Scriptorium.KorpusInfo.path(key: KorpusManager.KorpusKeyInfo): String {
    require (key.type.type == KorpusManager.DataType.PATH) {
        "$key is not a boolean"
    }
    return config[key.key] ?: ""
}

inline fun Scriptorium.KorpusInfo.artifact(key: KorpusManager.KorpusKeyInfo): String {
    require (key.type.type == KorpusManager.DataType.ARTIFACT) {
        "$key is not a boolean"
    }
    return config[key.key] ?: ""
}

inline fun Scriptorium.KorpusInfo.booleanList(key: KorpusManager.KorpusKeyInfo): List<Boolean> {
    require (key.type.listOf?.type == KorpusManager.DataType.BOOLEAN) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}

inline fun Scriptorium.KorpusInfo.intList(key: KorpusManager.KorpusKeyInfo): List<Int> {
    require (key.type.listOf?.type == KorpusManager.DataType.INT) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}

inline fun Scriptorium.KorpusInfo.stringList(key: KorpusManager.KorpusKeyInfo): List<String> {
    require (key.type.listOf?.type == KorpusManager.DataType.STRING) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}

inline fun Scriptorium.KorpusInfo.pathList(key: KorpusManager.KorpusKeyInfo): List<String> {
    require (key.type.listOf?.type == KorpusManager.DataType.PATH) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}

inline fun Scriptorium.KorpusInfo.artifactList(key: KorpusManager.KorpusKeyInfo): List<String> {
    require (key.type.listOf?.type == KorpusManager.DataType.ARTIFACT) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}
