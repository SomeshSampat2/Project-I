package com.innovatelabs3.projectI2.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.innovatelabs3.projectI2.ui.theme.OpenSansFont
import com.innovatelabs3.projectI2.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import androidx.compose.animation.ExperimentalAnimationApi

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShimmerEffect(isWebSearch: Boolean = false) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition()
    
    // Single shimmer animation for consistency
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val dotsAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val brush = remember(translateAnim.value) {
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim.value, y = 0f)
        )
    }

    // Loading message animation
    val loadingMessages = remember {
        if (isWebSearch) {
            listOf(
                "Searching the web",
                "Gathering information",
                "Processing results",
                "Almost ready"
            )
        } else {
            listOf(
                "Thinking",
                "Analyzing",
                "Processing",
                "Almost ready"
            )
        }
    }
    
    var currentMessageIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            // Only increment if not at last message
            if (currentMessageIndex < loadingMessages.size - 1) {
                currentMessageIndex++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Animated loading text with shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = loadingMessages[currentMessageIndex],
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                    fadeOut(animationSpec = tween(300))
                }
            ) { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = OpenSansFont,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    ),
                    color = TextSecondary
                )
            }
            
            Text(
                text = ".".repeat(dotsAnim.value.toInt() + 1),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OpenSansFont,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp
                ),
                modifier = Modifier
                    .width(40.dp)
                    .padding(horizontal = 4.dp),
                color = TextSecondary
            )
        }

        // First paragraph
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

        // Second paragraph
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
                            .background(
                                Brush.linearGradient(
                                    colors = shimmerColors,
                                    start = Offset.Zero,
                                    end = Offset(x = translateAnim.value, y = 0f)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Bullet point text
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = shimmerColors,
                                    start = Offset.Zero,
                                    end = Offset(x = translateAnim.value, y = 0f)
                                )
                            )
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
                        .background(
                            Brush.linearGradient(
                                colors = shimmerColors,
                                start = Offset.Zero,
                                end = Offset(x = translateAnim.value, y = 0f)
                            )
                        )
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
} 