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

import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsService
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

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
            KorpusInfo.LABEL
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

@Serializable
data class UpdateKorpus(
    val id: String,
    val config: Map<String, String>
)

@KsService
interface KorpusManager : RpcService {

    @KsMethod("/createType")
    suspend fun createType(type: KorpusType)

    @KsMethod("/updateType")
    suspend fun updateType(type: KorpusType)

    @KsMethod("/types")
    suspend fun getKorpusTypes(u: Unit): List<KorpusType>

    @KsMethod("/type")
    suspend fun getKorpusType(type: String): KorpusType?

    @KsMethod("/create")
    suspend fun createKorpus(korpus: CreateKorpus): String

    @KsMethod("/update")
    suspend fun updateKorpus(korpus: UpdateKorpus): String

    @KsMethod("/delete")
    suspend fun deleteKorpus(id: String): String
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

inline val KorpusInfo.label get() = config[KorpusInfo.LABEL] ?: id

inline fun KorpusInfo.boolean(key: KorpusKeyInfo): Boolean {
    require(key.type.type == DataType.BOOLEAN) {
        "$key is not a boolean"
    }
    return config[key.key].toBoolean()
}

inline fun KorpusInfo.int(key: KorpusKeyInfo): Int {
    require(key.type.type == DataType.INT) {
        "$key is not a boolean"
    }
    return config[key.key]?.toIntOrNull() ?: 0
}

inline fun KorpusInfo.string(key: KorpusKeyInfo): String {
    require(key.type.type == DataType.STRING) {
        "$key is not a boolean"
    }
    return config[key.key] ?: ""
}

inline fun KorpusInfo.path(key: KorpusKeyInfo): String {
    require(key.type.type == DataType.PATH) {
        "$key is not a boolean"
    }
    return config[key.key] ?: ""
}

inline fun KorpusInfo.artifact(key: KorpusKeyInfo): String {
    require(key.type.type == DataType.ARTIFACT) {
        "$key is not a boolean"
    }
    return config[key.key] ?: ""
}

inline fun KorpusInfo.booleanList(key: KorpusKeyInfo): List<Boolean> {
    require(key.type.listOf?.type == DataType.BOOLEAN) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}

inline fun KorpusInfo.intList(key: KorpusKeyInfo): List<Int> {
    require(key.type.listOf?.type == DataType.INT) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}

inline fun KorpusInfo.stringList(key: KorpusKeyInfo): List<String> {
    require(key.type.listOf?.type == DataType.STRING) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}

inline fun KorpusInfo.pathList(key: KorpusKeyInfo): List<String> {
    require(key.type.listOf?.type == DataType.PATH) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}

inline fun KorpusInfo.artifactList(key: KorpusKeyInfo): List<String> {
    require(key.type.listOf?.type == DataType.ARTIFACT) {
        "$key is not a boolean"
    }
    return Json.decodeFromString(config[key.key] ?: "[]")
}
