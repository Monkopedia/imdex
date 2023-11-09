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
data class Query(
    val query: String,
    val maxResults: Int = 10
)

@Serializable
data class QueryResponse(
    val results: List<DocumentSectionContent>
)

@KsService
interface Imdex : RpcService {

    @KsMethod("/query")
    suspend fun query(query: Query): QueryResponse

    @KsMethod("/resolve")
    suspend fun resolveLink(linkRequest: ResolveLinkRequest): DocumentLink?

    @KsMethod("/metadata")
    suspend fun metadata(document: Document): DocumentMetadata

    @KsMethod("/fetch")
    suspend fun fetch(document: Document): DocumentContent

    @KsMethod("/properties")
    suspend fun properties(document: Document): DocumentProperties

    @KsMethod("/parse")
    suspend fun parse(document: Document): ImdexNode
}
