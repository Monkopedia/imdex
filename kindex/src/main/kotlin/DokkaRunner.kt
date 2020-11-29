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
package com.monkopedia.kindex

import com.monkopedia.Log
import com.monkopedia.Logger
import com.monkopedia.debug
import com.monkopedia.error
import com.monkopedia.imdex.Scriptorium.KorpusInfo
import com.monkopedia.info
import com.monkopedia.warn
import java.io.File
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.DokkaConfigurationImpl
import org.jetbrains.dokka.DokkaGenerator
import org.jetbrains.dokka.DokkaModuleDescriptionImpl
import org.jetbrains.dokka.DokkaSourceSetID
import org.jetbrains.dokka.DokkaSourceSetImpl
import org.jetbrains.dokka.ExternalDocumentationLink
import org.jetbrains.dokka.Platform
import org.jetbrains.dokka.utilities.DokkaLogger

private val Logger.asDokkaLogger: DokkaLogger
    get() = object : DokkaLogger {
        override var errorsCount: Int = 0
        override var warningsCount: Int = 0

        override fun debug(message: String) = this@asDokkaLogger.debug(message)
        override fun error(message: String) = this@asDokkaLogger.error(message)
        override fun info(message: String) = this@asDokkaLogger.info(message)
        override fun progress(message: String) = this@asDokkaLogger.info(message)
        override fun warn(message: String) = this@asDokkaLogger.warn(message)
    }

fun defaultLinks(jdkVersion: Int): MutableList<DokkaConfiguration.ExternalDocumentationLink> =
    mutableListOf<DokkaConfiguration.ExternalDocumentationLink>().apply {
        // if (!config.noJdkLink) {
        val javadocLink =
            if (jdkVersion < 11) "https://docs.oracle.com/javase/$jdkVersion/docs/api/"
            else "https://docs.oracle.com/en/java/javase/$jdkVersion/docs/api/java.base/"
        val packageListLink =
            if (jdkVersion < 11) "$javadocLink/package-list"
            else "https://docs.oracle.com/en/java/javase/$jdkVersion/docs/api/element-list"
        this += ExternalDocumentationLink(javadocLink, packageListLink)
        // }

        // if (!config.noStdlibLink)
        // this += DokkaConfiguration.ExternalDocumentationLink
        //     .Builder("https://kotlinlang.org/api/latest/jvm/stdlib/")
        //     .build()
    }

fun runDokka(config: KorpusInfo) {
    val classpath = listOf(
        File(config.homeFile, "plugins.jar")
    )
    if (!classpath[0].exists()) {
        config::class.java.getResourceAsStream("/plugins.jar").copyTo(classpath[0].outputStream())
    }

    val configuration = DokkaConfigurationImpl(
        outputDir = config.homeFile,
        offlineMode = true,
        sourceSets = listOf(
            DokkaSourceSetImpl(
                displayName = "markdown",
                sourceSetID = DokkaSourceSetID("markdown", "markdown"),
                classpath = classpath,
                sourceRoots = setOf(
                    File(config.homeFile.absolutePath, "sources")
                ),
                dependentSourceSets = emptySet(),
                samples = emptySet(),
                includes = emptySet(),
                includeNonPublic = false,
                // includeRootPackage = true,
                reportUndocumented = false,
                skipEmptyPackages = true,
                skipDeprecated = false,
                jdkVersion = 8,
                sourceLinks = emptySet(), // defaultLinks(8),
                perPackageOptions = emptyList(),
                externalDocumentationLinks = emptySet(),
                languageVersion = null,
                apiVersion = null,
                noStdlibLink = true,
                noJdkLink = false,
                suppressedFiles = emptySet(),
                analysisPlatform = Platform.DEFAULT
            )
        ),
        pluginsClasspath = classpath,
        pluginsConfiguration = emptyMap(),
        modules = listOf(
            DokkaModuleDescriptionImpl("markdown", config.homeFile, emptySet())
        ),
        failOnWarning = false
    )
    DokkaGenerator(configuration, Log.asDokkaLogger).generate()
}
