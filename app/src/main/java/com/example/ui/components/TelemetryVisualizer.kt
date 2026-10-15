package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurboViolet

@Composable
fun FpsStabilityCanvas(
    history: List<Int>,
    targetFps: Int,
    currentFps: Int,
    stabilityScore: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberCardSurface, RoundedCornerShape(16.dp))
            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REAL-TIME FPS STABILITY",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentFps",
                            color = PerformanceGreen,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " / ${targetFps} TARGET",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(PerformanceGreen.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .border(1.dp, PerformanceGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$stabilityScore% STEADY",
                        color = PerformanceGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Waveform Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                val width = size.width
                val height = size.height

                // Draw target guideline
                val targetRatio = 1f - (targetFps / 140f).coerceIn(0.1f, 0.95f)
                val targetY = height * targetRatio
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(0f, targetY),
                    end = Offset(width, targetY),
                    strokeWidth = 1.5.dp.toPx()
                )

                if (history.size >= 2) {
                    val stepX = width / (history.size - 1)
                    val path = Path()
                    val fillPath = Path()

                    history.forEachIndexed { index, fps ->
                        val ratio = 1f - (fps.toFloat() / 140f).coerceIn(0.1f, 0.95f)
                        val x = index * stepX
                        val y = height * ratio

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }
                    fillPath.lineTo(width, height)
                    fillPath.close()

                    // Draw area gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PerformanceGreen.copy(alpha = 0.35f),
                                PerformanceGreen.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Draw stroke line
                    drawPath(
                        path = path,
                        color = PerformanceGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
fun PingWaveformCanvas(
    pingHistory: List<Int>,
    currentPing: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberCardSurface, RoundedCornerShape(16.dp))
            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NETWORK LATENCY TRACE",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentPing",
                            color = NeonCyan,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " ms (Ultra Low Jitter)",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DNS OPTIMIZED",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            ) {
                val width = size.width
                val height = size.height

                if (pingHistory.size >= 2) {
                    val stepX = width / (pingHistory.size - 1)
                    val path = Path()
                    val fillPath = Path()

                    pingHistory.forEachIndexed { index, ping ->
                        val ratio = 1f - ((ping - 10f) / 50f).coerceIn(0.1f, 0.9f)
                        val x = index * stepX
                        val y = height * ratio

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }
                    fillPath.lineTo(width, height)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.3f),
                                NeonCyan.copy(alpha = 0.02f)
                            )
                        )
                    )

                    drawPath(
                        path = path,
                        color = NeonCyan,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
