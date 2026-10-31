package com.example.tetris.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetris.model.BoardTheme
import com.example.tetris.model.Tetromino

@Composable
fun PiecePreviewBox(
    title: String,
    tetromino: Tetromino?,
    theme: BoardTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("preview_box_$title")
            .background(theme.boardBackground, shape = RoundedCornerShape(8.dp))
            .border(1.5.dp, theme.gridBorder.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = theme.gridBorder,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        MiniPieceCanvas(
            tetromino = tetromino,
            theme = theme,
            sizeDp = 50
        )
    }
}

@Composable
fun NextQueueBox(
    nextQueue: List<Tetromino>,
    theme: BoardTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("next_queue_box")
            .background(theme.boardBackground, shape = RoundedCornerShape(8.dp))
            .border(1.5.dp, theme.gridBorder.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "NEXT",
            color = theme.gridBorder,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        // Primary next piece
        val primary = nextQueue.firstOrNull()
        MiniPieceCanvas(
            tetromino = primary,
            theme = theme,
            sizeDp = 50
        )

        // Secondary upcoming piece
        if (nextQueue.size > 1) {
            MiniPieceCanvas(
                tetromino = nextQueue[1],
                theme = theme,
                sizeDp = 34
            )
        }
    }
}

@Composable
private fun MiniPieceCanvas(
    tetromino: Tetromino?,
    theme: BoardTheme,
    sizeDp: Int
) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boxWidth = size.width
            val cellSize = boxWidth / 4f

            if (tetromino != null) {
                val blocks = tetromino.currentBlocks()
                val color = theme.getColor(tetromino.colorIndex)

                for (i in 0 until 4) {
                    for (j in 0 until 4) {
                        if (i * 4 + j in blocks) {
                            val x = j * cellSize + 1f
                            val y = i * cellSize + 1f
                            val bSize = cellSize - 2f

                            drawRoundRect(
                                color = color,
                                topLeft = Offset(x, y),
                                size = Size(bSize, bSize),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            // Bevel highlight
                            drawRect(
                                color = Color.White.copy(alpha = 0.40f),
                                topLeft = Offset(x, y),
                                size = Size(bSize, bSize * 0.22f)
                            )
                        }
                    }
                }
            }
        }
    }
}
