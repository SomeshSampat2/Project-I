package com.example.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.R
import com.example.app.ui.theme.OpenSansFont
import com.example.app.ui.theme.Primary
import kotlinx.coroutines.delay

@Composable
fun CodeBlock(
    code: AnnotatedString,
    language: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCopiedToast by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column {
            language?.let {
                Surface(
                    color = Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language label with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_code),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Primary
                            )
                            Text(
                                text = language.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = OpenSansFont,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 16.sp
                                ),
                                color = Primary
                            )
                        }
                        
                        // Copy button
                        IconButton(
                            onClick = {
                                copyToClipboard(context, code.text)
                                showCopiedToast = true
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Primary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_copy),
                                contentDescription = "Copy code",
                                modifier = Modifier.size(16.dp),
                                tint = Primary
                            )
                        }
                    }
                }
            }
            
            // Code content
            Surface(
                color = Color(0xFF2B2B2B),
                shape = RoundedCornerShape(
                    topStart = if (language == null) 12.dp else 0.dp,
                    topEnd = if (language == null) 12.dp else 0.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp
                )
            ) {
                SelectionContainer {
                    Text(
                        text = code,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
    
    // Show copied toast
    if (showCopiedToast) {
        LaunchedEffect(Unit) {
            delay(2000)
            showCopiedToast = false
        }
        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Code", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
} 