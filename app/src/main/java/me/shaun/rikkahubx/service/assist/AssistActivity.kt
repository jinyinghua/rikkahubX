package me.shaun.rikkahubx.service.assist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.ToastType
import kotlinx.coroutines.launch
import me.rerere.ai.ui.UIMessagePart
import me.shaun.rikkahubx.R
import me.shaun.rikkahubx.BuildConfig
import me.shaun.rikkahubx.data.datastore.SettingsStore
import me.shaun.rikkahubx.data.datastore.getCurrentAssistant
import me.shaun.rikkahubx.service.ChatService
import me.shaun.rikkahubx.ui.components.ai.ChatInput
import me.shaun.rikkahubx.ui.context.LocalToaster
import me.shaun.rikkahubx.ui.hooks.rememberChatInputState
import me.shaun.rikkahubx.ui.pages.chat.ChatList
import me.shaun.rikkahubx.ui.pages.chat.ChatVM
import me.shaun.rikkahubx.ui.theme.RikkahubTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale
import kotlin.uuid.Uuid

class AssistActivity : ComponentActivity() {
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
                val vm: ChatVM = koinViewModel(
                    parameters = {
                        parametersOf(conversationId.toString())
                    }
                )
                val scope = rememberCoroutineScope()
                val toaster = LocalToaster.current
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                var showSheet by remember { mutableStateOf(true) }
                
                val setting by vm.settings.collectAsStateWithLifecycle()
                val conversation by vm.conversation.collectAsStateWithLifecycle()
                val loadingJob by vm.conversationJob.collectAsStateWithLifecycle()
                val currentChatModel by vm.currentChatModel.collectAsStateWithLifecycle()
                val enableWebSearch by vm.enableWebSearch.collectAsStateWithLifecycle()
                val errors by vm.errors.collectAsStateWithLifecycle()

                val chatInputState = rememberChatInputState()
                val chatListState = rememberLazyListState()

                LaunchedEffect(loadingJob) {
                    chatInputState.loading = loadingJob != null
                    if (loadingJob != null) {
                        chatListState.animateScrollToItem(conversation.currentMessages.size + 5)
                    }
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.75f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ChatList(
                                    innerPadding = PaddingValues(0.dp),
                                    conversation = conversation,
                                    state = chatListState,
                                    loading = loadingJob != null,
                                    previewMode = false,
                                    settings = setting,
                                    errors = errors,
                                    onDismissError = { vm.dismissError(it) },
                                    onClearAllErrors = { vm.clearAllErrors() },
                                    onRegenerate = { vm.regenerateAtMessage(it) },
                                    onEdit = {
                                        chatInputState.editingMessage = it.id
                                        chatInputState.setContents(it.parts)
                                    },
                                    onForkMessage = {
                                        // 跳转到主应用
                                        val intent = android.content.Intent(this@AssistActivity, me.shaun.rikkahubx.RouteActivity::class.java).apply {
                                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            putExtra("conversationId", conversationId.toString())
                                        }
                                        startActivity(intent)
                                        finish()
                                    },
                                    onDelete = { vm.deleteMessage(it) },
                                    onUpdateMessage = { newNode ->
                                        vm.updateConversation(
                                            conversation.copy(
                                                messageNodes = conversation.messageNodes.map { node ->
                                                    if (node.id == newNode.id) newNode else node
                                                }
                                            )
                                        )
                                        vm.saveConversationAsync()
                                    },
                                    onClickSuggestion = { suggestion ->
                                        chatInputState.editingMessage = null
                                        chatInputState.setMessageText(suggestion)
                                    },
                                    onTranslate = { message, locale ->
                                        vm.translateMessage(message, locale)
                                    },
                                    onClearTranslation = { message ->
                                        vm.clearTranslationField(message.id)
                                    },
                                    onJumpToMessage = { index ->
                                        scope.launch {
                                            chatListState.animateScrollToItem(index)
                                        }
                                    }
                                )
                            }

                            ChatInput(
                                state = chatInputState,
                                settings = setting,
                                conversation = conversation,
                                mcpManager = vm.mcpManager,
                                onCancelClick = {
                                    loadingJob?.cancel()
                                },
                                enableSearch = enableWebSearch,
                                onToggleSearch = {
                                    vm.updateSettings(setting.copy(enableWebSearch = !enableWebSearch))
                                },
                                onSendClick = {
                                    if (currentChatModel == null) {
                                        toaster.show("请先选择模型", type = ToastType.Error)
                                        return@ChatInput
                                    }
                                    val content = chatInputState.getContents()
                                    val finalContent = if (conversation.messageNodes.isEmpty()) {
                                        buildList {
                                            if (screenshotPath != null) {
                                                add(UIMessagePart.Image(screenshotPath.toUri().toString()))
                                            }
                                            if (!assistText.isNullOrBlank()) {
                                                add(UIMessagePart.Text("Screen Context:\n$assistText"))
                                            }
                                            addAll(content)
                                        }
                                    } else {
                                        content
                                    }
                                    
                                    if (chatInputState.isEditing()) {
                                        vm.handleMessageEdit(
                                            parts = finalContent,
                                            messageId = chatInputState.editingMessage!!,
                                        )
                                    } else {
                                        vm.handleMessageSend(finalContent)
                                        scope.launch {
                                            chatListState.requestScrollToItem(conversation.currentMessages.size + 5)
                                        }
                                    }
                                    chatInputState.clearInput()
                                },
                                onLongSendClick = {
                                    if (chatInputState.isEditing()) {
                                        vm.handleMessageEdit(
                                            parts = chatInputState.getContents(),
                                            messageId = chatInputState.editingMessage!!,
                                        )
                                    } else {
                                        vm.handleMessageSend(content = chatInputState.getContents(), answer = false)
                                        scope.launch {
                                            chatListState.requestScrollToItem(conversation.currentMessages.size + 5)
                                        }
                                    }
                                    chatInputState.clearInput()
                                },
                                onUpdateChatModel = {
                                    vm.setChatModel(assistant = setting.getCurrentAssistant(), model = it)
                                },
                                onUpdateAssistant = {
                                    vm.updateSettings(
                                        setting.copy(
                                            assistants = setting.assistants.map { assistant ->
                                                if (assistant.id == it.id) it else assistant
                                            }
                                        )
                                    )
                                },
                                onUpdateSearchService = { index ->
                                    vm.updateSettings(
                                        setting.copy(
                                            searchServiceSelected = index
                                        )
                                    )
                                },
                                onClearContext = {
                                    vm.handleMessageTruncate()
                                },
                                modifier = Modifier.padding(16.dp)
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
