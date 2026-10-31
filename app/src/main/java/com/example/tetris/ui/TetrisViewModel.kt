package com.example.tetris.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetris.audio.TetrisAudioAndHaptics
import com.example.tetris.data.TetrisPreferences
import com.example.tetris.game.LineClearEvent
import com.example.tetris.game.TetrisEngine
import com.example.tetris.model.ActionBanner
import com.example.tetris.model.BoardTheme
import com.example.tetris.model.GameState
import com.example.tetris.model.Tetromino
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TetrisUiState(
    val gameState: GameState = GameState.MENU,
    val field: Array<IntArray> = Array(20) { IntArray(10) { 0 } },
    val currentFigure: Tetromino? = null,
    val ghostY: Int = 0,
    val nextFigure: Tetromino? = null,
    val nextQueue: List<Tetromino> = emptyList(),
    val holdFigure: Tetromino? = null,
    val score: Int = 0,
    val lines: Int = 0,
    val level: Int = 1,
    val combo: Int = -1,
    val isBackToBack: Boolean = false,
    val isLocking: Boolean = false,
    val lockProgress: Float = 0f,
    val actionBanner: ActionBanner? = null,
    val screenShake: Boolean = false,
    val highScore: Int = 0,
    val isNewHighScore: Boolean = false,
    val boardTheme: BoardTheme = BoardTheme.OFFICIAL_GUIDELINE,
    val isSoundEnabled: Boolean = true,
    val isBgmEnabled: Boolean = false,
    val isHapticsEnabled: Boolean = true,
    val isSwipeEnabled: Boolean = true,
    val startingLevel: Int = 1,
    val clearingRows: List<Int> = emptyList(),
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TetrisUiState
        if (gameState != other.gameState) return false
        if (score != other.score) return false
        if (lines != other.lines) return false
        if (level != other.level) return false
        if (combo != other.combo) return false
        if (isBackToBack != other.isBackToBack) return false
        if (isLocking != other.isLocking) return false
        if (screenShake != other.screenShake) return false
        if (actionBanner != other.actionBanner) return false
        if (highScore != other.highScore) return false
        if (isNewHighScore != other.isNewHighScore) return false
        if (boardTheme != other.boardTheme) return false
        if (isSoundEnabled != other.isSoundEnabled) return false
        if (isBgmEnabled != other.isBgmEnabled) return false
        if (isHapticsEnabled != other.isHapticsEnabled) return false
        if (isSwipeEnabled != other.isSwipeEnabled) return false
        if (currentFigure != other.currentFigure) return false
        if (ghostY != other.ghostY) return false
        if (nextFigure != other.nextFigure) return false
        if (holdFigure != other.holdFigure) return false
        if (!field.contentDeepEquals(other.field)) return false
        if (clearingRows != other.clearingRows) return false
        return true
    }

    override fun hashCode(): Int {
        var result = gameState.hashCode()
        result = 31 * result + score
        result = 31 * result + lines
        result = 31 * result + level
        result = 31 * result + highScore
        result = 31 * result + currentFigure.hashCode()
        result = 31 * result + ghostY
        result = 31 * result + field.contentDeepHashCode()
        return result
    }
}

class TetrisViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = TetrisPreferences(application)
    private val audio = TetrisAudioAndHaptics(application).apply {
        isSoundEnabled = prefs.isSoundEnabled
        isHapticsEnabled = prefs.isHapticsEnabled
        isBgmEnabled = prefs.isBgmEnabled
    }

    private val engine = TetrisEngine(height = 20, width = 10, startingLevel = prefs.startingLevel)

    private val _uiState = MutableStateFlow(
        TetrisUiState(
            gameState = GameState.MENU,
            highScore = prefs.highScore,
            boardTheme = prefs.boardTheme,
            isSoundEnabled = prefs.isSoundEnabled,
            isBgmEnabled = prefs.isBgmEnabled,
            isHapticsEnabled = prefs.isHapticsEnabled,
            isSwipeEnabled = prefs.isSwipeEnabled,
            startingLevel = prefs.startingLevel,
            totalGamesPlayed = prefs.totalGamesPlayed,
            totalLinesCleared = prefs.totalLinesCleared
        )
    )
    val uiState: StateFlow<TetrisUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var isSoftDropping: Boolean = false
    private var bannerJob: Job? = null

    // DAS / ARR Repeat jobs
    private var dasJob: Job? = null

    init {
        startGame(prefs.startingLevel)
    }

    fun startGame(startLevel: Int? = null) {
        val levelToUse = startLevel ?: _uiState.value.startingLevel
        prefs.startingLevel = levelToUse
        engine.reset(levelToUse)
        _uiState.update { it.copy(gameState = GameState.PLAYING, isNewHighScore = false, actionBanner = null) }
        syncStateFromEngine()
        startGameLoop()
    }

    fun pauseGame() {
        if (engine.state == GameState.PLAYING) {
            engine.state = GameState.PAUSED
            stopDas()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
            audio.playPauseSound()
            stopGameLoop()
        }
    }

    fun resumeGame() {
        if (engine.state == GameState.PAUSED) {
            engine.state = GameState.PLAYING
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            audio.playMenuClick()
            startGameLoop()
        }
    }

    fun restartGame() {
        stopDas()
        startGame(_uiState.value.startingLevel)
    }

    fun returnToMenu() {
        stopDas()
        stopGameLoop()
        engine.state = GameState.MENU
        _uiState.update {
            it.copy(
                gameState = GameState.MENU,
                highScore = prefs.highScore,
                totalGamesPlayed = prefs.totalGamesPlayed,
                totalLinesCleared = prefs.totalLinesCleared,
                actionBanner = null
            )
        }
        audio.playMenuClick()
    }

    // --- LATERAL MOVEMENT WITH DAS & ARR ---

    fun startMoveLeft() {
        stopDas()
        if (engine.state != GameState.PLAYING) return
        performSideMove(-1)

        dasJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            delay(140) // DAS delay
            while (isActive && engine.state == GameState.PLAYING) {
                performSideMove(-1)
                delay(35) // ARR rate
            }
        }
    }

    fun stopMoveLeft() {
        stopDas()
    }

    fun startMoveRight() {
        stopDas()
        if (engine.state != GameState.PLAYING) return
        performSideMove(1)

        dasJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            delay(140) // DAS delay
            while (isActive && engine.state == GameState.PLAYING) {
                performSideMove(1)
                delay(35) // ARR rate
            }
        }
    }

    fun stopMoveRight() {
        stopDas()
    }

    private fun stopDas() {
        dasJob?.cancel()
        dasJob = null
    }

    private fun performSideMove(dx: Int) {
        if (engine.goSide(dx)) {
            audio.playMoveSound()
            audio.vibrateMove()
            engine.resetLockElapsed()
            syncStateFromEngine()
        }
    }

    fun moveLeft() = performSideMove(-1)
    fun moveRight() = performSideMove(1)

    // --- ROTATION ---

    fun rotate() {
        if (engine.state != GameState.PLAYING) return
        if (engine.rotate(1)) {
            audio.playRotateSound()
            audio.vibrateRotate()
            engine.resetLockElapsed()
            syncStateFromEngine()
        }
    }

    fun rotateCounterClockwise() {
        if (engine.state != GameState.PLAYING) return
        if (engine.rotate(-1)) {
            audio.playRotateSound()
            audio.vibrateRotate()
            engine.resetLockElapsed()
            syncStateFromEngine()
        }
    }

    // --- DROPS ---

    fun setSoftDrop(active: Boolean) {
        if (isSoftDropping == active) return
        isSoftDropping = active
        if (active && engine.state == GameState.PLAYING) {
            if (engine.softDrop()) {
                audio.playSoftDropSound()
                audio.vibrateMove()
                syncStateFromEngine()
            }
        }
    }

    fun hardDrop() {
        if (engine.state != GameState.PLAYING) return
        stopDas()
        val clearEvent = engine.hardDrop()
        audio.playHardDropSound()
        audio.vibrateDrop()
        triggerScreenShake()
        handleClearEvent(clearEvent)
        syncStateFromEngine()
    }

    fun holdPiece() {
        if (engine.state != GameState.PLAYING) return
        if (engine.holdPiece()) {
            audio.playHoldSound()
            audio.vibrateRotate()
            syncStateFromEngine()
        }
    }

    // --- AUTOMATIC GRAVITY & LOCK DELAY LOOP ---

    private fun startGameLoop() {
        stopGameLoop()
        gameLoopJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            while (isActive && engine.state == GameState.PLAYING) {
                val fallInterval = if (isSoftDropping) {
                    40L
                } else {
                    // Level 1: 750ms, Level 2: 700ms ... Level 15+: 80ms
                    (750L - (engine.level - 1) * 50L).coerceAtLeast(80L)
                }

                // If not resting on ground, fall automatically!
                if (!engine.isCurrentPieceGrounded()) {
                    delay(fallInterval)
                    if (!isActive || engine.state != GameState.PLAYING) break

                    if (!engine.isCurrentPieceGrounded()) {
                        engine.goDownGravity()
                        if (isSoftDropping) {
                            audio.playSoftDropSound()
                        }
                        syncStateFromEngine()
                    }
                } else {
                    // Piece is resting on ground/blocks: start 500ms lock delay
                    val lockDelayTime = 500L
                    var elapsed = 0L
                    val checkStep = 40L

                    while (isActive && engine.state == GameState.PLAYING && engine.isCurrentPieceGrounded() && elapsed < lockDelayTime) {
                        delay(checkStep)
                        elapsed += checkStep
                        engine.advanceLockDelay(checkStep)
                        syncStateFromEngine()
                    }

                    // If still grounded after lock delay, lock it into place!
                    if (isActive && engine.state == GameState.PLAYING && engine.isCurrentPieceGrounded()) {
                        val clearEvent = engine.lockCurrentPiece()
                        audio.playLockSound()
                        audio.vibrateDrop()
                        handleClearEvent(clearEvent)
                        syncStateFromEngine()
                    }
                }
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun handleClearEvent(event: LineClearEvent) {
        if (event.lines > 0) {
            if (event.isTetris) {
                audio.playLineClearSound(4)
                triggerScreenShake()
            } else if (event.isTSpin) {
                audio.playTSpinSound()
            } else {
                audio.playLineClearSound(event.lines)
            }
            audio.vibrateLineClear()

            viewModelScope.launch {
                delay(180)
                engine.clearRowFlash()
                syncStateFromEngine()
            }
        }

        // Show Action Banner (TETRIS, T-SPIN, BACK-TO-BACK, COMBOS)
        if (event.bannerTitle != null) {
            showActionBanner(event.bannerTitle, event.bannerSubtitle)
        }

        // Check Game Over
        if (engine.state == GameState.GAME_OVER) {
            stopGameLoop()
            stopDas()
            val isNewRecord = prefs.recordGameFinished(engine.score, engine.lines)
            _uiState.update {
                it.copy(
                    gameState = GameState.GAME_OVER,
                    isNewHighScore = isNewRecord,
                    highScore = prefs.highScore,
                    totalGamesPlayed = prefs.totalGamesPlayed,
                    totalLinesCleared = prefs.totalLinesCleared
                )
            }
            audio.playGameOverSound()
            audio.vibrateGameOver()
        }
    }

    private fun showActionBanner(title: String, subtitle: String?) {
        bannerJob?.cancel()
        bannerJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionBanner = ActionBanner(
                        title = title,
                        subtitle = subtitle,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            delay(1800)
            _uiState.update { it.copy(actionBanner = null) }
        }
    }

    private fun triggerScreenShake() {
        viewModelScope.launch {
            _uiState.update { it.copy(screenShake = true) }
            delay(120)
            _uiState.update { it.copy(screenShake = false) }
        }
    }

    private fun syncStateFromEngine() {
        val fieldCopy = Array(engine.height) { r -> engine.field[r].clone() }
        val ghost = if (engine.currentFigure != null) engine.calculateGhostY() else 0
        val isGrounded = engine.isCurrentPieceGrounded()
        val lockProg = if (isGrounded) (engine.lockElapsedMs.toFloat() / engine.lockDelayLimitMs).coerceIn(0f, 1f) else 0f

        _uiState.update { current ->
            current.copy(
                gameState = engine.state,
                field = fieldCopy,
                currentFigure = engine.currentFigure,
                ghostY = ghost,
                nextFigure = engine.nextQueue.firstOrNull(),
                nextQueue = engine.nextQueue.take(3),
                holdFigure = engine.holdFigure,
                score = engine.score,
                lines = engine.lines,
                level = engine.level,
                combo = engine.comboCount,
                isBackToBack = engine.isBackToBackActive,
                isLocking = isGrounded,
                lockProgress = lockProg,
                clearingRows = engine.clearingRows
            )
        }
    }

    // --- SETTINGS TOGGLES ---

    fun toggleSound() {
        val next = !_uiState.value.isSoundEnabled
        prefs.isSoundEnabled = next
        audio.isSoundEnabled = next
        _uiState.update { it.copy(isSoundEnabled = next) }
        if (next) audio.playMenuClick()
    }

    fun toggleBgm() {
        val next = !_uiState.value.isBgmEnabled
        prefs.isBgmEnabled = next
        audio.isBgmEnabled = next
        _uiState.update { it.copy(isBgmEnabled = next) }
    }

    fun toggleHaptics() {
        val next = !_uiState.value.isHapticsEnabled
        prefs.isHapticsEnabled = next
        audio.isHapticsEnabled = next
        _uiState.update { it.copy(isHapticsEnabled = next) }
        if (next) audio.vibrateRotate()
    }

    fun toggleSwipe() {
        val next = !_uiState.value.isSwipeEnabled
        prefs.isSwipeEnabled = next
        _uiState.update { it.copy(isSwipeEnabled = next) }
    }

    fun setStartingLevel(lvl: Int) {
        val bounded = lvl.coerceIn(1, 10)
        prefs.startingLevel = bounded
        _uiState.update { it.copy(startingLevel = bounded) }
    }

    fun selectTheme(theme: BoardTheme) {
        prefs.boardTheme = theme
        _uiState.update { it.copy(boardTheme = theme) }
        audio.playMenuClick()
    }

    override fun onCleared() {
        super.onCleared()
        stopDas()
        stopGameLoop()
        audio.release()
    }
}
