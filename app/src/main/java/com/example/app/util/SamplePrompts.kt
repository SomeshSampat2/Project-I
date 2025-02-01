package com.example.app.util

object SamplePrompts {
    val allSuggestions = listOf(
        // Creative & Writing
        Suggestion(
            title = "Story Writing",
            prompt = "Write a short story about a mysterious package that arrives at midnight"
        ),
        Suggestion(
            title = "Poetry",
            prompt = "Help me write a poem about the changing seasons"
        ),
        
        // Tech & Programming
        Suggestion(
            title = "Code Help",
            prompt = "Explain how to implement a binary search algorithm in Kotlin"
        ),
        Suggestion(
            title = "Mobile Dev",
            prompt = "What are the best practices for Android app architecture?"
        ),
        
        // Lifestyle & Wellness
        Suggestion(
            title = "Meditation",
            prompt = "Give me a 5-minute mindfulness exercise for stress relief"
        ),
        Suggestion(
            title = "Fitness",
            prompt = "Suggest a quick home workout routine without equipment"
        ),
        
        // Food & Cooking
        Suggestion(
            title = "Recipe",
            prompt = "Share a healthy breakfast recipe that takes 10 minutes to make"
        ),
        Suggestion(
            title = "Cooking Tips",
            prompt = "What are some essential cooking techniques every beginner should know?"
        ),
        
        // Travel & Culture
        Suggestion(
            title = "Travel Guide",
            prompt = "Plan a perfect weekend itinerary for visiting Tokyo"
        ),
        Suggestion(
            title = "Culture",
            prompt = "Tell me about interesting traditional festivals around the world"
        ),
        
        // Science & Nature
        Suggestion(
            title = "Space",
            prompt = "Explain black holes in simple terms"
        ),
        Suggestion(
            title = "Nature",
            prompt = "What makes fireflies glow? Explain the science behind it"
        ),
        
        // Art & Music
        Suggestion(
            title = "Art History",
            prompt = "Tell me about the most influential Renaissance artists"
        ),
        Suggestion(
            title = "Music",
            prompt = "How did jazz music evolve through the decades?"
        ),
        
        // Personal Growth
        Suggestion(
            title = "Productivity",
            prompt = "Share some effective time management techniques"
        ),
        Suggestion(
            title = "Learning",
            prompt = "What are the best methods for learning a new language?"
        )
    )

    val suggestions: List<Suggestion>
        get() = allSuggestions.shuffled().take(4)
}

data class Suggestion(
    val title: String,
    val prompt: String
) 