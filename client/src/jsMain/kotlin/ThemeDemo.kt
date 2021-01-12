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

import com.ccfraser.muirwik.components.MAppBarColor
import com.ccfraser.muirwik.components.MAppBarPosition
import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.MOptionColor
import com.ccfraser.muirwik.components.MTextFieldColor
import com.ccfraser.muirwik.components.MTypographyColor
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.ToolbarVariant
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.MIconEdge
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mFab
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.card.mCard
import com.ccfraser.muirwik.components.card.mCardActions
import com.ccfraser.muirwik.components.card.mCardHeader
import com.ccfraser.muirwik.components.color
import com.ccfraser.muirwik.components.form.MFormControlMargin
import com.ccfraser.muirwik.components.list.mListItemIcon
import com.ccfraser.muirwik.components.mAppBar
import com.ccfraser.muirwik.components.mCheckbox
import com.ccfraser.muirwik.components.mRadio
import com.ccfraser.muirwik.components.mSnackbar
import com.ccfraser.muirwik.components.mSwitch
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.mToolbar
import com.ccfraser.muirwik.components.mToolbarTitle
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ccfraser.muirwik.components.menu.mMenuList
import com.ccfraser.muirwik.components.styles.Theme
import kotlinext.js.js
import kotlinx.css.Align
import kotlinx.css.BoxSizing
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.Position
import kotlinx.css.alignContent
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.boxSizing
import kotlinx.css.display
import kotlinx.css.flex
import kotlinx.css.flexDirection
import kotlinx.css.fontFamily
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.paddingBottom
import kotlinx.css.paddingLeft
import kotlinx.css.paddingRight
import kotlinx.css.paddingTop
import kotlinx.css.position
import kotlinx.css.properties.transform
import kotlinx.css.properties.translateX
import kotlinx.css.properties.translateY
import kotlinx.css.width
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.unsafe
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.setState
import styled.StyleSheet
import styled.StyledBuilder
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledSpan

external interface ThemeDemoState : RState {
    var firstChecked: Boolean?
    var secondChecked: Boolean?
    var firstSelected: Boolean?
    var firstSwitched: Boolean?
    var secondSwitched: Boolean?
}

class ThemeDemo : RComponent<RProps, ThemeDemoState>() {
    val altBuilder = RBuilder()

    private object ComponentStyles : StyleSheet("theme_demo", isStatic = true) {

        val flex by css {
            flex(1.0)
            boxSizing = BoxSizing.borderBox
        }

        val column by css {
            display = Display.flex
            flexDirection = FlexDirection.column
        }
        val row by css {
            display = Display.flex
            flexDirection = FlexDirection.row
        }
        val alignCenter by css {
            alignItems = Align.center
        }

        val sectionColumn by css(column, alignCenter) {
            marginLeft = LinearDimension("16px")
            marginRight = LinearDimension("16px")
        }
        val mainArea by css {
            display = Display.flex
            alignItems = Align.center
            boxSizing = BoxSizing.borderBox
            fontFamily = "Roboto"
            margin(LinearDimension("64px"), LinearDimension.auto)
            width = LinearDimension("1664px")
            this.media("all and (max-width: 880px)") {
                flexDirection = FlexDirection.column
            }
            this.media("all and (max-width: 1712px)") {
                margin(LinearDimension("32px"), LinearDimension.auto)
                width = LinearDimension("832px")
            }
        }
        val columnHolder by css {
            display = Display.flex
            this.media("all and (max-width: 1712px)") {
                flexDirection = FlexDirection.column
            }
        }

        val toolbar by css(column) {
            backgroundColor = Color("#EEEEEE")
            width = LinearDimension("400px")
            height = LinearDimension("620px")
            padding(LinearDimension("30px"))
        }

        val smallSection by css(column, alignCenter) {
            backgroundColor = Color("#EEEEEE")
            width = LinearDimension("400px")
            height = LinearDimension("132px")
            alignContent = Align.center
            alignItems = Align.center
            display = Display.flex
            padding(LinearDimension("16px"))
        }

