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

import com.monkopedia.imdex.Korpus.Document
import com.monkopedia.imdex.Korpus.DocumentMetadata
import com.monkopedia.ksrpc.RpcObject
import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.RpcServiceChannel
import com.monkopedia.ksrpc.map
import com.monkopedia.markdown.ImdexNode
import kotlinx.serialization.Serializable

interface Imdex : RpcService {

    @Serializable
    data class Query(
        val query: String,
        val maxResults: Int = 10
    )

    @Serializable
    data class QueryResponse(
        val results: List<Korpus.DocumentSectionContent>
    )

    suspend fun query(query: Query): QueryResponse = map("/query", query)

    suspend fun resolveLink(linkRequest: Korpus.ResolveLinkRequest): Korpus.DocumentLink? =
        map("/resolve", linkRequest)

    suspend fun metadata(document: Document): DocumentMetadata = map("/metadata", document)

    suspend fun fetch(document: Document): Korpus.DocumentContent = map("/fetch", document)
    suspend fun properties(document: Document): Korpus.DocumentProperties = map("/properties", document)

    suspend fun parse(document: Document): ImdexNode = map("/parse", document)

    private class ImdexStub(private val channel: RpcServiceChannel) : Imdex, RpcService by channel
    companion object : RpcObject<Imdex>(Imdex::class, ::ImdexStub)
}