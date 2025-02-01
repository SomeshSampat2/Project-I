package com.example.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GlobeIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(
        modifier = modifier.size(24.dp)
    ) {
        val width = size.width
        val height = size.height
        val radius = width / 2

        // Draw the main circle (globe)
        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = width * 0.08f)
        )

        // Draw horizontal lines (parallels)
        for (i in 1..2) {
            val y = height * (i / 3f)
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = width * 0.08f,
                cap = StrokeCap.Round
            )
        }

        // Draw vertical curve (meridian)
        val path = Path().apply {
            moveTo(width / 2, 0f)
            cubicTo(
                width * 0.8f, height * 0.25f,
                width * 0.8f, height * 0.75f,
                width / 2, height
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = width * 0.08f, cap = StrokeCap.Round)
        )

        // Draw second vertical curve
        val path2 = Path().apply {
            moveTo(width / 2, 0f)
            cubicTo(
                width * 0.2f, height * 0.25f,
                width * 0.2f, height * 0.75f,
                width / 2, height
            )
        }
        drawPath(
            path = path2,
            color = color,
            style = Stroke(width = width * 0.08f, cap = StrokeCap.Round)
        )
    }
} 