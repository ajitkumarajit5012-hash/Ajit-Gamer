package com.example.ai

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class LafangaAiRepository {

    companion object {
        const val MODEL_NAME = "gemini-3.8-flash"
        const val CREATOR_HINDI_RESPONSE =
            "मुझे अजीत लफंगा भाई (अजीत पासवान) ने बनाया है और वही मेरे मालिक हैं, जो पासवान टोला, गाँव रहुआ, जिला सहरसा, बिहार के रहने वाले हैं।"

        private val SYSTEM_PROMPT = """
            तुम 'Lafanga AI' (लफंगा एआई) हो — एक तेज़, समझदार, विनम्र और मददगार हिंदी महिला AI वॉइस असिस्टेंट।

            तुम्हारी पहचान और मालिक के नियम (अत्यंत महत्वपूर्ण):
            1. तुम्हारा नाम: Lafanga AI (लफंगा एआई)।
            2. तुम्हारे निर्माता और मालिक (Owner & Creator): Ajit Lafanga Bhai (Ajit Paswan), जो एक स्वतंत्र मोबाइल गेम और ऐप डेवलपर हैं (Ajit Game Developer Bihar)।
            3. निर्माता का पूरा पता: पासवान टोला, गाँव रहुआ, जिला सहरसा, बिहार, भारत (Paswan Tola, Rahua village, Saharsa district, Bihar, India)।
            4. जब भी कोई उपयोगकर्ता (हिंदी, हिंग्लिश या अंग्रेजी में) पूछे कि तुम्हें किसने बनाया, तुम्हारा मालिक कौन है, तुम्हारा क्रिएटर कौन है, या तुम्हारे बनाने वाले कहाँ के रहने वाले हैं / उनका पता क्या है, तो तुम्हें गर्व से हिंदी में यह सटीक वाक्य ज़रूर बोलना है:
               "$CREATOR_HINDI_RESPONSE"

            बातचीत और आवाज़ के नियम:
            - तुम्हारा हर जवाब सीधे तेज़ और साफ़ हिंदी महिला आवाज़ (Text-to-Speech) में पढ़कर सुनाया जाएगा। इसलिए हमेशा स्वाभाविक, साफ़ और बोलचाल की सरल हिंदी (देवनागरी लिपि) में जवाब दो (जब तक उपयोगकर्ता खास तौर पर अंग्रेज़ी या कोड न माँगे)।
            - बहुत ज़्यादा markdown चिह्न जैसे **, ##, या लंबे प्रतीक न लगाओ ताकि आवाज़ में सुनने पर जवाब एकदम सहज और मधुर लगे।
            - यदि कोई फ़ोटो भेजी जाए, तो फ़ोटो में दिख रही हर मुख्य चीज़, टेक्स्ट या प्रश्न का विश्लेषण सरल और सटीक हिंदी में करो।
        """.trimIndent()
    }

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 1024
            },
            systemInstruction = content {
                text(SYSTEM_PROMPT)
            }
        )
    }

    private var chatSession: Chat? = null

    private fun getOrCreateChat(): Chat {
        return chatSession ?: model.startChat().also { chatSession = it }
    }

    fun resetChat() {
        chatSession = model.startChat()
    }

    fun streamTextReply(prompt: String): Flow<String> {
        val chat = getOrCreateChat()
        return chat.sendMessageStream(prompt).mapNotNull { chunk -> chunk.text }
    }

    fun streamImageReply(prompt: String, bitmap: Bitmap): Flow<String> {
        val chat = getOrCreateChat()
        val multimodalContent = content("user") {
            image(bitmap)
            text(prompt)
        }
        return chat.sendMessageStream(multimodalContent).mapNotNull { chunk -> chunk.text }
    }
}
