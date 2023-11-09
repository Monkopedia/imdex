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

import com.monkopedia.imdex.Document
import com.monkopedia.imdex.DocumentContent
import com.monkopedia.imdex.DocumentLink
import com.monkopedia.imdex.DocumentMetadata
import com.monkopedia.imdex.DocumentProperties
import com.monkopedia.imdex.DocumentSectionContent
import com.monkopedia.imdex.DocumentType
import com.monkopedia.imdex.Imdex
import com.monkopedia.imdex.ImdexNode
import com.monkopedia.imdex.Query
import com.monkopedia.imdex.QueryResponse
import com.monkopedia.imdex.ResolveLinkRequest
import com.monkopedia.imdex.cell
import com.monkopedia.imdex.convert
import com.monkopedia.imdex.link
import com.monkopedia.imdex.parseMarkdown
import com.monkopedia.imdex.root
import com.monkopedia.imdex.row
import com.monkopedia.imdex.table
import java.io.File
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ImdexService(
    private val config: Config,
    private val profileManager: ProfileManagerService,
    private val profile: Int
) : Imdex {

    val target = File(config.homeFile, "content")

    override suspend fun query(query: Query): QueryResponse =
        newSuspendedTransaction(db = StateDatabase.database) {
            return@newSuspendedTransaction QueryResponse(
                LuceneSearcher(profileManager, profile).findByText(
                    config,
                    query.query,
                    query.maxResults
                ).map {
                    val (sections, metadata) = it
                    val (document, segments) = sections
                    val base = File(target, document.path)
                    val content = base.readText()

                    val contentSection = segments.joinToString("") { (start, end) ->
                        try {
                            content.substring(start, end)
                        } catch (t: Throwable) {
                            ""
                        }
                    }
                    val parsed = convert(parseMarkdown(contentSection))
                    return@map DocumentSectionContent(
                        sections,
                        metadata,
                        contentSection,
                        parsed
                    )
                }
            )
        }

    override suspend fun resolveLink(linkRequest: ResolveLinkRequest): DocumentLink? {
        val (document, link, position) = linkRequest
        val base = File(target, document.path)
        if (!base.exists()) {
            return null
        }
        val linkFile = File(if (base.isDirectory) base else base.parentFile, link)
        if (linkFile.exists()) {
            var path = linkFile.toRelativeString(target)
            if (!path.startsWith("/")) {
                path = "/$path"
            }
            return DocumentLink(Document(path), position)
        }

        return null
    }

    override suspend fun metadata(document: Document): DocumentMetadata {
        val file = File(target, document.path)
        return file.metadata
    }

    override suspend fun properties(document: Document): DocumentProperties {
        return document.props
    }

    override suspend fun fetch(document: Document): DocumentContent {
        val metadata = document.metadata
        if (metadata.type == DocumentType.FOLDER) {
            return DocumentContent(
                document,
                metadata,
                if (document == Document.ROOT) filteredKorpii()
                else document.file.readFolder()
            )
        }
        return DocumentContent(document, metadata, document.file.readText())
    }

    override suspend fun parse(document: Document): ImdexNode {
        val metadata = document.metadata
        if (metadata.type == DocumentType.FOLDER) {
            val base =
                if (document == Document.ROOT) filteredKorpii()
                else document.file.readFolder()
            return root(base) {
                table {
                    row {
                        cell {
                            +"Content"
                        }
                    }
                    splitText("\n").forEach { file ->
                        val (url, label) = file.splitText("\r")
                        val urlText = url.get()
                        row {
                            cell {
                                link(urlText) {
                                    +label
                                }
                            }
                        }
                    }
                }
            }
        }
        return convert(parseMarkdown(document.file))
    }

    private fun File.readFolder(): String {
        return (
            listOf(
                "." to ".",
                ".." to ".."
            ) + list().orEmpty().filter { !it.endsWith(".imdex") && !it.endsWith(".props") }.map {
                it to (File(this, it).metadata.label)
            }.sortedBy { it.second }
            ).joinToString("\n") {
            "${it.first}\r${it.second}"
        }
    }

    private suspend fun filteredKorpii(): String {
        return profileManager.getKorpii(profile).joinToString("\n") {
            "$it\r$it"
        }
    }

    val Document.file: File
        get() = File(target, path)

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
