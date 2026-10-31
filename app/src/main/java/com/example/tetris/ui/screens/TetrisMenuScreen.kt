package com.example.tetris.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.tetris.model.BoardTheme
import com.example.tetris.ui.TetrisUiState

@Composable
fun TetrisMenuScreen(
    uiState: TetrisUiState,
    onStartGame: (Int) -> Unit,
    onStartingLevelChanged: (Int) -> Unit,
    onThemeSelected: (BoardTheme) -> Unit,
    onToggleSound: () -> Unit,
    onToggleBgm: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleSwipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = uiState.boardTheme
    var showInstructionsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.boardBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- ARCADE TITLE BANNER ---
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val titleColors = listOf(
                Color(0xFFEF4444), // T
                Color(0xFFF59E0B), // E
                Color(0xFF10B981), // T
                Color(0xFF06B6D4), // R
                Color(0xFF3B82F6), // I
                Color(0xFFA855F7)  // S
            )
            "TETRIS".forEachIndexed { index, char ->
                Text(
                    text = char.toString(),
                    color = titleColors[index % titleColors.size],
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
            }
        }

        Text(
            text = "RETRO ARCADE EDITION",
            color = theme.gridBorder.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 4.sp
        )

        // --- HIGH SCORE BADGE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HIGH SCORE",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${uiState.highScore}",
                            color = Color(0xFFFFD700),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "GAMES: ${uiState.totalGamesPlayed}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "LINES: ${uiState.totalLinesCleared}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- START GAME BUTTON ---
        Button(
            onClick = { onStartGame(uiState.startingLevel) },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("start_game_button"),
            colors = ButtonDefaults.buttonColors(containerColor = theme.getColor(4)),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.start_game),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // --- STARTING LEVEL SELECTOR ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.gridBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STARTING LEVEL",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "LVL ${uiState.startingLevel}",
                        color = theme.getColor(2),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = uiState.startingLevel.toFloat(),
                    onValueChange = { onStartingLevelChanged(it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = theme.getColor(2),
                        activeTrackColor = theme.getColor(2),
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("starting_level_slider")
                )
            }
        }

        // --- THEME SELECTOR ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.gridBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "VISUAL THEME",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                val chunkedThemes = BoardTheme.entries.chunked(3)
                chunkedThemes.forEach { rowThemes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowThemes.forEach { bTheme ->
                            val isSelected = bTheme == theme
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) bTheme.gridBorder.copy(alpha = 0.3f)
                                        else Color.Black.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) bTheme.gridBorder else Color.DarkGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onThemeSelected(bTheme) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = bTheme.displayName,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        // Fill extra slot in uneven row
                        if (rowThemes.size < 3) {
                            Spacer(modifier = Modifier.weight((3 - rowThemes.size).toFloat()))
                        }
                    }
                }
            }
        }

        // --- QUICK SETTINGS (SOUND, BGM, HAPTICS, GESTURES) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.gridBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SETTINGS",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Sound FX
                SettingToggleRow(
                    icon = if (uiState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    title = stringResource(R.string.sound_effects),
                    checked = uiState.isSoundEnabled,
                    onCheckedChange = { onToggleSound() },
                    tint = theme.getColor(2)
                )

                // Background Music
                SettingToggleRow(
                    icon = if (uiState.isBgmEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    title = "8-Bit Theme Melody",
                    checked = uiState.isBgmEnabled,
                    onCheckedChange = { onToggleBgm() },
                    tint = theme.getColor(1)
                )

                // Haptics
                SettingToggleRow(
                    icon = Icons.Default.Vibration,
                    title = stringResource(R.string.haptics),
                    checked = uiState.isHapticsEnabled,
                    onCheckedChange = { onToggleHaptics() },
                    tint = theme.getColor(3)
                )

                // Swipe Gestures
                SettingToggleRow(
                    icon = Icons.Default.TouchApp,
                    title = stringResource(R.string.swipe_mode),
                    checked = uiState.isSwipeEnabled,
                    onCheckedChange = { onToggleSwipe() },
                    tint = theme.getColor(4)
                )
            }
        }

        // --- HOW TO PLAY CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.gridBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "HOW TO PLAY",
                    color = theme.gridBorder,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "• D-PAD / SWIPE: Move piece left & right\n" +
                            "• ROTATE / TAP: Rotate piece clockwise\n" +
                            "• DOWN: Soft drop for faster descent (+1 pt)\n" +
                            "• DROP / DOUBLE TAP: Instant hard drop (+2 pts)\n" +
                            "• HOLD: Save current piece for later\n" +
                            "• Clear full rows to score points & level up!",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = tint,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}
