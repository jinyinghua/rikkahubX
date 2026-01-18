package me.rerere.rikkahub.service.assist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.service.ChatService
import me.rerere.rikkahub.ui.components.ai.ChatInput
import me.rerere.rikkahub.ui.hooks.ChatInputState
import me.rerere.rikkahub.ui.theme.RikkahubTheme
import org.koin.android.ext.android.inject
import kotlin.uuid.Uuid

class AssistActivity : ComponentActivity() {
    private val chatService by inject<ChatService>()
    private val settingsStore by inject<SettingsStore>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        val screenshotPath = intent.getStringExtra("screenshot_path")
        val assistTextFile = java.io.File(cacheDir, "assist_text.txt")
        val assistText = if (assistTextFile.exists()) assistTextFile.readText() else null
        val conversationId = Uuid.random()

        setContent {
            RikkahubTheme {
                val scope = rememberCoroutineScope()
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                var showSheet by remember { mutableStateOf(true) }
                val chatInputState = remember { ChatInputState() }
                
                LaunchedEffect(Unit) {
                    chatService.initializeConversation(conversationId)
                }

                if (showSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { 
                            showSheet = false
                            finish()
                        },
                        sheetState = sheetState,
                        dragHandle = null,
                        containerColor = Color.Transparent,
                        scrimColor = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            ChatInput(
                                state = chatInputState,
                                onSend = { content ->
                                    val finalContent = buildList {
                                        if (screenshotPath != null) {
                                            add(UIMessagePart.Image(screenshotPath.toUri().toString()))
                                        }
                                        if (!assistText.isNullOrBlank()) {
                                            add(UIMessagePart.Text("Screen Context:\n$assistText"))
                                        }
                                        addAll(content)
                                    }
                                    chatService.sendMessage(conversationId, finalContent)
                                    // 发送后跳转到主应用的聊天页面
                                    val intent = android.content.Intent(this@AssistActivity, me.rerere.rikkahub.RouteActivity::class.java).apply {
                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        putExtra("conversationId", conversationId.toString())
                                    }
                                    startActivity(intent)
                                    finish()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
