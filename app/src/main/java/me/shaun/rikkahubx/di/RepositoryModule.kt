package me.shaun.rikkahubx.di

import me.shaun.rikkahubx.data.repository.ConversationRepository
import me.shaun.rikkahubx.data.repository.GenMediaRepository
import me.shaun.rikkahubx.data.repository.MemoryRepository
import org.koin.dsl.module

val repositoryModule = module {
    single {
        ConversationRepository(get(), get(), get(), get())
    }

    single {
        MemoryRepository(get())
    }

    single {
        GenMediaRepository(get())
    }
}
