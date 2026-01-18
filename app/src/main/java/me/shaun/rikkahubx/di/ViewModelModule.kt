package me.shaun.rikkahubx.di

import me.shaun.rikkahubx.ui.pages.assistant.AssistantVM
import me.shaun.rikkahubx.ui.pages.assistant.detail.AssistantDetailVM
import me.shaun.rikkahubx.ui.pages.backup.BackupVM
import me.shaun.rikkahubx.ui.pages.chat.ChatVM
import me.shaun.rikkahubx.ui.pages.debug.DebugVM
import me.shaun.rikkahubx.ui.pages.developer.DeveloperVM
import me.shaun.rikkahubx.ui.pages.history.HistoryVM
import me.shaun.rikkahubx.ui.pages.imggen.ImgGenVM
import me.shaun.rikkahubx.ui.pages.prompts.PromptVM
import me.shaun.rikkahubx.ui.pages.setting.SettingVM
import me.shaun.rikkahubx.ui.pages.share.handler.ShareHandlerVM
import me.shaun.rikkahubx.ui.pages.translator.TranslatorVM
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModel<ChatVM> { params ->
        ChatVM(
            id = params.get(),
            context = get(),
            settingsStore = get(),
            conversationRepo = get(),
            chatService = get(),
            updateChecker = get(),
            analytics = get()
        )
    }
    viewModelOf(::SettingVM)
    viewModelOf(::DebugVM)
    viewModelOf(::HistoryVM)
    viewModelOf(::AssistantVM)
    viewModel<AssistantDetailVM> {
        AssistantDetailVM(
            id = it.get(),
            settingsStore = get(),
            memoryRepository = get(),
            context = get(),
        )
    }
    viewModelOf(::TranslatorVM)
    viewModel<ShareHandlerVM> {
        ShareHandlerVM(
            text = it.get(),
            settingsStore = get(),
        )
    }
    viewModelOf(::BackupVM)
    viewModelOf(::ImgGenVM)
    viewModelOf(::DeveloperVM)
    viewModelOf(::PromptVM)
}
