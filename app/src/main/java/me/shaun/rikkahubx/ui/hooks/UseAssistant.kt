package me.shaun.rikkahubx.ui.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.shaun.rikkahubx.data.datastore.Settings
import me.shaun.rikkahubx.data.datastore.getCurrentAssistant
import me.shaun.rikkahubx.data.model.Assistant

@Composable
fun rememberAssistantState(
    settings: Settings,
    onUpdateSettings: (Settings) -> Unit
): AssistantState {
    return remember(settings, onUpdateSettings) {
        AssistantState(settings, onUpdateSettings)
    }
}

class AssistantState(
    private val settings: Settings,
    private val onUpdateSettings: (Settings) -> Unit
) {
    private var _currentAssistant by mutableStateOf(
        settings.getCurrentAssistant()
    )
    val currentAssistant get() = _currentAssistant

    fun setSelectAssistant(assistant: Assistant) {
        onUpdateSettings(
            settings.copy(
                assistantId = assistant.id
            )
        )
    }
}
