package com.monkopedia.imdex

import com.monkopedia.kpages.ViewControllerFactory
import com.monkopedia.kpages.preferences.PreferenceAdapter
import com.monkopedia.kpages.preferences.PreferenceBuilder
import com.monkopedia.kpages.preferences.PreferenceScreen
import com.monkopedia.kpages.preferences.SelectionOption
import com.monkopedia.kpages.preferences.option
import com.monkopedia.kpages.preferences.preference
import com.monkopedia.kpages.preferences.preferenceCategory
import com.monkopedia.kpages.preferences.selectionPreference
import com.monkopedia.kpages.preferences.switchPreference
import com.monkopedia.kpages.preferences.textInputPreference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

expect suspend fun getScriptorium(): Scriptorium

val rootSettingsFactory: ViewControllerFactory = PreferenceScreen {
    RootSettings()
}


val profileSettingsFactory: ViewControllerFactory = PreferenceScreen { path ->
    val profileId = path.split("/").last()
    ProfileSettings(profileId)
}

val korpusSettingsFactory: ViewControllerFactory = PreferenceScreen { path ->
    val korpusId = path.split("/").last()
    KorpusSettings(korpusId)
}

data class SelectionProfileInfo(
    val info: ProfileManager.ProfileInfo
) : SelectionOption {
    override var label: String = info.label
}

abstract class LoadableSettings : PreferenceAdapter() {
    private var hasLoaded = false
        set(value) {
            field = value
            println("Notify changed")
            notifyChanged()
            println("Notify changed done")
        }
    final override val title: String
        get() = (if (hasLoaded) loadedTitle else "Loading...").also {
            println("Getting title $it")
        }
    abstract val loadedTitle: String

    final override fun PreferenceBuilder.build() {
        if (hasLoaded) {
            buildLoaded()
        } else {
            GlobalScope.launch {
                load()
                hasLoaded = true
            }
        }
    }

    abstract suspend fun load()
    abstract fun PreferenceBuilder.buildLoaded()
}

class RootSettings : LoadableSettings() {
    private lateinit var defaultWeb: SelectionProfileInfo
    private lateinit var defaultCmd: SelectionProfileInfo
    private lateinit var korpii: List<Scriptorium.KorpusInfo>
    private lateinit var profiles: List<SelectionProfileInfo>
    private lateinit var korpusManager: KorpusManager
    private lateinit var profileManager: ProfileManager
    private lateinit var scriptorium: Scriptorium
    override val loadedTitle: String = "Settings"

    override fun PreferenceBuilder.buildLoaded() {
        preferenceCategory("Defaults") {
            selectionPreference<SelectionProfileInfo> {
                title = "Default CMD Profile"
                initialState = defaultCmd
                profiles.forEach(::option)
                onChange = {
                    profileManager.setCmdDefault(it?.info?.id ?: error("Nothing selected"))
                }
                selectionPreference<SelectionProfileInfo> {
                    title = "Default CMD Profile"
                    initialState = defaultWeb
                    profiles.forEach(::option)
                    onChange = {
                        profileManager.setWebDefault(it?.info?.id ?: error("Nothing selected"))
                    }
                }
            }
        }
        preferenceCategory("Profiles") {
            for (profile in profiles) {
                preference {
                    title = profile.label
                    onClick = {
                        navigator.push("/profile/${profile.info.id}")
                    }
                }
            }
            preference {
                title = "Create new profile"
                onClick = {
                    val profile = profileManager.createProfile("New profile")
                    navigator.push("/profile/${profile.id}")
                }
            }
        }
        preferenceCategory("Korpii") {
            for (korpus in korpii) {
                preference {
                    title = korpus.label
                    onClick = {
                        navigator.push("/korpus/${korpus.id}")
                    }
                }
            }
            preference {
                title = "Create new korpus"
                onClick = {
                    val korpus = korpusManager.createKorpus(
                        KorpusManager.CreateKorpus(
                            KorpusManager.CreateKorpus.DEFAULT_TYPE,
                            emptyMap()
                        )
                    )
                    navigator.push("/korpus/$korpus")
                }
            }
        }
    }

    override suspend fun load() {
        scriptorium = getScriptorium()
        profileManager = scriptorium.profileManager(Unit)
        korpusManager = scriptorium.korpusManager(Unit)
        profiles = profileManager.getProfiles(Unit).map(::SelectionProfileInfo)
        korpii = scriptorium.getKorpii(Unit)
        defaultCmd = SelectionProfileInfo(profileManager.getProfileInfo(ProfileManager.DEFAULT_CMD))
        defaultWeb = SelectionProfileInfo(profileManager.getProfileInfo(ProfileManager.DEFAULT_WEB))
    }
}

