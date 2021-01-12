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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.DefaultMutableThemeStyle
import com.googlecode.lanterna.graphics.ThemeStyle
import com.monkopedia.lanterna.Lanterna.gui

object Style {

    val markdownStyle by lazy {
        gui.theme.getDefinition(Style::class.java)
    }

    const val LINK_STYLE_NAME = "LINK"
    val DEFAULT_LINK_STYLE: ThemeStyle = DefaultMutableThemeStyle(
        TextColor.Factory.fromString("#81c754"),
        TextColor.Factory.fromString("#282828"),
        SGR.BOLD
    )
    val linkStyle by lazy {
        markdownStyle.getCustom(LINK_STYLE_NAME, DEFAULT_LINK_STYLE)
    }

    const val SELECTED_LINK_STYLE_NAME = "LINK_SELECTED"
    val DEFAULT_SELECTED_LINK_STYLE: ThemeStyle = DefaultMutableThemeStyle(
        TextColor.Factory.fromString("#282828"),
        TextColor.Factory.fromString("#81c754"),
        SGR.BOLD
    )
    val selectedLinkStyle by lazy {
        markdownStyle.getCustom(SELECTED_LINK_STYLE_NAME, DEFAULT_SELECTED_LINK_STYLE)
    }

    const val CODE_STYLE_NAME = "CODE_BLOCK"
    val DEFAULT_CODE_STYLE: ThemeStyle by lazy {
        DefaultMutableThemeStyle(
            gui.theme.defaultDefinition.normal.foreground,
            TextColor.Factory.fromString("#282828"),
            *gui.theme.defaultDefinition.normal.sgRs.toTypedArray()
        )
    }
    val codeStyle by lazy {
        markdownStyle.getCustom(CODE_STYLE_NAME, DEFAULT_CODE_STYLE)
    }

    const val ASCII_FONTS_NAME = "ASCII_FONTS"
    const val ASCII_FONTS_DEFAULT = true
    val asciiFontsEnabled by lazy {
        markdownStyle.getBooleanProperty(ASCII_FONTS_NAME, ASCII_FONTS_DEFAULT)
    }

    const val HEADER_PRIMARY_NAME = "HEADER_PRIMARY"
    val DEFAULT_HEADER_PRIMARY: ThemeStyle by lazy {
        DefaultMutableThemeStyle(
            markdownStyle.normal.foreground,
            markdownStyle.normal.background,
            SGR.UNDERLINE,
            SGR.BOLD
        )
    }

    val headerPrimaryStyle by lazy {
        markdownStyle.getCustom(HEADER_PRIMARY_NAME, DEFAULT_HEADER_PRIMARY)
    }

    const val HEADER_SECONDARY_NAME = "HEADER_SECONDARY"
    val DEFAULT_HEADER_SECONDARY: ThemeStyle by lazy {
        DefaultMutableThemeStyle(
            markdownStyle.normal.foreground,
            markdownStyle.normal.background,
            SGR.UNDERLINE
        )
    }

    val headerSecondaryStyle by lazy {
        markdownStyle.getCustom(HEADER_SECONDARY_NAME, DEFAULT_HEADER_SECONDARY)
    }
}
