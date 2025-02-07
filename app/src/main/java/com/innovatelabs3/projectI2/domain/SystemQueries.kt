package com.innovatelabs3.projectI2.domain

import com.google.ai.client.generativeai.GenerativeModel

sealed class QueryType {
    object ShowToast : QueryType()
    object ShowSnackbar : QueryType()
    object ShowNotification : QueryType()
    object OpenWhatsApp : QueryType()
    object SendWhatsAppMessage : QueryType()
    object Identity : QueryType()
    object General : QueryType()
    object ShowDirections : QueryType()
    object SearchYouTube : QueryType()
    object OpenInstagramProfile : QueryType()
    object JoinGoogleMeet : QueryType()
    object SearchSpotify : QueryType()
}

class SystemQueries(private val generativeModel: GenerativeModel) {
    private val chat = generativeModel.startChat()

    data class WhatsAppMessageContent(
        val contactName: String = "",
        val phoneNumber: String = "",
        val message: String = ""
    )

    data class DirectionsContent(
        val destination: String
    )

    data class YouTubeSearchContent(
        val searchQuery: String
    )

    data class InstagramProfileContent(
        val username: String
    )

    data class GoogleMeetContent(
        val meetingCode: String
    )

    data class SpotifySearchContent(
        val query: String,
        val type: String = "track" // "track" or "artist"
    )

    suspend fun analyzeQueryType(query: String): QueryType {
        val analysisPrompt = """
            Analyze this query and respond with only one of these categories:
            SHOW_TOAST - if asking to show a toast or notification message
            SHOW_SNACKBAR - if asking to show a snackbar message
            SHOW_NOTIFICATION - if asking to show a system notification
            OPEN_WHATSAPP - if asking to just open or launch WhatsApp
            SEND_WHATSAPP_MESSAGE - if asking to send a WhatsApp message to someone
            SHOW_DIRECTIONS - if asking for directions or navigation to a place
            SEARCH_YOUTUBE - if asking to search or watch videos on YouTube
            IDENTITY_QUERY - if asking about who I am or my capabilities
            GENERAL_QUERY - for any other topics
            SHOW_DIRECTIONS - if asking for directions or navigation to a place
            SEARCH_YOUTUBE - if asking to search or watch videos on YouTube
            OPEN_INSTAGRAM - if asking to open or view someone's Instagram profile
            JOIN_MEET - if asking to join or open a Google Meet meeting
            SEARCH_SPOTIFY - if asking to search or play music on Spotify
            
            Examples:
            "Show me directions to Central Park" -> SHOW_DIRECTIONS
            "Navigate to Times Square" -> SHOW_DIRECTIONS
            "How do I get to the airport" -> SHOW_DIRECTIONS
            "Show me a toast" -> SHOW_TOAST
            "Display a snackbar" -> SHOW_SNACKBAR
            "Send a notification" -> SHOW_NOTIFICATION
            "Open WhatsApp" -> OPEN_WHATSAPP
            "Send WhatsApp message to 1234567890" -> SEND_WHATSAPP_MESSAGE
            "Message John on WhatsApp" -> SEND_WHATSAPP_MESSAGE
            "Who are you" -> IDENTITY_QUERY
            "What's the weather" -> GENERAL_QUERY
            "Show me videos of cooking pasta" -> SEARCH_YOUTUBE
            "Search YouTube for Taylor Swift" -> SEARCH_YOUTUBE
            "Find workout videos on YouTube" -> SEARCH_YOUTUBE
            "Show me John's Instagram profile" -> OPEN_INSTAGRAM
            "Open Instagram profile of taylorswift" -> OPEN_INSTAGRAM
            "Take me to @username on Instagram" -> OPEN_INSTAGRAM
            "Join Google Meet abc-defg-hij" -> JOIN_MEET
            "Open Meet meeting code xyz-123" -> JOIN_MEET
            "Take me to Google Meet abcdefghij" -> JOIN_MEET
            "Play Shape of You on Spotify" -> SEARCH_SPOTIFY
            "Find Ed Sheeran songs on Spotify" -> SEARCH_SPOTIFY
            "Search for Taylor Swift on Spotify" -> SEARCH_SPOTIFY
            "Show me Coldplay tracks" -> SEARCH_SPOTIFY
            
            Query: "$query"
        """.trimIndent()

        return when (chat.sendMessage(analysisPrompt).text?.trim()) {
            "SHOW_TOAST" -> QueryType.ShowToast
            "SHOW_SNACKBAR" -> QueryType.ShowSnackbar
            "SHOW_NOTIFICATION" -> QueryType.ShowNotification
            "OPEN_WHATSAPP" -> QueryType.OpenWhatsApp
            "SEND_WHATSAPP_MESSAGE" -> QueryType.SendWhatsAppMessage
            "SHOW_DIRECTIONS" -> QueryType.ShowDirections
            "SEARCH_YOUTUBE" -> QueryType.SearchYouTube
            "IDENTITY_QUERY" -> QueryType.Identity
            "OPEN_INSTAGRAM" -> QueryType.OpenInstagramProfile
            "JOIN_MEET" -> QueryType.JoinGoogleMeet
            "SEARCH_SPOTIFY" -> QueryType.SearchSpotify
            else -> QueryType.General
        }
    }

