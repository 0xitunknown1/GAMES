package com.example.tetris.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetris.model.ActionBanner
import com.example.tetris.model.BoardTheme
import com.example.tetris.model.Tetromino
import kotlin.math.abs

/**
 * 10x20 Tetris Game Board with responsive Canvas rendering, 3D beveled blocks,
 * lock delay pulsating highlight, ghost piece projection, line clear flash,
 * screen impact shake, and floating arcade event banner.
 */
@Composable
fun TetrisBoardView(
    field: Array<IntArray>,
    currentFigure: Tetromino?,
    ghostY: Int,
    clearingRows: List<Int>,
    isLocking: Boolean,
    lockProgress: Float,
    actionBanner: ActionBanner?,
    screenShake: Boolean,
    theme: BoardTheme,
    isSwipeEnabled: Boolean,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDrop: (Boolean) -> Unit,
    onHardDrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

    val shakeOffset = if (screenShake) IntOffset(0, 4) else IntOffset.Zero

    BoxWithConstraints(
        modifier = modifier
            .testTag("tetris_board_container")
            .offset { shakeOffset }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Enforce 10:20 aspect ratio (1:2)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(10f / 20f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("tetris_canvas")
                    .pointerInput(isSwipeEnabled) {
                        if (!isSwipeEnabled) return@pointerInput

                        detectTapGestures(
                            onTap = { onRotate() },
                            onDoubleTap = { onHardDrop() }
                        )
                    }
                    .pointerInput(isSwipeEnabled) {
                        if (!isSwipeEnabled) return@pointerInput

                        detectDragGestures(
                            onDragStart = {
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                            },
                            onDragEnd = {
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                                onSoftDrop(false)
                            },
                            onDragCancel = {
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                                onSoftDrop(false)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDragX += dragAmount.x
                                accumulatedDragY += dragAmount.y

                                val cellSize = size.width / 10f
                                val thresholdX = cellSize * 0.70f
                                val thresholdY = cellSize * 0.70f

                                if (abs(accumulatedDragX) > thresholdX) {
                                    if (accumulatedDragX > 0) onMoveRight() else onMoveLeft()
                                    accumulatedDragX = 0f
                                }

                                if (accumulatedDragY > thresholdY) {
                                    onSoftDrop(true)
                                    accumulatedDragY = 0f
                                }
                            }
                        )
                    }
            ) {
                val boardWidth = size.width
                val boardHeight = size.height
                val cellSize = boardWidth / 10f

                // 1. Board Background
                drawRoundRect(
                    color = theme.boardBackground,
                    size = Size(boardWidth, boardHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                // 2. Subtle Grid Lines
                for (col in 0..10) {
                    val x = col * cellSize
                    drawLine(
                        color = theme.gridLine,
                        start = Offset(x, 0f),
                        end = Offset(x, boardHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                for (row in 0..20) {
                    val y = row * cellSize
                    drawLine(
                        color = theme.gridLine,
                        start = Offset(0f, y),
                        end = Offset(boardWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 3. Settled Blocks in Field
                for (r in 0 until 20) {
                    val isClearing = r in clearingRows
                    for (c in 0 until 10) {
                        val colorIdx = field[r][c]
                        if (colorIdx > 0) {
                            val baseColor = if (isClearing) Color.White else theme.getColor(colorIdx)
                            drawBlock(
                                col = c,
                                row = r,
                                cellSize = cellSize,
                                color = baseColor,
                                isFlash = isClearing,
                                lockGlowAlpha = 0f
                            )
                        }
                    }
                }

                // 4. Ghost Piece (Drop Projection Shadow)
                if (currentFigure != null && ghostY > currentFigure.y) {
                    val blocks = currentFigure.currentBlocks()
                    for (i in 0 until 4) {
                        for (j in 0 until 4) {
                            if (i * 4 + j in blocks) {
                                val r = i + ghostY
                                val c = j + currentFigure.x
                                if (r in 0 until 20 && c in 0 until 10) {
                                    drawGhostBlock(
                                        col = c,
                                        row = r,
                                        cellSize = cellSize,
                                        outlineColor = theme.ghostColor
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Active Falling Tetromino (with lock delay pulsating glow)
                if (currentFigure != null) {
                    val blocks = currentFigure.currentBlocks()
                    val pieceColor = theme.getColor(currentFigure.colorIndex)
                    val lockGlow = if (isLocking) (0.25f + lockProgress * 0.45f) else 0f

                    for (i in 0 until 4) {
                        for (j in 0 until 4) {
                            if (i * 4 + j in blocks) {
                                val r = i + currentFigure.y
                                val c = j + currentFigure.x
                                if (r in 0 until 20 && c in 0 until 10) {
                                    drawBlock(
                                        col = c,
                                        row = r,
                                        cellSize = cellSize,
                                        color = pieceColor,
                                        isFlash = false,
                                        lockGlowAlpha = lockGlow
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Glowing Outer Arcade Border
                drawRoundRect(
                    color = theme.gridBorder,
                    size = Size(boardWidth, boardHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Floating Action Banner (TETRIS!, T-SPIN, BACK-TO-BACK, COMBOS)
            AnimatedVisibility(
                visible = actionBanner != null,
                enter = scaleIn(initialScale = 0.7f) + fadeIn(),
                exit = scaleOut(targetScale = 1.15f) + fadeOut()
            ) {
                if (actionBanner != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = actionBanner.color,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = actionBanner.title,
                                color = actionBanner.color,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center
                            )
                            if (actionBanner.subtitle != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = actionBanner.subtitle,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws a single tetromino block with classic arcade 3D beveled edges and optional lock glow.
 */
private fun DrawScope.drawBlock(
    col: Int,
    row: Int,
    cellSize: Float,
    color: Color,
    isFlash: Boolean,
    lockGlowAlpha: Float
) {
    val padding = 1.2f
    val x = col * cellSize + padding
    val y = row * cellSize + padding
    val size = cellSize - (padding * 2f)
    val corner = 3.dp.toPx()

    if (isFlash) {
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(x, y),
            size = Size(size, size),
            cornerRadius = CornerRadius(corner, corner)
        )
        return
    }

    // Outer block body
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(size, size),
        cornerRadius = CornerRadius(corner, corner)
    )

    // Highlight top-left bevel (lighter tone)
    val bevelWidth = size * 0.16f

    // Top border highlight
    drawRect(
        color = Color.White.copy(alpha = 0.45f),
        topLeft = Offset(x + 1f, y + 1f),
        size = Size(size - 2f, bevelWidth)
    )
    // Left border highlight
    drawRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = Offset(x + 1f, y + 1f),
        size = Size(bevelWidth, size - 2f)
    )

    // Bottom-right bevel shadow (darker tone)
    drawRect(
        color = Color.Black.copy(alpha = 0.40f),
        topLeft = Offset(x + 1f, y + size - bevelWidth - 1f),
        size = Size(size - 2f, bevelWidth)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.40f),
        topLeft = Offset(x + size - bevelWidth - 1f, y + 1f),
        size = Size(bevelWidth, size - 2f)
    )

    // Inner center square for tactile retro depth
    val innerMargin = bevelWidth * 1.3f
    drawRoundRect(
        color = color.copy(alpha = 0.60f),
        topLeft = Offset(x + innerMargin, y + innerMargin),
        size = Size(size - (innerMargin * 2f), size - (innerMargin * 2f)),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Lock delay pulsating white overlay
    if (lockGlowAlpha > 0f) {
        drawRoundRect(
            color = Color.White.copy(alpha = lockGlowAlpha),
            topLeft = Offset(x, y),
            size = Size(size, size),
            cornerRadius = CornerRadius(corner, corner)
        )
    }
}

/**
 * Draws the ghost piece block (translucent drop destination indicator).
 */
private fun DrawScope.drawGhostBlock(
    col: Int,
    row: Int,
    cellSize: Float,
    outlineColor: Color
) {
    val padding = 2f
    val x = col * cellSize + padding
    val y = row * cellSize + padding
    val size = cellSize - (padding * 2f)
    val corner = 3.dp.toPx()

    // Translucent fill
    drawRoundRect(
        color = outlineColor,
        topLeft = Offset(x, y),
        size = Size(size, size),
        cornerRadius = CornerRadius(corner, corner)
    )

    // Crisp outline
    drawRoundRect(
        color = outlineColor.copy(alpha = 0.85f),
        topLeft = Offset(x, y),
        size = Size(size, size),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = 1.5.dp.toPx())
    )
}
