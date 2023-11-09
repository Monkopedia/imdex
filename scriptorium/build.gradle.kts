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
    // Use mavenCentral for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(project(":protocol"))
    implementation(libs.ksrpc)
    implementation(libs.ksrpc.server)
    implementation(project(":markdown"))

    // Use the Kotlin JDK 8 standard library.
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.core.jvm)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.client.apache)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.dataformat.xml)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.sqlite.jdbc)
    implementation(libs.hikaricp)

    implementation(libs.lucene.core)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.netty)

    implementation(libs.flexmark.all)
    implementation(libs.clikt)
    implementation(libs.lanterna)

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {

// Define the main class for the application.
    mainClass.set("com.monkopedia.scriptorium.AppKt")
}

val fatJar = task("fatJar", type = Jar::class) {
    // baseName = "${project.name}-fat"
    manifest {
        attributes["Implementation-Title"] = "iMDex Server"
        attributes["Implementation-Version"] = "1.0"
        attributes["Main-Class"] = application.mainClass
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs += "-Xskip-prerelease-check"
    }
}

val browser = rootProject.findProject(":client")!!

val copy = tasks.register<Copy>("copyJsBundleToKtor") {
    from("${browser.buildDir}/distributions")
    into("$buildDir/processedResources/web")
}

tasks.named("copyJsBundleToKtor") {
    mustRunAfter(browser.tasks["jsBrowserProductionWebpack"])
}

tasks.named("fatJar") {
    mustRunAfter("copyJsBundleToKtor")
}

sourceSets {
    main {
        resources {
            srcDir("$buildDir/processedResources")
            compiledBy(copy)
        }
    }
}
