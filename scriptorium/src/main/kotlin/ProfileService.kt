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

import com.monkopedia.imdex.IntKey
import com.monkopedia.imdex.Profile
import com.monkopedia.imdex.ProfileManager
import com.monkopedia.imdex.ProfileManager.Companion.DEFAULT_CMD
import com.monkopedia.imdex.ProfileManager.Companion.DEFAULT_WEB
import com.monkopedia.imdex.ProfileManager.Companion.GLOBAL
import com.monkopedia.imdex.get
import com.monkopedia.ksrpc.Service
import java.lang.ref.WeakReference
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

private const val defaultName = "New Profile"

class ProfileManagerService(private val scriptorium: ScriptoriumService) :
    Service(), ProfileManager {
    private val profileLock = Mutex()
    private val profileCache = mutableMapOf<Int, WeakReference<Profile>>()
    private val profiles by lazy {
        mutableMapOf<Int, ProfileManager.ProfileInfo>().also { profiles ->
            transaction {

                ConfigDao.find {
                    ConfigTable.key eq Profile.PROFILE.key
                }.forEach {
                    profiles[it.profile] =
                        ProfileManager.ProfileInfo(it.profile, it.value ?: defaultName)
                }
                if (profiles.isEmpty()) {
                    ConfigDao.new {
                        profile = GLOBAL
                        key = Profile.PROFILE.key
                        value = defaultName
                    }
                    profiles[GLOBAL] = ProfileManager.ProfileInfo(GLOBAL, defaultName)
                }
            }
        }
    }
    private var defaultWeb: Int
    private var defaultCmd: Int

    init {
        defaultWeb = DEFAULT_WEB.redirect ?: GLOBAL
        defaultCmd = DEFAULT_CMD.redirect ?: GLOBAL
    }

    override suspend fun profile(id: Int): Profile = profileLock.withLock {
        val id = resolve(id)
        return profileCache[id]?.get() ?: ProfileService(this, id).also {
            profileCache[id] = WeakReference(it)
        }
    }

    override suspend fun getProfileInfo(id: Int): ProfileManager.ProfileInfo {
        val id = resolve(id)
        return profiles[id] ?: throw IllegalArgumentException("Profile $id does not exist")
    }

    override suspend fun getProfiles(u: Unit): List<ProfileManager.ProfileInfo> =
        profileLock.withLock {
            return profiles.values.toList()
        }

    override suspend fun createProfile(label: String): ProfileManager.ProfileInfo =
        profileLock.withLock {
            val maxId = profiles.keys.maxOrNull() ?: 0
            val newId = maxId + 1
            transaction {
                ConfigDao.new {
                    profile = newId
                    key = Profile.PROFILE.key
                    value = label
                }
            }
            return ProfileManager.ProfileInfo(newId, label).also {
                profiles[newId] = it
            }
        }

    private fun resolve(id: Int): Int = when (id) {
        DEFAULT_CMD -> defaultCmd
        DEFAULT_WEB -> defaultWeb
        else -> id
    }

    override suspend fun setCmdDefault(id: Int) {
        if (id != defaultCmd) {
            DEFAULT_CMD.redirect = id
            defaultCmd = id
        }
    }

    override suspend fun setWebDefault(id: Int) {
        if (id != defaultWeb) {
            DEFAULT_WEB.redirect = id
            defaultWeb = id
        }
    }

    internal suspend fun getKorpii(profile: Int): List<String> {
        if (profile == GLOBAL) {
            return scriptorium.getKorpii(Unit).map { it.id }
        }
        return profile(profile).get(Profile.ENABLED_KORPII)
    }

    internal suspend fun notifyNameChange(profile: Int, value: Profile.ProfileValue) =
        profileLock.withLock {
            profiles[profile] = ProfileManager.ProfileInfo(profile, value.value ?: defaultName)
        }

    var Int.redirect: Int?
        get() {
            return transaction {
                ConfigDao.find {
                    ConfigTable.profile eq this@redirect and (ConfigTable.key eq REDIRECT_KEY.key)
                }.firstOrNull()?.value?.toIntOrNull()
            }
        }
        set(value) {
            transaction {
                var dao = ConfigDao.find {
                    ConfigTable.profile eq this@redirect and (ConfigTable.key eq REDIRECT_KEY.key)
                }.firstOrNull()
                if (dao == null) {
                    dao = ConfigDao.new {
                        profile = this@redirect
                        key = REDIRECT_KEY.key
                    }
                }
                dao.value = value.toString()
            }
        }

    companion object {
        private val REDIRECT_KEY = IntKey("redirect")
    }
}

class ProfileService(private val parent: ProfileManagerService, private val profile: Int) :
    Service(), Profile {
    private val lock = Mutex()

    private val data by lazy {
        mutableMapOf<String, Pair<UUID, String?>>().also { data ->
            transaction {
                ConfigDao.find {
                    ConfigTable.profile eq profile
                }.forEach {
                    data[it.key] = Pair(it.id.value, it.value)
                }
            }
        }
    }

    override suspend fun getId(u: Unit): Int {
        return profile
    }

    override suspend fun getLabel(u: Unit): String {
        return get(Profile.PROFILE)
    }

    override suspend fun get(key: String): Profile.ProfileValue {
        return Profile.ProfileValue(key, data[key]?.second)
    }

    override suspend fun set(value: Profile.ProfileValue): Unit = lock.withLock {
        val d = data[value.key]
        if (value.value != d?.second) {
            if (value.key == Profile.ENABLED_KORPII.key && profile == GLOBAL) {
                throw IllegalArgumentException("Cannot modify global korpii")
            }
            val id = newSuspendedTransaction {
                if (d != null) {
                    ConfigDao.findById(d.first)!!.also {
                        it.value = value.value
                    }.id.value
                } else {
                    val n = ConfigDao.new {
                        this.profile = this@ProfileService.profile
                        this.key = value.key
                        this.value = value.value
                    }
                    commit()
                    n.id.value
                }
            }
            data[value.key] = Pair(id, value.value)
            if (value.key == Profile.PROFILE.key) {
                parent.notifyNameChange(profile, value)
            }
        }
    }
}
