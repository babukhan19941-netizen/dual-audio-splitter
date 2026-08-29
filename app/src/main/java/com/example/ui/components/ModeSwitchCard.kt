package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioMode
import com.example.ui.theme.CallEmerald
import com.example.ui.theme.CallEmeraldGlow
import com.example.ui.theme.MediaCyan
import com.example.ui.theme.MediaCyanGlow
import com.example.ui.theme.PolishBlue
import com.example.ui.theme.PolishBlueDark
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun ModeSwitchSection(
    currentMode: AudioMode,
    onToggleMode: () -> Unit,
    onSelectMode: (AudioMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSplit = currentMode == AudioMode.SPLIT_CHANNELS

    val rightAccentColor by animateColorAsState(
        targetValue = if (isSplit) CallEmerald else MediaCyan,
        animationSpec = tween(350),
        label = "rightAccent"
    )

    // Hero Dark Card (Slate 900 with deep shadow)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = PolishBlueDark),
        shape = RoundedCornerShape(32.dp),
        color = Slate900,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row with Monospace Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isSplit) "ACTIVE ROUTING • SPLIT" else "ACTIVE ROUTING • UNIFIED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Text(
                    text = "v9.3.1-STABLE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.40f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dual Channel Capsule Visualizers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Capsule (Media Audio)
                ChannelCapsuleView(
                    channelTitle = "LEFT CH.",
                    channelType = "MEDIA",
                    icon = Icons.Default.MusicNote,
                    accentColor = MediaCyan,
                    accentGlow = MediaCyanGlow,
                    modifier = Modifier.weight(1f)
                )

                // Vertical Gradient Divider
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .width(1.dp)
                        .height(90.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Right Capsule (Call Phone Audio or Unified Media)
                ChannelCapsuleView(
                    channelTitle = "RIGHT CH.",
                    channelType = if (isSplit) "CALLS" else "MEDIA",
                    icon = if (isSplit) Icons.Default.Call else Icons.Default.MusicNote,
                    accentColor = rightAccentColor,
                    accentGlow = if (isSplit) CallEmeraldGlow else MediaCyanGlow,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // PRIMARY 1-CLICK ACTION BUTTON (Professional Polish Blue Rounded Pill)
            Button(
                onClick = onToggleMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("one_click_mode_switch_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PolishBlue,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Mode",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSplit) {
                            "MERGE TO MEDIA AUDIO"
                        } else {
                            "SPLIT (L: MEDIA / R: CALL)"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Selector Pills in slate theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeSelectorPill(
                    label = "Split (L: Media / R: Call)",
                    isSelected = currentMode == AudioMode.SPLIT_CHANNELS,
                    onClick = { onSelectMode(AudioMode.SPLIT_CHANNELS) },
                    modifier = Modifier.weight(1f)
                )
                ModeSelectorPill(
                    label = "Unified (Dual Media)",
                    isSelected = currentMode == AudioMode.UNIFIED_MEDIA,
                    onClick = { onSelectMode(AudioMode.UNIFIED_MEDIA) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChannelCapsuleView(
    channelTitle: String,
    channelType: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    accentGlow: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Capsule Enclosure
        Box(
            modifier = Modifier
                .width(108.dp)
                .height(148.dp)
                .clip(RoundedCornerShape(54.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(54.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Bottom Gradient Glowing Pool
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                accentColor.copy(alpha = 0.22f)
                            )
                        )
                    )
            )

            // Inner Capsule Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = channelType,
                        tint = accentGlow,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = channelTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = channelType,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = accentGlow
                )
            }
        }
    }
}

@Composable
private fun ModeSelectorPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) PolishBlue.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f),
        border = BorderStroke(
            1.dp,
            if (isSelected) PolishBlue else Color.White.copy(alpha = 0.10f)
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 9.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
        }
    }
}

