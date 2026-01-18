package me.shaun.rikkahubx.ui.context

import androidx.compose.runtime.staticCompositionLocalOf
import me.shaun.rikkahubx.data.datastore.Settings

val LocalSettings = staticCompositionLocalOf<Settings> {
    error("No SettingsStore provided")
}
