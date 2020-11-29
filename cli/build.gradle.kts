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
}

repositories {
    jcenter()
    mavenLocal()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
    maven(url = "https://kotlinx.bintray.com/kotlinx/")
}

kotlin {
    // Determine host preset.
    val hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> mingwX64("native")
        else -> throw GradleException(
            "Host OS '$hostOs' is not supported in Kotlin/Native $project."
        )
    }

    hostTarget.apply {
        binaries {
            executable {
                entryPoint = "com.monkopedia.cli.main"
                runTask?.args(3000)
            }
        }
    }
    sourceSets["commonMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9-native-mt")
        implementation(project(":protocol"))
        implementation("io.ktor:ktor-client-curl:1.4.1")
        implementation("com.github.ajalt.clikt:clikt:3.0.1")
    }
    sourceSets["nativeMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9-native-mt")
        implementation(project(":protocol"))
        implementation("io.ktor:ktor-client-curl:1.4.1")
        implementation("com.github.ajalt.clikt:clikt:3.0.1")
    }
}
