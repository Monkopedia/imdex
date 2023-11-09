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

import com.monkopedia.Log
import com.monkopedia.debug
import com.monkopedia.imdex.Document
import com.monkopedia.imdex.DocumentContent
import com.monkopedia.imdex.DocumentMetadata
import com.monkopedia.imdex.DocumentProperties
import com.monkopedia.imdex.DocumentType.FOLDER
import com.monkopedia.imdex.DocumentType.MARKDOWN
import com.monkopedia.imdex.Korpus
import com.monkopedia.imdex.KorpusInfo
import com.monkopedia.scriptorium.IndexType.CREATE
import com.monkopedia.scriptorium.IndexType.DELETE
import com.monkopedia.scriptorium.IndexType.UPDATE
import com.monkopedia.warn
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class IndexType {
    CREATE,
    UPDATE,
    DELETE
}

class KorpusService(private val config: Config, private val k: KorpusInfo) : Korpus {
    val target = File(config.homeFile, "content")
    val pendingLock = Mutex()
    val pendingChanges = mutableListOf<Pair<DocumentContent, IndexType>>()

    override suspend fun rootDocument(u: Unit): Document {
        return Document(Document.ROOT, k.id)
    }

    override suspend fun createDocument(content: DocumentContent): DocumentMetadata {
        if (!content.document.parent.file.exists()) {
            createDocument(
                DocumentContent(
                    content.document.parent,
                    DocumentMetadata(FOLDER, "", content.document.parent.name),
                    ""
                )
            )
            if (!content.document.parent.file.exists()) {
                throw IllegalArgumentException(
                    "Parent ${content.document.parent} of ${content.document} does " +
                        "not exist and couldn't be created"
                )
            }
        }
        if (content.document.file.exists() && !content.document.file.isDirectory) {
            throw IllegalArgumentException("${content.document} already exists")
        }

        withContext(Dispatchers.IO) {
            if (content.metadata.type == FOLDER) {
                require(content.document.file.mkdir())
            } else {
                content.document.file.writeText(content.content)
            }
            content.document.metadata = content.metadata.copy(
                hash = content.content.encodeToByteArray().md5sum()
            )
        }
        pendingLock.withLock {
            pendingChanges.add(content to CREATE)
        }
        return content.document.metadata
    }

    override suspend fun updateDocument(content: DocumentContent): DocumentMetadata {
        if (!content.document.parent.file.exists()) {
            throw IllegalArgumentException("Parent ${content.document.parent} does not exist")
        }
        if (!content.document.file.exists()) {
            throw IllegalArgumentException("${content.document} does not exist")
        }
        val newSum = content.content.encodeToByteArray().md5sum()
        val existingMetadata = content.document.metadata
        if (newSum == existingMetadata.hash) {
            return existingMetadata
        }
        withContext(Dispatchers.IO) {
            content.document.file.writeText(content.content)
            content.document.metadata = content.metadata.copy(
                hash = newSum
            )
        }
        pendingLock.withLock {
            pendingChanges.add(content to UPDATE)
        }
        return content.document.metadata
    }

    override suspend fun updateProperties(properties: DocumentProperties): DocumentMetadata {
        if (!properties.document.parent.file.exists()) {
            throw IllegalArgumentException("Parent ${properties.document.parent} does not exist")
        }
        if (!properties.document.file.exists()) {
            throw IllegalArgumentException("${properties.document} does not exist")
        }
        val existingProps = properties.document.props
        val metadata = properties.document.metadata
        if (properties == existingProps) {
            return metadata
        }
        withContext(Dispatchers.IO) {
            properties.document.props = properties
        }
        pendingLock.withLock {
            pendingChanges.add(
                DocumentContent(
                    properties.document, metadata, properties.document.content
                ) to UPDATE
            )
        }
        return metadata
    }

    override suspend fun deleteDocument(document: Document): DocumentMetadata {
        val oldMetadata = try {
            document.metadata
        } catch (t: Throwable) {
            DocumentMetadata(if (document.file.isDirectory) FOLDER else MARKDOWN, "", document.name)
        }

        require(document.file.metadataFile.delete()) {
            "Failed to delete metadata for $document"
        }
        require(document.file.delete()) {
            document.metadata = oldMetadata
            "Failed to delete content for $document"
        }

        pendingLock.withLock {
            pendingChanges.add(DocumentContent(document, oldMetadata, "") to DELETE)
        }
        return oldMetadata
    }

    override suspend fun updateIndex(u: Unit) {
        pendingLock.withLock {
            Log.debug("Running index with ${pendingChanges.size} changes")
            LuceneIndexer.runIndexing(config) {
                index(pendingChanges)
            }
            Log.debug("Finished index with ${pendingChanges.size} changes")
            pendingChanges.clear()
        }
    }

    override suspend fun fullIndex(u: Unit) {
        pendingLock.withLock {
            Log.warn("Performing full index, this may take some time")
            LuceneIndexer.runIndexing(config) {
                clearKorpus(k.id)
                val fileFlow = findAll(Document(Document.ROOT, k.id))
                val docs = mutableListOf<Pair<DocumentContent, IndexType>>()

                fileFlow.collect {
                    docs.add(it to CREATE)
                    if (docs.size == 100) {
                        index(docs)
                        docs.clear()
                    }
                }
                index(docs)
                docs.clear()
            }
        }
    }

    private fun findAll(document: Document) = flow {
        this.findAll(document)
    }

    private suspend fun FlowCollector<DocumentContent>.findAll(document: Document) {
        val metadata = document.metadata
        emit(
            DocumentContent(
                document,
                metadata,
                if (metadata.type != FOLDER) document.file.readText() else ""
            )
        )
        if (metadata.type == FOLDER) {
            for (child in document.file.list()) {
                // TODO: Support files that end with .imdex
                if (child.endsWith(".imdex")) {
                    continue
                }
                findAll(Document(document, child))
            }
        }
    }

    val Document.file: File
        get() = File(target, path)

    val Document.content: String
        get() = if (file.exists()) file.readText() else ""

    var Document.metadata: DocumentMetadata
        get() = file.metadata
        set(value) {
            file.metadata = value
        }

    var Document.props: DocumentProperties
        get() = file.props ?: DocumentProperties(this, emptyMap())
        set(value) {
            file.props = value
        }
}

fun ByteArray.md5sum(): String {
    val bytes = MessageDigest
        .getInstance("MD5")
        .digest(this)
    return bytes.toHex()
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