class ProfileSettings(private val profileId: String) : LoadableSettings() {

    private lateinit var profileInfo: ProfileManager.ProfileInfo
    private lateinit var scriptorium: Scriptorium
    private lateinit var profileManager: ProfileManager
    private lateinit var profile: Profile
    private lateinit var korpii: List<Scriptorium.KorpusInfo>
    private lateinit var enabledKorpii: MutableList<String>
    private var lastLabel: String? = null

    override val loadedTitle: String
        get() = profileInfo.label

    override fun PreferenceBuilder.buildLoaded() {
        preferenceCategory("Profile settings") {
            textInputPreference {
                title = "Label"
                subtitle = lastLabel ?: profileInfo.label
                value = lastLabel ?: profileInfo.label
                onChange = {
                    profile.set(Profile.PROFILE, it)
                    lastLabel = it
                    notifyChanged()
                }
            }
        }
        preferenceCategory("Enable korpii") {
            for (korpus in korpii) {
                switchPreference {
                    title = korpus.label
                    initialState = enabledKorpii.contains(korpus.id)
                    onChange = { enabled ->
                        if (enabled) {
                            enabledKorpii.add(korpus.id)
                        } else {
                            enabledKorpii.remove(korpus.id)
                        }
                        profile.set(Profile.ENABLED_KORPII, enabledKorpii)
                    }
                }
            }
        }
    }

    override suspend fun load() {
        scriptorium = getScriptorium()
        profileManager = scriptorium.profileManager(Unit)
        profileInfo = profileManager.getProfileInfo(
            profileId.toIntOrNull() ?: error("Unrecognized profile $profileId")
        )
        profile = profileManager.profile(
            profileId.toIntOrNull() ?: error("Unrecognized profile $profileId")
        )
        korpii = scriptorium.getKorpii(Unit)
        enabledKorpii = profile.get(Profile.ENABLED_KORPII).toMutableList()
    }
}

class KorpusSettings(private val korpusId: String) : LoadableSettings() {
    private lateinit var korpusInfo: Scriptorium.KorpusInfo
    private lateinit var korpusManager: KorpusManager
    private lateinit var scriptorium: Scriptorium
    private lateinit var type: KorpusManager.KorpusType
    override val loadedTitle: String
        get() = korpusInfo.label

    override fun PreferenceBuilder.buildLoaded() {
        preferenceCategory("Korpus settings") {
            for (key in type.keys) {
                val singleType = key.type.type
                if (singleType != null) {
                    when (singleType) {
                        KorpusManager.DataType.PATH,
                        KorpusManager.DataType.ARTIFACT,
                        KorpusManager.DataType.INT,
                        KorpusManager.DataType.STRING ->  {
                            textInputPreference {
                                title = key.displayName
                                subtitle = korpusInfo.config[key.key]
                                value = korpusInfo.config[key.key]
                                title = "Change ${key.displayName}"
                                onChange = { value ->
                                    val newKorpusInfo = korpusInfo.copy(config = korpusInfo.config.toMutableMap().also {
                                        it[key.key] = value
                                    })
                                    korpusManager.updateKorpus(KorpusManager.UpdateKorpus(newKorpusInfo.id, newKorpusInfo.config))
                                    korpusInfo = newKorpusInfo
                                    notifyChanged()
                                }
                            }
                        }
                        KorpusManager.DataType.BOOLEAN -> {
                            switchPreference {
                                title = key.displayName
                                initialState = korpusInfo.boolean(key)
                                onChange = { value ->
                                    val newKorpusInfo = korpusInfo.copy(config = korpusInfo.config.toMutableMap().also {
                                        it[key.key] = value.toString()
                                    })
                                    korpusManager.updateKorpus(KorpusManager.UpdateKorpus(newKorpusInfo.id, newKorpusInfo.config))
                                    korpusInfo = newKorpusInfo
                                    notifyChanged()
                                }
                            }
                        }
                    }
                } else {
                    // Lists unsupported for now.
                }
            }
        }
    }

    override suspend fun load() {
        scriptorium = getScriptorium()
        korpusManager = scriptorium.korpusManager(Unit)
        korpusInfo = scriptorium.getKorpii(Unit).find { it.id == korpusId }
            ?: error("Can't find korpus $korpusId")
        type = korpusManager.getKorpusType(korpusInfo.type) ?: error("Unrecognized type ${korpusInfo.type}")
    }
}