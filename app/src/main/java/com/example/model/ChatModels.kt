package com.example.model

import android.net.Uri
import java.util.UUID

enum class MessageRole {
    USER,
    ASSISTANT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val imageUri: Uri? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

data class LafangaUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val pendingImageUri: Uri? = null,
    val isGenerating: Boolean = false,
    val isListening: Boolean = false,
    val partialTranscript: String = "",
    val micRmsDb: Float = 0f,
    val isAutoSpeakEnabled: Boolean = true,
    val isTtsReady: Boolean = false,
    val speakingMessageId: String? = null,
    val showClearDialog: Boolean = false,
    val showCreatorSheet: Boolean = false
)
