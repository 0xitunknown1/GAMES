package com.example.tetris.model

import androidx.compose.ui.graphics.Color

/**
 * 7 Tetromino shapes following official Tetris Guideline SRS (Super Rotation System).
 * Index = row * 4 + col (i.e. i * 4 + j in a 4x4 matrix).
 */
enum class TetrominoType(
    val id: Int,
    val pieceName: String,
    val defaultColorIndex: Int,
    val rotations: List<List<Int>>
) {
    I(
        id = 0,
        pieceName = "I",
        defaultColorIndex = 1, // Cyan
        rotations = listOf(
            listOf(4, 5, 6, 7),     // 0: Spawn (Horizontal)
            listOf(2, 6, 10, 14),   // 1: Right (Vertical)
            listOf(8, 9, 10, 11),   // 2: 180 (Horizontal)
            listOf(1, 5, 9, 13)     // 3: Left (Vertical)
        )
    ),
    J(
        id = 1,
        pieceName = "J",
        defaultColorIndex = 2, // Blue
        rotations = listOf(
            listOf(0, 4, 5, 6),     // 0: Spawn
            listOf(1, 2, 5, 9),     // 1: Right
            listOf(4, 5, 6, 10),    // 2: 180
            listOf(1, 5, 8, 9)      // 3: Left
        )
    ),
    L(
        id = 2,
        pieceName = "L",
        defaultColorIndex = 3, // Orange
        rotations = listOf(
            listOf(2, 4, 5, 6),     // 0: Spawn
            listOf(1, 5, 9, 10),    // 1: Right
            listOf(4, 5, 6, 8),     // 2: 180
            listOf(0, 1, 5, 9)      // 3: Left
        )
    ),
    O(
        id = 3,
        pieceName = "O",
        defaultColorIndex = 4, // Yellow
        rotations = listOf(
            listOf(1, 2, 5, 6),
            listOf(1, 2, 5, 6),
            listOf(1, 2, 5, 6),
            listOf(1, 2, 5, 6)
        )
    ),
    S(
        id = 4,
        pieceName = "S",
        defaultColorIndex = 5, // Green
        rotations = listOf(
            listOf(1, 2, 4, 5),     // 0: Spawn
            listOf(1, 5, 6, 10),    // 1: Right
            listOf(5, 6, 8, 9),     // 2: 180
            listOf(0, 4, 5, 9)      // 3: Left
        )
    ),
    T(
        id = 5,
        pieceName = "T",
        defaultColorIndex = 6, // Purple
        rotations = listOf(
            listOf(1, 4, 5, 6),     // 0: Spawn
            listOf(1, 5, 6, 9),     // 1: Right
            listOf(4, 5, 6, 9),     // 2: 180
            listOf(1, 4, 5, 9)      // 3: Left
        )
    ),
    Z(
        id = 6,
        pieceName = "Z",
        defaultColorIndex = 7, // Red
        rotations = listOf(
            listOf(0, 1, 5, 6),     // 0: Spawn
            listOf(2, 5, 6, 9),     // 1: Right
            listOf(4, 5, 9, 10),    // 2: 180
            listOf(1, 4, 5, 8)      // 3: Left
        )
    );

    companion object {
        fun random(): TetrominoType = entries.random()
    }
}

/**
 * Super Rotation System (SRS) Wall Kick Data.
 * Coordinates are (dx, dy) in screen space:
 * dx > 0 is right, dx < 0 is left.
 * dy > 0 is DOWN, dy < 0 is UP (floor kick!).
 */
