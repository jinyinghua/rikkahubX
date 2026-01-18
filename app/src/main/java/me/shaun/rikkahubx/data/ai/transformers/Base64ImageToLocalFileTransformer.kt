package me.shaun.rikkahubx.data.ai.transformers

import me.rerere.ai.ui.UIMessage
import me.shaun.rikkahubx.utils.convertBase64ImagePartToLocalFile

object Base64ImageToLocalFileTransformer : OutputMessageTransformer {
    override suspend fun onGenerationFinish(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> {
        return messages.map { message ->
            ctx.context.convertBase64ImagePartToLocalFile(message)
        }
    }
}
