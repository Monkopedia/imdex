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

    application
}

version = "0.1"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm {
        withJava()
    }
    js {
        browser {

            useCommonJs()
            nodejs()
            webpackTask {
                output.libraryTarget =
                    org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target.GLOBAL
            }
            binaries.executable()
        }
    }
    sourceSets["commonMain"].dependencies {
        api(libs.ksrpc)
        api(libs.kpages)
        api(project(":protocol"))
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.coroutines.core)
        api(libs.ktor.io)
    }
    sourceSets["jvmMain"].dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
        implementation(libs.slf4j.api)
        implementation(project(":markdown"))
        api(libs.kpages.lanterna)
        implementation(libs.ktor.server.core)
        implementation(libs.ktor.server.host.common)
        implementation(libs.ktor.server.netty)
        implementation(libs.ktor.client.core)

        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.clikt)
        implementation(libs.logback.classic)
    }
    sourceSets["jvmTest"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-test")
        implementation("org.jetbrains.kotlin:kotlin-test-junit")
    }
    sourceSets["jsMain"].dependencies {
        compileOnly(libs.ktor.client.core)
        compileOnly(libs.ktor.client.js)
        implementation(libs.kotlin.css)
        implementation(libs.kotlin.emotion)
        implementation(libs.kotlin.react)
        implementation(libs.kotlin.react.dom)
        implementation(libs.kotlin.react.router.dom)
        implementation(libs.muirwik.components)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlin.stdlib.js)
        implementation(libs.ktor.http)
        implementation(libs.ktor.http.cio)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.io)
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kpages)
        implementation(project(":protocol"))
        implementation(npm("codemirror", "5.58.3"))
        implementation(npm("showdown", "1.9.1"))
        implementation(npm("css-loader", "3.5.2"))
        implementation(npm("style-loader", "1.1.3"))
        implementation(npm("bootstrap", "^4.4.1"))
        implementation(npm("crypto", "1.0.1"))
        implementation(npm("crypto-browserify", "3.12.0"))
        implementation(npm("react", "~16.13.1"))
        implementation(npm("react-dom", "~16.13.1"))
        implementation(npm("webpack", "4.44.1"))
    }
}

//tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinJsDce::class) {
//    keep += "kotlin.defineModule"
//    keep += "io.ktor.http.Headers"
//    println("Adding to $name")
//}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xskip-prerelease-check"
        freeCompilerArgs += "-Xno-param-assertions"
    }
}

application {
    mainClass.set("com.monkopedia.imdex.AppKt")
}
