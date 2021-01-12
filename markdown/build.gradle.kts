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

    java
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    mavenLocal()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
    maven(url = "https://kotlinx.bintray.com/kotlinx/")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.slf4j:slf4j-api:1.6.1")
    api(project(":protocol"))

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

    implementation("org.jetbrains.exposed:exposed-core:0.26.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.26.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.26.2")
    implementation("org.xerial:sqlite-jdbc:3.32.3.2")

    implementation("org.apache.lucene:lucene-core:6.4.1")

    implementation("com.vladsch.flexmark:flexmark-all:0.62.2")
    implementation("com.github.ajalt:clikt:2.8.0")

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
