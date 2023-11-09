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

@Serializable
data class KorpusInfo(
    val id: String,
    val type: String,
    val config: Map<String, String>
) {
    companion object {
        const val LABEL = "imdex.label"
    }
}

enum class Platform {
    WEB,
    CMD
}

@KsService
interface Scriptorium : RpcService {

    @KsMethod("/manage")
    suspend fun korpusManager(u: Unit): KorpusManager

    @KsMethod("/profiles")
    suspend fun profileManager(u: Unit): ProfileManager

    @KsMethod("/get")
    suspend fun getKorpii(u: Unit): List<KorpusInfo>

    @KsMethod("/korpus")
    suspend fun korpus(id: String): Korpus

    @KsMethod("/imdex")
    suspend fun imdex(profile: Int): Imdex
}
