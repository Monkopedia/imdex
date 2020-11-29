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

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

enum class ArtifactState {
    BLANK,
    DOWNLOADED,
    DOKKAD
}

object ArtifactStateTable : LongIdTable("artifacts") {
    val artifact = varchar("artifact", 200)
    val specVersion = varchar("specVersion", 60)
    val version = varchar("version", 60)
    val state = enumerationByName("state", 25, ArtifactState::class).default(ArtifactState.BLANK)
}

class ArtifactStateDao(id: EntityID<Long>) : LongEntity(id) {
    var artifact by ArtifactStateTable.artifact
    var version by ArtifactStateTable.version
    var specVersion by ArtifactStateTable.specVersion
    var state by ArtifactStateTable.state

    companion object :
        LongEntityClass<ArtifactStateDao>(ArtifactStateTable, ArtifactStateDao::class.java)
}
