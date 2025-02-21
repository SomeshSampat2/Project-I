package com.innovatelabs3.projectI2.data.model

import android.net.Uri

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: Uri? = null
) 