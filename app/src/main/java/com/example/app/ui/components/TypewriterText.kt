package com.example.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    typingDelay: Long = 15L,
    wordsPerFrame: Int = 2,
    shouldAnimate: Boolean = true,
    onAnimationComplete: () -> Unit = {}
) {
    var textToDisplay by remember(text) { mutableStateOf(if (shouldAnimate) "" else text.toString()) }
    var currentPosition by remember(text) { mutableStateOf(0) }
    var isAnimating by remember(text) { mutableStateOf(shouldAnimate) }

    LaunchedEffect(text) {
        if (!shouldAnimate) {
            textToDisplay = text.toString()
            return@LaunchedEffect
        }
        
        isAnimating = true
        currentPosition = 0
        textToDisplay = ""
        
        while (currentPosition < text.length && isAnimating) {
            delay(typingDelay)
            var nextPosition = currentPosition
            
            repeat(wordsPerFrame) {
                val next = text.indexOf(' ', nextPosition + 1)
                if (next == -1 || next - currentPosition > 20) {
                    nextPosition += minOf(20, text.length - nextPosition)
                    return@repeat
                }
                nextPosition = next
            }
            
            if (nextPosition > currentPosition) {
                currentPosition = nextPosition
                textToDisplay = text.substring(0, currentPosition)
            } else {
                currentPosition++
                textToDisplay = text.substring(0, currentPosition)
            }
        }
        
        if (currentPosition >= text.length) {
            textToDisplay = text.toString()
            onAnimationComplete()
        }
    }

    DisposableEffect(text) {
        onDispose {
            isAnimating = false
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = textToDisplay,
            modifier = modifier,
            style = style,
            overflow = TextOverflow.Visible
        )
        if (textToDisplay.length < text.length) {
            BlinkingCursor()
        }
    }
}

@Composable
private fun BlinkingCursor() {
    val cursorColor = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )

    Text(
        text = "▌",
        color = cursorColor.copy(alpha = alpha),
        style = MaterialTheme.typography.bodyLarge
    )
} 