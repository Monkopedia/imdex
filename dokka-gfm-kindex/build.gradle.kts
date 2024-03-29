/*
 * Copyright 2014-2019 JetBrains s.r.o. and Dokka project contributors.
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
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(libs.dokka.base)
    compileOnly(libs.dokka.core)
}

afterEvaluate {
    extensions.configure(com.github.autostyle.gradle.AutostyleExtension::class) {
        kotlinGradle {
            // Since kotlin doesn't pick up on multi platform projects
            filter.include("**/*.kt")
            ktlint("0.39.0") {
                userData(mapOf("android" to "true"))
            }

            licenseHeader(
                """
                |Copyright 2014-2019 JetBrains s.r.o. and Dokka project contributors.
                |
                |Licensed under the Apache License, Version 2.0 (the "License");
                |you may not use this file except in compliance with the License.
                |You may obtain a copy of the License at
                |
                |    https://www.apache.org/licenses/LICENSE-2.0
                |
                |Unless required by applicable law or agreed to in writing, software
                |distributed under the License is distributed on an "AS IS" BASIS,
                |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                |See the License for the specific language governing permissions and
                |limitations under the License.""".trimMargin()
            )
        }
    }
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xskip-prerelease-check"
    }
}
