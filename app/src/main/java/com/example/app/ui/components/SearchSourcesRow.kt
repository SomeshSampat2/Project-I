package com.example.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.R
import com.example.app.data.model.SearchSource
import com.example.app.ui.theme.LocalAppColors
import com.example.app.ui.theme.OpenSansFont

@Composable
fun SearchSourcesRow(
    sources: List<SearchSource>,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = sources.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
        ) {
            items(sources) { source ->
                SearchSourceCard(source)
            }
        }
    }
}

@Composable
private fun SearchSourceCard(source: SearchSource) {
    val uriHandler = LocalUriHandler.current
    val colors = LocalAppColors.current
    
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { uriHandler.openUri(source.url) },
        colors = CardDefaults.cardColors(
            containerColor = colors.primary.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 8.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.surface,
                            colors.surface.copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Source badge and title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Title
                    Text(
                        text = source.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OpenSansFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Source badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.primary.copy(alpha = if (colors.isDark) 0.2f else 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_link),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = colors.primary
                            )
                            Text(
                                text = "Source",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = OpenSansFont,
                                    letterSpacing = 0.3.sp,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Preview text
                Text(
                    text = source.content,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = OpenSansFont,
                        lineHeight = 18.sp,
                        fontSize = 13.sp
                    ),
                    color = colors.textPrimary.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
} 