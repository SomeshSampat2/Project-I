package com.innovatelabs3.projectI2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.innovatelabs3.projectI2.BuildConfig
import com.innovatelabs3.projectI2.data.model.ChatMessage
import com.innovatelabs3.projectI2.data.model.SearchSource
import com.innovatelabs3.projectI2.data.network.RetrofitClient
import com.innovatelabs3.projectI2.data.network.SearchRequest
import com.innovatelabs3.projectI2.data.network.TavilyApiService
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.innovatelabs3.projectI2.domain.SystemQueries
import com.innovatelabs3.projectI2.domain.QueryType
import com.innovatelabs3.projectI2.utils.GenericUtils
import com.innovatelabs3.projectI2.ProjectIApplication
import com.innovatelabs3.projectI2.utils.ContactUtils

class UserViewModel : ViewModel() {
    private val context = ProjectIApplication.getContext()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = listOf(
            SafetySetting(harmCategory = HarmCategory.HARASSMENT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.HATE_SPEECH, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.SEXUALLY_EXPLICIT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.DANGEROUS_CONTENT, threshold = BlockThreshold.NONE)
        )
    )

    private val systemQueries = SystemQueries(generativeModel)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val tavilyApiService = RetrofitClient.tavilyRetrofit.create(TavilyApiService::class.java)
    private var isSearchMode = MutableStateFlow(false)
    val searchMode: StateFlow<Boolean> = isSearchMode.asStateFlow()

    private val animatedMessages = mutableSetOf<Long>()
    private val _searchSources = MutableStateFlow<List<SearchSource>>(emptyList())
    val searchSources: StateFlow<List<SearchSource>> = _searchSources.asStateFlow()

    private val _showToast = MutableStateFlow<String?>(null)
    val showToast: StateFlow<String?> = _showToast.asStateFlow()

    private val _showSnackbar = MutableStateFlow<String?>(null)
    val showSnackbar: StateFlow<String?> = _showSnackbar.asStateFlow()

    private val _showNotification = MutableStateFlow<SystemQueries.NotificationContent?>(null)
    val showNotification: StateFlow<SystemQueries.NotificationContent?> = _showNotification.asStateFlow()

    fun shouldAnimateMessage(timestamp: Long): Boolean {
        return if (timestamp !in animatedMessages) {
            animatedMessages.add(timestamp)
            true
        } else {
            false
        }
    }

    fun toggleSearchMode() {
        isSearchMode.value = !isSearchMode.value
        if (!isSearchMode.value) {
            _searchSources.value = emptyList()
        }
    }

    fun processCommand(command: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _chatMessages.value = _chatMessages.value + ChatMessage(command, true)

                if (isSearchMode.value) {
                    handleSearchQuery(command)
                } else {
                    when (systemQueries.analyzeQueryType(command)) {
                        is QueryType.ShowToast -> {
                            val message = systemQueries.extractToastMessage(command)
                            _showToast.value = message
                            addAssistantMessage("I've shown a toast message saying: $message")
                        }
                        is QueryType.ShowSnackbar -> {
                            val message = systemQueries.extractSnackbarMessage(command)
                            _showSnackbar.value = message
                            addAssistantMessage("I've shown a snackbar message saying: $message")
                        }
                        is QueryType.ShowNotification -> {
                            val content = systemQueries.extractNotificationContent(command)
                            _showNotification.value = content
                            addAssistantMessage("I've shown a notification with title: ${content.title} and message: ${content.message}")
                        }
                        is QueryType.OpenWhatsApp -> {
                            GenericUtils.openWhatsApp(context)
                            addAssistantMessage("I'm opening WhatsApp for you. If it's not installed, I'll take you to the Play Store.")
                        }
                        is QueryType.Identity -> {
                            addAssistantMessage(systemQueries.getIdentityResponse())
                        }
                        is QueryType.General -> {
                            val response = systemQueries.handleGeneralQuery(command)
                            addAssistantMessage(response)
                        }
                        is QueryType.SendWhatsAppMessage -> {
                            val content = systemQueries.extractWhatsAppMessageContent(command)
                            if (content.phoneNumber.isNotEmpty()) {
                                // Direct phone number was provided
                                GenericUtils.openWhatsAppChat(context, content.phoneNumber, content.message)
                                addAssistantMessage("Opening WhatsApp chat with ${content.phoneNumber}")
                            } else if (content.contactName.isNotEmpty()) {
                                // Contact name was provided
                                if (ContactUtils.checkContactPermission(context)) {
                                    val contactInfo = ContactUtils.findContactByName(context, content.contactName)
                                    if (contactInfo != null) {
                                        GenericUtils.openWhatsAppChat(context, contactInfo.phoneNumber, content.message)
                                        addAssistantMessage("Opening WhatsApp chat with ${contactInfo.name}")
                                    } else {
                                        addAssistantMessage("Sorry, I couldn't find '${content.contactName}' in your contacts.")
                                    }
                                } else {
                                    addAssistantMessage("I need permission to access contacts to find ${content.contactName}'s number. Please grant contacts permission in settings.")
                                }
                            } else {
                                addAssistantMessage("Sorry, I couldn't understand who you want to message.")
                            }
                        }
                        is QueryType.ShowDirections -> {
                            val content = systemQueries.extractDirectionsContent(command)
                            if (content.destination.isNotEmpty()) {
                                GenericUtils.openGoogleMaps(context, content.destination)
                                addAssistantMessage("Opening Google Maps with directions to ${content.destination}")
                            } else {
                                addAssistantMessage("Sorry, I couldn't understand the destination. Please specify where you want to go.")
                            }
                        }
                        is QueryType.SearchYouTube -> {
                            val content = systemQueries.extractYouTubeSearchQuery(command)
                            if (content.searchQuery.isNotEmpty()) {
                                GenericUtils.openYouTubeSearch(context, content.searchQuery)
                                addAssistantMessage("Opening YouTube to search for '${content.searchQuery}'")
                            } else {
                                addAssistantMessage("Sorry, I couldn't understand what you want to search for on YouTube.")
                            }
                        }
                        is QueryType.OpenInstagramProfile -> {
                            val content = systemQueries.extractInstagramUsername(command)
                            if (content.username.isNotEmpty()) {
                                GenericUtils.openInstagramProfile(context, content.username)
                                addAssistantMessage("Opening Instagram profile for @${content.username}")
                            } else {
                                addAssistantMessage("Sorry, I couldn't understand which Instagram profile you want to view.")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                addAssistantMessage("Sorry, I encountered an error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun handleSearchQuery(query: String) {
        try {
            val searchRequest = SearchRequest(query = query)
            try {
                val searchResponse = tavilyApiService.search(searchRequest)
                
                // Check if we got valid results
                if (searchResponse.results.isNotEmpty()) {
                    _searchSources.value = searchResponse.results.take(3).map { result ->
                        SearchSource(
                            title = result.title,
                            url = result.url,
                            content = result.content
                        )
                    }

                    val summaryPrompt = """
                        You are a helpful AI assistant. Using the search results below, provide a natural, 
                        conversational response to the query: "$query"
                        
                        Search results:
                        ${searchResponse.answer ?: ""}
                        
                        ${searchResponse.results.take(3).joinToString("\n\n") { result ->
                            """
                            ${result.title}
                            ${result.content}
                            """.trimIndent()
                        }}
                        
                        Guidelines:
                        - Respond naturally as if having a conversation
                        - Focus on the most relevant information
                        - Avoid mentioning "search results" or "sources"
                        - Don't use numbered points or structured formats
                        - Blend information smoothly into a cohesive response
                        - Keep it concise but informative
                        - Use markdown for emphasis where appropriate
                    """.trimIndent()

                    val summaryResponse = generativeModel.startChat().sendMessage(summaryPrompt)
                    addAssistantMessage(summaryResponse.text ?: "Sorry, I couldn't find relevant information.")
                } else {
                    // Fallback to Gemini if no search results
                    fallbackToGemini(query)
                }

            } catch (e: Exception) {
                // Fallback to Gemini on API error
                fallbackToGemini(query)
            }

        } catch (e: Exception) {
            addAssistantMessage("Error performing search: ${e.localizedMessage}")
            _searchSources.value = emptyList()
        }
    }

    private suspend fun fallbackToGemini(query: String) {
        _searchSources.value = emptyList()
        val newChat = generativeModel.startChat()
        val response = newChat.sendMessage(query).text
        addAssistantMessage(response ?: "Sorry, I couldn't process your request.")
    }

    fun clearToast() {
        _showToast.value = null
    }

    fun clearSnackbar() {
        _showSnackbar.value = null
    }

    fun clearNotification() {
        _showNotification.value = null
    }

    private fun addAssistantMessage(message: String) {
        _chatMessages.value = _chatMessages.value + ChatMessage(message, false)
    }
} 