object SrsKickData {
    // Standard kick table for J, L, S, T, Z (3x3 bounding box)
    private val JLSTZ_KICKS = mapOf(
        Pair(0, 1) to listOf(Pair(0, 0), Pair(-1, 0), Pair(-1, -1), Pair(0, 2), Pair(-1, 2)),
        Pair(1, 0) to listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(0, -2), Pair(1, -2)),
        Pair(1, 2) to listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(0, -2), Pair(1, -2)),
        Pair(2, 1) to listOf(Pair(0, 0), Pair(-1, 0), Pair(-1, -1), Pair(0, 2), Pair(-1, 2)),
        Pair(2, 3) to listOf(Pair(0, 0), Pair(1, 0), Pair(1, -1), Pair(0, 2), Pair(1, 2)),
        Pair(3, 2) to listOf(Pair(0, 0), Pair(-1, 0), Pair(-1, 1), Pair(0, -2), Pair(-1, -2)),
        Pair(3, 0) to listOf(Pair(0, 0), Pair(-1, 0), Pair(-1, 1), Pair(0, -2), Pair(-1, -2)),
        Pair(0, 3) to listOf(Pair(0, 0), Pair(1, 0), Pair(1, -1), Pair(0, 2), Pair(1, 2))
    )

    // Kick table for I tetromino (4x4 bounding box)
    private val I_KICKS = mapOf(
        Pair(0, 1) to listOf(Pair(0, 0), Pair(-2, 0), Pair(1, 0), Pair(-2, 1), Pair(1, -2)),
        Pair(1, 0) to listOf(Pair(0, 0), Pair(2, 0), Pair(-1, 0), Pair(2, -1), Pair(-1, 2)),
        Pair(1, 2) to listOf(Pair(0, 0), Pair(-1, 0), Pair(2, 0), Pair(-1, -2), Pair(2, 1)),
        Pair(2, 1) to listOf(Pair(0, 0), Pair(1, 0), Pair(-2, 0), Pair(1, 2), Pair(-2, -1)),
        Pair(2, 3) to listOf(Pair(0, 0), Pair(2, 0), Pair(-1, 0), Pair(2, -1), Pair(-1, 2)),
        Pair(3, 2) to listOf(Pair(0, 0), Pair(-2, 0), Pair(1, 0), Pair(-2, 1), Pair(1, -2)),
        Pair(3, 0) to listOf(Pair(0, 0), Pair(1, 0), Pair(-2, 0), Pair(1, 2), Pair(-2, -1)),
        Pair(0, 3) to listOf(Pair(0, 0), Pair(-1, 0), Pair(2, 0), Pair(-1, -2), Pair(2, 1))
    )

    fun getTests(type: TetrominoType, fromRot: Int, toRot: Int): List<Pair<Int, Int>> {
        if (type == TetrominoType.O) return listOf(Pair(0, 0))
        val key = Pair(fromRot, toRot)
        return if (type == TetrominoType.I) {
            I_KICKS[key] ?: listOf(Pair(0, 0), Pair(-1, 0), Pair(1, 0))
        } else {
            JLSTZ_KICKS[key] ?: listOf(Pair(0, 0), Pair(-1, 0), Pair(1, 0), Pair(0, -1))
        }
    }
}

data class Tetromino(
    val type: TetrominoType,
    val x: Int = 3,
    val y: Int = 0,
    val rotation: Int = 0,
    val colorIndex: Int = type.defaultColorIndex
) {
    fun currentBlocks(): List<Int> {
        return type.rotations[rotation % type.rotations.size]
    }

    fun rotated(direction: Int = 1): Tetromino {
        val numRotations = type.rotations.size
        val newRotation = (rotation + direction + numRotations) % numRotations
        return copy(rotation = newRotation)
    }
}

enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER
}

/**
 * Visual banner for game events: TETRIS!, T-SPIN, BACK-TO-BACK, COMBOS!
 */
