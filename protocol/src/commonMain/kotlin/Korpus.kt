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

const val ID_KEY = "korpus.id"
const val DATA_PATH_KEY = "korpus.path"

@Serializable
data class Document(
    val path: String
) {
    constructor(parent: Document, name: String) : this("${parent.path}$SEPARATOR$name")

    val parent: Document
        get() {
            val segments = path.split(SEPARATOR)
            if (segments.size == 1) {
                throw IllegalArgumentException("Root file has no parent")
            }
            return Document(
                segments.toMutableList().also { it.removeLast() }.joinToString(SEPARATOR)
            )
        }

    val korpus: String
        get() {
            require(this != ROOT) {
                "Root is not part of any korpus"
            }
            return path.split(SEPARATOR).first { it.isNotEmpty() }
        }

    val name: String
        get() = path.split(SEPARATOR).last()

    companion object {
        const val SEPARATOR = "/"
        val ROOT = Document("")
    }
}

@Serializable
enum class DocumentType {
    FOLDER, MARKDOWN, CHAT
}

@Serializable
data class DocumentMetadata(
    val type: DocumentType,
    val hash: String,
    val label: String
)

@Serializable
data class DocumentContent(
    val document: Document,
    val metadata: DocumentMetadata,
    val content: String
)

@Serializable
data class DocumentProperties(
    val document: Document,
    val properties: Map<String, String>
)

@Serializable
data class DocumentSection(
    val document: Document,
    val sections: List<Pair<Int, Int>>
)

@Serializable
data class DocumentSectionContent(
    val documentSection: DocumentSection,
    val documentType: DocumentMetadata,
    val content: String,
    val parsed: ImdexNode?
)

@Serializable
data class ResolveLinkRequest(
    val document: Document,
    val link: String,
    val position: String? = null
)

@Serializable
data class DocumentLink(
    val document: Document,
    val scrollPosition: String? = null
)

@KsService
interface Korpus : RpcService {

    /**
     * The document holding all other documents in this korpus.
     */
    @KsMethod("/root")
    suspend fun rootDocument(u: Unit = Unit): Document

    /**
     * Add a new item to this korpus, won't be included in indexing until [updateIndex] is called.
     */
    @KsMethod("/create")
    suspend fun createDocument(document: DocumentContent): DocumentMetadata

    /**
     * Update properties for one item in this korpus, won't be included in indexing
     * until [updateIndex] is called.
     */
    @KsMethod("/properties")
    suspend fun updateProperties(properties: DocumentProperties): DocumentMetadata

    /**
     * Update content for one item in this korpus, won't be included in indexing
     * until [updateIndex] is called.
     */
    @KsMethod("/update")
    suspend fun updateDocument(document: DocumentContent): DocumentMetadata

    /**
     * Delete an item to this korpus, won't be removed from indexing until [updateIndex] is called.
     */
    @KsMethod("/delete")
    suspend fun deleteDocument(document: Document): DocumentMetadata

    /**
     * Commit any changes to this korpus to the index.
     */
    @KsMethod("/index")
    suspend fun updateIndex(u: Unit = Unit)

    /**
     * Update all content in this korpus in the index.
     */
    @KsMethod("/fullIndex")
    suspend fun fullIndex(u: Unit = Unit)
}
