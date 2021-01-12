package com.monkopedia.imdex

import com.ccfraser.muirwik.components.dialog.mDialog
import com.ccfraser.muirwik.components.dialog.mDialogTitle
import com.monkopedia.kpages.LifecycleComponent
import com.monkopedia.kpages.Mutable
import com.monkopedia.kpages.Navigator
import react.RBuilder
import react.RProps
import react.RState


actual fun Navigator.showLoadingUi(document: Korpus.Document): Closeable {
    DialogComponent.loading.value = true
    return object : Closeable {
        override suspend fun close() {
            DialogComponent.loading.value = false
        }
    }
}


external interface DialogState: RState {
    var showingLoading: Boolean?
}

class DialogComponent : LifecycleComponent<RProps, DialogState>() {
    init {
        loading.intoState {
            showingLoading = it
        }
    }

    override fun RBuilder.render() {
        mDialog(
            open = state.showingLoading ?: false,
        ) {
            mDialogTitle("Loading...")
        }
    }

    companion object{
        val loading = Mutable(false)
    }
}