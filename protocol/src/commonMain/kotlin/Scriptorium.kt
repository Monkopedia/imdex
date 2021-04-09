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

import com.monkopedia.ksrpc.RpcObject
import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.RpcServiceChannel
import com.monkopedia.ksrpc.map
import com.monkopedia.ksrpc.service
import kotlinx.serialization.Serializable

interface Scriptorium : RpcService {

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

    suspend fun korpusManager(u: Unit): KorpusManager = service("/manage", KorpusManager, u)

    enum class Platform {
        WEB,
        CMD
    }

    suspend fun profileManager(u: Unit): ProfileManager =
        service("/profiles", ProfileManager, u)

    suspend fun getKorpii(u: Unit): List<KorpusInfo> = map("/get", u)

    suspend fun korpus(id: String): Korpus = service("/korpus", Korpus, id)

    suspend fun imdex(profile: Int): Imdex = service("/imdex", Imdex, profile)

    private class ScriptoriumStub(private val channel: RpcServiceChannel) :
        Scriptorium,
        RpcService by channel

    companion object : RpcObject<Scriptorium>(Scriptorium::class, ::ScriptoriumStub)
}
