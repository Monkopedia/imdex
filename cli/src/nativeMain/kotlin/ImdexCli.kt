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
package com.monkopedia.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.pair
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.monkopedia.imdex.Imdex
import com.monkopedia.imdex.Korpus
import com.monkopedia.imdex.KorpusManager
import com.monkopedia.imdex.ProfileManager
import com.monkopedia.imdex.Scriptorium
import com.monkopedia.ksrpc.connect
import com.monkopedia.ksrpc.deserialized
import com.monkopedia.ksrpc.toKsrpcUri
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = ImdexCli().main(args)

class ImdexCli : CliktCommand() {
    private val imdexPath by option("-i", "--imdex", help = "Ksrpc uri to imdex server")
        .default("http://localhost:8001/scriptorium")
    lateinit var scriptorium: Scriptorium

    init {
        Platform.isMemoryLeakCheckerActive = false
        subcommands(
            List(this),
            Create(this),
            Configure(this),
            Search(this),
            Fetch(this),
            GetProps(this),
            SetProps(this)
        )
    }

    override fun run() {
        runBlocking {
            scriptorium = Scriptorium.wrap(imdexPath.toKsrpcUri().connect().deserialized())
        }
    }
}

class List(val root: ImdexCli) : CliktCommand(name = "list") {
    val korpus by option("-k", "--korpus", help = "Limit which korpii are listed")

    override fun run() = runBlocking {
        root.scriptorium.getKorpii(Unit).forEach {
            if (korpus == null || korpus == it.id) {
                println("Korpus: ${it.id}")
                println("Type: ${it.type}")
                println("Config:")
                for ((k, v) in it.config) {
                    println("    $k: $v")
                }
                println("\n")
            }
        }
    }
}

class Create(val root: ImdexCli) : CliktCommand(name = "create") {
    val type by option("-t", "--type", help = "iMDex type of the korpus provider")
        .required()
    val config by option(
        "-c",
        "--config",
        help = "Secify a config value for the korpus as key value"
    )
        .pair()
        .multiple()

    override fun run(): Unit = runBlocking {
        val config = config.toMap()
        root.scriptorium.korpusManager(Unit).createKorpus(
            KorpusManager.CreateKorpus(
                type,
                config
            )
        )
    }
}

class Configure(val root: ImdexCli) : CliktCommand(name = "configure") {
    val korpus by option("-k", "--korpus", help = "ID of korpus to configure")
        .required()
    val config by option(
        "-c",
        "--config",
        help = "Secify a config value for the korpus as key value"
    ).pair().multiple()

    val remove by option("-r", "--remove", help = "Key of a config value to remove")
        .multiple()

    override fun run(): Unit = runBlocking {
        val config = root.scriptorium.getKorpii(Unit).find { it.id == korpus }
            ?: error("Cannot find korpus $korpus")
        val newConfig = config.config.toMutableMap()
        for ((k, v) in this@Configure.config) {
            newConfig[k] = v
        }
        for (r in remove) {
            if (!newConfig.containsKey(r)) {
                println("Warning: Korpus $korpus doesn't appear to have config $r")
            }
            newConfig.remove(r)
        }
        root.scriptorium.korpusManager(Unit).updateKorpus(
            KorpusManager.UpdateKorpus(korpus, newConfig)
        )
    }
}

class Search(val root: ImdexCli) : CliktCommand(name = "search") {
    val korpus by option("-k", "--korpus", help = "Limit which korpii are listed")
    val query by argument("query")
    val max by option("-n", "--number", help = "Maximum number of results to display")
        .int()
        .default(10)
    val profile by option("-p", "--profile", help = "The profile to use")
        .int()
        .default(ProfileManager.DEFAULT_CMD)

    override fun run(): Unit = runBlocking {
        var hasShown = false
        root.scriptorium.imdex(profile).query(
            Imdex.Query(
                query,
                max
            )
        ).results.forEach {
            if (korpus == null || korpus == it.documentSection.document.korpus) {
                if (hasShown) {
                    println("\n${Array(terminalWidth) { '=' }.joinToString("")}\n")
                } else {
                    hasShown = true
                }
                println("Document: ${it.documentSection.document.path}")
                println("${it.content.trimEnd('\n')}")
            }
            println("")
        }
    }
}

class Fetch(val root: ImdexCli) : CliktCommand(name = "fetch") {
    val korpus by option("-k", "--korpus", help = "ID of korpus to fetch")
        .required()
    val documentId by option("-d", "--document", help = "Document ID to fetch")
        .required()
    val profile by option("-p", "--profile", help = "The profile to use")
        .int()
        .default(ProfileManager.DEFAULT_CMD)
    override fun run(): Unit = runBlocking {
        println(root.scriptorium.imdex(profile).fetch(Korpus.Document(documentId)).content)
    }
}

class GetProps(val root: ImdexCli) : CliktCommand(name = "get_props") {
    val korpus by option("-k", "--korpus", help = "ID of korpus to fetch")
        .required()
    val documentId by option("-d", "--document", help = "Document ID to fetch")
        .required()
    val profile by option("-p", "--profile", help = "The profile to use")
        .int()
        .default(ProfileManager.DEFAULT_CMD)
    override fun run(): Unit = runBlocking {
        println("Getting props $profile $documentId")
        try {
            val result = root.scriptorium.imdex(profile).properties(Korpus.Document(documentId))
            println("Document: ${result.document.path}")
            println("${result.properties}")
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}

class SetProps(val root: ImdexCli) : CliktCommand(name = "set_props") {
    val korpus by option("-k", "--korpus", help = "ID of korpus to fetch")
        .required()
    val documentId by option("-d", "--document", help = "Document ID to fetch")
        .required()
    val profile by option("-p", "--profile", help = "The profile to use")
        .int()
        .default(ProfileManager.DEFAULT_CMD)
    val options by option("--set", help = "A property to set").pair().multiple()
    val unset by option("--unset", help = "A property to remove").multiple()

    override fun run(): Unit = runBlocking {
        val document = Korpus.Document(documentId)
        val existingProps = root.scriptorium.imdex(profile)
            .properties(document)
            .properties
            .toMutableMap()
        for ((k, v) in options) {
            existingProps[k] = v
        }
        for (k in unset) {
            existingProps.remove(k)
        }
        val k = root.scriptorium.korpus(korpus)
        k.updateProperties(Korpus.DocumentProperties(document, existingProps))
        k.updateIndex(Unit)
    }
}



val terminalWidth: Int
    get() = 100
