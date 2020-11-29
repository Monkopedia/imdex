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

import kotlinx.serialization.decodeFromString
import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.PrefixQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.Directory

class LuceneSearcher(private val profileManager: ProfileManagerService, private val profile: Int) {

    fun findAll(index: Directory): List<LocationDescription> {
        val query: Query = MatchAllDocsQuery()
        val documents = executeQuery(index, query, Int.MAX_VALUE)
        return documents.map(::toLocationDescription)
    }

    suspend fun findByText(config: Config, text: String, maxResults: Int = 10):
        List<LocationDescription> {
            val index = LuceneIndexer.directory(config)
            val words = text.split(" ").toMutableList()
            val last = words.removeLast()
            val query: Query = BooleanQuery.Builder().also {
                for (word in words) {
                    it.add(
                        TermQuery(Term(DocumentFields.TEXT_FIELD, word)),
                        BooleanClause.Occur.SHOULD
                    )
                }
                it.add(
                    PrefixQuery(Term(DocumentFields.TEXT_FIELD, last)),
                    BooleanClause.Occur.SHOULD
                )
                it.add(
                    BooleanQuery.Builder().also { korpusReq ->
                        for (korpus in profileManager.getKorpii(profile)) {
                            korpusReq.add(
                                PrefixQuery(Term(DocumentFields.KORPUS_ID_FIELD, korpus)),
                                BooleanClause.Occur.SHOULD
                            )
                        }
                    }.build(),
                    BooleanClause.Occur.MUST
                )
            }.build()
            val documents = executeQuery(index, query, maxResults)
            return documents.map(::toLocationDescription)
        }

    private fun executeQuery(index: Directory, query: Query, maxResults: Int): List<Document> {
        // val sort = Sort(SortField(DocumentFields.NAME_FIELD, SortField.Type.STRING))
        val reader: IndexReader = DirectoryReader.open(index)
        val searcher = IndexSearcher(reader)
        val topDocs: TopDocs = searcher.search(query, maxResults)
        return topDocs.scoreDocs.map { scoreDoc ->
            searcher.doc(scoreDoc.doc)
        }
    }

    private fun toLocationDescription(document: Document): LocationDescription {
        val locationText = document.getField(DocumentFields.LOCATION_FIELD).stringValue()
        return json.decodeFromString(locationText)
    }
}
