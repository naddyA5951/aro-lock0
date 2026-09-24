package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GpsPoint
import com.example.ui.theme.AmberGold
import com.example.ui.theme.NightCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TextSecondaryDark
import com.example.util.UnitFormatter
import kotlin.math.max
import kotlin.math.min

@Composable
fun ElevationChart(
    points: List<GpsPoint>,
    useMetric: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NightCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Elevation profile will appear as you trek",
                color = TextSecondaryDark,
                fontSize = 13.sp
            )
        }
        return
    }

    var minAlt = Double.MAX_VALUE
    var maxAlt = Double.MIN_VALUE
    val altList = mutableListOf<Double>()

    for (p in points) {
        if (p.altitude > 0.0) {
            altList.add(p.altitude)
            minAlt = min(minAlt, p.altitude)
            maxAlt = max(maxAlt, p.altitude)
        }
    }

    if (altList.size < 2 || maxAlt <= minAlt) {
        // Flat or single elevation
        minAlt = 0.0
        maxAlt = if (altList.isNotEmpty()) altList.first() + 50.0 else 100.0
    }

    val altRange = max(10.0, maxAlt - minAlt)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NightCard)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ELEVATION PROFILE",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Min: ${UnitFormatter.formatElevationAltitude(minAlt, useMetric)}",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
                Text(
                    text = "Max: ${UnitFormatter.formatElevationAltitude(maxAlt, useMetric)}",
                    color = AmberGold,
                    fontSize = 11.sp
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .padding(top = 10.dp)
        ) {
            val width = size.width
            val height = size.height

            val linePath = Path()
            val fillPath = Path()

            val stepX = width / (altList.size - 1).coerceAtLeast(1)

            altList.forEachIndexed { index, alt ->
                val normY = ((alt - minAlt) / altRange).toFloat().coerceIn(0f, 1f)
                val x = index * stepX
                val y = height - (normY * (height - 15f)) - 5f

                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw gradient area under the curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SageGreen.copy(alpha = 0.35f),
                        SageGreen.copy(alpha = 0.05f)
                    )
                )
            )

            // Draw profile curve
            drawPath(
                path = linePath,
                color = SageGreen,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Draw baseline
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 1f
            )
        }
    }
}
