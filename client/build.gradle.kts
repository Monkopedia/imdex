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

    id("kotlin-dce-js")
    application
}

version = "0.1"

repositories {
    jcenter()
    mavenLocal()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
    maven(url = "https://kotlinx.bintray.com/kotlinx/")
    maven("https://kotlin.bintray.com/kotlin-js-wrappers")
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
        api("com.monkopedia:ksrpc:0.1.1")
        api("com.monkopedia:kpages:0.0.5")
        api(project(":protocol"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9-native-mt")
        api("io.ktor:ktor-io:1.4.0")
    }
    sourceSets["jvmMain"].dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
        implementation("org.slf4j:slf4j-api:1.6.1")
        implementation(project(":markdown"))
        api("com.monkopedia:lanterna-ext:0.0.5")
        implementation("io.ktor:ktor-server-core:1.4.0")
        implementation("io.ktor:ktor-server-host-common:1.4.0")
        implementation("io.ktor:ktor-server-netty:1.4.0")
        implementation("io.ktor:ktor-client-core:1.4.0")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
        implementation("com.github.ajalt:clikt:2.8.0")
        implementation("ch.qos.logback:logback-classic:1.2.3")
    }
    sourceSets["jvmTest"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-test")
        implementation("org.jetbrains.kotlin:kotlin-test-junit")
    }
    sourceSets["jsMain"].dependencies {
        compileOnly("io.ktor:ktor-client-core:1.4.0")
        compileOnly("io.ktor:ktor-client-js:1.4.0")
        implementation("org.jetbrains:kotlin-css:1.0.0-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-styled:5.2.0-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react:16.13.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.124-kotlin-1.4.10")
        implementation("com.ccfraser.muirwik:muirwik-components:0.6.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
        implementation("io.ktor:ktor-http:1.5.0")
        implementation("io.ktor:ktor-http-cio:1.5.0")
        implementation("io.ktor:ktor-client-core:1.5.0")
        implementation("io.ktor:ktor-io:1.5.0")
        implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-css:1.0.0-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-styled:5.2.0-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react:16.13.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
        implementation("com.ccfraser.muirwik:muirwik-components:0.6.2")
        implementation("com.monkopedia:kpages:0.0.5")
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

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinJsDce::class) {
    keep += "kotlin.defineModule"
    keep += "io.ktor.http.Headers"
    println("Adding to $name")
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xskip-prerelease-check"
        freeCompilerArgs += "-Xno-param-assertions"
    }
}

application {
    mainClassName = "com.monkopedia.imdex.AppKt"
}
