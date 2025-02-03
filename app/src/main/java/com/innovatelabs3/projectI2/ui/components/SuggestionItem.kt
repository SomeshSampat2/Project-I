package com.innovatelabs3.projectI2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.innovatelabs3.projectI2.R
import com.innovatelabs3.projectI2.ui.theme.OpenSansFont
import com.innovatelabs3.projectI2.util.Suggestion

@Composable
fun SuggestionItem(
    suggestion: Suggestion,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = when (suggestion.title) {
                            "Get Creative" -> R.drawable.ic_creative
                            "Code Help" -> R.drawable.ic_code
                            "Tech Trends" -> R.drawable.ic_trending
                            "AI & ML" -> R.drawable.ic_brain
                            "Mobile Dev" -> R.drawable.ic_code
                            "Problem Solving" -> R.drawable.ic_brain
                            "Learning" -> R.drawable.ic_code
                            "UI/UX" -> R.drawable.ic_creative
                            "Tech Career" -> R.drawable.ic_trending
                            "System Design" -> R.drawable.ic_code
                            "Best Practices" -> R.drawable.ic_brain
                            "Testing" -> R.drawable.ic_code
                            else -> R.drawable.ic_creative
                        }
                    ),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = OpenSansFont,
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = suggestion.prompt,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = OpenSansFont,
                    lineHeight = 18.sp
                ),
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
} 