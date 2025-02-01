package com.example.app.util

object WelcomePrompts {
    private val prompts = listOf(
        "What's on your mind today?",
        "Let's solve something together",
        "What would you like to explore?",
        "Ready to help you out...",
        "Share your thoughts...",
        "What can I help you with?",
        "Curious about something?",
        "Let's dive into your questions",
        "What shall we discover today?"
    )

    fun getRandomPrompt(): String {
        return prompts.random()
    }
} 