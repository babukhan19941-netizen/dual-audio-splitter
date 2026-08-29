package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioMode
import com.example.ui.theme.CallEmerald
import com.example.ui.theme.MediaCyan
import com.example.ui.theme.PolishBlue
import com.example.ui.theme.PolishBlueContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun AudioChannelVisualizerSection(
    leftAmp: Float,
    rightAmp: Float,
    audioMode: AudioMode,
    isMediaActive: Boolean,
    isCallActive: Boolean,
    modifier: Modifier = Modifier
) {
    val isSplit = audioMode == AudioMode.SPLIT_CHANNELS
    val rightColor = if (isSplit) CallEmerald else MediaCyan

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Stereo VU",
                            tint = PolishBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live Channel VU Meter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Independent Left / Right Output",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MediaCyan.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text("L: MEDIA", fontSize = 10.sp, color = MediaCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(rightColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (isSplit) "R: CALL" else "R: MEDIA",
                            fontSize = 10.sp,
                            color = rightColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Visualizer Bars inside dark slate enclosure
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Slate900)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Channel Visualizer
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LEFT EAR (MEDIA)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MediaCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LiveChannelWaveformCanvas(
                        amplitude = if (isMediaActive) leftAmp.coerceAtLeast(0.12f) else 0.02f,
                        color = MediaCyan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    )
                }

                // Center Earphone Divider Icon
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Slate800),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Headphones",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Right Channel Visualizer
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSplit) "RIGHT EAR (CALL)" else "RIGHT EAR (MEDIA)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = rightColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LiveChannelWaveformCanvas(
                        amplitude = if (isSplit) {
                            if (isCallActive) rightAmp.coerceAtLeast(0.12f) else 0.02f
                        } else {
                            if (isMediaActive) rightAmp.coerceAtLeast(0.12f) else 0.02f
                        },
                        color = rightColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveChannelWaveformCanvas(
    amplitude: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val barCount = 12
        val totalWidth = size.width
        val barWidth = (totalWidth / (barCount * 1.5f)).coerceAtLeast(3f)
        val spacing = (totalWidth - (barCount * barWidth)) / (barCount - 1)
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val normalizedIdx = i.toFloat() / barCount
            val waveMod = kotlin.math.sin(phase + normalizedIdx * 4f) * 0.35f + 0.65f
            val dynamicHeight = (maxHeight * amplitude * waveMod.toFloat()).coerceIn(4f, maxHeight)

            val x = i * (barWidth + spacing)
            val y = (maxHeight - dynamicHeight) / 2f

            drawRoundRect(
                color = color.copy(alpha = (0.4f + amplitude * 0.6f).coerceIn(0.2f, 1f)),
                topLeft = Offset(x, y),
                size = Size(barWidth, dynamicHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

