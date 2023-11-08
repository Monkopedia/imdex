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
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    java
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm {
        withJava()
    }
    js {
        browser {}
    }
    val hostOs = System.getProperty("os.name")
    when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> mingwX64("native")
    }
    sourceSets["commonMain"].dependencies {
        api(libs.ksrpc)
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.coroutines.core)
    }
    sourceSets["jvmMain"].dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
        implementation(libs.slf4j.api)
        compileOnly(libs.ktor.server.core)
        compileOnly(libs.ktor.server.host.common)
        compileOnly(libs.ktor.server.netty)
        compileOnly(libs.ktor.client.core)

        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.clikt)
        implementation(libs.logback.classic)
    }
    sourceSets["jsMain"].dependencies {
        compileOnly(libs.ktor.client.core)
        compileOnly(libs.ktor.client.js)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xskip-prerelease-check"
        freeCompilerArgs += "-Xno-param-assertions"
    }
}
