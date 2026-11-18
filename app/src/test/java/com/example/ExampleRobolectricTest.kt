package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.LafangaAiRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Lafanga AI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Lafanga AI", appName)
    }

    @Test
    fun `creator hindi identity string contains full creator details`() {
        val identity = LafangaAiRepository.CREATOR_HINDI_RESPONSE
        assertTrue(identity.contains("अजीत लफंगा भाई (अजीत पासवान)"))
        assertTrue(identity.contains("पासवान टोला, गाँव रहुआ, जिला सहरसा, बिहार"))
    }
}
