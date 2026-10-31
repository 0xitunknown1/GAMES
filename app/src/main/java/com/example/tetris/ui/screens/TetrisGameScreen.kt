package com.example.tetris.ui.screens

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetris.model.GameState
import com.example.tetris.ui.TetrisUiState
import com.example.tetris.ui.components.ArcadeControls
import com.example.tetris.ui.components.GameOverOverlay
import com.example.tetris.ui.components.NextQueueBox
import com.example.tetris.ui.components.PauseOverlay
import com.example.tetris.ui.components.PiecePreviewBox
import com.example.tetris.ui.components.TetrisBoardView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TetrisGameScreen(
    uiState: TetrisUiState,
    onStartMoveLeft: () -> Unit,
    onStopMoveLeft: () -> Unit,
    onStartMoveRight: () -> Unit,
    onStopMoveRight: () -> Unit,
    onRotateCw: () -> Unit,
    onRotateCcw: () -> Unit,
    onSoftDrop: (Boolean) -> Unit,
    onHardDrop: () -> Unit,
    onHold: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onReturnToMenu: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = uiState.boardTheme
    val focusRequester = remember { FocusRequester() }

    BackHandler {
        if (uiState.gameState == GameState.PLAYING) {
            onPause()
        } else {
            onReturnToMenu()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.boardBackground)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            onStartMoveLeft()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            onStartMoveRight()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            onRotateCw()
                            true
                        }
                        KeyEvent.KEYCODE_Z -> {
                            onRotateCcw()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            onSoftDrop(true)
                            true
                        }
                        KeyEvent.KEYCODE_SPACE -> {
                            onHardDrop()
                            true
                        }
                        KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_H -> {
                            onHold()
                            true
                        }
                        KeyEvent.KEYCODE_P -> {
                            if (uiState.gameState == GameState.PLAYING) onPause()
                            else if (uiState.gameState == GameState.PAUSED) onResume()
                            true
                        }
                        KeyEvent.KEYCODE_R -> {
                            onRestart()
                            true
                        }
                        KeyEvent.KEYCODE_M -> {
                            onReturnToMenu()
                            true
                        }
                        else -> false
                    }
                } else if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            onStopMoveLeft()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            onStopMoveRight()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            onSoftDrop(false)
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SCORE",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.score}",
                                    color = theme.getColor(1),
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column {
                                Text(
                                    text = "BEST",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.highScore}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onReturnToMenu,
                            modifier = Modifier.testTag("btn_top_menu")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onToggleSound,
                            modifier = Modifier.testTag("btn_toggle_sound")
                        ) {
                            Icon(
                                imageVector = if (uiState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Toggle Sound",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                if (uiState.gameState == GameState.PLAYING) onPause()
                                else if (uiState.gameState == GameState.PAUSED) onResume()
                            },
                            modifier = Modifier.testTag("btn_pause")
                        ) {
                            Icon(
                                imageVector = if (uiState.gameState == GameState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Pause Game",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = theme.boardBackground
                    )
                )
            },
            containerColor = theme.boardBackground
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- MAIN PLAYFIELD & SIDE PANEL ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Board
                    TetrisBoardView(
                        field = uiState.field,
                        currentFigure = uiState.currentFigure,
                        ghostY = uiState.ghostY,
                        clearingRows = uiState.clearingRows,
                        isLocking = uiState.isLocking,
                        lockProgress = uiState.lockProgress,
                        actionBanner = uiState.actionBanner,
                        screenShake = uiState.screenShake,
                        theme = theme,
                        isSwipeEnabled = uiState.isSwipeEnabled,
                        onMoveLeft = onStartMoveLeft,
                        onMoveRight = onStartMoveRight,
                        onRotate = onRotateCw,
                        onSoftDrop = onSoftDrop,
                        onHardDrop = onHardDrop,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Right side: Dashboard Info (HOLD, NEXT QUEUE, LEVEL, LINES, B2B)
                    Column(
                        modifier = Modifier
                            .width(76.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PiecePreviewBox(
                            title = "HOLD",
                            tetromino = uiState.holdFigure,
                            theme = theme
                        )

                        NextQueueBox(
                            nextQueue = uiState.nextQueue,
                            theme = theme
                        )

                        // Level badge
                        MiniStatCard(
                            label = "LEVEL",
                            value = "${uiState.level}",
                            color = theme.getColor(3)
                        )

                        // Lines badge
                        MiniStatCard(
                            label = "LINES",
                            value = "${uiState.lines}",
                            color = theme.getColor(5)
                        )

                        // B2B indicator if active
                        if (uiState.isBackToBack) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "B2B",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // --- ON-SCREEN ARCADE CONTROLS WITH DAS ---
                ArcadeControls(
                    theme = theme,
                    onStartMoveLeft = onStartMoveLeft,
                    onStopMoveLeft = onStopMoveLeft,
                    onStartMoveRight = onStartMoveRight,
                    onStopMoveRight = onStopMoveRight,
                    onRotateCw = onRotateCw,
                    onRotateCcw = onRotateCcw,
                    onSoftDrop = onSoftDrop,
                    onHardDrop = onHardDrop,
                    onHold = onHold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        // --- OVERLAYS ---
        if (uiState.gameState == GameState.PAUSED) {
            PauseOverlay(
                score = uiState.score,
                lines = uiState.lines,
                level = uiState.level,
                theme = theme,
                onResume = onResume,
                onRestart = onRestart,
                onMenu = onReturnToMenu
            )
        }

        if (uiState.gameState == GameState.GAME_OVER) {
            GameOverOverlay(
                score = uiState.score,
                highScore = uiState.highScore,
                isNewHighScore = uiState.isNewHighScore,
                lines = uiState.lines,
                level = uiState.level,
                theme = theme,
                onRestart = onRestart,
                onMenu = onReturnToMenu
            )
        }
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = color,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black
        )
    }
}
