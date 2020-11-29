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
package com.monkopedia.scriptorium

import com.monkopedia.imdex.KorpusManager.KorpusType
import com.monkopedia.imdex.Scriptorium.KorpusInfo
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.sql.Connection
import java.util.UUID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

object KorpusTable : LongIdTable("korpus") {
    val config = text("config").default("{}")
}

private val serializer = KorpusInfo.serializer()

class KorpusDao(id: EntityID<Long>) : LongEntity(id) {

    var config by KorpusTable.config.transform(
        { config ->
            json.encodeToString(serializer, config)
        },
        { c ->
            json.decodeFromString(serializer, c)
        }
    )

    companion object : LongEntityClass<KorpusDao>(KorpusTable, KorpusDao::class.java)
}

private val typeSerializer = KorpusType.serializer()

object TypeTable : LongIdTable("type") {
    val type = varchar("type", 128).index(isUnique = true)
    val config = text("config").default("{}")
}

class KorpusTypeDao(id: EntityID<Long>) : LongEntity(id) {

    var type by TypeTable.type
    var config by TypeTable.config.transform(
        { config ->
            json.encodeToString(typeSerializer, config)
        },
        { c ->
            json.decodeFromString(typeSerializer, c)
        }
    )

    companion object : LongEntityClass<KorpusTypeDao>(TypeTable, KorpusTypeDao::class.java)
}

object ConfigTable : UUIDTable("config") {
    val profile = integer("profile")
    val key = varchar("key", 64)
    val value = varchar("value", 512).nullable()

    init {
        uniqueIndex(profile, key)
    }
}

class ConfigDao(entityID: EntityID<UUID>) : UUIDEntity(entityID) {
    var profile by ConfigTable.profile
    var key by ConfigTable.key
    var value by ConfigTable.value

    companion object : UUIDEntityClass<ConfigDao>(ConfigTable, ConfigDao::class.java)
}

object StateDatabase {
    lateinit var database: Database

    fun init(config: Config) {
        val db = File(config.homeFile, "state.db")
        val dbConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${db.absolutePath}"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 1
        }
        val dataSource = HikariDataSource(dbConfig)
        database = Database.connect(dataSource)
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction(db = database) {
            SchemaUtils.createMissingTablesAndColumns(KorpusTable, TypeTable, ConfigTable)
        }
    }
}
