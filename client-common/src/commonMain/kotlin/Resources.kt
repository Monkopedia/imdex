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
package com.monkopedia.markdown

import com.monkopedia.ksrpc.RpcService
import io.ktor.utils.io.core.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Resources {
    val resources = mutableListOf<suspend () -> Unit>()

    fun <T : Closeable> T.use(): T {
        resources += {
            this.close()
        }
        return this
    }

    fun <T : RpcService> T.use(): T {
        resources += this::close
        return this
    }

    suspend fun close() {
        var exception: Exception? = null
        for (resource in resources.reversed()) {
            try {
                resource.invoke()
            } catch (closeException: Exception) {
                if (exception == null) {
                    exception = closeException
                } else {
                    exception.addSuppressed(closeException)
                }
            }
        }
        if (exception != null) throw exception
    }
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <T> withResources(block: Resources.() -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val res = Resources()
    try {
        return res.block()
    } finally {
        res.close()
    }
}
