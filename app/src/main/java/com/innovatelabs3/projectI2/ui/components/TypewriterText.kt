package com.innovatelabs3.projectI2.ui.components

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
    shouldAnimate: Boolean = true,
    wordsPerFrame: Int = 3,  // Added parameter for words per frame
    onAnimationComplete: () -> Unit = {}
) {
    var currentText by remember { mutableStateOf(AnnotatedString("")) }
    var currentIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(text, shouldAnimate) {
        if (shouldAnimate) {
            currentText = AnnotatedString("")
            currentIndex = 0
            
            while (currentIndex < text.length) {
                delay(15) // Typing speed
                
                // Calculate next position based on words per frame
                var nextPosition = currentIndex
                repeat(wordsPerFrame) {
                    val next = text.indexOf(' ', nextPosition + 1)
                    if (next == -1 || next - currentIndex > 8) {  // Increased max chars per frame
                        nextPosition += minOf(8, text.length - nextPosition)
                        return@repeat
                    }
                    nextPosition = next
                }
                
                // Ensure we move at least one character forward
                if (nextPosition <= currentIndex) {
                    nextPosition = minOf(currentIndex + 1, text.length)
                }
                
                val builder = AnnotatedString.Builder()
                builder.append(text.subSequence(0, nextPosition))
                
                // Copy all span styles from original text up to current position
                text.spanStyles.forEach { span ->
                    if (span.start <= nextPosition) {
                        val end = minOf(span.end, nextPosition)
                        builder.addStyle(
                            span.item,
                            span.start,
                            end
                        )
                    }
                }
                
                // Copy all paragraph styles
                text.paragraphStyles.forEach { para ->
                    if (para.start <= nextPosition) {
                        val end = minOf(para.end, nextPosition)
                        builder.addStyle(
                            para.item,
                            para.start,
                            end
                        )
                    }
                }
                
                currentText = builder.toAnnotatedString()
                currentIndex = nextPosition
            }
            onAnimationComplete()
        } else {
            currentText = text
            currentIndex = text.length
            onAnimationComplete()
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = currentText,
            modifier = modifier,
            style = style,
            softWrap = true
        )
        if (currentIndex < text.length) {
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