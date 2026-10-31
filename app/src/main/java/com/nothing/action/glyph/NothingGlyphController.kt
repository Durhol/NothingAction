package com.nothing.action.glyph

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import com.nothing.action.core.model.DeviceDetector
import com.nothing.action.core.model.GlyphHardwareType
import kotlinx.coroutines.*

/**
 * Direct Binder client for Nothing OS Glyph Interface service (com.nothing.thirdparty.GlyphService).
 * Compliant with Android 14, 15 and 16.
 */
class NothingGlyphController(private val context: Context) {

    companion object {
        private const val TAG = "NothingGlyph"
        private const val GLYPH_DESCRIPTOR = "com.nothing.thirdparty.IGlyphService"
        private const val GLYPH_PACKAGE = "com.nothing.thirdparty"
        private const val GLYPH_SERVICE = "com.nothing.thirdparty.GlyphService"
        private const val GLYPH_ACTION = "com.nothing.thirdparty.bind_glyphservice"

        private const val TX_SET_FRAME_COLORS = 1
        private const val TX_OPEN_SESSION = 2
        private const val TX_CLOSE_SESSION = 3
        private const val TX_REGISTER_SDK = 5
        private const val TX_REGISTER_MATRIX_SDK = 6
        private const val TX_SET_MATRIX_COLORS = 7
    }

    private var binder: IBinder? = null
    private var isConnected = false
    private var isSessionOpen = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val deviceInfo = DeviceDetector.getDeviceInfo()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service
            isConnected = true
            Log.d(TAG, "Glyph service connected")
            scope.launch {
                initSession()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
            isConnected = false
            isSessionOpen = false
            Log.d(TAG, "Glyph service disconnected")
        }
    }

    fun bind() {
        if (isConnected) return
        try {
            val intent = Intent(GLYPH_ACTION).apply {
                component = ComponentName(GLYPH_PACKAGE, GLYPH_SERVICE)
            }
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind Glyph service: ${e.message}")
        }
    }

    fun unbind() {
        if (isConnected) {
            try {
                closeSession()
                context.unbindService(connection)
            } catch (_: Exception) {}
            binder = null
            isConnected = false
            isSessionOpen = false
        }
    }

    private fun initSession() {
        val b = binder ?: return
        try {
            val model = Build.MODEL ?: "A059"
            if (deviceInfo.glyphType == GlyphHardwareType.MATRIX) {
                // Register matrix
                val p = Parcel.obtain()
                val r = Parcel.obtain()
                try {
                    p.writeInterfaceToken(GLYPH_DESCRIPTOR)
                    p.writeString(model)
                    b.transact(TX_REGISTER_MATRIX_SDK, p, r, 0)
                    r.readException()
                } finally {
                    p.recycle()
                    r.recycle()
                }
            } else {
                // Register strips
                val p = Parcel.obtain()
                val r = Parcel.obtain()
                try {
                    p.writeInterfaceToken(GLYPH_DESCRIPTOR)
                    p.writeString("NothingAction")
                    p.writeString(model)
                    b.transact(TX_REGISTER_SDK, p, r, 0)
                    r.readException()
                } finally {
                    p.recycle()
                    r.recycle()
                }
            }

            // Open session
            val pOpen = Parcel.obtain()
            val rOpen = Parcel.obtain()
            try {
                pOpen.writeInterfaceToken(GLYPH_DESCRIPTOR)
                b.transact(TX_OPEN_SESSION, pOpen, rOpen, 0)
                rOpen.readException()
                isSessionOpen = true
                Log.d(TAG, "Glyph session initialized successfully for model: $model")
            } finally {
                pOpen.recycle()
                rOpen.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Glyph session: ${e.message}")
        }
    }

    private fun closeSession() {
        val b = binder ?: return
        if (!isSessionOpen) return
        try {
            val p = Parcel.obtain()
            val r = Parcel.obtain()
            try {
                p.writeInterfaceToken(GLYPH_DESCRIPTOR)
                b.transact(TX_CLOSE_SESSION, p, r, 0)
                r.readException()
            } finally {
                p.recycle()
                r.recycle()
            }
        } catch (_: Exception) {}
        isSessionOpen = false
    }

    fun triggerPulse(cycles: Int = 2, maxBrightness: Int = 3800) {
        if (!isConnected) {
            bind()
        }
        scope.launch {
            if (!isSessionOpen) {
                initSession()
                delay(50)
            }

            val channelCount = deviceInfo.glyphLedsCount.coerceAtLeast(3)
            val frames = listOf(
                (maxBrightness * 0.25f).toInt(),
                (maxBrightness * 0.65f).toInt(),
                maxBrightness,
                (maxBrightness * 0.70f).toInt(),
                (maxBrightness * 0.30f).toInt(),
                0
            )

            repeat(cycles) {
                for (brightness in frames) {
                    val colors = IntArray(channelCount) { brightness }
                    sendColors(colors)
                    delay(30)
                }
                delay(60)
            }
            // Turn off at end
            sendColors(IntArray(channelCount) { 0 })
        }
    }

    private fun sendColors(colors: IntArray) {
        val b = binder ?: return
        try {
            val p = Parcel.obtain()
            val r = Parcel.obtain()
            try {
                p.writeInterfaceToken(GLYPH_DESCRIPTOR)
                p.writeIntArray(colors)
                val tx = if (deviceInfo.glyphType == GlyphHardwareType.MATRIX) {
                    TX_SET_MATRIX_COLORS
                } else {
                    TX_SET_FRAME_COLORS
                }
                b.transact(tx, p, r, 0)
                r.readException()
            } finally {
                p.recycle()
                r.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending frame colors: ${e.message}")
        }
    }
}
