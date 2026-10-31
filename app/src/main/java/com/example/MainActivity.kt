package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.tetris.model.GameState
import com.example.tetris.ui.TetrisViewModel
import com.example.tetris.ui.screens.TetrisGameScreen
import com.example.tetris.ui.screens.TetrisMenuScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TetrisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(uiState.boardTheme.boardBackground),
                    color = uiState.boardTheme.boardBackground
                ) {
                    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                        when (uiState.gameState) {
                            GameState.MENU -> {
                                TetrisMenuScreen(
                                    uiState = uiState,
                                    onStartGame = { lvl -> viewModel.startGame(lvl) },
                                    onStartingLevelChanged = { lvl -> viewModel.setStartingLevel(lvl) },
                                    onThemeSelected = { theme -> viewModel.selectTheme(theme) },
                                    onToggleSound = { viewModel.toggleSound() },
                                    onToggleBgm = { viewModel.toggleBgm() },
                                    onToggleHaptics = { viewModel.toggleHaptics() },
                                    onToggleSwipe = { viewModel.toggleSwipe() }
                                )
                            }
                            GameState.PLAYING,
                            GameState.PAUSED,
                            GameState.GAME_OVER -> {
                                TetrisGameScreen(
                                    uiState = uiState,
                                    onStartMoveLeft = { viewModel.startMoveLeft() },
                                    onStopMoveLeft = { viewModel.stopMoveLeft() },
                                    onStartMoveRight = { viewModel.startMoveRight() },
                                    onStopMoveRight = { viewModel.stopMoveRight() },
                                    onRotateCw = { viewModel.rotate() },
                                    onRotateCcw = { viewModel.rotateCounterClockwise() },
                                    onSoftDrop = { active -> viewModel.setSoftDrop(active) },
                                    onHardDrop = { viewModel.hardDrop() },
                                    onHold = { viewModel.holdPiece() },
                                    onPause = { viewModel.pauseGame() },
                                    onResume = { viewModel.resumeGame() },
                                    onRestart = { viewModel.restartGame() },
                                    onReturnToMenu = { viewModel.returnToMenu() },
                                    onToggleSound = { viewModel.toggleSound() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Kept running so browser frame focus switches do not freeze the falling blocks
    }
}
