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

class GlobalEvent<T>(
    private val listeners: MutableList<(T) -> Unit> = mutableListOf()
) {
    fun register(listener: (T) -> Unit) {
        listeners.add(listener)
    }

    fun unregister(listener: (T) -> Unit) {
        listeners.remove(listener)
    }

    fun dispatch(value: T) {
        listeners.forEach { it.invoke(value) }
    }
}

fun <T> LifecycleComponent<*, *>.listen(event: GlobalEvent<T>, v: (T) -> Unit) = attach {
    onActivate = {
        event.register(v)
    }
    onDeactivate = {
        event.unregister(v)
    }
}

object Events {
    val Save = GlobalEvent<Unit>()
}
