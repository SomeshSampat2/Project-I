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

    suspend fun analyzeQueryType(query: String): QueryType {
        val analysisPrompt = """
            Analyze this query and respond with only one of these categories:
            SHOW_TOAST - if asking to show a toast or notification message
            SHOW_SNACKBAR - if asking to show a snackbar message
            SHOW_NOTIFICATION - if asking to show a system notification
            OPEN_WHATSAPP - if asking to just open or launch WhatsApp
            SEND_WHATSAPP_MESSAGE - if asking to send a WhatsApp message to someone
            SHOW_DIRECTIONS - if asking for directions or navigation to a place
            IDENTITY_QUERY - if asking about who I am or my capabilities
            GENERAL_QUERY - for any other topics
            
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
            
            Query: "$query"
        """.trimIndent()

        return when (chat.sendMessage(analysisPrompt).text?.trim()) {
            "SHOW_TOAST" -> QueryType.ShowToast
            "SHOW_SNACKBAR" -> QueryType.ShowSnackbar
            "SHOW_NOTIFICATION" -> QueryType.ShowNotification
            "OPEN_WHATSAPP" -> QueryType.OpenWhatsApp
            "SEND_WHATSAPP_MESSAGE" -> QueryType.SendWhatsAppMessage
            "SHOW_DIRECTIONS" -> QueryType.ShowDirections
            "IDENTITY_QUERY" -> QueryType.Identity
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

    data class NotificationContent(
        val title: String = "Project I",
        val message: String = "Notification message",
        val priority: NotificationPriority = NotificationPriority.DEFAULT
    )

    enum class NotificationPriority {
        HIGH, DEFAULT, LOW
    }
} 