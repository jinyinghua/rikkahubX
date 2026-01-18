package me.rerere.rikkahub.service.assist

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

private const val TAG = "AssistSession"

class AssistSession(context: Context) : VoiceInteractionSession(context) {

    override fun onHandleAssist(
        data: Bundle?,
        structure: AssistStructure?,
        content: AssistContent?
    ) {
        super.onHandleAssist(data, structure, content)
        Log.d(TAG, "onHandleAssist")
        val textContent = StringBuilder()
        structure?.let {
            for (i in 0 until it.windowNodeCount) {
                extractText(it.getWindowNodeAt(i).rootViewNode, textContent)
            }
        }
        val assistText = textContent.toString().trim()
        if (assistText.isNotBlank()) {
            val file = File(context.cacheDir, "assist_text.txt")
            file.writeText(assistText)
        }
    }

    private fun extractText(node: AssistStructure.ViewNode?, out: StringBuilder) {
        if (node == null) return
        if (node.visibility == android.view.View.VISIBLE) {
            val text = node.text
            if (!text.isNullOrBlank()) {
                out.append(text).append("\n")
            }
        }
        for (i in 0 until node.childCount) {
            extractText(node.getChildAt(i), out)
        }
    }

    override fun onHandleScreenshot(screenshot: Bitmap?) {
        super.onHandleScreenshot(screenshot)
        Log.d(TAG, "onHandleScreenshot: ${screenshot != null}")
        
        screenshot?.let { bitmap ->
            // 保存截图到临时文件并启动 Activity
            val file = File(context.cacheDir, "assist_screenshot.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            val intent = Intent(context, AssistActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                putExtra("screenshot_path", file.absolutePath)
            }
            startAssistantActivity(intent)
        }
    }
}
