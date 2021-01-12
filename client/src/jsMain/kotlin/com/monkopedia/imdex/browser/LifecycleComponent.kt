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
package com.monkopedia.imdex.browser

import com.monkopedia.kpages.LifecycleComponent
import kotlinext.js.js
import react.RComponent
import react.RProps
import react.RState

//abstract class LifecycleComponent<P : RProps, S : RState> : RComponent<P, S> {
//
//    private val callbacks = mutableListOf<LifecycleAware>()
//
//    constructor() : super()
//    constructor(props: P) : super(props)
//
//    fun registerLifecycleAware(l: LifecycleAware) {
//        l.onActivate?.invoke()
//        callbacks.add(l)
//    }
//
//    inline fun attach(builder: LifecycleAware.() -> Unit) {
//        registerLifecycleAware(js { }.unsafeCast<LifecycleAware>().apply(builder))
//    }
//
//    override fun componentWillUnmount() {
//        callbacks.forEach {
//            it.onDeactivate?.invoke()
//        }
//    }
//}
//
//external interface LifecycleAware {
//    var onActivate: (() -> Unit)?
//    var onDeactivate: (() -> Unit)?
//}

fun LifecycleComponent<*, *>.globalKey(c: Char, listener: () -> Unit) = attach {
    onActivate = {
        Keymap.addGlobalKey(c, listener)
    }
    onDeactivate = {
        Keymap.removeGlobalKey(c)
    }
}

fun LifecycleComponent<*, *>.globalKey(key: String, listener: () -> Unit) = attach {
    onActivate = {
        Keymap.addGlobalKey(key, listener)
    }
    onDeactivate = {
        Keymap.removeGlobalKey(key)
    }
}
