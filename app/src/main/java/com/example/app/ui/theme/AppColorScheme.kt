package com.example.app.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
data class AppColorScheme(
    val primary: Color,
    val primaryDark: Color,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val userMessageBg: Color,
    val botMessageBg: Color,
    val inputBg: Color,
    val divider: Color,
    val error: Color,
    val success: Color,
    val warning: Color,
    val isDark: Boolean
) {
    companion object {
        val Light = AppColorScheme(
            primary = Primary,
            primaryDark = PrimaryDark,
            background = Background,
            surface = Surface,
            textPrimary = TextPrimary,
            textSecondary = TextSecondary,
            userMessageBg = UserMessageBg,
            botMessageBg = BotMessageBg,
            inputBg = InputBg,
            divider = Divider,
            error = Error,
            success = Success,
            warning = Warning,
            isDark = false
        )

        val Dark = AppColorScheme(
            primary = PrimaryDark_Dark,
            primaryDark = PrimaryDark_Dark,
            background = Background_Dark,
            surface = Surface_Dark,
            textPrimary = TextPrimary_Dark,
            textSecondary = TextSecondary_Dark,
            userMessageBg = UserMessageBg_Dark,
            botMessageBg = BotMessageBg_Dark,
            inputBg = InputBg_Dark,
            divider = Divider_Dark,
            error = Error,
            success = Success,
            warning = Warning,
            isDark = true
        )
    }
} 