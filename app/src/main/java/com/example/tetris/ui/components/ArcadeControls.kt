package com.example.tetris.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetris.model.BoardTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ArcadeControls(
    theme: BoardTheme,
    onStartMoveLeft: () -> Unit,
    onStopMoveLeft: () -> Unit,
    onStartMoveRight: () -> Unit,
    onStopMoveRight: () -> Unit,
    onRotateCw: () -> Unit,
    onRotateCcw: () -> Unit,
    onSoftDrop: (Boolean) -> Unit,
    onHardDrop: () -> Unit,
    onHold: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- LEFT CLUSTER: D-PAD (Left, Right, Soft Drop with DAS support) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Button with press & hold (DAS)
                DasArcadeButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    label = "LEFT",
                    testTag = "btn_move_left",
                    color = theme.gridBorder,
                    onPressDown = onStartMoveLeft,
                    onPressUp = onStopMoveLeft
                )

                // Right Button with press & hold (DAS)
                DasArcadeButton(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    label = "RIGHT",
                    testTag = "btn_move_right",
                    color = theme.gridBorder,
                    onPressDown = onStartMoveRight,
                    onPressUp = onStopMoveRight
                )
            }

            // Down (Soft drop) button
            var isDownPressed by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(width = 118.dp, height = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isDownPressed) theme.gridBorder.copy(alpha = 0.45f)
                        else theme.boardBackground
                    )
                    .border(
                        width = 2.dp,
                        color = theme.gridBorder.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .pointerInteropFilter { event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isDownPressed = true
                                onSoftDrop(true)
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                isDownPressed = false
                                onSoftDrop(false)
                                true
                            }
                            else -> false
                        }
                    }
                    .testTag("btn_soft_drop"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Soft Drop",
                        tint = theme.gridBorder,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DOWN",
                        color = theme.gridBorder,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- CENTER: HOLD BUTTON ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TapArcadeButton(
                icon = Icons.Default.ChangeCircle,
                label = "HOLD",
                testTag = "btn_hold",
                color = Color(0xFFF59E0B),
                sizeDp = 50,
                onClick = onHold
            )
        }

        // --- RIGHT CLUSTER: ROTATE & HARD DROP ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rotate CCW
                TapArcadeButton(
                    icon = Icons.AutoMirrored.Filled.RotateLeft,
                    label = "CCW",
                    testTag = "btn_rotate_ccw",
                    color = theme.getColor(6), // Purple
                    onClick = onRotateCcw
                )

                // Rotate CW (Primary Action)
                TapArcadeButton(
                    icon = Icons.AutoMirrored.Filled.RotateRight,
                    label = "ROT",
                    testTag = "btn_rotate_cw",
                    color = theme.getColor(1), // Cyan
                    isPrimary = true,
                    onClick = onRotateCw
                )
            }

            // Hard Drop Button
            Box(
                modifier = Modifier
                    .size(width = 118.dp, height = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(theme.getColor(7).copy(alpha = 0.25f))
                    .border(
                        width = 2.dp,
                        color = theme.getColor(7),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = theme.getColor(7)),
                        onClick = onHardDrop
                    )
                    .testTag("btn_hard_drop"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Hard Drop",
                        tint = theme.getColor(7),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DROP",
                        color = theme.getColor(7),
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DasArcadeButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    color: Color,
    onPressDown: () -> Unit,
    onPressUp: () -> Unit,
    sizeDp: Int = 54
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .scale(if (isPressed) 0.92f else 1.0f)
            .clip(CircleShape)
            .background(if (isPressed) color.copy(alpha = 0.40f) else color.copy(alpha = 0.16f))
            .border(width = 2.dp, color = color, shape = CircleShape)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onPressDown()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onPressUp()
                        true
                    }
                    else -> false
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun TapArcadeButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Int = 54,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .scale(if (isPressed) 0.92f else 1.0f)
            .clip(CircleShape)
            .background(
                if (isPrimary) color.copy(alpha = 0.35f)
                else color.copy(alpha = 0.18f)
            )
            .border(
                width = if (isPrimary) 2.5.dp else 1.8.dp,
                color = color,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = (sizeDp / 2).dp, color = color),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}
