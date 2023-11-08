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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm")
    kotlin("plugin.serialization")

    application
}

repositories {
    mavenCentral()
    mavenLocal()
}

val `dokka-plugins` by configurations.creating {
    extendsFrom()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(project(":protocol"))
    implementation(libs.ksrpc)
    implementation(project(":markdown"))
    implementation(project(":fileindexer"))

    // Use the Kotlin JDK 8 standard library.
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.core.jvm)
    implementation(libs.ktor.client.apache)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.dataformat.xml)

    add("dokka-plugins", project(":dokka-gfm-kindex"))
    add("dokka-plugins", libs.dokka.base)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.sqlite.jdbc)
    implementation(libs.hikaricp)

    implementation(libs.dokka.core)

    implementation(libs.flexmark.all)
    implementation(libs.clikt)
    implementation(libs.lanterna)

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xskip-prerelease-check"
    }
}

application {
    mainClass.set("com.monkopedia.kindex.KindexKt")
}

val deps = configurations["dokka-plugins"].map { if (it.isDirectory) it else zipTree(it) }
val serviceDir = "$buildDir/services"

tasks.create("pluginServices") {
    dependsOn(rootProject.findProject(":dokka-gfm-kindex")?.tasks?.getByName("jar")!!)
    doFirst {
        deps.map {
            if (it is File) {
                fileTree(it)
            } else {
                val zipTree = it as FileTree
                zipTree
            }
        }.flatMap {
            it.matching {
                include("META-INF/services/**")
            }.files
        }.groupBy {
            var parent: File? = it.parentFile
            while (parent != null && parent.name != "META-INF") {
                parent = parent.parentFile
            }
            if (parent != null) "META-INF/" + it.toRelativeString(parent)
            else throw IllegalArgumentException("Can't find parent for $it")
        }.forEach {
            val file = File("$serviceDir/${it.key}")
            file.parentFile.mkdirs()
            file.writeText(it.value.flatMap { it.readLines() }.joinToString("\n"))
        }
    }
}

tasks.create("pluginsJar", Jar::class) {
    dependsOn("pluginServices")
    destinationDirectory.set(File(buildDir, "resources"))
    archiveFileName.set("plugins.jar")
    isZip64 = true
    from(deps) {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/services/**")
    }
    from(serviceDir)
}

sourceSets {
    main {
        resources.srcDir(files("$buildDir/resources").builtBy("pluginsJar"))
    }
}
