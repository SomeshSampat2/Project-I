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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeBlock(
    code: AnnotatedString,
    language: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2B2B2B))
    ) {
        Column {
            language?.let {
                Text(
                    text = it,
                    color = Color(0xFF808080),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(start = 12.dp, top = 8.dp, end = 12.dp)
                )
            }
            SelectionContainer {
                Text(
                    text = code,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Code", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
} 