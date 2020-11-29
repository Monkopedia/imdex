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
package com.monkopedia.kindex

import com.monkopedia.imdex.Scriptorium.KorpusInfo
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class Downloader(
    private val config: KorpusInfo,
    private val mavenClient: MavenClient
) {
    val sourcesFile by lazy {
        File(config.homeFile, "sources").also { it.mkdirs() }
    }

    suspend fun ensureUpdated(dao: ArtifactStateDao, artifact: MavenArtifact): Boolean {
        if (!artifact.isResolved) {
            throw IllegalArgumentException("$artifact is not resolved")
        }
        if (dao.version == artifact.version && dao.state >= ArtifactState.DOWNLOADED) {
            // Nothing to do if already downloaded.
            return false
        }
        val jarFile = createTempFile()
        mavenClient.getSources(artifact, jarFile)
        val artifactSources = File(sourcesFile, artifact.copy(version = null).toString())
        if (artifactSources.exists()) {
            artifactSources.deleteRecursively()
        }
        artifactSources.mkdirs()

        val zipIn = ZipInputStream(FileInputStream(jarFile))
        var entry = zipIn.nextEntry
        while (entry != null) {
            val filePath = File(artifactSources, entry.name)
            if (!entry.isDirectory) {
                if (filePath.extension == "kt") {
                    zipIn.copyTo(filePath.outputStream())
                }
            } else {
                filePath.mkdir()
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()

        dao.version = artifact.version.toString()
        dao.state = ArtifactState.DOWNLOADED
        return true
    }
}
