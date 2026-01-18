package me.shaun.rikkahubx.ui.pages.developer

import androidx.lifecycle.ViewModel
import me.shaun.rikkahubx.data.ai.AILoggingManager

class DeveloperVM(
    private val aiLoggingManager: AILoggingManager
) : ViewModel() {
    val logs = aiLoggingManager.getLogs()
}
