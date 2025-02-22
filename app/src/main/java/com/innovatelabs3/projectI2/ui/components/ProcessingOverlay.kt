package com.innovatelabs3.projectI2.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProcessingOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        // Processing text with shimmer effect
        val shimmerColors = listOf(
            Color.White.copy(alpha = 0.2f),
            Color.White.copy(alpha = 0.8f),
            Color.White.copy(alpha = 0.2f)
        )

        val transition = rememberInfiniteTransition()
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing)
            )
        )

        Text(
            text = "Processing Image",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = shimmerColors,
                        startX = translateAnim,
                        endX = translateAnim + 200f
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}