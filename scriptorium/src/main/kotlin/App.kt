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
package com.monkopedia.scriptorium

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.monkopedia.FileLogger
import com.monkopedia.Log
import com.monkopedia.StdoutLogger
import com.monkopedia.error
import com.monkopedia.ksrpc.KsrpcEnvironment
import com.monkopedia.ksrpc.channels.SerializedService
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.onError
import com.monkopedia.ksrpc.serialized
import com.monkopedia.ksrpc.server.ServiceApp
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.BaseApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.defaultResource
import io.ktor.server.http.content.resolveResource
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

fun main(args: Array<String>) = App().main(args)

class App : ServiceApp("scriptorium") {
    private val log by option("-l", "--log", help = "Path to log to, or stdout")
        .default("/tmp/scriptorium.log")
    private val service by lazy {
        ScriptoriumService(Config())
    }
    override val env: KsrpcEnvironment<String> by lazy {
        ksrpcEnvironment {
            onError { e ->
                Log.error(
                    StringWriter().also {
                        e.printStackTrace(PrintWriter(it))
                    }.toString(),
                )
            }
        }
    }

    override fun run() {
        if (log == "stdout") {
            Log.init(StdoutLogger)
        } else {
            Log.init(FileLogger(File(log)))
        }
        super.run()
    }

    private fun Application.extracted(function: Application.() -> Unit) {
        function()
        install(CORS) {
            anyHost()
        }
        install(StatusPages) {
            status(HttpStatusCode.NotFound) { call, _ ->
                val content = call.resolveResource("web/index.html", null)
                if (content != null) {
                    call.respond(content)
                }
            }
        }
    }

    override fun embeddedServer(
        port: Int,
        function: Application.() -> Unit,
    ): BaseApplicationEngine {
        return embeddedServer(Netty, port) {
            extracted(function)
        }.start()
    }

    override fun createRouting(routing: Routing) {
        super.createRouting(routing)
        routing.apply {
            static("/") {
                //                        default("index.html")
                resources("web")
                defaultResource("web/index.html")
            }
        }
    }

    override fun createChannel(): SerializedService<String> {
        return service.serialized(env)
    }
}
