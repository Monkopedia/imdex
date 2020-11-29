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
import com.monkopedia.imdex.Scriptorium
import com.monkopedia.ksrpc.SerializedChannel
import com.monkopedia.ksrpc.ServiceApp
import com.monkopedia.ksrpc.serializedChannel
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

    override fun run() {
        if (log == "stdout") {
            Log.init(StdoutLogger)
        } else {
            Log.init(FileLogger(File(log)))
        }
        super.run()
    }

    override fun createChannel(): SerializedChannel {
        return Scriptorium.serializedChannel(
            service,
            errorListener = { e ->
                Log.error(
                    StringWriter().also {
                        e.printStackTrace(PrintWriter(it))
                    }.toString()
                )
            }
        )
    }
}
