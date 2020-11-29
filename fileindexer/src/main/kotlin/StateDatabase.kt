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
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object MdFileTable : LongIdTable("md_files") {
    val file = text("file")
    val hash = text("hash")
}

class MdFileDao(id: EntityID<Long>) : LongEntity(id) {
    var file by MdFileTable.file
    var hash by MdFileTable.hash

    companion object : LongEntityClass<MdFileDao>(MdFileTable, MdFileDao::class.java)
}
