package com.example.tetris.data

import android.content.Context
import android.content.SharedPreferences
import com.example.tetris.model.BoardTheme

class TetrisPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)

    var highScore: Int
        get() = prefs.getInt("key_high_score", 0)
        set(value) = prefs.edit().putInt("key_high_score", value).apply()

    var totalLinesCleared: Int
        get() = prefs.getInt("key_total_lines", 0)
        set(value) = prefs.edit().putInt("key_total_lines", value).apply()

    var totalGamesPlayed: Int
        get() = prefs.getInt("key_games_played", 0)
        set(value) = prefs.edit().putInt("key_games_played", value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("key_sound_enabled", true)
        set(value) = prefs.edit().putBoolean("key_sound_enabled", value).apply()

    var isBgmEnabled: Boolean
        get() = prefs.getBoolean("key_bgm_enabled", false)
        set(value) = prefs.edit().putBoolean("key_bgm_enabled", value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean("key_haptics_enabled", true)
        set(value) = prefs.edit().putBoolean("key_haptics_enabled", value).apply()

    var isSwipeEnabled: Boolean
        get() = prefs.getBoolean("key_swipe_enabled", true)
        set(value) = prefs.edit().putBoolean("key_swipe_enabled", value).apply()

    var startingLevel: Int
        get() = prefs.getInt("key_starting_level", 1)
        set(value) = prefs.edit().putInt("key_starting_level", value.coerceIn(1, 10)).apply()

    var boardTheme: BoardTheme
        get() {
            val name = prefs.getString("key_board_theme", BoardTheme.NEON_ARCADE.name)
            return try {
                BoardTheme.valueOf(name ?: BoardTheme.NEON_ARCADE.name)
            } catch (_: Exception) {
                BoardTheme.NEON_ARCADE
            }
        }
        set(value) = prefs.edit().putString("key_board_theme", value.name).apply()

    fun recordGameFinished(finalScore: Int, linesCleared: Int): Boolean {
        totalGamesPlayed += 1
        totalLinesCleared += linesCleared
        if (finalScore > highScore) {
            highScore = finalScore
            return true // New record!
        }
        return false
    }
}
