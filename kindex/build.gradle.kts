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
    jcenter()
    mavenLocal()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
    maven(url = "https://kotlinx.bintray.com/kotlinx/")
}

val `dokka-plugins` by configurations.creating {
    extendsFrom()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(project(":protocol"))
    implementation("com.monkopedia:ksrpc:0.1.1")
    implementation(project(":markdown"))
    implementation(project(":fileindexer"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0-RC2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("io.ktor:ktor-client-core:1.3.2")
    implementation("io.ktor:ktor-client-core-jvm:1.3.2")
    implementation("io.ktor:ktor-client-apache:1.3.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.10.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.10.0")
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.0")

    add("dokka-plugins", project(":dokka-gfm-kindex"))
    add("dokka-plugins", "org.jetbrains.dokka:dokka-base:1.4.10")

    implementation("org.jetbrains.exposed:exposed-core:0.26.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.26.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.26.2")
    implementation("org.xerial:sqlite-jdbc:3.32.3.2")
    implementation("com.zaxxer:HikariCP:3.4.2")

    implementation("org.jetbrains.dokka:dokka-core:1.4.10")

    implementation("com.vladsch.flexmark:flexmark-all:0.62.2")
    implementation("com.github.ajalt:clikt:2.8.0")
    implementation("com.googlecode.lanterna:lanterna:3.0.3")

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
    mainClassName = "com.monkopedia.kindex.KindexKt"
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
