package com.example.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerEffect() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = 0f)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // First paragraph - 3 lines
        Column(modifier = Modifier.fillMaxWidth(0.9f)) {
            repeat(3) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(if (it == 2) 0.7f else 1f)
                        .height(12.dp)
                        .padding(vertical = 1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Second paragraph - 2 lines
        Column(modifier = Modifier.fillMaxWidth(0.85f)) {
            repeat(2) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(if (it == 1) 0.4f else 1f)
                        .height(12.dp)
                        .padding(vertical = 1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bullet points - smaller and more compact
        Column(modifier = Modifier.fillMaxWidth(0.95f)) {
            repeat(3) {
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bullet point
                    Spacer(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Bullet point text
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Code block simulation - more compact
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.1f))
                .padding(6.dp)
        ) {
            repeat(4) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(if (it == 3) 0.5f else 0.9f)
                        .height(10.dp)
                        .padding(vertical = 1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
} 