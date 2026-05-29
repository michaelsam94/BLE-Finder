package com.example.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.domain.repository.AudioFeedbackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

class AudioFeedbackImpl : AudioFeedbackRepository {

    private var isEnabled = true
    private var pingJob: Job? = null
    
    private val audioDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val audioScope = CoroutineScope(audioDispatcher)

    @Volatile
    private var currentRssi: Float = -80f

    override suspend fun startProximityPing(rssiFlow: Flow<Float>) {
        audioScope.launch {
            rssiFlow.conflate().collect { rssi ->
                currentRssi = rssi
            }
        }

        pingJob?.cancel()
        pingJob = audioScope.launch {
            while (isEnabled) {
                val rssi = currentRssi
                val clampedRssi = rssi.coerceIn(-90f, -40f)
                val fraction = (clampedRssi - (-90f)) / (-40f - (-90f)) // 0.0 to 1.0
                val frequency = 400 + (fraction * 800).toInt() // 400 to 1200 Hz
                val interval = 2000 - (fraction * 1850).toLong() // 2000 to 150ms

                playPingTone(frequency)
                delay(interval)
            }
        }
    }

    override suspend fun stopPing() {
        pingJob?.cancel()
        pingJob = null
    }

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            pingJob?.cancel()
            pingJob = null
        }
    }

    override fun isEnabled(): Boolean = isEnabled

    private fun playPingTone(frequency: Int) {
        if (!isEnabled) return
        
        val sampleRate = 44100
        val attackMs = 50
        val decayMs = 30
        val totalMs = attackMs + decayMs
        val totalSamples = (sampleRate * totalMs / 1000)
        
        val attackSamples = (sampleRate * attackMs / 1000)
        val decaySamples = (sampleRate * decayMs / 1000)

        val numBytes = totalSamples * 2
        val pcmData = ByteArray(numBytes)

        for (i in 0 until totalSamples) {
            val angle = 2.0 * PI * frequency * i / sampleRate
            val rawValue = sin(angle)

            val envelope = when {
                i < attackSamples -> i.toDouble() / attackSamples
                else -> {
                    val decayLeft = totalSamples - i
                    decayLeft.toDouble() / decaySamples
                }
            }

            val sampleValue = (rawValue * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            
            val index = i * 2
            pcmData[index] = (sampleValue and 0xff).toByte()
            pcmData[index + 1] = ((sampleValue shr 8) and 0xff).toByte()
        }

        var audioTrack: AudioTrack? = null
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcmData.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcmData, 0, pcmData.size)
            audioTrack.play()
            
            Thread.sleep(totalMs.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (ex: Exception) {
                // ignore
            }
        }
    }
}
