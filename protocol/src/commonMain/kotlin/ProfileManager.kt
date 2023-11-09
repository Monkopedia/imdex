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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@Serializable
data class ProfileInfo(
    var id: Int,
    var label: String
)

@KsService
interface ProfileManager : RpcService {

    @KsMethod("/profile")
    suspend fun profile(id: Int): Profile

    @KsMethod("/info")
    suspend fun getProfileInfo(id: Int): ProfileInfo

    @KsMethod("/infos")
    suspend fun getProfiles(u: Unit): List<ProfileInfo>

    @KsMethod("/create")
    suspend fun createProfile(label: String): ProfileInfo

    @KsMethod("/web_default")
    suspend fun setWebDefault(id: Int)

    @KsMethod("/cmd_default")
    suspend fun setCmdDefault(id: Int)
}

const val GLOBAL = 0
const val DEFAULT_WEB = -1
const val DEFAULT_CMD = -2

@Serializable
data class ProfileValue(
    var key: String,
    var value: String?
)

@KsService
interface Profile : RpcService {

    @KsMethod("/label")
    suspend fun getLabel(u: Unit): String

    @KsMethod("/id")
    suspend fun getId(u: Unit): Int

    @KsMethod("/get")
    suspend fun get(key: String): ProfileValue

    @KsMethod("/set")
    suspend fun set(value: ProfileValue): Unit
}

val PROFILE =
    StringKey("profile_name").withDefault("New Profile")
val ENABLED_KORPII =
    ListSerializer(String.serializer()).configKey("korpii").withDefault(emptyList())

suspend fun <T> Profile.get(key: TypedKey<T>): T {
    return key.toValue(get(key.key).value)
}

suspend fun <T> Profile.set(key: TypedKey<T>, value: T) {
    set(ProfileValue(key.key, key.fromValue(value)))
}

sealed class TypedKey<T>(val key: String) {
    abstract fun fromValue(v: T): String?
    abstract fun toValue(v: String?): T
}

fun <T> TypedKey<T?>.withDefault(default: T) = DefaultKey(this, default)

class DefaultKey<T>(val baseKey: TypedKey<T?>, val default: T) : TypedKey<T>(baseKey.key) {
    override fun fromValue(v: T): String? = baseKey.fromValue(v)
    override fun toValue(v: String?): T = v?.let { baseKey.toValue(it) } ?: default
}

fun stringKey(key: String) = StringKey(key)

class StringKey(key: String) : TypedKey<String?>(key) {
    override fun fromValue(v: String?): String? = v
    override fun toValue(v: String?): String? = v
}

fun booleanKey(key: String) = BooleanKey(key)

class BooleanKey(key: String) : TypedKey<Boolean?>(key) {
    override fun fromValue(v: Boolean?): String? = v?.toString()
    override fun toValue(v: String?): Boolean? = v?.let { it.toBoolean() }
}

fun intKey(key: String) = IntKey(key)

class IntKey(key: String) : TypedKey<Int?>(key) {
    override fun fromValue(v: Int?): String? = v?.toString()
    override fun toValue(v: String?): Int? = v?.let { it.toInt() }
}

fun <T> KSerializer<T>.configKey(key: String): JsonKey<T> = JsonKey(key, this)

class JsonKey<T>(key: String, val serializer: KSerializer<T>) : TypedKey<T?>(key) {
    override fun fromValue(v: T?): String? = v?.let { Json.encodeToString(serializer, it) }
    override fun toValue(v: String?): T? = v?.let { Json.decodeFromString(serializer, it) }
}