        val largeSection by css(column, alignCenter) {
            backgroundColor = Color("#EEEEEE")
            width = LinearDimension("400px")
            height = LinearDimension("400px")
            alignContent = Align.center
            alignItems = Align.center
            display = Display.flex
            padding(LinearDimension("16px"))
        }
        val largeRowSection by css(row, alignCenter) {
            backgroundColor = Color("#EEEEEE")
            width = LinearDimension("400px")
            height = LinearDimension("400px")
        }
        val card by css {
            borderRadius = LinearDimension("2px")
            width = LinearDimension("262px")
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css(ComponentStyles.mainArea)
            styledDiv {
                css(ComponentStyles.columnHolder)
                styledDiv {
                    css(ComponentStyles.sectionColumn)
                    buttons(theme)
                    flex()
                    selection(theme)
                }
                styledDiv {
                    css(ComponentStyles.sectionColumn)
                    switchesAndSlides(theme)
                    flex()
                    cards(theme)
                }
                styledDiv {
                    css(ComponentStyles.sectionColumn)
                    menu(theme)
                    flex()
                    textFields(theme)
                }
                styledDiv {
                    css(ComponentStyles.sectionColumn)
                    toolbar(theme)
                }
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.toolbar(theme: Theme) {
        mTypography(
            "Tool Bar",
            variant = MTypographyVariant.subtitle2,
            color = MTypographyColor.textSecondary
        )
        styledDiv {
            css(ComponentStyles.toolbar)
            column {
                css {
                    width = LinearDimension("340px")
                    height = LinearDimension("560px")
                }

                styledDiv {
                    css {
                        height = LinearDimension("24px")
                        width = LinearDimension("100%")
                        backgroundColor =
                            Color(theme.palette.primary.dark)
                    }
                }
                mAppBar(
                    color = MAppBarColor.primary,
                    position = MAppBarPosition.static
                ) {
                    mToolbar(variant = ToolbarVariant.dense) {
                        mIconButton(
                            "menu",
                            edge = MIconEdge.start,
                            color = MColor.inherit
                        ) {
                        }
                        mToolbarTitle("Title")
                        mIconButton(
                            "search",
                            edge = MIconEdge.end,
                            color = MColor.inherit
                        )
                        mIconButton(
                            "more_vert",
                            color = MColor.inherit,
                            edge = MIconEdge.end
                        )
                    }
                }
                styledDiv {
                    css {
                        height = LinearDimension("220px")
                        overflow = Overflow.hidden
                    }
                    styledDiv {
                        css {
                            transform {
                                translateY(
                                    LinearDimension("-64px")
                                )
                            }
                        }
                        attrs {
                            classes = setOf("material-image")
                            unsafe {
                                +"""
                                                                    <svg version="1.1"
                                                                        xmlns="http://www.w3.org/2000/svg"
                                                                        xmlns:xlink="http://www.w3.org/1999/xlink"
                                                                        x="0px" y="0px" viewBox="0 0 300 300"
                                                                        style="enable-background:new 0 0 300 300;"
                                                                        xml:space="preserve">
                                                                        <g>
                                                                            <rect x="0.3" y="0.5" width="300" height="300" fill="${theme.palette.secondary.dark}" classes="material-image__background">
                                                                            </rect>
                                                                            <g>
                                                                                <path classes="material-image--shape-circle" fill="${theme.palette.secondary.light}" d="M124.5,171l26.3-45.5c-7.7-4.6-16.7-7.2-26.3-7.2c-28.6,0-51.7,23.2-51.7,51.7c0,28.6,23.2,51.7,51.7,51.7 c28.3,0,51.2-22.6,51.7-50.8H124.5z">
                                                                                </path>
                                                                                <path classes="material-image--shape-triangle" fill="${theme.palette.secondary.light}" d="M176.2,170.1c0,0.3,0,0.6,0,1H228l-51.7-89.6l-25.5,44.1C166,134.5,176.2,151.1,176.2,170.1z">
                                                                                </path>
                                                                                <path classes="material-image--shape-intersection" fill="${theme.palette.secondary.dark}" d="M176.2,171c0-0.3,0-0.6,0-1c0-19-10.2-35.6-25.5-44.6L124.5,171H176.2z">
                                                                                </path>
                                                                            </g>
                                                                        </g>
                                                                    </svg>
                                """.trimIndent()
                            }
                        }
                    }
                }
                styledDiv {
                    css {
                        backgroundColor =
                            Color(theme.palette.background.paper)
                        width = LinearDimension("100%")
                        paddingLeft = LinearDimension("24px")
                        paddingRight = LinearDimension("32px")
                        flex(1.0)
                    }
                    styledDiv {
                        css {
                            marginLeft = LinearDimension.auto
                            width = LinearDimension("48px")
                            transform {
                                translateX(LinearDimension("8px"))
                                translateY(LinearDimension("-24px"))
                            }
                        }
                        mFab("add", color = MColor.secondary) {
                        }
                    }
                    mTypography(
                        "Pellentesque habitant morbi tristique senectus et netus et malesuada " +
                            "fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae, " +
                            "ultricies eget, tempor sit amet, ante. Donec eu libero sit amet " +
                            "quam egestas semper. Aenean ultricies mi vitae est. Pellentesque " +
                            "habitant morbi tristique senectus et netus et malesuada fames ac " +
                            "turpis egestas. Vestibulum tortor quam, feugiat vitae, ultricies " +
                            "eget, tempor sit amet, ante",
                        variant = MTypographyVariant.caption,
                    )
                }
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.menu(theme: Theme) {
        mTypography(
            "Menu",
            variant = MTypographyVariant.subtitle2,
            color = MTypographyColor.textSecondary
        )
        styledDiv {
            css(ComponentStyles.largeSection)
            styledDiv {
                css {
                    position = Position.absolute
                    width = LinearDimension("368px")
                }
                mAppBar(
                    color = MAppBarColor.secondary,
                    position = MAppBarPosition.static
                ) {
                    mToolbar(variant = ToolbarVariant.dense) {
                        mToolbarTitle("Title")
                    }
                }
            }
            // mDrawer(true) {
            styledDiv {
                css {
                    backgroundColor = Color("#0000004d")
                    position = Position.absolute
                    width = LinearDimension("368px")
                    height = LinearDimension("368px")
                    display = Display.flex
                }
                mMenuList {
                    css {
                        backgroundColor =
                            Color(theme.palette.background.paper)
                        width = LinearDimension("240px")
                    }
                    styledDiv {
                        css {
                            height =
                                LinearDimension("140px")
                            width = LinearDimension("100%")
                            overflow = Overflow.hidden
                            transform {
                                translateY(
                                    LinearDimension("-8px")
                                )
                            }
                        }
                        styledDiv {
                            css {
                                transform {
                                    translateY(
                                        LinearDimension("-56px")
                                    )
                                }
                            }
                            attrs {
                                classes =
                                    setOf("material-image")
                                unsafe {
                                    +"""
                                                                        <svg version="1.1"
                                                                            xmlns="http://www.w3.org/2000/svg"
                                                                            xmlns:xlink="http://www.w3.org/1999/xlink"
                                                                            x="0px" y="0px" viewBox="0 0 300 300"
                                                                            style="enable-background:new 0 0 300 300;"
                                                                            xml:space="preserve">
                                                                            <g>
                                                                                <rect x="0.3" y="0.5" width="300" height="300" classes="material-image__background">
                                                                                </rect>
                                                                                <g>
                                                                                    <path classes="material-image--shape-circle" fill="${theme.palette.primary.light}" d="M124.5,171l26.3-45.5c-7.7-4.6-16.7-7.2-26.3-7.2c-28.6,0-51.7,23.2-51.7,51.7c0,28.6,23.2,51.7,51.7,51.7 c28.3,0,51.2-22.6,51.7-50.8H124.5z">
                                                                                    </path>
                                                                                    <path classes="material-image--shape-triangle" fill="${theme.palette.primary.light}" d="M176.2,170.1c0,0.3,0,0.6,0,1H228l-51.7-89.6l-25.5,44.1C166,134.5,176.2,151.1,176.2,170.1z"></path><path classes="material-image--shape-intersection" d="M176.2,171c0-0.3,0-0.6,0-1c0-19-10.2-35.6-25.5-44.6L124.5,171H176.2z">
                                                                                    </path>
                                                                                </g>
                                                                            </g>
                                                                        </svg>
                                    """.trimIndent()
                                }
                            }
                        }
                    }
                    styledDiv {
                        css {
                            height = LinearDimension("-8px")
                        }
                    }
                    mMenuItem(button = true) {
                        mListItemIcon("share")
                        mTypography(
                            "Share",
                            variant = MTypographyVariant.inherit
                        )
                    }
                    mMenuItem(
                        button = true,
                        selected = true
                    ) {
                        mListItemIcon("cloud_upload")
                        mTypography(
                            "Upload",
                            variant = MTypographyVariant.inherit
                        )
                    }
                    mMenuItem(button = true) {
                        mListItemIcon("content_copy")
                        mTypography(
                            "Copy",
                            variant = MTypographyVariant.inherit
                        )
                    }
                    mMenuItem(button = true) {
                        mListItemIcon("print")
                        mTypography(
                            "Print this page",
                            variant = MTypographyVariant.inherit
                        )
                    }
                }
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.textFields(theme: Theme) {
        mTypography(
            "Text Field",
            variant = MTypographyVariant.subtitle2,
            color = MTypographyColor.textSecondary
        )
        column {
            css(ComponentStyles.smallSection)
            row {
                css {
                    width = LinearDimension("100%")
                    height = LinearDimension("50%")
                }
                mTypography(
                    "Primary",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary
                ) {
                    css {
                        paddingTop = LinearDimension("22px")
                        width = LinearDimension("25%")
                    }
                }
                mTextField(
                    "Single-line input label",
                    defaultValue = "Input text for a single line",
                    placeholder = "Single-line-input label",
                    margin = MFormControlMargin.none
                ) {
                    attrs {
                        color = MTextFieldColor.primary
                    }
                }
            }
            row {
                css {
                    width = LinearDimension("100%")
                    height = LinearDimension("50%")
                }

                mTypography(
                    "Secondary",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary
                ) {
                    css {
                        paddingTop = LinearDimension("22px")
                        width = LinearDimension("25%")
                    }
                }
                mTextField(
                    "Single-line input label",
                    defaultValue = "Input text for a single line",
                    placeholder = "Single-line-input label",
                    margin = MFormControlMargin.none
                ) {
                    attrs {
                        color = MTextFieldColor.secondary
                    }
                }
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.cards(theme: Theme) {
        mTypography(
            "Cards",
            variant = MTypographyVariant.subtitle2,
            color = MTypographyColor.textSecondary
        )
        styledDiv {
            css(ComponentStyles.largeSection)
            mCard(raised = true) {
                css(ComponentStyles.card)
                mCardHeader(
                    "Title",
                    "Subhead",
                    avatar = altBuilder.styledDiv {
                        css {
                            backgroundColor =
                                Color(theme.palette.text.secondary)
                            borderRadius = LinearDimension("20px")
                            width = LinearDimension("40px")
                            height = LinearDimension("40px")
                        }
                    }
                )
                styledDiv {
                    css {
                        height = LinearDimension("140px")
                        overflow = Overflow.hidden
                    }
                    styledDiv {
                        css {
                            transform {
                                translateY(LinearDimension("-64px"))
                            }
                        }
                        attrs {
                            classes = setOf("material-image")
                            unsafe {
                                +"""
                                                                        <svg version="1.1"
                                                                            xmlns="http://www.w3.org/2000/svg"
                                                                            xmlns:xlink="http://www.w3.org/1999/xlink"
                                                                            x="0px" y="0px" viewBox="0 0 300 300"
                                                                            style="enable-background:new 0 0 300 300;"
                                                                            xml:space="preserve">
                                                                            <g>
                                                                                <rect x="0.3" y="0.5" width="300" height="300" classes="material-image__background">
                                                                                </rect>
                                                                                <g>
                                                                                    <path classes="material-image--shape-circle" fill="${theme.palette.primary.light}" d="M124.5,171l26.3-45.5c-7.7-4.6-16.7-7.2-26.3-7.2c-28.6,0-51.7,23.2-51.7,51.7c0,28.6,23.2,51.7,51.7,51.7 c28.3,0,51.2-22.6,51.7-50.8H124.5z">
                                                                                    </path>
                                                                                    <path classes="material-image--shape-triangle" fill="${theme.palette.primary.light}" d="M176.2,170.1c0,0.3,0,0.6,0,1H228l-51.7-89.6l-25.5,44.1C166,134.5,176.2,151.1,176.2,170.1z"></path><path classes="material-image--shape-intersection" d="M176.2,171c0-0.3,0-0.6,0-1c0-19-10.2-35.6-25.5-44.6L124.5,171H176.2z">
                                                                                    </path>
                                                                                </g>
                                                                            </g>
                                                                        </svg>
                                """.trimIndent()
                            }
                        }
                    }
                }
                mTypography(
                    "Pellentesque habitant morbi tristique senectus et netus et malesuada " +
                        "fames ac turpis egestas.",
                    variant = MTypographyVariant.body2
                ) {
                    css {
                        padding(LinearDimension("16px"))
                    }
                }
                mCardActions(disableSpacing = true) {
                    styledDiv {
                        css {
                            marginLeft = LinearDimension.auto
                            paddingRight = LinearDimension("8px")
                            paddingBottom = LinearDimension("8px")
                        }

                        mButton(
                            "Button",
                            variant = MButtonVariant.contained,
                            color = MColor.secondary,
                            size = MButtonSize.small
                        ) {
                        }
                    }
                }
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.switchesAndSlides(theme: Theme) {
        mTypography(
            "Switches and sliders",
            variant = MTypographyVariant.subtitle2,
            color = MTypographyColor.textSecondary
        )
        column {
            css(ComponentStyles.smallSection)
            row {
                css {
                    width = LinearDimension("100%")
                    height = LinearDimension("50%")
                    alignContent = Align.center
                    alignItems = Align.center
                }
                mTypography(
                    "Primary",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary
                ) {
                    css {
                        paddingLeft = LinearDimension("16px")
                        width = LinearDimension("200px")
                    }
                }
                mCheckbox(
                    checked = state.firstChecked ?: false,
                    color = MOptionColor.primary,
                    onChange = { _, selected ->
                        setState {
                            firstChecked = selected
                        }
                    }
                )

                mRadio(
                    checked = state.firstSelected ?: false,
                    color = MOptionColor.primary,
                    onChange = { _, selected ->
                        setState {
                            firstSelected = true
                        }
                    }
                )
                mSwitch(
                    checked = state.firstSwitched ?: false,
                    color = MOptionColor.primary,
                    onChange = { _, selected ->
                        setState {
                            firstSwitched = selected
                        }
                    }
                )
            }
            row {
                css {
                    width = LinearDimension("100%")
                    height = LinearDimension("50%")
                    alignContent = Align.center
                    alignItems = Align.center
                }
                mTypography(
                    "Secondary",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary
                ) {
                    css {
                        paddingLeft = LinearDimension("16px")
                        width = LinearDimension("200px")
                    }
                }
                mCheckbox(
                    checked = state.secondChecked ?: false,
                    color = MOptionColor.secondary,
                    onChange = { _, selected ->
                        setState {
                            secondChecked = selected
                        }
                    }
                )

                mRadio(
                    checked = !(state.firstSelected ?: false),
                    color = MOptionColor.secondary,
                    onChange = { _, selected ->
                        setState {
                            firstSelected = false
                        }
                    }
                )
                mSwitch(
                    checked = state.secondSwitched ?: false,
                    color = MOptionColor.secondary,
                    onChange = { _, selected ->
                        setState {
                            secondSwitched = selected
                        }
                    }
                )
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.selection(theme: Theme) {
        mTypography(
            "Selection",
            variant = MTypographyVariant.subtitle2,
            color = MTypographyColor.textSecondary
        )
        column {
            css(ComponentStyles.smallSection)
            row {
                css {
                    height = LinearDimension("50%")
                }
                mTypography(
                    "Primary",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary
                ) {
                    css {
                        paddingLeft = LinearDimension("16px")
                        paddingBottom = LinearDimension("40px")
                    }
                }
                mSnackbar(
                    "Single-line snackbar",
                    open = true,
                    autoHideDuration = null
                ) {
                    attrs.action = altBuilder.div {
                        mButton(
                            "Action",
                            color = MColor.primary,
                            variant = MButtonVariant.text,
                            size = MButtonSize.small
                        )
                    }
                }
            }
            row {
                css {
                    height = LinearDimension("50%")
                }
                mTypography(
                    "Secondary",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary
                ) {
                    css {
                        paddingLeft = LinearDimension("16px")
                        paddingBottom = LinearDimension("52px")
                    }
                }
                mSnackbar(
                    "Single-line snackbar",
                    open = true,
                    autoHideDuration = null
                ) {
                    attrs.action = altBuilder.div {
                        mButton(
                            "Action",
                            color = MColor.secondary,
                            variant = MButtonVariant.text,
                            size = MButtonSize.small
                        )
                    }
                }
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.buttons(theme: Theme) {
        mTypography(
            "Buttons",
            variant = MTypographyVariant.subtitle2,
            color = MTypographyColor.textSecondary
        )
        styledDiv {
            css(ComponentStyles.largeRowSection)
            column {
                center()
                css {
                    width = LinearDimension("50%")
                }

                mTypography(
                    "Primary",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary
                )
                mFab(
                    "add",
                    color = MColor.primary,
                    size = MButtonSize.large
                )
                space("32px")

                mFab("add", size = MButtonSize.large) {
                    attrs {
                        asDynamic().style = js {
                            this.backgroundColor =
                                theme.palette.background.default
                            this.color = theme.palette.primary.main
                        }
                    }
                }
                space("32px")
                mButton(
                    "Normal",
                    color = MColor.primary,
                    variant = MButtonVariant.text
                )
                space("32px")
                mButton(
                    "Normal",
                    color = MColor.primary,
                    variant = MButtonVariant.contained
                )
            }
            column {
                center()
                css {
                    width = LinearDimension("50%")
                }

                mTypography(
                    "Secondary",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary
                )
                mFab(
                    "add",
                    color = MColor.secondary,
                    size = MButtonSize.large
                )
                styledDiv {
                    css {
                        height = LinearDimension("32px")
                    }
                }

                mFab("add", size = MButtonSize.large) {
                    attrs {
                        asDynamic().style = js {
                            this.backgroundColor =
                                theme.palette.background.default
                            this.color =
                                theme.palette.secondary.main
                        }
                    }
                }
                styledDiv {
                    css {
                        height = LinearDimension("32px")
                    }
                }
                mButton(
                    "Normal",
                    color = MColor.secondary,
                    variant = MButtonVariant.text
                )
                styledDiv {
                    css {
                        height = LinearDimension("32px")
                    }
                }
                mButton(
                    "Normal",
                    color = MColor.secondary,
                    variant = MButtonVariant.contained
                )
            }
        }
    }

    private inline fun StyledBuilder<*>.center() {
        css(ComponentStyles.alignCenter)
    }

    private inline fun RBuilder.column(content: StyledDOMBuilder<DIV>.() -> Unit) {
        styledDiv {
            css(ComponentStyles.column)
            content()
        }
    }

    private inline fun RBuilder.row(content: StyledDOMBuilder<DIV>.() -> Unit) {
        styledDiv {
            css(ComponentStyles.row)
            content()
        }
    }

    private fun RBuilder.flex() {
        styledSpan {
            css(ComponentStyles.flex)
        }
    }

    override fun componentDidMount() {
    }
}

private fun RBuilder.space(s: String) {
    styledDiv {
        css {
            width = LinearDimension(s)
            height = LinearDimension(s)
        }
    }
}
