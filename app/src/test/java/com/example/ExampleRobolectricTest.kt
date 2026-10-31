package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.NetPulseDatabase
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
    fun `read string from context and verify sha256 signature`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NetPulse Pro", appName)

        val sig = NetPulseDatabase.computeSha256Signature("PIN:2580")
        assertEquals(24, sig.length)
        assertTrue(sig.all { it.isDigit() || it in 'A'..'F' })
    }
}
