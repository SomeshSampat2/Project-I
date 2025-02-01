package com.innovatelabs3.projectI2.util

object SamplePrompts {
    val allSuggestions = listOf(
        // Creative & Writing
        Suggestion(
            title = "Story Writing",
            prompt = "Write a short story set in the bustling streets of Mumbai, where an unexpected encounter changes a life."
        ),
        Suggestion(
            title = "Poetry",
            prompt = "Help me write a poem that captures the vibrant spirit of an Indian monsoon."
        ),
        
        // Tech & Programming
        Suggestion(
            title = "Code Help",
            prompt = "Explain how to implement a binary search algorithm in Kotlin"
        ),
        Suggestion(
            title = "Mobile Dev",
            prompt = "What are the best practices for Android app architecture? Also, mention popular frameworks used in India."
        ),
        
        // Lifestyle & Wellness
        Suggestion(
            title = "Meditation",
            prompt = "Give me a 5-minute mindfulness exercise inspired by ancient Indian meditation techniques"
        ),
        Suggestion(
            title = "Fitness",
            prompt = "Suggest a quick home workout routine without equipment suited for busy professionals"
        ),
        
        // Food & Cooking
        Suggestion(
            title = "Recipe",
            prompt = "Share an authentic recipe for Masala Dosa with step-by-step instructions"
        ),
        Suggestion(
            title = "Cooking Tips",
            prompt = "What are some essential cooking techniques for preparing traditional Indian dishes?"
        ),
        
        // Travel & Culture
        Suggestion(
            title = "Travel Guide",
            prompt = "Plan a perfect weekend itinerary for exploring Delhi and Agra"
        ),
        Suggestion(
            title = "Culture",
            prompt = "Tell me about the cultural significance of Diwali and other Indian festivals"
        ),
        
        // Science & Nature
        Suggestion(
            title = "Space",
            prompt = "Explain black holes in simple terms"
        ),
        Suggestion(
            title = "Nature",
            prompt = "What makes the Western Ghats ecologically unique? Explain the science behind it"
        ),
        
        // Art & Music
        Suggestion(
            title = "Art History",
            prompt = "Discuss the influence of Indian classical dance forms like Bharatanatyam on modern choreography"
        ),
        Suggestion(
            title = "Music",
            prompt = "How has Bollywood music evolved over the years while blending traditional Indian sounds?"
        ),
        
        // Personal Growth
        Suggestion(
            title = "Productivity",
            prompt = "Share some effective time management techniques for a busy workday"
        ),
        Suggestion(
            title = "Learning",
            prompt = "What are the best methods for learning a new language, including regional Indian languages?"
        )
    )

    val suggestions: List<Suggestion>
        get() = allSuggestions.shuffled().take(4)
}

data class Suggestion(
    val title: String,
    val prompt: String
) 