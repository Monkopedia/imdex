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

import com.monkopedia.imdex.Document as KDocument
import com.monkopedia.imdex.DocumentContent
import com.monkopedia.imdex.DocumentMetadata
import com.monkopedia.imdex.DocumentSection
import com.monkopedia.imdex.DocumentType.MARKDOWN
import com.monkopedia.imdex.ImdexNode
import com.monkopedia.imdex.ImdexNodeType.BULLET
import com.monkopedia.imdex.ImdexNodeType.FENCED_CODE
import com.monkopedia.imdex.ImdexNodeType.HEADING
import com.monkopedia.imdex.ImdexNodeType.PARAGRAPH
import com.monkopedia.imdex.ImdexNodeType.ROOT
import com.monkopedia.imdex.ImdexNodeType.TABLE_CONTAINER
import com.monkopedia.imdex.ImdexNodeType.TABLE_DIVIDER_ROW
import com.monkopedia.imdex.ImdexNodeType.TABLE_ROW
import com.monkopedia.imdex.SpanType.USE_DATA
import com.monkopedia.imdex.convert
import com.monkopedia.imdex.parseMarkdown
import java.io.File
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.Directory
import org.apache.lucene.store.MMapDirectory

object DocumentFields {
    const val TEXT_FIELD = "text"
    const val LOCATION_FIELD = "location"
    const val TYPE = "type"
    const val LABEL = "label"
    const val KORPUS_ID_FIELD = "korpus"
    const val DOCUMENT_PATH_FIELD = "document"
}

class IndexWrapper private constructor(index: Directory) {
    private val analyzer = StandardAnalyzer()
    private val config = IndexWriterConfig(analyzer)
    private val writer = IndexWriter(index, config)

    suspend fun clearKorpus(korpus: String) {
        val query: Query = BooleanQuery.Builder().also {
            it.add(
                TermQuery(Term(DocumentFields.KORPUS_ID_FIELD, korpus)),
                BooleanClause.Occur.MUST,
            )
        }.build()
        writer.deleteDocuments(query)
    }

    suspend fun index(changes: Collection<Pair<DocumentContent, IndexType>>) {
        for ((d, type) in changes) {
            if (type != IndexType.CREATE) {
                deleteFile(d)
            }
            if (type != IndexType.DELETE) {
                indexFile(d)
            }
        }
    }

    private suspend fun indexFile(document: DocumentContent) {
        val nodes = convert(parseMarkdown(document.content))
        val document: Collection<Document> =
            toDocuments(document.content, nodes, document.document, document.metadata)
        writer.addDocuments(document)
    }

    private fun deleteFile(document: DocumentContent) {
        val query: Query = BooleanQuery.Builder().also {
            it.add(
                TermQuery(Term(DocumentFields.DOCUMENT_PATH_FIELD, document.document.path)),
                BooleanClause.Occur.MUST,
            )
            it.add(
                TermQuery(Term(DocumentFields.KORPUS_ID_FIELD, document.document.korpus)),
                BooleanClause.Occur.MUST,
            )
        }.build()
        writer.deleteDocuments(query)
    }

    private fun toDocuments(
        content: String,
        markdown: ImdexNode,
        filePath: KDocument,
        metadata: DocumentMetadata,
    ): Collection<Document> {
        return sequence {
            buildString {
                extractIndex(filePath, metadata, content, markdown, mutableListOf(), this)
            }
        }.map { (location, text) ->
            Document().also { document ->
                document.add(
                    StoredField(
                        DocumentFields.LOCATION_FIELD,
                        json.encodeToString(location),
                    ),
                )
                document.add(
                    TextField(
                        DocumentFields.TEXT_FIELD,
                        text,
                        Field.Store.NO,
                    ),
                )
                document.add(
                    StringField(
                        DocumentFields.TYPE,
                        location.metadata.type.name,
                        Field.Store.YES,
                    ),
                )
                document.add(
                    StringField(
                        DocumentFields.LABEL,
                        location.metadata.label,
                        Field.Store.YES,
                    ),
                )
                document.add(
                    StringField(
                        DocumentFields.KORPUS_ID_FIELD,
                        location.document.document.korpus,
                        Field.Store.YES,
                    ),
                )
                document.add(
                    StringField(
                        DocumentFields.DOCUMENT_PATH_FIELD,
                        location.document.document.path,
                        Field.Store.YES,
                    ),
                )
            }
        }.toList()
    }

