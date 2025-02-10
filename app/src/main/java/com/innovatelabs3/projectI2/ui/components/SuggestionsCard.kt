package com.innovatelabs3.projectI2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.innovatelabs3.projectI2.ui.theme.OpenSansFont
import com.innovatelabs3.projectI2.ui.theme.Primary
import androidx.compose.ui.graphics.Color

@Composable
fun SuggestionsCard(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Text with styled Project I
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome to ",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = OpenSansFont,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.3.sp
                ),
                color = Primary
            )
            Text(
                text = "Project I",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = OpenSansFont,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                ),
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try these examples to get started",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = OpenSansFont,
                color = Color.Black
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Scrolling suggestions
        ScrollingSuggestions(
            onSuggestionClick = onSuggestionClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}