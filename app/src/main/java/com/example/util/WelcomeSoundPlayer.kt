package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

class WelcomeSoundPlayer(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var isFailed = false
    private var pendingSpeak = false

    init {
        initTts()
    }

    private fun initTts() {
        isReady = false
        isFailed = false
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts
                if (engine != null) {
                    val usResult = engine.setLanguage(Locale.US)
                    if (usResult == TextToSpeech.LANG_MISSING_DATA ||
                        usResult == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        val enResult = engine.setLanguage(Locale.ENGLISH)
                        if (enResult == TextToSpeech.LANG_MISSING_DATA ||
                            enResult == TextToSpeech.LANG_NOT_SUPPORTED
                        ) {
                            engine.defaultVoice?.locale?.let { engine.setLanguage(it) }
                        }
                    }
                    engine.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    engine.setPitch(0.95f)
                    engine.setSpeechRate(0.95f)
                    isReady = true
                    if (pendingSpeak) {
                        pendingSpeak = false
                        speakNow()
                    }
                }
            } else {
                isFailed = true
                if (pendingSpeak) {
                    pendingSpeak = false
                    playFallbackChime()
                }
            }
        }
    }

    fun playWelcomeBoss() {
        when {
            isReady && tts != null -> speakNow()
            isFailed -> playFallbackChime()
            else -> {
                pendingSpeak = true
                if (tts == null) {
                    initTts()
                }
            }
        }
    }

    private fun speakNow() {
        val result = tts?.speak(
            "Welcome boss!",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "welcome_boss_utterance"
        )
        if (result == TextToSpeech.ERROR) {
            playFallbackChime()
        }
    }

    private fun playFallbackChime() {
        scope.launch {
            runCatching {
                val sampleRate = 44100
                val notes = listOf(523.25 to 120, 659.25 to 120, 783.99 to 220) // C5 -> E5 -> G5
                val totalSamples = notes.sumOf { (_, ms) -> (sampleRate * ms) / 1000 }
                val pcm = ShortArray(totalSamples)
                var offset = 0

                for ((freq, durationMs) in notes) {
                    val count = (sampleRate * durationMs) / 1000
                    for (i in 0 until count) {
                        val env = when {
                            i < 400 -> i / 400.0
                            i > count - 800 -> ((count - i).coerceAtLeast(0)) / 800.0
                            else -> 1.0
                        }
                        val sample = sin(2.0 * PI * freq * i / sampleRate) * 0.35 * env
                        pcm[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                    offset += count
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
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
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                kotlinx.coroutines.delay(650L)
                track.stop()
                track.release()
            }
        }
    }

    fun release() {
        pendingSpeak = false
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
        isReady = false
    }
}
