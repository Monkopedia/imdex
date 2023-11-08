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
    mavenCentral()
    mavenLocal()
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
        implementation(libs.kotlinx.coroutines.core)
        implementation(project(":protocol"))
        implementation(libs.ktor.client.curl)
        implementation(libs.clikt)
    }
    sourceSets["nativeMain"].dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(project(":protocol"))
        implementation(libs.ktor.client.curl)
        implementation(libs.clikt)
    }
}