data class ActionBanner(
    val title: String,
    val subtitle: String? = null,
    val color: Color = Color(0xFFFFD700),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Visual themes for the Tetris board.
 */
enum class BoardTheme(
    val displayName: String,
    val boardBackground: Color,
    val gridBorder: Color,
    val gridLine: Color,
    val emptyCell: Color,
    val ghostColor: Color,
    val pieceColors: List<Color> // Index 0 is empty; Indices 1..7 correspond to pieces
) {
    OFFICIAL_GUIDELINE(
        displayName = "Tetris Standard",
        boardBackground = Color(0xFF10131C),
        gridBorder = Color(0xFF38BDF8),
        gridLine = Color(0xFF1E2538),
        emptyCell = Color(0xFF0C0E17),
        ghostColor = Color(0x38FFFFFF),
        pieceColors = listOf(
            Color(0xFF000000),      // 0: empty
            Color(0xFF00E5FF),      // 1: I (Cyan)
            Color(0xFF2563EB),      // 2: J (Blue)
            Color(0xFFF97316),      // 3: L (Orange)
            Color(0xFFFACC15),      // 4: O (Yellow)
            Color(0xFF22C55E),      // 5: S (Green)
            Color(0xFFA855F7),      // 6: T (Purple)
            Color(0xFFEF4444)       // 7: Z (Red)
        )
    ),
    NEON_ARCADE(
        displayName = "Neon Arcade",
        boardBackground = Color(0xFF0A0F1D),
        gridBorder = Color(0xFF00F0FF),
        gridLine = Color(0xFF152238),
        emptyCell = Color(0xFF060914),
        ghostColor = Color(0x4000F0FF),
        pieceColors = listOf(
            Color(0xFF000000),      // 0: empty
            Color(0xFF00F0FF),      // 1: Cyan
            Color(0xFF3B82F6),      // 2: Blue
            Color(0xFFFF9900),      // 3: Amber/Orange
            Color(0xFFFFEE00),      // 4: Yellow
            Color(0xFF00FF66),      // 5: Emerald
            Color(0xFFD946EF),      // 6: Fuchsia
            Color(0xFFFF0055)       // 7: Hot Red
        )
    ),
    CLASSIC_PYGAME(
        displayName = "Classic Pygame",
        boardBackground = Color(0xFF18181B),
        gridBorder = Color(0xFF71717A),
        gridLine = Color(0xFF27272A),
        emptyCell = Color(0xFF09090B),
        ghostColor = Color(0x33A1A1AA),
        pieceColors = listOf(
            Color(0xFF000000),      // 0: empty
            Color(100, 179, 179),   // 1: Teal
            Color(120, 37, 179),    // 2: Purple
            Color(180, 80, 40),     // 3: Amber
            Color(200, 180, 40),    // 4: Yellow
            Color(80, 134, 22),     // 5: Olive
            Color(180, 34, 122),    // 6: Magenta
            Color(180, 34, 22)      // 7: Red
        )
    ),
    RETRO_GAMEBOY(
        displayName = "Retro 1989",
        boardBackground = Color(0xFF8B956D),
        gridBorder = Color(0xFF0F380F),
        gridLine = Color(0xFF7A865D),
        emptyCell = Color(0xFF879268),
        ghostColor = Color(0x400F380F),
        pieceColors = listOf(
            Color(0xFF8B956D),      // 0: empty
            Color(0xFF0F380F),      // 1
            Color(0xFF306230),      // 2
            Color(0xFF0F380F),      // 3
            Color(0xFF306230),      // 4
            Color(0xFF0F380F),      // 5
            Color(0xFF306230),      // 6
            Color(0xFF0F380F)       // 7
        )
    ),
    CYBERPUNK(
        displayName = "Cyberpunk",
        boardBackground = Color(0xFF050510),
        gridBorder = Color(0xFFFF007F),
        gridLine = Color(0xFF151528),
        emptyCell = Color(0xFF080816),
        ghostColor = Color(0x40FF007F),
        pieceColors = listOf(
            Color(0xFF000000),      // 0: empty
            Color(0xFF00F0FF),      // 1: electric cyan
            Color(0xFF0055FF),      // 2: electric blue
            Color(0xFFFF7700),      // 3: neon orange
            Color(0xFFFFEE00),      // 4: neon yellow
            Color(0xFF00FF66),      // 5: toxic green
            Color(0xFFFF007F),      // 6: hot pink
            Color(0xFFFF2A2A)       // 7: neon red
        )
    );

    fun getColor(index: Int): Color {
        return if (index in pieceColors.indices) pieceColors[index] else pieceColors.getOrElse(1) { Color.Cyan }
    }
}
