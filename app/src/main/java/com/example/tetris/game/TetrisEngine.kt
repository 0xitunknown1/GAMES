package com.example.tetris.game

import com.example.tetris.model.GameState
import com.example.tetris.model.SrsKickData
import com.example.tetris.model.Tetromino
import com.example.tetris.model.TetrominoType

data class LineClearEvent(
    val lines: Int,
    val isTetris: Boolean,
    val isTSpin: Boolean,
    val isBackToBack: Boolean,
    val combo: Int,
    val pointsAwarded: Int,
    val bannerTitle: String?,
    val bannerSubtitle: String?
)

/**
 * Authentic Tetris Guideline Game Engine:
 * - 7-Bag Randomizer (guarantees fair piece distributions)
 * - 3-piece Next Queue
 * - Lock Delay (500ms sliding / tucking window with up to 15 resets)
 * - Super Rotation System (SRS) 5-point wall & floor kicks
 * - T-Spin detection (3-corner rule)
 * - Back-to-Back (B2B) multipliers & Combos
 * - Standard scoring rules & Level speed scaling
 */
class TetrisEngine(
    val height: Int = 20,
    val width: Int = 10,
    var startingLevel: Int = 1
) {
    var field: Array<IntArray> = Array(height) { IntArray(width) { 0 } }
        private set

    var score: Int = 0
        private set

    var lines: Int = 0
        private set

    var level: Int = startingLevel
        private set

    var state: GameState = GameState.MENU

    var currentFigure: Tetromino? = null
        private set

    // 3-piece upcoming queue
    val nextQueue: MutableList<Tetromino> = mutableListOf()

    var holdFigure: Tetromino? = null
        private set

    var canHoldThisTurn: Boolean = true
        private set

    // Lock delay state
    val lockDelayLimitMs: Long = 500L
    var lockElapsedMs: Long = 0L
        private set
    var lockResets: Int = 0
        private set
    val maxLockResets: Int = 15

    // T-Spin & Combo tracking
    private var lastActionWasRotate: Boolean = false
    var comboCount: Int = -1
        private set
    var isBackToBackActive: Boolean = false
        private set

    // Flash animation rows
    var clearingRows: List<Int> = emptyList()
        private set

    // Internal 7-bag
    private val pieceBag = mutableListOf<TetrominoType>()

    init {
        reset(startingLevel)
    }

    fun reset(startLvl: Int = startingLevel) {
        startingLevel = startLvl
        level = startLvl
        score = 0
        lines = 0
        comboCount = -1
        isBackToBackActive = false
        clearingRows = emptyList()
        field = Array(height) { IntArray(width) { 0 } }
        holdFigure = null
        canHoldThisTurn = true
        state = GameState.PLAYING
        pieceBag.clear()
        nextQueue.clear()

        // Populate next queue with 3 pieces from 7-bag
        refillBagIfNeeded()
        while (nextQueue.size < 4) {
            nextQueue.add(drawFromBag())
        }

        spawnNextFigure()
    }

    private fun refillBagIfNeeded() {
        if (pieceBag.isEmpty()) {
            val shuffled = TetrominoType.entries.shuffled()
            pieceBag.addAll(shuffled)
        }
    }

    private fun drawFromBag(): Tetromino {
        refillBagIfNeeded()
        val type = pieceBag.removeAt(0)
        // Spawn positions: I and O spawn centered; J, L, S, T, Z spawn at x=3, y=0
        val spawnX = if (type == TetrominoType.O) 4 else 3
        return Tetromino(type = type, x = spawnX, y = 0, rotation = 0)
    }

    private fun spawnNextFigure() {
        refillBagIfNeeded()
        while (nextQueue.size < 4) {
            nextQueue.add(drawFromBag())
        }

        val next = nextQueue.removeAt(0)
        currentFigure = next
        canHoldThisTurn = true
        lastActionWasRotate = false
        lockElapsedMs = 0L
        lockResets = 0

        val figure = currentFigure
        if (figure != null && intersects(figure)) {
            state = GameState.GAME_OVER
        }
    }

    fun isCurrentPieceGrounded(): Boolean {
        val figure = currentFigure ?: return false
        return intersects(figure.copy(y = figure.y + 1))
    }

    fun intersects(tetromino: Tetromino): Boolean {
        val blocks = tetromino.currentBlocks()
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                if (i * 4 + j in blocks) {
                    val targetX = j + tetromino.x
                    val targetY = i + tetromino.y

                    if (targetX < 0 || targetX >= width) return true
                    if (targetY >= height) return true
                    if (targetY >= 0 && field[targetY][targetX] > 0) return true
                }
            }
        }
        return false
    }

    /**
     * Move horizontally. Resets lock delay if grounded.
     */
    fun goSide(dx: Int): Boolean {
        val figure = currentFigure ?: return false
        if (state != GameState.PLAYING) return false

        val candidate = figure.copy(x = figure.x + dx)
        if (!intersects(candidate)) {
            currentFigure = candidate
            lastActionWasRotate = false
            onPieceManipulatedWhileGrounded()
            return true
        }
        return false
    }

    /**
     * Rotate with Super Rotation System (SRS) wall & floor kicks.
     */
    fun rotate(direction: Int = 1): Boolean {
        val figure = currentFigure ?: return false
        if (state != GameState.PLAYING) return false

        val oldRot = figure.rotation
        val numRotations = figure.type.rotations.size
        val newRot = (oldRot + direction + numRotations) % numRotations
        val rotatedPiece = figure.copy(rotation = newRot)

        val kickTests = SrsKickData.getTests(figure.type, oldRot, newRot)
        for (kick in kickTests) {
            val candidate = rotatedPiece.copy(
                x = figure.x + kick.first,
                y = figure.y + kick.second
            )
            if (!intersects(candidate)) {
                currentFigure = candidate
                lastActionWasRotate = true
                onPieceManipulatedWhileGrounded()
                return true
            }
        }
        return false
    }

    private fun onPieceManipulatedWhileGrounded() {
        if (isCurrentPieceGrounded()) {
            if (lockResets < maxLockResets) {
                lockElapsedMs = 0L
                lockResets++
            }
        } else {
            // Slipped off a ledge! Reset lock timer completely
            lockElapsedMs = 0L
        }
    }

    /**
     * Automatic gravity fall: moves piece down 1 cell without awarding manual drop points.
     */
    fun goDownGravity(): Boolean {
        val figure = currentFigure ?: return false
        if (state != GameState.PLAYING) return false

        val candidate = figure.copy(y = figure.y + 1)
        if (!intersects(candidate)) {
            currentFigure = candidate
            lastActionWasRotate = false
            lockElapsedMs = 0L
            return true
        }
        return false
    }

    /**
     * Soft drop: Move down 1 cell manually.
     * Returns true if moved, false if already grounded. Awards +1 point.
     */
    fun softDrop(): Boolean {
        val figure = currentFigure ?: return false
        if (state != GameState.PLAYING) return false

        val candidate = figure.copy(y = figure.y + 1)
        if (!intersects(candidate)) {
            currentFigure = candidate
            score += 1
            lastActionWasRotate = false
            lockElapsedMs = 0L
            return true
        }
        return false
    }

    fun resetLockElapsed() {
        if (lockResets < maxLockResets) {
            lockElapsedMs = 0L
            lockResets++
        }
    }

    /**
     * Hard drop: instantly drop to ghost position and lock immediately.
     */
    fun hardDrop(): LineClearEvent {
        val figure = currentFigure ?: return emptyClearEvent()
        if (state != GameState.PLAYING) return emptyClearEvent()

        var dropRows = 0
        var candidate = figure
        while (!intersects(candidate.copy(y = candidate.y + 1))) {
            candidate = candidate.copy(y = candidate.y + 1)
            dropRows++
        }
        currentFigure = candidate
        score += dropRows * 2
        return lockCurrentPiece()
    }

    /**
     * Advance lock delay by deltaMs if grounded.
     * If lock delay expires, locks down the piece.
     */
    fun advanceLockDelay(deltaMs: Long): LineClearEvent? {
        if (state != GameState.PLAYING || currentFigure == null) return null
        if (isCurrentPieceGrounded()) {
            lockElapsedMs += deltaMs
            if (lockElapsedMs >= lockDelayLimitMs) {
                return lockCurrentPiece()
            }
        } else {
            lockElapsedMs = 0L
        }
        return null
    }

    /**
     * Hold current piece.
     */
    fun holdPiece(): Boolean {
        if (!canHoldThisTurn || state != GameState.PLAYING) return false
        val current = currentFigure ?: return false

        val held = holdFigure
        if (held == null) {
            holdFigure = current.copy(x = 3, y = 0, rotation = 0)
            spawnNextFigure()
        } else {
            holdFigure = current.copy(x = 3, y = 0, rotation = 0)
            val spawnX = if (held.type == TetrominoType.O) 4 else 3
            currentFigure = held.copy(x = spawnX, y = 0, rotation = 0)
        }
        canHoldThisTurn = false
        lockElapsedMs = 0L
        lockResets = 0
        lastActionWasRotate = false
        return true
    }

    fun calculateGhostY(): Int {
        val figure = currentFigure ?: return 0
        var ghostY = figure.y
        while (!intersects(figure.copy(y = ghostY + 1))) {
            ghostY++
        }
        return ghostY
    }

    /**
     * Locks current tetromino, checks for T-Spin, clears lines, updates score/combos/B2B,
     * and spawns the next piece.
     */
    fun lockCurrentPiece(): LineClearEvent {
        val figure = currentFigure ?: return emptyClearEvent()
        val blocks = figure.currentBlocks()

        // 1. T-Spin detection (3-corner rule)
        val isTSpin = checkTSpin(figure)

        // 2. Burn into field
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                if (i * 4 + j in blocks) {
                    val targetY = i + figure.y
                    val targetX = j + figure.x
                    if (targetY in 0 until height && targetX in 0 until width) {
                        field[targetY][targetX] = figure.colorIndex
                    }
                }
            }
        }

        // 3. Clear lines & evaluate scores
        val clearEvent = evaluateLineClears(isTSpin)

        // 4. Spawn next figure
        spawnNextFigure()
        return clearEvent
    }

    private fun checkTSpin(figure: Tetromino): Boolean {
        if (figure.type != TetrominoType.T || !lastActionWasRotate) return false

        // In the 3x3 bounding box of T, the corners are:
        // (x, y), (x+2, y), (x, y+2), (x+2, y+2)
        val corners = listOf(
            Pair(figure.x, figure.y),
            Pair(figure.x + 2, figure.y),
            Pair(figure.x, figure.y + 2),
            Pair(figure.x + 2, figure.y + 2)
        )

        var occupiedCorners = 0
        for ((cx, cy) in corners) {
            if (cx < 0 || cx >= width || cy >= height || (cy >= 0 && field[cy][cx] > 0)) {
                occupiedCorners++
            }
        }
        return occupiedCorners >= 3
    }

    private fun evaluateLineClears(isTSpin: Boolean): LineClearEvent {
        val fullRows = mutableListOf<Int>()
        for (i in 0 until height) {
            var full = true
            for (j in 0 until width) {
                if (field[i][j] == 0) {
                    full = false
                    break
                }
            }
            if (full) fullRows.add(i)
        }

        val linesCleared = fullRows.size

        if (linesCleared == 0) {
            comboCount = -1 // Combo broken
            if (isTSpin) {
                score += 100 * level
                return LineClearEvent(
                    lines = 0,
                    isTetris = false,
                    isTSpin = true,
                    isBackToBack = false,
                    combo = -1,
                    pointsAwarded = 100 * level,
                    bannerTitle = "T-SPIN",
                    bannerSubtitle = null
                )
            }
            return emptyClearEvent()
        }

        clearingRows = fullRows

        // Shift rows down
        for (row in fullRows) {
            for (r in row downTo 1) {
                for (c in 0 until width) {
                    field[r][c] = field[r - 1][c]
                }
            }
            for (c in 0 until width) {
                field[0][c] = 0
            }
        }

        lines += linesCleared
        comboCount++

        val isTetris = linesCleared == 4
        val isDifficult = isTetris || isTSpin
        val isB2B = isDifficult && isBackToBackActive
        if (isDifficult) {
            isBackToBackActive = true
        } else {
            isBackToBackActive = false
        }

        // Base points according to Guideline
        var basePoints = when {
            isTSpin && linesCleared == 1 -> 800 * level
            isTSpin && linesCleared == 2 -> 1200 * level
            isTSpin && linesCleared == 3 -> 1600 * level
            isTetris -> 800 * level
            linesCleared == 3 -> 500 * level
            linesCleared == 2 -> 300 * level
            else -> 100 * level
        }

        // Back-to-Back multiplier: 1.5x
        if (isB2B) {
            basePoints = (basePoints * 1.5).toInt()
        }

        // Combo bonus: 50 * combo * level
        val comboBonus = if (comboCount > 0) 50 * comboCount * level else 0
        val totalAwarded = basePoints + comboBonus
        score += totalAwarded

        // Level up every 10 lines
        level = (startingLevel + (lines / 10)).coerceAtMost(20)

        // Banner text
        val title = when {
            isTSpin && isTetris -> "T-SPIN TETRIS!"
            isTSpin -> "T-SPIN ${when (linesCleared) { 1 -> "SINGLE"; 2 -> "DOUBLE"; else -> "TRIPLE" }}"
            isTetris -> "TETRIS!"
            linesCleared == 3 -> "TRIPLE"
            linesCleared == 2 -> "DOUBLE"
            else -> "SINGLE"
        }

        val subtitle = when {
            isB2B && comboCount > 0 -> "B2B • COMBO x$comboCount"
            isB2B -> "BACK-TO-BACK"
            comboCount > 0 -> "COMBO x$comboCount"
            else -> null
        }

        return LineClearEvent(
            lines = linesCleared,
            isTetris = isTetris,
            isTSpin = isTSpin,
            isBackToBack = isB2B,
            combo = comboCount,
            pointsAwarded = totalAwarded,
            bannerTitle = title,
            bannerSubtitle = subtitle
        )
    }

    fun clearRowFlash() {
        clearingRows = emptyList()
    }

    private fun emptyClearEvent() = LineClearEvent(
        lines = 0,
        isTetris = false,
        isTSpin = false,
        isBackToBack = false,
        combo = -1,
        pointsAwarded = 0,
        bannerTitle = null,
        bannerSubtitle = null
    )
}
