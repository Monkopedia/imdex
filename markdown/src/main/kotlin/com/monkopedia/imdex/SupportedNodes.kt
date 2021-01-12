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

import com.monkopedia.imdex.handlers.AutoLinkHandler
import com.monkopedia.imdex.handlers.BlockQuoteHandler
import com.monkopedia.imdex.handlers.BulletListHandler
import com.monkopedia.imdex.handlers.BulletListItemHandler
import com.monkopedia.imdex.handlers.CodeHandler
import com.monkopedia.imdex.handlers.DefinitionListHandler
import com.monkopedia.imdex.handlers.DocumentHandler
import com.monkopedia.imdex.handlers.EmphasisHandler
import com.monkopedia.imdex.handlers.EscapedCharacterHandler
import com.monkopedia.imdex.handlers.FencedCodeBlockHandler
import com.monkopedia.imdex.handlers.FootnoteBlockHandler
import com.monkopedia.imdex.handlers.FootnoteHandler
import com.monkopedia.imdex.handlers.HardLineBreakHandler
import com.monkopedia.imdex.handlers.HeadingHandler
import com.monkopedia.imdex.handlers.HtmlBlockHandler
import com.monkopedia.imdex.handlers.HtmlCommentBlockHandler
import com.monkopedia.imdex.handlers.HtmlEntityHandler
import com.monkopedia.imdex.handlers.HtmlInlineHandler
import com.monkopedia.imdex.handlers.ImageHandler
import com.monkopedia.imdex.handlers.IndentedCodeBlockHandler
import com.monkopedia.imdex.handlers.LinkHandler
import com.monkopedia.imdex.handlers.LinkRefHandler
import com.monkopedia.imdex.handlers.OrderedListHandler
import com.monkopedia.imdex.handlers.OrderedListItemHandler
import com.monkopedia.imdex.handlers.ParagraphHandler
import com.monkopedia.imdex.handlers.SoftLineBreakHandler
import com.monkopedia.imdex.handlers.StrikethroughHandler
import com.monkopedia.imdex.handlers.StrongEmphasisHandler
import com.monkopedia.imdex.handlers.SuperscriptHandler
import com.monkopedia.imdex.handlers.TableBlockHandler
import com.monkopedia.imdex.handlers.TableBodyHandler
import com.monkopedia.imdex.handlers.TableCellHandler
import com.monkopedia.imdex.handlers.TableHeadHandler
import com.monkopedia.imdex.handlers.TableRowHandler
import com.monkopedia.imdex.handlers.TableSeparatorHandler
import com.monkopedia.imdex.handlers.TextBaseHandler
import com.monkopedia.imdex.handlers.TextHandler
import com.monkopedia.imdex.handlers.ThematicBreakHandler
import com.monkopedia.imdex.handlers.TypographicQuotesHandler
import com.monkopedia.imdex.handlers.TypographicSmartsHandler