    private suspend fun SequenceScope<Pair<LocationDescription, String>>.extractIndex(
        filePath: KDocument,
        metadata: DocumentMetadata,
        content: String,
        node: ImdexNode,
        segments: MutableList<Pair<Int, Int>>,
        stringBuilder: StringBuilder,
    ) {
        when (node.type) {
            TABLE_CONTAINER -> {
                val rows = node.children.filter { it.type == TABLE_ROW }.toMutableList()
                val head = rows.removeFirstOrNull()
                val separator = node.children.find { it.type == TABLE_DIVIDER_ROW }
                if (head != null) {
                    val segments = listOfNotNull(
                        head.s to head.e,
                        separator?.let { it.s to it.e },
                    ).toMutableList()
                    val text = buildString {
                        doExtraction(filePath, metadata, content, head, segments, this)
                    }
                    val doc = DocumentSection(filePath, segments)
                    yield(LocationDescription(doc, metadata) to text)
                }
                for (node in rows) {
                    val segments = listOfNotNull(
                        head?.let { it.s to it.e },
                        separator?.let { it.s to it.e },
                        node.s to node.e,
                    ).toMutableList()
                    val text = buildString {
                        doExtraction(filePath, metadata, content, node, segments, this)
                    }
                    val doc = DocumentSection(filePath, segments)
                    yield(LocationDescription(doc, metadata) to text)
                }
            }

            HEADING,
            FENCED_CODE,
            ROOT,
            BULLET,
            PARAGRAPH,
            -> {
                val segments = mutableListOf(node.s to node.e)
                val text = buildString {
                    doExtraction(filePath, metadata, content, node, segments, this)
                }
                val doc = DocumentSection(filePath, segments)
                yield(LocationDescription(doc, metadata) to text)
            }

            else -> {
                doExtraction(filePath, metadata, content, node, segments, stringBuilder)
            }
        }
    }

    private suspend fun SequenceScope<Pair<LocationDescription, String>>.doExtraction(
        filePath: KDocument,
        metadata: DocumentMetadata,
        content: String,
        node: ImdexNode,
        segments: MutableList<Pair<Int, Int>>,
        stringBuilder: StringBuilder,
    ) {
        if (metadata.type == MARKDOWN) {
            extractText(content, node, stringBuilder)
            node.children.forEach {
                extractIndex(filePath, metadata, content, it, segments, stringBuilder)
            }
        }
    }

    private fun extractText(
        content: String,
        node: ImdexNode,
        stringBuilder: StringBuilder,
    ) {
        if (node.spans.isNotEmpty()) {
            node.spans.map {
                if (it.type.contains(USE_DATA)) {
                    stringBuilder.append(it.data!!)
                } else {
                    stringBuilder.append(content.substring(node.s!!, node.e!!))
                }
            }
        } else {
            node.children.forEach { n ->
                extractText(content, n, stringBuilder)
            }
        }
    }

    companion object {
        suspend fun runIndexing(directory: Directory, indexWork: suspend IndexWrapper.() -> Unit) {
            val indexWrapper = IndexWrapper(directory)
            indexWrapper.indexWork()
            indexWrapper.writer.commit()
            indexWrapper.writer.close()
        }
    }
}

object LuceneIndexer {
    private val lock = Mutex()
    private var directory: Directory? = null

    fun directory(config: Config): Directory = directory
        ?: MMapDirectory(
            File(config.homeFile, "lucene").also {
                it.mkdirs()
            }.toPath(),
        ).also {
            this.directory = it
        }

    suspend fun runIndexing(config: Config, indexWork: suspend IndexWrapper.() -> Unit) =
        lock.withLock {
            IndexWrapper.runIndexing(directory(config), indexWork)
        }
}

@Serializable
data class LocationDescription(
    val document: DocumentSection,
    val metadata: DocumentMetadata,
)
