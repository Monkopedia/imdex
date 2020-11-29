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
package com.monkopedia.imdex.browser

import com.monkopedia.imdex.Profile
import com.monkopedia.imdex.ProfileManager.Companion.GLOBAL
import com.monkopedia.imdex.get
import com.monkopedia.markdown.withResources
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.paddingLeft
import kotlinx.css.paddingRight
import kotlinx.css.px
import react.RBuilder
import react.RProps
import react.RState
import react.dom.h1
import styled.css
import styled.styledDiv

external interface SettingsState : RState {
    var isReady: Boolean?
}

class SettingsScreen : LifecycleComponent<RProps, SettingsState>() {
    override fun RBuilder.render() {
        if (state.isReady != true) {
            styledDiv {
                css {
                    paddingLeft = 16.px
                    paddingRight = 16.px
                }
                h1 { +"Loading..." }
            }
            GlobalScope.launch {
                val profile = GLOBAL
                loadProfile(profile)
            }
        } else {
            styledDiv {
                css {
                    paddingLeft = 16.px
                    paddingRight = 16.px
                }

                h1 { +"Settings" }
            }
        }
    }

    private suspend fun loadProfile(profile: Int) = withResources {
        val profileManager = iMDexService.scriptorium().profileManager(Unit).use()
        val profileService = profileManager.profile(profile).use()
        val korpusManager = iMDexService.scriptorium().korpusManager(Unit).use()
        val info = profileManager.getProfileInfo(profile)
        val allKorpii = iMDexService.scriptorium().getKorpii(Unit)
        val enabledKorpii = profileService.get(Profile.ENABLED_KORPII)
    }
}
