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

import com.monkopedia.imdex.ImdexNodeType.ROOT
import com.vladsch.flexmark.ast.HtmlEntity
import com.vladsch.flexmark.ast.HtmlInline
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.definition.DefinitionExtension
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.ins.InsExtension
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.toc.SimTocExtension
import com.vladsch.flexmark.ext.toc.TocExtension
import com.vladsch.flexmark.ext.typographic.TypographicExtension
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import java.io.File
import kotlin.reflect.KClass
import org.slf4j.LoggerFactory

private const val DEBUG_PARSING = false

private val LOGGER = LoggerFactory.getLogger("NodeHandler")

private var indent = 0

class NodeBinder<T : Node>(val nodeCls: KClass<T>, val handler: NodeHandler<T>) {
    suspend fun onNode(node: Node, state: ParsingState, function: suspend () -> Unit) {
        if (DEBUG_PARSING) LOGGER.info(
            "${Array(indent.coerceAtLeast(0)) { "  " }
                .joinToString("")}Starting parsing for $nodeCls"
        )
        if (DEBUG_PARSING) indent++
        try {
            if (DEBUG_PARSING && node is HtmlEntity) {
                LOGGER.info(
                    "${Array(indent.coerceAtLeast(0)) { "  " }
                        .joinToString("")}Html: ${node.chars}"
                )
            }
            if (DEBUG_PARSING && node is HtmlInline) {
                LOGGER.info(
                    "${Array(indent.coerceAtLeast(0)) { "  " }
                        .joinToString("")}Html inline: ${node.chars}"
                )
            }
            handler.onNode(node as T, state, function)
        } finally {
            if (DEBUG_PARSING) indent--
            if (DEBUG_PARSING) LOGGER.info(
                "${Array(indent.coerceAtLeast(0)) { "  " }
                    .joinToString("")}Ending parsing for $nodeCls"
            )
        }
    }
}

interface NodeHandler<T : Node> {
    suspend fun onNode(node: T, state: ParsingState, handleChildren: suspend () -> Unit)
}

suspend fun convert(node: Node): ImdexNode {
    val state = ParsingState()
    val rootNode = ImdexNode(ROOT, node.startOffset, node.endOffset)
    state.withHolder(rootNode) {
        visitNodes(node, state)
    }
    return rootNode
}

fun parseMarkdown(file: File): Node = parseMarkdown(file.readText())

fun parseMarkdown(text: String): Node {
    val options = MutableDataSet()
    options.set(
        Parser.EXTENSIONS,
        listOf(
            TablesExtension.create(),
            TaskListExtension.create(),
            EscapedCharacterExtension.create(),
            AutolinkExtension.create(),
            AbbreviationExtension.create(),
            DefinitionExtension.create(),
            TypographicExtension.create(),

            WikiLinkExtension.create(),
            StrikethroughSubscriptExtension.create(),
            FootnoteExtension.create(),
            TocExtension.create(),
            SimTocExtension.create(),
            InsExtension.create(),
            SuperscriptExtension.create(),
        )
    )

    val parser: Parser = Parser.builder(options).build()
    return parser.parse(text)
}

suspend fun visitNodes(node: Node, state: ParsingState) {
    val nodeCls = node::class
    val handler = enumValues<SupportedNodes>().find {
        it.binder.nodeCls == nodeCls
    }?.binder ?: throw IllegalArgumentException("Unsupported node $nodeCls")
    handler.onNode(node, state) {
        if (node.hasChildren()) {
            node.childIterator.forEach {
                visitNodes(it, state)
            }
        }
    }
}
