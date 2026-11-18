package com.example.voice

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private const val TAG = "HindiVoiceManager"

class HindiVoiceManager(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onSpeechError: (String) -> Unit
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private var activeUtterancePrefix: String? = null
    private var remainingChunksForActiveUtterance = 0

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureHindiFemaleVoice()
                _isTtsReady.value = true
                Log.d(TAG, "Hindi TextToSpeech initialized successfully.")
            } else {
                Log.e(TAG, "TextToSpeech initialization failed with status: $status")
                _isTtsReady.value = false
            }
        }
    }

    private fun configureHindiFemaleVoice() {
        val tts = textToSpeech ?: return
        val hindiLocale = Locale.forLanguageTag("hi-IN")

        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )

        val langResult = tts.setLanguage(hindiLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "hi-IN language missing/unsupported on engine, attempting generic Hindi.")
            tts.setLanguage(Locale("hi"))
        }

        val bestFemaleVoice = selectBestHindiFemaleVoice(tts)
        if (bestFemaleVoice != null) {
            tts.voice = bestFemaleVoice
            Log.d(TAG, "Selected Hindi female voice: ${bestFemaleVoice.name}")
        } else {
            Log.d(TAG, "Using default hi-IN voice with female pitch tuning.")
        }

        tts.setPitch(1.08f)
        tts.setSpeechRate(0.98f)

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val msgId = utteranceId?.substringBefore("::")
                if (!msgId.isNullOrBlank()) {
                    _speakingMessageId.value = msgId
                }
            }

            override fun onDone(utteranceId: String?) {
                val msgId = utteranceId?.substringBefore("::")
                if (msgId != null && msgId == activeUtterancePrefix) {
                    remainingChunksForActiveUtterance--
                    if (remainingChunksForActiveUtterance <= 0) {
                        _speakingMessageId.value = null
                        abandonAudioFocus()
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _speakingMessageId.value = null
                abandonAudioFocus()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(TAG, "TTS utterance error ($errorCode) for $utteranceId")
                _speakingMessageId.value = null
                abandonAudioFocus()
            }
        })
    }

    private fun selectBestHindiFemaleVoice(tts: TextToSpeech): Voice? {
        val voices = runCatching { tts.voices }.getOrNull().orEmpty()
        val hindiVoices = voices.filter { voice ->
            val lang = voice.locale.language.lowercase(Locale.US)
            val tag = voice.locale.toLanguageTag().lowercase(Locale.US)
            lang == "hi" || lang == "hin" || tag.startsWith("hi")
        }
        if (hindiVoices.isEmpty()) return null

        return hindiVoices.maxByOrNull { voice ->
            var score = 0
            val name = voice.name.lowercase(Locale.US)
            val features = voice.features.joinToString(",").lowercase(Locale.US)

            val isFemaleMatch = name.contains("female") ||
                features.contains("female") ||
                name.contains("-hia-") ||
                name.contains("-hie-") ||
                name.contains("-cfn-") ||
                name.contains("-f-") ||
                name.contains("lekha") ||
                name.contains("kalpana") ||
                name.contains("heera") ||
                name.contains("sangeeta")

            val isMaleMatch = (!name.contains("female") && name.contains("male")) ||
                name.contains("-hib-") ||
                name.contains("-hid-") ||
                name.contains("-hif-") ||
                name.contains("-m-")

            if (isFemaleMatch) score += 100
            if (isMaleMatch) score -= 100
            if (!voice.isNetworkConnectionRequired) score += 30
            if (voice.quality >= Voice.QUALITY_HIGH) score += 20
            if (voice.locale.country.equals("IN", ignoreCase = true)) score += 10
            score
        }
    }

    fun speak(rawText: String, messageId: String) {
        val tts = textToSpeech ?: return
        if (!_isTtsReady.value) {
            Log.w(TAG, "TTS not ready yet.")
            return
        }

        val cleanText = sanitizeForSpeech(rawText)
        if (cleanText.isBlank()) return

        stopSpeaking()
        ensureLoudMediaVolume()
        requestTransientAudioFocus()

        val chunks = splitIntoSpeechChunks(cleanText)
        activeUtterancePrefix = messageId
        remainingChunksForActiveUtterance = chunks.size
        _speakingMessageId.value = messageId

        chunks.forEachIndexed { index, chunk ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            }
            tts.speak(chunk, queueMode, params, "$messageId::$index")
        }
    }

    fun stopSpeaking() {
        activeUtterancePrefix = null
        remainingChunksForActiveUtterance = 0
        _speakingMessageId.value = null
        runCatching { textToSpeech?.stop() }
        abandonAudioFocus()
    }

    private fun ensureLoudMediaVolume() {
        runCatching {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val targetMinVolume = (maxVolume * 0.8f).toInt().coerceAtLeast(1)
            if (currentVolume < targetMinVolume) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetMinVolume, 0)
            }
        }
    }

    private fun requestTransientAudioFocus() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .build()
                audioFocusRequest = request
                audioManager.requestAudioFocus(request)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        }
    }

    private fun abandonAudioFocus() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        }
    }

    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startHindiListening() {
        stopSpeaking()
        if (!isSpeechRecognitionAvailable()) {
            onSpeechError("इस डिवाइस पर हिंदी वॉइस इनपुट उपलब्ध नहीं है।")
            return
        }

        stopListeningInternal()

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer
        _partialTranscript.value = ""
        _rmsDb.value = 0f

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                _rmsDb.value = rmsdB.coerceIn(0f, 12f)
            }

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                _rmsDb.value = 0f
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _rmsDb.value = 0f
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "कोई आवाज़ समझ नहीं आई, कृपया दोबारा हिंदी में बोलें।"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "कुछ सुनाई नहीं दिया, कृपया माइक दबाकर बोलें।"
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "इंटरनेट या वॉइस सेवा से कनेक्शन नहीं हो पाया।"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "माइक्रोफ़ोन की अनुमति आवश्यक है।"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "वॉइस पहचान व्यस्त है, कृपया एक सेकंड बाद कोशिश करें।"
                    else -> "वॉइस इनपुट में समस्या आई। कृपया दोबारा बोलें या टाइप करें।"
                }
                _partialTranscript.value = ""
                if (error != SpeechRecognizer.ERROR_CLIENT) {
                    onSpeechError(message)
                }
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                _rmsDb.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcript = matches?.firstOrNull()?.trim().orEmpty()
                _partialTranscript.value = ""
                if (transcript.isNotEmpty()) {
                    onSpeechResult(transcript)
                } else {
                    onSpeechError("कोई शब्द पहचाना नहीं जा सका।")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.trim()
                    .orEmpty()
                if (partial.isNotEmpty()) {
                    _partialTranscript.value = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        _isListening.value = true
        recognizer.startListening(intent)
    }

    fun stopListening() {
        runCatching { speechRecognizer?.stopListening() }
        _isListening.value = false
        _rmsDb.value = 0f
    }

    fun cancelListening() {
        stopListeningInternal()
        _isListening.value = false
        _partialTranscript.value = ""
        _rmsDb.value = 0f
    }

    private fun stopListeningInternal() {
        runCatching {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        }
        speechRecognizer = null
    }

    fun release() {
        cancelListening()
        stopSpeaking()
        runCatching { textToSpeech?.shutdown() }
        textToSpeech = null
    }

    private fun sanitizeForSpeech(text: String): String {
        return text
            .replace(Regex("```[\\s\\S]*?```"), " ")
            .replace(Regex("[*_`#>~]"), "")
            .replace(Regex("^\\s*[-•+]\\s+", RegexOption.MULTILINE), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun splitIntoSpeechChunks(text: String, maxChunkLength: Int = 3000): List<String> {
        if (text.length <= maxChunkLength) return listOf(text)
        val chunks = mutableListOf<String>()
        var remaining = text
        while (remaining.length > maxChunkLength) {
            val slice = remaining.substring(0, maxChunkLength)
            val breakIdx = maxOf(
                slice.lastIndexOf('।'),
                slice.lastIndexOf('.'),
                slice.lastIndexOf('?'),
                slice.lastIndexOf('!')
            )
            val splitAt = if (breakIdx > maxChunkLength / 2) breakIdx + 1 else maxChunkLength
            chunks.add(remaining.substring(0, splitAt).trim())
            remaining = remaining.substring(splitAt).trim()
        }
        if (remaining.isNotEmpty()) {
            chunks.add(remaining)
        }
        return chunks
    }
}
