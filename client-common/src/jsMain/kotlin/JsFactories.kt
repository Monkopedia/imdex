package com.monkopedia.markdown

import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mTypography
import com.monkopedia.kpages.INSTANCE
import com.monkopedia.kpages.Navigator
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

actual val rootSettingsFactory: ViewControllerFactory
    get() = factory { _, title ->
        title.value = "Settings"
        mTypography("Coming soon") {  }
    }

actual val defaultFactory: ViewControllerFactory = factory { path, title ->
    val (path, scroll) = path.split("?scroll=")
    val data = ImdexApp.INSTANCE.cache.get(path, Navigator.INSTANCE)
    title.value = data.content.metadata.label
    child(MarkdownScreen::class) {
        attrs {
            document = data.content
            nodes = data.nodes
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
