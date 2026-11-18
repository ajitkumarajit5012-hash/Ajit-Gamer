package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.ai.LafangaAiRepository
import com.example.model.ChatMessage
import com.example.model.LafangaUiState
import com.example.model.MessageRole
import com.example.util.ImageUtils
import com.example.voice.HindiVoiceManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "LafangaViewModel"

class LafangaViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository = LafangaAiRepository()
    private val _uiState = MutableStateFlow(LafangaUiState())
    val uiState: StateFlow<LafangaUiState> = _uiState.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private var activeGenerationJob: Job? = null

    private val voiceManager = HindiVoiceManager(
        context = application,
        onSpeechResult = { transcript ->
            sendMessage(overrideText = transcript)
        },
        onSpeechError = { errorMsg ->
            emitSnackbar(errorMsg)
        }
    )

    init {
        viewModelScope.launch {
            combine(
                voiceManager.isListening,
                voiceManager.partialTranscript,
                voiceManager.rmsDb,
                voiceManager.speakingMessageId,
                voiceManager.isTtsReady
            ) { listening, partial, rms, speakingId, ttsReady ->
                VoiceSnapshot(listening, partial, rms, speakingId, ttsReady)
            }.collect { voice ->
                _uiState.update { state ->
                    state.copy(
                        isListening = voice.isListening,
                        partialTranscript = voice.partialTranscript,
                        micRmsDb = voice.rmsDb,
                        speakingMessageId = voice.speakingMessageId,
                        isTtsReady = voice.isTtsReady
                    )
                }
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun attachImage(uri: Uri) {
        _uiState.update { it.copy(pendingImageUri = uri) }
    }

    fun clearPendingImage() {
        _uiState.update { it.copy(pendingImageUri = null) }
    }

    fun createCameraPhotoUri(): Uri {
        return ImageUtils.createCameraPhotoUri(getApplication())
    }

    fun toggleAutoSpeak() {
        val nextValue = !_uiState.value.isAutoSpeakEnabled
        _uiState.update { it.copy(isAutoSpeakEnabled = nextValue) }
        if (!nextValue) {
            voiceManager.stopSpeaking()
        }
    }

    fun startHindiVoiceInput() {
        if (_uiState.value.isGenerating) {
            emitSnackbar("कृपया जवाब पूरा होने का इंतज़ार करें।")
            return
        }
        voiceManager.startHindiListening()
    }

    fun stopListeningAndSend() {
        voiceManager.stopListening()
    }

    fun cancelListening() {
        voiceManager.cancelListening()
    }

    fun speakMessage(message: ChatMessage) {
        if (message.text.isBlank()) return
        if (_uiState.value.speakingMessageId == message.id) {
            voiceManager.stopSpeaking()
        } else {
            voiceManager.speak(message.text, message.id)
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun speakCreatorIntroduction() {
        val introId = "creator_intro_tts"
        voiceManager.speak(LafangaAiRepository.CREATOR_HINDI_RESPONSE, introId)
    }

    fun setClearDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showClearDialog = visible) }
    }

    fun setCreatorSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showCreatorSheet = visible) }
    }

    fun clearChat() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        voiceManager.cancelListening()
        voiceManager.stopSpeaking()
        aiRepository.resetChat()
        _uiState.update {
            it.copy(
                messages = emptyList(),
                inputText = "",
                pendingImageUri = null,
                isGenerating = false,
                showClearDialog = false
            )
        }
    }

    fun emitSnackbar(message: String) {
        _snackbarEvents.tryEmit(message)
    }

    fun sendQuickPrompt(prompt: String) {
        sendMessage(overrideText = prompt)
    }

    fun sendMessage(overrideText: String? = null) {
        if (_uiState.value.isGenerating) return

        val currentState = _uiState.value
        val attachedUri = currentState.pendingImageUri
        val rawText = (overrideText ?: currentState.inputText).trim()

        if (rawText.isEmpty() && attachedUri == null) return

        val promptToSend = if (rawText.isNotEmpty()) {
            rawText
        } else {
            getApplication<Application>().getString(R.string.photo_prompt_default)
        }

        voiceManager.stopSpeaking()

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            text = promptToSend,
            imageUri = attachedUri
        )
        val assistantMessage = ChatMessage(
            role = MessageRole.ASSISTANT,
            text = "",
            isStreaming = true
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage + assistantMessage,
                inputText = if (overrideText == null) "" else state.inputText,
                pendingImageUri = null,
                isGenerating = true
            )
        }

        activeGenerationJob = viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    val flow = if (attachedUri != null) {
                        val bitmap = ImageUtils.loadScaledBitmap(getApplication(), attachedUri)
                            ?: throw IllegalStateException("फ़ोटो लोड नहीं हो सकी। कृपया दूसरी फ़ोटो चुनें।")
                        aiRepository.streamImageReply(promptToSend, bitmap)
                    } else {
                        aiRepository.streamTextReply(promptToSend)
                    }

                    flow.collect { chunk ->
                        accumulated.append(chunk)
                        val partial = accumulated.toString()
                        updateAssistantMessage(
                            messageId = assistantMessage.id,
                            text = partial,
                            isStreaming = true,
                            isError = false
                        )
                    }
                }

                val finalReply = accumulated.toString().trim()
                if (finalReply.isEmpty()) {
                    val fallbackErr = "माफ़ कीजिए, अभी कोई जवाब प्राप्त नहीं हुआ। कृपया दोबारा पूछें।"
                    updateAssistantMessage(
                        messageId = assistantMessage.id,
                        text = fallbackErr,
                        isStreaming = false,
                        isError = true
                    )
                } else {
                    updateAssistantMessage(
                        messageId = assistantMessage.id,
                        text = finalReply,
                        isStreaming = false,
                        isError = false
                    )
                    if (_uiState.value.isAutoSpeakEnabled) {
                        voiceManager.speak(finalReply, assistantMessage.id)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI generation timed out", e)
                val timeoutMsg = "नेटवर्क धीमा होने के कारण समय समाप्त हो गया। कृपया दोबारा प्रयास करें।"
                updateAssistantMessage(
                    messageId = assistantMessage.id,
                    text = timeoutMsg,
                    isStreaming = false,
                    isError = true
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI generation failed", e)
                val errText = e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "जवाब लाने में त्रुटि हुई। कृपया इंटरनेट जांचकर फिर कोशिश करें।"
                updateAssistantMessage(
                    messageId = assistantMessage.id,
                    text = errText,
                    isStreaming = false,
                    isError = true
                )
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    private fun updateAssistantMessage(
        messageId: String,
        text: String,
        isStreaming: Boolean,
        isError: Boolean
    ) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(text = text, isStreaming = isStreaming, isError = isError)
                    } else {
                        msg
                    }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }

    private data class VoiceSnapshot(
        val isListening: Boolean,
        val partialTranscript: String,
        val rmsDb: Float,
        val speakingMessageId: String?,
        val isTtsReady: Boolean
    )
}