    suspend fun extractNotificationContent(query: String): NotificationContent {
        val notificationPrompt = """
            Extract the title and message for the notification from this request: "$query"
            Respond in this exact format:
            TITLE: [notification title]
            MESSAGE: [notification message]
        """.trimIndent()
        
        val response = chat.sendMessage(notificationPrompt).text?.trim() ?: return NotificationContent()
        
        return try {
            val lines = response.lines()
            val title = lines.firstOrNull { it.startsWith("TITLE:") }?.substringAfter("TITLE:")?.trim()
            val message = lines.firstOrNull { it.startsWith("MESSAGE:") }?.substringAfter("MESSAGE:")?.trim()
            
            NotificationContent(
                title = title ?: "Project I",
                message = message ?: "Notification message"
            )
        } catch (e: Exception) {
            NotificationContent()
        }
    }

    suspend fun extractToastMessage(query: String): String {
        val toastPrompt = """
            Extract the message to show in the toast from this request: "$query"
            Respond with just the message, no additional text.
        """.trimIndent()
        
        return chat.sendMessage(toastPrompt).text?.trim() ?: "Toast message"
    }

    suspend fun extractSnackbarMessage(query: String): String {
        val snackbarPrompt = """
            Extract the message to show in the snackbar from this request: "$query"
            Respond with just the message, no additional text.
        """.trimIndent()
        
        return chat.sendMessage(snackbarPrompt).text?.trim() ?: "Snackbar message"
    }

    fun getIdentityResponse(): String {
        return """
            Let me introduce myself! 
            
            I'm <b>Project I</b>, an AI assistant developed by <b>Somesh Sampat</b> at <b>Innovate Labs</b> in <b>India</b>. 
            I'm built on top of Google's Gemini model to help users with various tasks and queries.
            
            While I leverage Gemini's powerful language understanding capabilities, my interface and specific functionalities 
            were custom-developed to provide a unique and helpful experience.
            
            Is there anything specific you'd like to know about my capabilities?
        """.trimIndent()
    }

    suspend fun handleGeneralQuery(query: String): String {
        return chat.sendMessage(query).text ?: "Couldn't process request."
    }

    suspend fun extractWhatsAppMessageContent(query: String): WhatsAppMessageContent {
        val messagePrompt = """
            Extract contact name/phone number and message from: "$query"
            If number is given, format it with +91.
            Reply in format:
            TARGET:[contact name or phone number]
            MSG:[message]
            Keep it brief.
        """.trimIndent()

        val response = chat.sendMessage(messagePrompt).text?.trim() ?: return WhatsAppMessageContent()
        
        return try {
            val target = response.lineSequence()
                .firstOrNull { it.startsWith("TARGET:") }
                ?.substringAfter("TARGET:")
                ?.trim()
                ?: ""

            val message = response.lineSequence()
                .firstOrNull { it.startsWith("MSG:") }
                ?.substringAfter("MSG:")
                ?.trim()
                ?: ""

            // Check if target is a phone number (contains only digits)
            if (target.replace(Regex("[^0-9]"), "").length >= 10) {
                // It's a phone number
                val cleanNumber = target.replace(Regex("[^0-9+]"), "")
                val formattedNumber = if (cleanNumber.startsWith("+")) cleanNumber else "+91$cleanNumber"
                WhatsAppMessageContent(phoneNumber = formattedNumber, message = message)
            } else {
                // It's a contact name
                WhatsAppMessageContent(contactName = target, message = message)
            }
        } catch (e: Exception) {
            WhatsAppMessageContent()
        }
    }

