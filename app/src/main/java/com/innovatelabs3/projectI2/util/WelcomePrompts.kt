package com.innovatelabs3.projectI2.util

object WelcomePrompts {
    private val prompts = listOf(
        "Hi there!",
        "Ask me!",
        "Hello :)",
        "Need help?",
        "Hey!",
        "Hi friend!",
        "Ask away!",
        "Let's go!"
    )

    fun getRandomPrompt(): String {
        return prompts.random()
    }
} 