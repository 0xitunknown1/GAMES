package com.example.tetris.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Self-contained 8-bit audio synthesizer and haptic feedback manager.
 * Generates crisp retro sound effects and background chiptune melody without external MP3 files.
 */
class TetrisAudioAndHaptics(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var bgmJob: Job? = null

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true
    var isBgmEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                startBgm()
            } else {
                stopBgm()
            }
        }

    // --- HAPTIC FEEDBACK ---

    fun vibrateMove() {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(12)
        }
    }

    fun vibrateRotate() {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(18, 180))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(18)
        }
    }

    fun vibrateDrop() {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(35)
        }
    }

    fun vibrateLineClear() {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 40, 60, 70)
            val amplitudes = intArrayOf(0, 200, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }
    }

    fun vibrateGameOver() {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 80, 50, 150)
            val amplitudes = intArrayOf(0, 255, 0, 180)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    // --- SYNTHESIZED SOUND EFFECTS ---

    fun playMoveSound() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(frequency = 320.0, durationMs = 40, volume = 0.25f, waveType = WaveType.SQUARE)
        }
    }

    fun playRotateSound() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(frequency = 480.0, durationMs = 30, volume = 0.3f, waveType = WaveType.SQUARE)
            playTone(frequency = 640.0, durationMs = 35, volume = 0.35f, waveType = WaveType.SQUARE)
        }
    }

    fun playSoftDropSound() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(frequency = 180.0, durationMs = 25, volume = 0.2f, waveType = WaveType.TRIANGLE)
        }
    }

    fun playHardDropSound() {
        if (!isSoundEnabled) return
        scope.launch {
            playPitchDrop(startFreq = 260.0, endFreq = 80.0, durationMs = 90, volume = 0.45f)
        }
    }

    fun playLockSound() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(frequency = 220.0, durationMs = 35, volume = 0.35f, waveType = WaveType.TRIANGLE)
            playTone(frequency = 140.0, durationMs = 40, volume = 0.4f, waveType = WaveType.TRIANGLE)
        }
    }

    fun playHoldSound() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(frequency = 440.0, durationMs = 30, volume = 0.3f, waveType = WaveType.SQUARE)
            playTone(frequency = 660.0, durationMs = 40, volume = 0.35f, waveType = WaveType.SQUARE)
        }
    }

    fun playTSpinSound() {
        if (!isSoundEnabled) return
        scope.launch {
            val chord = listOf(587.33, 739.99, 880.0, 1174.66)
            for (freq in chord) {
                playTone(frequency = freq, durationMs = 50, volume = 0.4f, waveType = WaveType.SQUARE)
            }
        }
    }

    fun playLineClearSound(lineCount: Int = 1) {
        if (!isSoundEnabled) return
        scope.launch {
            // Melodic arpeggio based on line count
            val notes = when (lineCount) {
                1 -> listOf(523.25, 659.25, 783.99) // C5, E5, G5
                2 -> listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
                3 -> listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
                else -> listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98) // TETRIS 4 lines!
            }
            for (freq in notes) {
                playTone(frequency = freq, durationMs = 60, volume = 0.4f, waveType = WaveType.SQUARE)
            }
        }
    }

    fun playGameOverSound() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = listOf(440.0, 415.3, 392.0, 369.99, 329.63, 261.63, 196.0)
            for (freq in notes) {
                playTone(frequency = freq, durationMs = 120, volume = 0.4f, waveType = WaveType.SAWTOOTH)
            }
        }
    }

    fun playMenuClick() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(frequency = 587.33, durationMs = 45, volume = 0.3f, waveType = WaveType.SQUARE)
            playTone(frequency = 880.0, durationMs = 60, volume = 0.35f, waveType = WaveType.SQUARE)
        }
    }

    fun playPauseSound() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(frequency = 440.0, durationMs = 50, volume = 0.3f, waveType = WaveType.TRIANGLE)
            playTone(frequency = 330.0, durationMs = 70, volume = 0.25f, waveType = WaveType.TRIANGLE)
        }
    }

    // --- SYNTHESIZED BACKGROUND MUSIC (TETRIS THEME KOROBEINIKI) ---

    private fun startBgm() {
        stopBgm()
        bgmJob = scope.launch {
            // Notes frequencies in Hz
            val E5 = 659.25
            val B4 = 493.88
            val C5 = 523.25
            val D5 = 587.33
            val A4 = 440.00
            val G4 = 392.00
            val F4 = 349.23
            val GS4 = 415.30

            // (note, durationMs)
            val melody = listOf(
                Pair(E5, 300L), Pair(B4, 150L), Pair(C5, 150L), Pair(D5, 300L),
                Pair(C5, 150L), Pair(B4, 150L), Pair(A4, 300L), Pair(A4, 150L),
                Pair(C5, 150L), Pair(E5, 300L), Pair(D5, 150L), Pair(C5, 150L),
                Pair(B4, 450L), Pair(C5, 150L), Pair(D5, 300L), Pair(E5, 300L),
                Pair(C5, 300L), Pair(A4, 300L), Pair(A4, 400L),
                // Part B
                Pair(D5, 300L), Pair(F4 * 2, 150L), Pair(A4 * 2, 300L), Pair(G4 * 2, 150L),
                Pair(F4 * 2, 150L), Pair(E5, 450L), Pair(C5, 150L), Pair(E5, 300L),
                Pair(D5, 150L), Pair(C5, 150L), Pair(B4, 300L), Pair(B4, 150L),
                Pair(C5, 150L), Pair(D5, 300L), Pair(E5, 300L), Pair(C5, 300L),
                Pair(A4, 300L), Pair(A4, 400L)
            )

            while (isActive && isBgmEnabled) {
                for ((freq, dur) in melody) {
                    if (!isActive || !isBgmEnabled) break
                    playTone(frequency = freq, durationMs = (dur * 0.85).toInt(), volume = 0.15f, waveType = WaveType.SQUARE)
                    delay((dur * 0.15).toLong().coerceAtLeast(10L))
                }
                delay(300L)
            }
        }
    }

    private fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
    }

    // --- LOW-LEVEL PCM TONE GENERATOR ---

    private enum class WaveType {
        SQUARE, TRIANGLE, SAWTOOTH
    }

    private fun playTone(
        frequency: Double,
        durationMs: Int,
        volume: Float = 0.3f,
        waveType: WaveType = WaveType.SQUARE
    ) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate / 1000).coerceAtLeast(1)
        val generatedSnd = ShortArray(numSamples)
        val period = sampleRate / frequency

        for (i in 0 until numSamples) {
            val progressInPeriod = (i % period) / period
            val waveSample: Double = when (waveType) {
                WaveType.SQUARE -> if (progressInPeriod < 0.5) 1.0 else -1.0
                WaveType.TRIANGLE -> {
                    if (progressInPeriod < 0.5) {
                        (progressInPeriod * 4.0) - 1.0
                    } else {
                        3.0 - (progressInPeriod * 4.0)
                    }
                }
                WaveType.SAWTOOTH -> (progressInPeriod * 2.0) - 1.0
            }

            // Envelope to avoid pop/clicks (fade in first 5% and fade out last 10%)
            val attack = (numSamples * 0.05).coerceAtLeast(1.0)
            val release = (numSamples * 0.10).coerceAtLeast(1.0)
            val envelope = when {
                i < attack -> (i / attack)
                i > numSamples - release -> ((numSamples - i) / release)
                else -> 1.0
            }

            val sample = (waveSample * Short.MAX_VALUE * volume * envelope).toInt()
            generatedSnd[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(generatedSnd, 0, numSamples)
            track.play()
            Thread.sleep(durationMs.toLong() + 20)
            track.stop()
            track.release()
        } catch (_: Exception) {
            // AudioTrack failure handled gracefully
        }
    }

    private fun playPitchDrop(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Float = 0.4f
    ) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate / 1000).coerceAtLeast(1)
        val generatedSnd = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val phaseIncrement = 2.0 * Math.PI * currentFreq / sampleRate
            phase += phaseIncrement

            val decay = 1.0 - progress
            val sample = (sin(phase) * Short.MAX_VALUE * volume * decay).toInt()
            generatedSnd[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(generatedSnd, 0, numSamples)
            track.play()
            Thread.sleep(durationMs.toLong() + 20)
            track.stop()
            track.release()
        } catch (_: Exception) {
            // Handled gracefully
        }
    }

    fun release() {
        stopBgm()
    }
}
