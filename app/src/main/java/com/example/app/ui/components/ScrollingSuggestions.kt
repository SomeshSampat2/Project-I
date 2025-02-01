package com.example.app.ui.components

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.Primary
import com.example.app.util.SamplePrompts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.ScrollableDefaults
import kotlinx.coroutines.coroutineScope

@Composable
fun ScrollingSuggestions(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Primary,                    // Blue
        Color(0xFFE11D48),         // Rose
        Color(0xFF9333EA),         // Purple
        Color(0xFF06B6D4),         // Cyan
        Color(0xFFF59E0B),         // Amber
        Color(0xFF10B981),         // Emerald
        Color(0xFFEF4444),         // Red
        Color(0xFFF97316),         // Orange
        Color(0xFF84CC16),         // Lime
        Color(0xFF14B8A6),         // Teal
        Color(0xFF6366F1),         // Indigo
        Color(0xFFD946EF),         // Fuchsia
        Color(0xFFF43F5E),         // Light Red
        Color(0xFF8B5CF6),         // Violet
        Color(0xFF0EA5E9),         // Light Blue
        Color(0xFF22C55E),         // Green
        Color(0xFFEC4899),         // Pink
        Color(0xFFCA8A04),         // Yellow
        Color(0xFF0891B2),         // Dark Cyan
        Color(0xFF7C3AED),         // Purple
        Color(0xFFDC2626),         // Dark Red
        Color(0xFF059669),         // Dark Emerald
        Color(0xFF6D28D9),         // Dark Purple
        Color(0xFFEA580C)          // Dark Orange
    ).shuffled()

    val firstRowState = rememberLazyListState()
    val secondRowState = rememberLazyListState()
    
    // Reduce list size for better performance
    val firstRowItems = remember { 
        List(40) { SamplePrompts.allSuggestions.take(8)[it % 8] }
    }
    val secondRowItems = remember { 
        List(40) { SamplePrompts.allSuggestions.drop(8)[it % 8] }
    }
    
    // Create fixed color map
    val suggestionColors = remember {
        val shuffledColors = colors.shuffled()
        SamplePrompts.allSuggestions.mapIndexed { index, suggestion ->
            suggestion to shuffledColors[index % shuffledColors.size]
        }.toMap()
    }
    
    // Start second row from the end
    LaunchedEffect(Unit) {
        secondRowState.scrollToItem(secondRowItems.size - 8)
    }
    
    // Optimize scroll position reset
    LaunchedEffect(firstRowState.firstVisibleItemIndex) {
        if (firstRowState.firstVisibleItemIndex > firstRowItems.size - 10) {
            firstRowState.animateScrollToItem(5)
        }
    }
    
    LaunchedEffect(secondRowState.firstVisibleItemIndex) {
        if (secondRowState.firstVisibleItemIndex < 5) {
            secondRowState.animateScrollToItem(secondRowItems.size - 10)
        }
    }
    
    // Optimize auto-scrolling animation
    LaunchedEffect(Unit) {
        while (true) {
            coroutineScope {
                if (!firstRowState.isScrollInProgress && !secondRowState.isScrollInProgress) {
                    launch { firstRowState.scrollBy(1.5f) }
                    launch { secondRowState.scrollBy(-1.5f) }
                }
            }
            delay(16)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row
        LazyRow(
            state = firstRowState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = true,
            flingBehavior = ScrollableDefaults.flingBehavior()
        ) {
            items(
                items = firstRowItems,
                key = { it.hashCode() }
            ) { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    color = suggestionColors[suggestion] ?: Primary,
                    onClick = { onSuggestionClick(suggestion.prompt) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }

        // Second row
        LazyRow(
            state = secondRowState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = true,
            flingBehavior = ScrollableDefaults.flingBehavior()
        ) {
            items(
                items = secondRowItems,
                key = { it.hashCode() }
            ) { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    color = suggestionColors[suggestion] ?: Primary,
                    onClick = { onSuggestionClick(suggestion.prompt) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
} 