package com.innovatelabs3.projectI2.domain

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.innovatelabs3.projectI2.BuildConfig

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
    object BookUber : QueryType()
    object SearchProduct : QueryType()
    object SaveContact : QueryType()
    object SearchFiles : QueryType()
    object SendEmail : QueryType()
    object MakeCall : QueryType()
}

class SystemQueries {
    // For quick analysis and simple extractions
    private val analyzerModel = GenerativeModel(
        modelName = "gemini-1.5-flash-8b",
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = listOf(
            SafetySetting(harmCategory = HarmCategory.HARASSMENT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.HATE_SPEECH, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.SEXUALLY_EXPLICIT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.DANGEROUS_CONTENT, threshold = BlockThreshold.NONE)
        )
    )

    // For detailed responses and complex content
    private val responseModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = listOf(
            SafetySetting(harmCategory = HarmCategory.HARASSMENT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.HATE_SPEECH, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.SEXUALLY_EXPLICIT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.DANGEROUS_CONTENT, threshold = BlockThreshold.NONE)
        )
    )

    private val analyzerChat = analyzerModel.startChat()
    private val responseChat = responseModel.startChat()

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

    data class UberRideContent(
        val destination: String
    )

    data class ProductSearchContent(
        val query: String,
        val platform: String = "flipkart" // "flipkart" or "amazon"
    )

    data class ContactSaveContent(
        val name: String,
        val phoneNumber: String
    )

    data class PhonePePayment(
        val amount: String,
        val recipientUpiId: String,
        val recipientName: String = "Recipient"
    )

    data class EmailContent(
        val to: String,
        val subject: String = "",
        val body: String = "",
        val isHtml: Boolean = false
    )

    data class CallContent(
        val contactName: String = "",
        val phoneNumber: String = ""
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
            BOOK_UBER - if asking to book an Uber ride or get a cab to somewhere
            SEARCH_PRODUCT - if asking to search for a product on Flipkart or Amazon
            SAVE_CONTACT - if asking to save or add a contact/phone number
            SEARCH_FILES - if asking to find or search for files, documents, photos, or videos on the device
            SEND_EMAIL - if asking to send an email
            MAKE_CALL - if asking to call someone or dial a phone number
            
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
            "Book an Uber to Central Park" -> BOOK_UBER
            "Get me a cab to the airport" -> BOOK_UBER
            "Call Uber to take me to Times Square" -> BOOK_UBER
            "Search for iPhone on Flipkart" -> SEARCH_PRODUCT
            "Find me headphones on Amazon" -> SEARCH_PRODUCT
            "Look for running shoes" -> SEARCH_PRODUCT
            "Save John's number 9876543210" -> SAVE_CONTACT
            "Add contact Mary with phone 1234567890" -> SAVE_CONTACT
            "Save this number 9898989898 as Dad" -> SAVE_CONTACT
            "Find files named project" -> SEARCH_FILES
            "Search for photos with name vacation" -> SEARCH_FILES
            "Look for documents containing report" -> SEARCH_FILES
            "Send mail to john@gmail.com inviting him for today's meeting at 4 PM" -> SEND_EMAIL
            "Call John" -> MAKE_CALL
            "Call 9999999999" -> MAKE_CALL
            "Dial +1234567890" -> MAKE_CALL
            "Make a call to Mary" -> MAKE_CALL
            "Phone Dad" -> MAKE_CALL
            
            Query: "$query"
        """.trimIndent()

        return when (analyzerChat.sendMessage(analysisPrompt).text?.trim()) {
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
            "BOOK_UBER" -> QueryType.BookUber
            "SEARCH_PRODUCT" -> QueryType.SearchProduct
            "SAVE_CONTACT" -> QueryType.SaveContact
            "SEARCH_FILES" -> QueryType.SearchFiles
            "SEND_EMAIL" -> QueryType.SendEmail
            "MAKE_CALL" -> QueryType.MakeCall
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
        
        val response = responseChat.sendMessage(notificationPrompt).text?.trim() ?: return NotificationContent()
        
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
        return analyzerChat.sendMessage("Extract toast message from: '$query'").text?.trim() ?: "Toast message"
    }

    suspend fun extractSnackbarMessage(query: String): String {
        return analyzerChat.sendMessage("Extract snackbar message from: '$query'").text?.trim() ?: "Snackbar message"
    }

    suspend fun extractDirectionsContent(query: String): DirectionsContent {
        val response = analyzerChat.sendMessage("Extract destination from: '$query'").text?.trim() ?: return DirectionsContent("")
        return DirectionsContent(response)
    }

    suspend fun extractYouTubeSearchQuery(query: String): YouTubeSearchContent {
        val response = analyzerChat.sendMessage("Extract YouTube search query from: '$query'").text?.trim() ?: return YouTubeSearchContent("")
        return YouTubeSearchContent(response)
    }

    suspend fun extractInstagramUsername(query: String): InstagramProfileContent {
        val response = analyzerChat.sendMessage("Extract Instagram username from: '$query'").text?.trim() ?: return InstagramProfileContent("")
        return InstagramProfileContent(response.removePrefix("@"))
    }

    suspend fun extractGoogleMeetCode(query: String): GoogleMeetContent {
        val response = analyzerChat.sendMessage("Extract Google Meet code from: '$query'").text?.trim() ?: return GoogleMeetContent("")
        return GoogleMeetContent(response)
    }

    suspend fun extractSpotifySearchContent(query: String): SpotifySearchContent {
        val response = analyzerChat.sendMessage("Extract Spotify search query from: '$query'").text?.trim() ?: return SpotifySearchContent("")
        return SpotifySearchContent(response)
    }

    suspend fun extractUberDestination(query: String): UberRideContent {
        val response = analyzerChat.sendMessage("Extract Uber destination from: '$query'").text?.trim() ?: return UberRideContent("")
        return UberRideContent(response)
    }

    suspend fun extractProductSearchContent(query: String): ProductSearchContent {
        val searchPrompt = """
            Extract product search query and platform from: "$query"
            Reply in format:
            PLATFORM:[flipkart/amazon]
            QUERY:[product to search]
            Default to flipkart if platform not specified.
            Keep it brief.
        """.trimIndent()

        val response = responseChat.sendMessage(searchPrompt).text?.trim() ?: return ProductSearchContent("")
        
        return try {
            val platform = response.lineSequence()
                .firstOrNull { it.startsWith("PLATFORM:") }
                ?.substringAfter("PLATFORM:")
                ?.trim()
                ?.lowercase()
                ?: "flipkart"

            val searchQuery = response.lineSequence()
                .firstOrNull { it.startsWith("QUERY:") }
                ?.substringAfter("QUERY:")
                ?.trim()
                ?: ""
            
            ProductSearchContent(searchQuery, platform)
        } catch (e: Exception) {
            ProductSearchContent("")
        }
    }

    suspend fun extractContactDetails(query: String): ContactSaveContent {
        val contactPrompt = """
            Extract name and phone number from: "$query"
            Reply in format:
            NAME:[contact name]
            PHONE:[phone number]
            Format phone number with only digits.
            Keep it brief.
        """.trimIndent()

        val response = responseChat.sendMessage(contactPrompt).text?.trim() ?: return ContactSaveContent("", "")
        
        return try {
            val name = response.lineSequence()
                .firstOrNull { it.startsWith("NAME:") }
                ?.substringAfter("NAME:")
                ?.trim()
                ?.split(" ")
                ?.joinToString(" ") { word -> 
                    word.lowercase().replaceFirstChar { it.uppercase() }
                }
                ?: ""

            val phone = response.lineSequence()
                .firstOrNull { it.startsWith("PHONE:") }
                ?.substringAfter("PHONE:")
                ?.trim()
                ?.replace(Regex("[^0-9]"), "")  // Keep only digits
                ?: ""
            
            ContactSaveContent(name, phone)
        } catch (e: Exception) {
            ContactSaveContent("", "")
        }
    }

    suspend fun handleGeneralQuery(query: String): String {
        return responseChat.sendMessage(query).text ?: "Sorry, I couldn't process your request."
    }

    suspend fun getIdentityResponse(): String {
        // Standard identity response that covers everything
        val standardResponse = """
            I'm Project I, your AI Agent created by Somesh Sampat at Innovate Labs. I help you get things done through natural conversations - whether it's making calls, sending messages, booking rides, or handling emails. I'm designed to make your daily tasks easier and more convenient. Just tell me what you need!
        """.trimIndent()

        // Always return the standard response regardless of the specific identity question
        return standardResponse
    }

    suspend fun extractWhatsAppMessageContent(query: String): WhatsAppMessageContent {
        val messagePrompt = """
            Extract WhatsApp message details from: "$query"
            craft the message in brief by understanding the intent of user and then make that as the real message that user wants to send
            Reply in format:
            CONTACT_NAME:[name]
            PHONE:[phone number]
            MESSAGE:[message to send]
            Keep phone number empty if not directly specified.
            Keep contact name empty if not specified.
            Keep it brief.
        """.trimIndent()

        val response = analyzerChat.sendMessage(messagePrompt).text?.trim() ?: return WhatsAppMessageContent()
        
        return try {
            val contactName = response.lineSequence()
                .firstOrNull { it.startsWith("CONTACT_NAME:") }
                ?.substringAfter("CONTACT_NAME:")
                ?.trim()
                ?: ""

            val phoneNumber = response.lineSequence()
                .firstOrNull { it.startsWith("PHONE:") }
                ?.substringAfter("PHONE:")
                ?.trim()
                ?.replace(Regex("[^0-9+]"), "")  // Keep only digits and plus sign
                ?: ""

            val message = response.lineSequence()
                .firstOrNull { it.startsWith("MESSAGE:") }
                ?.substringAfter("MESSAGE:")
                ?.trim()
                ?: ""
            
            WhatsAppMessageContent(contactName, phoneNumber, message)
        } catch (e: Exception) {
            WhatsAppMessageContent()
        }
    }

    suspend fun extractSearchQuery(query: String): String {
        val searchPrompt = """
            Extract the search term from: "$query"
            Reply with just the search term, keep it brief.
        """.trimIndent()

        return analyzerChat.sendMessage(searchPrompt).text?.trim() ?: ""
    }

    suspend fun extractEmailContent(query: String): EmailContent {
        val emailPrompt = """
            Extract email details from: "$query"
            If subject is not specified, create an appropriate one.
            If the content is about a meeting, include relevant details in a professional manner.
            Reply in format:
            TO:[email address]
            SUBJECT:[subject line]
            BODY:[email body]
            
            Guidelines:
            - Keep the tone professional
            - Include all important details (time, date, purpose if mentioned)
            - Format the body properly with greetings and signature
            - If meeting related, include location/link if specified
            
            Example:
            Query: "send mail to john@gmail.com inviting him for today's meeting at 4 PM"
            TO:john@gmail.com
            SUBJECT:Meeting Invitation - Today at 4 PM
            BODY:Dear John,

            I hope this email finds you well. I would like to invite you to a meeting scheduled for today at 4 PM.

            Looking forward to your participation.

            Best regards
        """.trimIndent()

        val response = responseChat.sendMessage(emailPrompt).text?.trim() ?: return EmailContent("")

        return try {
            val to = response.lineSequence()
                .firstOrNull { it.startsWith("TO:") }
                ?.substringAfter("TO:")
                ?.trim() ?: ""

            val subject = response.lineSequence()
                .firstOrNull { it.startsWith("SUBJECT:") }
                ?.substringAfter("SUBJECT:")
                ?.trim() ?: "Meeting Invitation"

            val body = response.substringAfter("BODY:", "")
                .trim()
                .ifEmpty { "No content provided" }

            EmailContent(to, subject, body)
        } catch (e: Exception) {
            EmailContent("")
        }
    }

    suspend fun extractCallDetails(query: String): CallContent {
        val callPrompt = """
            Extract call details from: "$query"
            Reply in format:
            CONTACT_NAME:[name]
            PHONE:[phone number]
            
            Guidelines:
            - If direct phone number is given, keep contact name empty
            - If name is given, keep phone empty
            - Remove any non-digit characters from phone number except +
            
            Example:
            Query: "call john"
            CONTACT_NAME:john
            PHONE:
            
            Query: "call 9999999999"
            CONTACT_NAME:
            PHONE:9999999999
        """.trimIndent()

        val response = analyzerChat.sendMessage(callPrompt).text?.trim() ?: return CallContent()

        return try {
            val contactName = response.lineSequence()
                .firstOrNull { it.startsWith("CONTACT_NAME:") }
                ?.substringAfter("CONTACT_NAME:")
                ?.trim() ?: ""

            val phoneNumber = response.lineSequence()
                .firstOrNull { it.startsWith("PHONE:") }
                ?.substringAfter("PHONE:")
                ?.trim()
                ?.replace(Regex("[^0-9+]"), "") ?: ""

            CallContent(contactName, phoneNumber)
        } catch (e: Exception) {
            CallContent()
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

fun String.extractPhonePePaymentDetails(): SystemQueries.PhonePePayment? {
    val regex = """(?i)(?:pay|send)\s+(\d+)(?:\s+(?:rs|rupees))?\s+(?:to\s+)?([a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+)(?:\s+(?:through|via|using|with)\s+phonepe)?""".toRegex()
    
    return regex.find(this)?.let { match ->
        val amount = match.groupValues[1]
        val upiId = match.groupValues[2]
        SystemQueries.PhonePePayment(amount, upiId)
    }
} 