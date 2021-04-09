package com.monkopedia.imdex

import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mTypography
import com.monkopedia.kpages.ViewControllerFactory
import com.monkopedia.kpages.factory
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

actual val themeDemoFactory: ViewControllerFactory = factory { path, title ->
    title.value = "Theme demo"
    child(ThemeDemo::class) {
    }
}

actual val searchFactory: ViewControllerFactory
    get() = factory { _, title ->
        title.value = "Search"
        mTypography("Coming soon") { }
    }

actual val defaultFactory: ViewControllerFactory = factory { path, title ->
    val (path, scroll) = if (path.contains("?scroll=")) path.split("?scroll=")
    else listOf(path, "")
    val data = ImdexApp.INSTANCE.cache.get(path)
    if (data != null) {
        title.value = data.content.metadata.label
        child(MarkdownScreen::class) {
            attrs {
                document = data.content
                nodes = data.nodes
            }
        }
    } else {
        title.value = "Loading"
        child(MarkdownScreen::class) {
            attrs {
                this.title = title
                content = if (path == "/") Korpus.Document.ROOT else Korpus.Document(path)
            }
        }
    }
}

actual val errorFactory: ViewControllerFactory = factory { path, title ->
    title.value = "Error"
    child(ErrorComponent::class) {
        attrs {
            error = enumValueOf<Error>(path.substring("/error/".length))
        }
    }
}

external interface ErrorProps : RProps {
    var error: Error?
}

class ErrorComponent : RComponent<ErrorProps, RState>() {
    override fun RBuilder.render() {
        mTypography(props.error?.title ?: "", MTypographyVariant.h1)
        mTypography(props.error?.description ?: "", MTypographyVariant.body2)
    }
}
