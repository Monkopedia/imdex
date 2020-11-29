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

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File
import kotlinx.serialization.Serializable

private val mapper = XmlMapper(
    JacksonXmlModule().apply {
        setDefaultUseWrapper(false)
    }
).registerKotlinModule()
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

@Serializable
data class MavenArtifact(
    val pkg: String,
    val target: String,
    val version: String? = null
) {
    val isResolved get() = version != null

    override fun toString(): String {
        if (version != null) {
            return "$pkg:$target:$version"
        }
        return shortString()
    }

    fun shortString(): String = "$pkg:$target"

    companion object {
        fun from(spec: String): MavenArtifact {
            val parts = spec.split(":")
            return if (parts.size < 3) {
                MavenArtifact(parts[0], parts[1])
            } else {
                MavenArtifact(parts[0], parts[1], parts[2])
            }
        }
    }
}

interface MavenClient {

    suspend fun has(artifact: MavenArtifact): Boolean
    suspend fun resolve(artifact: MavenArtifact): MavenArtifact
    suspend fun getSources(artifact: MavenArtifact, output: File)

    companion object {
        fun forUrls(vararg urls: String): MavenClient {
            return MultiClient(urls.map { MavenClientImpl(it) })
        }
    }
}

private class MultiClient(private val clients: List<MavenClient>) : MavenClient {
    override suspend fun has(artifact: MavenArtifact): Boolean = clients.any { it.has(artifact) }

    override suspend fun resolve(artifact: MavenArtifact): MavenArtifact {
        if (artifact.isResolved) {
            return artifact
        }
        return clients.find { it.has(artifact) }?.resolve(artifact)
            ?: throw IllegalArgumentException("Can't find repo holding $artifact")
    }

    override suspend fun getSources(artifact: MavenArtifact, output: File) {
        clients.find { it.has(artifact) }?.getSources(artifact, output)
            ?: throw IllegalArgumentException("Can't find repo holding $artifact")
    }
}

val client = HttpClient()

private class MavenClientImpl(private val baseUrl: String) : MavenClient {

    private val metadataCache =
        mutableMapOf<MavenArtifact, Pair<Boolean, MetadataFormat.Metadata?>>()

    override suspend fun has(artifact: MavenArtifact): Boolean {
        val (present, _) = metadataCache[artifact] ?: fetchMetadata(artifact)
        return present
    }

    override suspend fun resolve(artifact: MavenArtifact): MavenArtifact {
        if (artifact.isResolved) {
            return artifact
        }
        val (present, metadata) = metadataCache[artifact] ?: fetchMetadata(artifact)
        if (!present) {
            throw IllegalArgumentException("Can't find $artifact")
        }
        return artifact.copy(
            version = metadata?.versioning?.latest
                ?: metadata?.versioning?.versions?.version?.sorted()?.max()
                ?: throw IllegalArgumentException("No versions found for $artifact")
        )
    }

    private suspend fun fetchMetadata(artifact: MavenArtifact):
        Pair<Boolean, MetadataFormat.Metadata?> {
            val response: HttpResponse = client.get(
                "$baseUrl/${artifact.pkg.replace(".", "/")}/" +
                    "${artifact.target}/maven-metadata.xml"
            )
            return (
                if (response.status.value != 200) {
                    Pair(false, null)
                } else {
                    Pair(
                        true,
                        mapper.readValue<MetadataFormat.Metadata>(response.content.toInputStream())
                    )
                }
                ).also {
                metadataCache[artifact] = it
            }
        }

    override suspend fun getSources(artifact: MavenArtifact, output: File) {
        if (!artifact.isResolved) {
            throw IllegalArgumentException("Can't download unresolved $artifact")
        }
        val content = client.get<ByteArray>(
            "$baseUrl/${artifact.pkg.replace(".", "/")}/" +
                "${artifact.target}/${artifact.version}/" +
                "${artifact.target}-${artifact.version}-sources.jar"
        )
        output.writeBytes(content)
    }
}

object MetadataFormat {
    @JsonSerialize
    data class Metadata(val versioning: Versioning? = null)

    @JsonSerialize
    @JsonRootName("versioning")
    data class Versioning(
        val latest: String? = null,
        val release: String? = null,
        val versions: Versions? = null,
        val lastUpdated: String? = null
    )

    @JsonSerialize
    @JsonRootName("versions")
    data class Versions(val version: List<String>? = null)
}
