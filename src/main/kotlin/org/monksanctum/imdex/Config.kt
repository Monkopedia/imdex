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
package org.monksanctum.imdex

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val json: Json by lazy {
    Json { isLenient = true }
}

inline fun <reified T : Any> File.readJson(): T =
    json.decodeFromString(if (exists()) readText() else "{}")

inline fun <reified T : Any> File.writeJson(obj: T) =
    writeText(json.encodeToString(obj))

internal val defaultKindexHome: File
    get() = File(File(System.getenv("HOME")), ".imdex").also { it.mkdirs() }

@Serializable
data class Config(
    val home: String = defaultKindexHome.absolutePath
) {
    val homeFile: File get() = File(home)
}

inline fun File.withDefault(creator: () -> String): File = also {
    if (!it.exists()) {
        it.writeText(creator())
    }
}

val Config.themeFile: File
    get() = File(homeFile, "theme.json").withDefault { defaultThemeText }

val Config.fontFile: File
    get() = File(homeFile, "fonts.properties").withDefault { defaultFontText }

private const val defaultFontText = """
h1=http://www.figlet.org/fonts/starwars.flf
h2=http://www.figlet.org/fonts/straight.flf
"""

private const val defaultThemeText =
    """
{
  "colors": {
    "primary": "#81c784",
    "secondary": "#b2fab4",
    "tertiary": "#519657",
    "text": "#fafafa",
    "disabledText": "#fafafa",
    "bg": "#282828",
    "bgHighlight": "#363636",
    "disabled": "#969696",
    "blackeled": "#000000"
  },
  "default": {
    "normal": {
      "foreground": "text",
      "background": "black",
      "sgrs": []
    },
    "selected": {
      "foreground": "bg",
      "background": "primary",
      "sgrs": ["BOLD"]
    },
    "active": {
      "foreground": "bg",
      "background": "primary",
      "sgrs": ["BOLD"]
    },
    "prelight": {
      "foreground": "primary",
      "background": "black",
      "sgrs": []
    },
    "insensitive": {
      "foreground": "disabledText",
      "background": "black",
      "sgrs": []
    }
  },
  "postRenderer": null,
  "themeOverrides": {
    "com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer": {
      "prelight": {
        "foreground": "text",
        "background": "black"
      }
    },
    "mdview.Style": {
      "custom": {
        "LINK": {
          "foreground": "primary",
          "background": "bg",
          "sgrs": ["BOLD"]
        },
        "LINK_SELECTED": {
          "foreground": "bg",
          "background": "primary",
          "sgrs": ["BOLD"]
        },
        "CODE_BLOCK": {
          "foreground": "text",
          "background": "bg",
          "sgrs": []
        },
        "HEADER_PRIMARY": {
          "foreground": "text",
          "background": "black",
          "sgrs": ["UNDERLINE", "BOLD"]
        },
        "HEADER_SECONDARY": {
          "foreground": "text",
          "background": "black",
          "sgrs": ["BOLD"]
        }
      },
      "properties": {
        "ASCII_FONTS": true
      }
    }
  }
}

"""
