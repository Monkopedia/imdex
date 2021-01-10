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

import com.monkopedia.imdex.Imdex
import com.monkopedia.imdex.Korpus
import com.monkopedia.ksrpc.Service
import com.monkopedia.markdown.ImdexNode
import com.monkopedia.markdown.cell
import com.monkopedia.markdown.convert
import com.monkopedia.markdown.h1
import com.monkopedia.markdown.link
import com.monkopedia.markdown.parseMarkdown
import com.monkopedia.markdown.root
import com.monkopedia.markdown.row
import com.monkopedia.markdown.table
import java.io.File
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ImdexService(
    private val config: Config,
    private val profileManager: ProfileManagerService,
    private val profile: Int
) : Service(), Imdex {

    val target = File(config.homeFile, "content")

    override suspend fun query(query: Imdex.Query): Imdex.QueryResponse =
        newSuspendedTransaction(db = StateDatabase.database) {
            return@newSuspendedTransaction Imdex.QueryResponse(
                LuceneSearcher(profileManager, profile).findByText(
                    config,
                    query.query,
                    query.maxResults
                ).map {
                    val (sections, metadata) = it
                    val (document, segments) = sections
                    val base = File(target, document.path)
                    val content = base.readText()

                    return@map Korpus.DocumentSectionContent(
                        sections,
                        metadata,
                        segments.joinToString("") { (start, end) ->
                            content.substring(start, end)
                        }
                    )
                }
            )
        }

    override suspend fun resolveLink(linkRequest: Korpus.ResolveLinkRequest): Korpus.DocumentLink? {
        val (document, link, position) = linkRequest
        val base = File(target, document.path)
        if (!base.exists()) {
            return null
        }
        val linkFile = File(if (base.isDirectory) base else base.parentFile, link)
        if (linkFile.exists()) {
            val path = linkFile.toRelativeString(target)
            return Korpus.DocumentLink(Korpus.Document(path), position)
        }

        return null
    }

    override suspend fun metadata(document: Korpus.Document): Korpus.DocumentMetadata {
        val file = File(target, document.path)
        return file.metadata
    }

    override suspend fun properties(document: Korpus.Document): Korpus.DocumentProperties {
        return document.props
    }

    override suspend fun fetch(document: Korpus.Document): Korpus.DocumentContent {
        val metadata = document.metadata
        if (metadata.type == Korpus.DocumentType.FOLDER) {
            return Korpus.DocumentContent(
                document,
                metadata,
                if (document == Korpus.Document.ROOT) filteredKorpii()
                else document.file.readFolder()
            )
        }
        return Korpus.DocumentContent(document, metadata, document.file.readText())
    }

    override suspend fun parse(document: Korpus.Document): ImdexNode {
        val metadata = document.metadata
        if (metadata.type == Korpus.DocumentType.FOLDER) {
            val base =
                if (document == Korpus.Document.ROOT) filteredKorpii()
                else document.file.readFolder()
            return root(base) {
                h1 {
                    +document.name
                }
                table {
                    row {
                        cell {
                            +"Content"
                        }
                    }
                    splitText("\n").forEach { file ->
                        val text = file.get()
                        row {
                            cell {
                                link(text) {
                                    +file
                                }
                            }
                        }
                    }
                }
            }
        }
        return convert(parseMarkdown(document.file))
    }

    private suspend fun filteredKorpii(): String {
        return profileManager.getKorpii(profile).joinToString("\n")
    }

    val Korpus.Document.file: File
        get() = File(target, path)

    var Korpus.Document.metadata: Korpus.DocumentMetadata
        get() = file.metadata
        set(value) {
            file.metadata = value
        }

    var Korpus.Document.props: Korpus.DocumentProperties
        get() = file.props ?: Korpus.DocumentProperties(this, emptyMap())
        set(value) {
            file.props = value
        }
}