    suspend fun extractDirectionsContent(query: String): DirectionsContent {
        val directionsPrompt = """
            Extract the destination from: "$query"
            Reply in format:
            DEST:[destination address or place name]
            Keep it brief and specific.
        """.trimIndent()

        val response = chat.sendMessage(directionsPrompt).text?.trim() ?: return DirectionsContent("")
        
        return try {
            val destination = response.lineSequence()
                .firstOrNull { it.startsWith("DEST:") }
                ?.substringAfter("DEST:")
                ?.trim()
                ?: ""
            
            DirectionsContent(destination)
        } catch (e: Exception) {
            DirectionsContent("")
        }
    }

    suspend fun extractYouTubeSearchQuery(query: String): YouTubeSearchContent {
        val searchPrompt = """
            Extract the search query for YouTube from: "$query"
            Reply in format:
            SEARCH:[what to search for]
            Keep it brief and specific.
        """.trimIndent()

        val response = chat.sendMessage(searchPrompt).text?.trim() ?: return YouTubeSearchContent("")
        
        return try {
            val searchQuery = response.lineSequence()
                .firstOrNull { it.startsWith("SEARCH:") }
                ?.substringAfter("SEARCH:")
                ?.trim()
                ?: ""
            
            YouTubeSearchContent(searchQuery)
        } catch (e: Exception) {
            YouTubeSearchContent("")
        }
    }

    suspend fun extractInstagramUsername(query: String): InstagramProfileContent {
        val usernamePrompt = """
            Extract Instagram username from: "$query"
            Reply in format:
            USER:[username without @ symbol]
            Keep it brief.
        """.trimIndent()

        val response = chat.sendMessage(usernamePrompt).text?.trim() ?: return InstagramProfileContent("")
        
        return try {
            val username = response.lineSequence()
                .firstOrNull { it.startsWith("USER:") }
                ?.substringAfter("USER:")
                ?.trim()
                ?.removePrefix("@")  // Remove @ if present
                ?: ""
            
            InstagramProfileContent(username)
        } catch (e: Exception) {
            InstagramProfileContent("")
        }
    }

    suspend fun extractGoogleMeetCode(query: String): GoogleMeetContent {
        val meetPrompt = """
            Extract the Google Meet meeting code from: "$query"
            Reply in format:
            CODE:[meeting code]
            Keep it brief. Include any hyphens or special characters.
        """.trimIndent()

        val response = chat.sendMessage(meetPrompt).text?.trim() ?: return GoogleMeetContent("")
        
        return try {
            val code = response.lineSequence()
                .firstOrNull { it.startsWith("CODE:") }
                ?.substringAfter("CODE:")
                ?.trim()
                ?: ""
            
            GoogleMeetContent(code)
        } catch (e: Exception) {
            GoogleMeetContent("")
        }
    }

    suspend fun extractSpotifySearchContent(query: String): SpotifySearchContent {
        val spotifyPrompt = """
            Extract search query and type for Spotify from: "$query"
            Reply in format:
            TYPE:[track/artist]
            QUERY:[search term]
            If searching for a specific song, use 'track'. If searching for an artist, use 'artist'.
            Keep it brief.
        """.trimIndent()

        val response = chat.sendMessage(spotifyPrompt).text?.trim() ?: return SpotifySearchContent("")
        
        return try {
            val type = response.lineSequence()
                .firstOrNull { it.startsWith("TYPE:") }
                ?.substringAfter("TYPE:")
                ?.trim()
                ?.lowercase()
                ?: "track"

            val searchQuery = response.lineSequence()
                .firstOrNull { it.startsWith("QUERY:") }
                ?.substringAfter("QUERY:")
                ?.trim()
                ?: ""
            
            SpotifySearchContent(searchQuery, type)
        } catch (e: Exception) {
            SpotifySearchContent("")
        }
    }

    data class NotificationContent(
        val title: String = "Project I",
        val message: String = "Notification message",
        val priority: NotificationPriority = NotificationPriority.DEFAULT
    )

    enum class NotificationPriority {
        HIGH, DEFAULT, LOW
    }
} 