enum class SupportedNodes(val binder: NodeBinder<*>) {
    AutoLink(
        NodeBinder(
            com.vladsch.flexmark.ast.AutoLink::class,
            AutoLinkHandler
        )
    ),
    BlockQuote(
        NodeBinder(
            com.vladsch.flexmark.ast.BlockQuote::class,
            BlockQuoteHandler
        )
    ),
    BulletList(
        NodeBinder(
            com.vladsch.flexmark.ast.BulletList::class,
            BulletListHandler
        )
    ),
    BulletListItem(
        NodeBinder(
            com.vladsch.flexmark.ast.BulletListItem::class,
            BulletListItemHandler
        )
    ),
    Code(
        NodeBinder(
            com.vladsch.flexmark.ast.Code::class,
            CodeHandler
        )
    ),
    Emphasis(
        NodeBinder(
            com.vladsch.flexmark.ast.Emphasis::class,
            EmphasisHandler
        )
    ),
    FencedCodeBlock(
        NodeBinder(
            com.vladsch.flexmark.ast.FencedCodeBlock::class,
            FencedCodeBlockHandler
        )
    ),
    HardLineBreak(
        NodeBinder(
            com.vladsch.flexmark.ast.HardLineBreak::class,
            HardLineBreakHandler
        )
    ),
    Heading(
        NodeBinder(
            com.vladsch.flexmark.ast.Heading::class,
            HeadingHandler
        )
    ),
    HtmlCommentBlock(
        NodeBinder(
            com.vladsch.flexmark.ast.HtmlCommentBlock::class,
            HtmlCommentBlockHandler
        )
    ),
    HtmlInline(
        NodeBinder(
            com.vladsch.flexmark.ast.HtmlInline::class,
            HtmlInlineHandler
        )
    ),
    HtmlBlock(
        NodeBinder(
            com.vladsch.flexmark.ast.HtmlBlock::class,
            HtmlBlockHandler
        )
    ),
    HtmlEntity(
        NodeBinder(
            com.vladsch.flexmark.ast.HtmlEntity::class,
            HtmlEntityHandler
        )
    ),
    Image(
        NodeBinder(
            com.vladsch.flexmark.ast.Image::class,
            ImageHandler
        )
    ),
    IndentedCodeBlock(
        NodeBinder(
            com.vladsch.flexmark.ast.IndentedCodeBlock::class,
            IndentedCodeBlockHandler
        )
    ),
    Link(
        NodeBinder(
            com.vladsch.flexmark.ast.Link::class,
            LinkHandler
        )
    ),
    LinkRef(
        NodeBinder(
            com.vladsch.flexmark.ast.LinkRef::class,
            LinkRefHandler
        )
    ),
    OrderedList(
        NodeBinder(
            com.vladsch.flexmark.ast.OrderedList::class,
            OrderedListHandler
        )
    ),
    OrderedListItem(
        NodeBinder(
            com.vladsch.flexmark.ast.OrderedListItem::class,
            OrderedListItemHandler
        )
    ),
    Paragraph(
        NodeBinder(
            com.vladsch.flexmark.ast.Paragraph::class,
            ParagraphHandler
        )
    ),
    SoftLineBreak(
        NodeBinder(
            com.vladsch.flexmark.ast.SoftLineBreak::class,
            SoftLineBreakHandler
        )
    ),
    StrongEmphasis(
        NodeBinder(
            com.vladsch.flexmark.ast.StrongEmphasis::class,
            StrongEmphasisHandler
        )
    ),
    Text(
        NodeBinder(
            com.vladsch.flexmark.ast.Text::class,
            TextHandler
        )
    ),
    TextBase(
        NodeBinder(
            com.vladsch.flexmark.ast.TextBase::class,
            TextBaseHandler
        )
    ),
    EscapedCharacter(
        NodeBinder(
            com.vladsch.flexmark.ext.escaped.character.EscapedCharacter::class,
            EscapedCharacterHandler
        )
    ),
    ThematicBreak(
        NodeBinder(
            com.vladsch.flexmark.ast.ThematicBreak::class,
            ThematicBreakHandler
        )
    ),
    Strikethrough(
        NodeBinder(
            com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough::class,
            StrikethroughHandler
        )
    ),
    TableBlock(
        NodeBinder(
            com.vladsch.flexmark.ext.tables.TableBlock::class,
            TableBlockHandler
        )
    ),
    TableBody(
        NodeBinder(
            com.vladsch.flexmark.ext.tables.TableBody::class,
            TableBodyHandler
        )
    ),
    TableCell(
        NodeBinder(
            com.vladsch.flexmark.ext.tables.TableCell::class,
            TableCellHandler
        )
    ),
    TableHead(
        NodeBinder(
            com.vladsch.flexmark.ext.tables.TableHead::class,
            TableHeadHandler
        )
    ),
    TableRow(
        NodeBinder(
            com.vladsch.flexmark.ext.tables.TableRow::class,
            TableRowHandler
        )
    ),
    TableSeparator(
        NodeBinder(
            com.vladsch.flexmark.ext.tables.TableSeparator::class,
            TableSeparatorHandler
        )
    ),
    TypographicQuotes(
        NodeBinder(
            com.vladsch.flexmark.ext.typographic.TypographicQuotes::class,
            TypographicQuotesHandler
        )
    ),
    TypographicSmarts(
        NodeBinder(
            com.vladsch.flexmark.ext.typographic.TypographicSmarts::class,
            TypographicSmartsHandler
        )
    ),
    Document(
        NodeBinder(
            com.vladsch.flexmark.util.ast.Document::class,
            DocumentHandler
        )
    ),
    Superscript(
        NodeBinder(
            com.vladsch.flexmark.ext.superscript.Superscript::class,
            SuperscriptHandler
        )
    ),
    Footnote(
        NodeBinder(
            com.vladsch.flexmark.ext.footnotes.Footnote::class,
            FootnoteHandler
        )
    ),
    FootnoteBlock(
        NodeBinder(
            com.vladsch.flexmark.ext.footnotes.FootnoteBlock::class,
            FootnoteBlockHandler
        )
    ),
    DefinitionList(
        NodeBinder(
            com.vladsch.flexmark.ext.definition.DefinitionList::class,
            DefinitionListHandler
        )
    )
}
