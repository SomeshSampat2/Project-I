package com.innovatelabs3.projectI2.ui.navigation


import com.innovatelabs3.projectI2.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconResId: Int
) {
    object Chat : BottomNavItem(
        route = "chat",
        title = "Chat",
        iconResId = R.drawable.ic_chat
    )
    
    object PhotoEditor : BottomNavItem(
        route = "photoEditor",
        title = "PhotoEditor",
        iconResId = R.drawable.ic_photo_editor
    )
} 