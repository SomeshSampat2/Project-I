package com.innovatelabs3.projectI2.ui.viewmodel

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
import com.innovatelabs3.projectI2.utils.ContactUtils
import com.innovatelabs3.projectI2.utils.FileSearchUtils
import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.innovatelabs3.projectI2.utils.FileSearchResult
import com.innovatelabs3.projectI2.utils.PaymentUtils
import com.innovatelabs3.projectI2.domain.extractPhonePePaymentDetails
import com.innovatelabs3.projectI2.domain.models.PhonePePayment
import com.innovatelabs3.projectI2.utils.EmailUtils
import com.innovatelabs3.projectI2.utils.CallUtils
import kotlinx.coroutines.delay
import android.net.Uri

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = listOf(
            SafetySetting(harmCategory = HarmCategory.HARASSMENT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.HATE_SPEECH, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.SEXUALLY_EXPLICIT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.DANGEROUS_CONTENT, threshold = BlockThreshold.NONE)
        )
    )

    private val systemQueries = SystemQueries()
    
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

    private val _requestPermission = MutableStateFlow<String?>(null)
    val requestPermission: StateFlow<String?> = _requestPermission.asStateFlow()

    private var lastOperation: (() -> Unit)? = null

    private val _lastQueryType = MutableStateFlow<QueryType>(QueryType.General)
    val lastQueryType: StateFlow<QueryType> = _lastQueryType.asStateFlow()

    // Add a new state flow for search results
    private val _searchResults = MutableStateFlow<List<FileSearchResult>>(emptyList())
    val searchResults: StateFlow<List<FileSearchResult>> = _searchResults.asStateFlow()

    private var pendingCall: CallDetails? = null

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedImage = MutableStateFlow<Uri?>(null)
    val selectedImage: StateFlow<Uri?> = _selectedImage

    data class CallDetails(
        val number: String,
        val displayName: String
    )

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

    fun processCommand(message: String) {
        viewModelScope.launch {
            // Set loading true at the start, individual branches will manage turning it off.
            _isLoading.value = true 
            
            try {
                // Add user message (potentially with image)
                _chatMessages.value += ChatMessage(
                    content = message, 
                    isUser = true,
                    imageUri = selectedImage.value
                )

                // Get bitmap if image is selected
                val bitmap = selectedImage.value?.let { uri ->
                    GenericUtils.uriToBitmap(getApplication(), uri)
                }
                
                // If there's an image, use multimodal streaming
                if (bitmap != null) {
                    _lastQueryType.value = QueryType.ImageAnalysis
                    setSelectedImage(null) // Clear the image preview

                    val inputContent = com.google.ai.client.generativeai.type.content {
                        image(bitmap)
                        text(message)
                    }

                    // Add placeholder for streaming response
                    val placeholderMessage = ChatMessage("", isUser = false)
                    _chatMessages.value += placeholderMessage
                    var currentContent = ""
                    var isFirstChunk = true

                    try {
                        generativeModel.generateContentStream(inputContent).collect { chunk ->
                            chunk.text?.let { textChunk ->
                                if (isFirstChunk) {
                                    _isLoading.value = false // Stop loading ON first chunk
                                    isFirstChunk = false
                                }
                                currentContent += textChunk
                                _chatMessages.value = _chatMessages.value.dropLast(1) +
                                        placeholderMessage.copy(content = currentContent)
                            }
                        }
                    } catch (e: Exception) {
                        _isLoading.value = false // Stop loading on error
                        _chatMessages.value = _chatMessages.value.dropLast(1) +
                                placeholderMessage.copy(content = "Sorry, I encountered an error analyzing the image: ${e.localizedMessage}")
                    }
                    // Ensure loading is false if the stream finishes without content
                    if (isFirstChunk) {
                        _isLoading.value = false 
                    }
                    // REMOVED old non-streaming call:
                    // val response = systemQueries.analyzeImageWithQuery(it, message)
                    // addAssistantMessage(response)
                    
                } else {
                    // --- Text-based processing starts here ---
                    
                    // Check for PhonePe payment first (non-streaming)
                    message.extractPhonePePaymentDetails()?.let { payment ->
                        handleSystemQuery(payment)
                        _isLoading.value = false // Stop loading after handling
                        return@launch // Exit after handling payment
                    }

                    if (isSearchMode.value) {
                        // handleSearchQuery already implements streaming and manages isLoading
                        handleSearchQuery(message)
                    } else {
                        val type = systemQueries.analyzeQueryType(message)
                        _lastQueryType.value = type
                        
                        when (type) {
                            // --- Non-streaming cases ---
                            is QueryType.ShowToast -> {
                                val query = systemQueries.extractToastMessage(message)
                                _showToast.value = query
                                addAssistantMessage("I've shown a toast message saying: $query")
                                _isLoading.value = false
                            }
                            is QueryType.ShowSnackbar -> {
                                val query = systemQueries.extractSnackbarMessage(message)
                                _showSnackbar.value = query
                                addAssistantMessage("I've shown a snackbar message saying: $query")
                                _isLoading.value = false
                            }
                            // ... (Other non-streaming cases like OpenWhatsApp, Identity, Directions, etc. need _isLoading.value = false) ...
                             is QueryType.ShowNotification -> {
                                val content = systemQueries.extractNotificationContent(message)
                                _showNotification.value = content
                                addAssistantMessage("I've shown a notification with title: ${content.title} and message: ${content.message}")
                                _isLoading.value = false
                            }
                            is QueryType.OpenWhatsApp -> {
                                GenericUtils.openWhatsApp(context)
                                addAssistantMessage("I'm opening WhatsApp for you. If it's not installed, I'll take you to the Play Store.")
                                _isLoading.value = false
                            }
                            is QueryType.Identity -> {
                                addAssistantMessage(systemQueries.getIdentityResponse())
                                _isLoading.value = false
                            }
                            is QueryType.SendWhatsAppMessage -> {
                                // ... (logic as before) ...
                                // Make sure isLoading is set to false after attempting to open chat/show message
                                _isLoading.value = false
                            }
                             is QueryType.ShowDirections -> {
                                val content = systemQueries.extractDirectionsContent(message)
                                if (content.destination.isNotEmpty()) {
                                    GenericUtils.openGoogleMaps(context, content.destination)
                                    addAssistantMessage("Opening Google Maps with directions to ${content.destination}")
                                } else {
                                    addAssistantMessage("Sorry, I couldn't understand the destination. Please specify where you want to go.")
                                }
                                _isLoading.value = false
                            }
                            // ... Add _isLoading.value = false to all other non-streaming cases ...
                            
                            // --- Cases potentially needing permissions (handle isLoading within their logic/callbacks) ---
                            is QueryType.SaveContact -> { /* ... keep existing logic ... ensure isLoading=false on completion/error/permission denial */ }
                            is QueryType.SearchFiles -> { /* ... keep existing logic ... ensure isLoading=false on completion/error/permission denial */ }
                            is QueryType.MakeCall -> { /* ... keep existing logic ... ensure isLoading=false on completion/error/permission denial */ }

                            // --- Cases that might involve network but aren't streaming the main response ---
                             is QueryType.ScrapDataFromWebUrl -> {
                                // This one handles isLoading internally, keep as is for now
                                _isLoading.value = true
                                try {
                                    // ... (scraping logic) ...
                                } catch (e: Exception) {
                                   // ... error handling ...
                                } finally {
                                    _isLoading.value = false
                                }
                            }
                            
                            // --- Streaming Case (already updated) ---
                            QueryType.General -> {
                                // Streaming logic handles isLoading internally
                                // (Code block for QueryType.General as updated previously)
                                _isLoading.value = true // Show loading initially
                                val placeholderMessage = ChatMessage("", isUser = false)
                                _chatMessages.value += placeholderMessage
                                var currentContent = ""
                                var isFirstChunk = true
                                
                                try {
                                    generativeModel.generateContentStream(message).collect { chunk ->
                                        chunk.text?.let { textChunk ->
                                            if (isFirstChunk) {
                                                _isLoading.value = false // Stop loading ON first chunk
                                                isFirstChunk = false
                                            }
                                            currentContent += textChunk
                                            _chatMessages.value = _chatMessages.value.dropLast(1) +
                                                    placeholderMessage.copy(content = currentContent)
                                        }
                                    }
                                } catch (e: Exception) {
                                    _isLoading.value = false // Stop loading on error too
                                    _chatMessages.value = _chatMessages.value.dropLast(1) +
                                            placeholderMessage.copy(content = "Sorry, I encountered an error: ${e.localizedMessage}")
                                }
                                // Ensure loading is false if the stream finishes without content or error
                                if (isFirstChunk) { 
                                    _isLoading.value = false
                                }
                            }
                            else -> {
                                // Should not happen if all types are covered
                                _isLoading.value = false 
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                addAssistantMessage("Sorry, I encountered an error: ${e.localizedMessage}")
                _isLoading.value = false // Ensure loading stops on outer error
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

                    _isLoading.value = true // Show loading initially
                    // Add placeholder for streaming response
                    val placeholderMessage = ChatMessage("", isUser = false)
                    _chatMessages.value += placeholderMessage
                    var currentContent = ""
                    var isFirstChunk = true
                    
                    try {
                        generativeModel.generateContentStream(summaryPrompt).collect { chunk ->
                           chunk.text?.let { textChunk ->
                                if (isFirstChunk) {
                                    _isLoading.value = false // Stop loading ON first chunk
                                    isFirstChunk = false
                                }
                                currentContent += textChunk
                                _chatMessages.value = _chatMessages.value.dropLast(1) + 
                                        placeholderMessage.copy(content = currentContent)
                            } 
                        }
                    } catch (e: Exception) {
                        _isLoading.value = false // Stop loading on error too
                        _chatMessages.value = _chatMessages.value.dropLast(1) +
                                placeholderMessage.copy(content = "Sorry, I couldn't summarize the search results: ${e.localizedMessage}")
                        // Optionally fallback to Gemini if summary fails
                        // fallbackToGemini(query)
                    }
                    // Ensure loading is false if the stream finishes without content or error
                    if (isFirstChunk) { 
                        _isLoading.value = false
                    }
                    
                    // Remove old non-streaming call
                    // val summaryResponse = generativeModel.startChat().sendMessage(summaryPrompt)
                    // addAssistantMessage(summaryResponse.text ?: "Sorry, I couldn't find relevant information.")
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
        _isLoading.value = true // Show loading initially
        // Add an initial placeholder message
        val placeholderMessage = ChatMessage("", isUser = false)
        _chatMessages.value += placeholderMessage
        var currentContent = ""
        var isFirstChunk = true

        try {
            generativeModel.generateContentStream(query).collect { chunk ->
                chunk.text?.let { textChunk ->
                    if (isFirstChunk) {
                        _isLoading.value = false // Stop loading ON first chunk
                        isFirstChunk = false
                    }
                    currentContent += textChunk
                    // Update the last message content incrementally
                    _chatMessages.value = _chatMessages.value.dropLast(1) +
                            placeholderMessage.copy(content = currentContent)
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false // Stop loading on error too
            // Update the placeholder with an error message
            _chatMessages.value = _chatMessages.value.dropLast(1) +
                    placeholderMessage.copy(content = "Sorry, I encountered an error: ${e.localizedMessage}")
        }
        // Ensure loading is false if the stream finishes without content or error
        if (isFirstChunk) { 
            _isLoading.value = false
        }
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

    fun clearPermissionRequest() {
        _requestPermission.value = null
    }

    fun retryLastOperation() {
        lastOperation?.invoke()
        lastOperation = null
    }

    private fun addAssistantMessage(message: String) {
        _chatMessages.value += ChatMessage(message, false)
    }

    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun handleSystemQuery(query: Any) {
        when (query) {
            is PhonePePayment -> {
                PaymentUtils.openPhonePe(
                    context = context,
                    recipientUpiId = query.recipientUpiId,
                    recipientName = query.recipientName,
                    amount = query.amount
                )
                addAssistantMessage("Opening PhonePe to pay ₹${query.amount} to ${query.recipientUpiId}")
            }
            else -> {
                // Handle other query types
            }
        }
    }

    private fun handleCallRequest(phoneNumber: String, displayName: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            CallUtils.makePhoneCall(context, phoneNumber) {
                // Permission callback not needed here as we already have permission
            }
            addAssistantMessage("Calling $displayName")
        } else {
            pendingCall = CallDetails(phoneNumber, displayName)
            _requestPermission.value = "call"
            addAssistantMessage("I need permission to make phone calls. Please grant the permission when prompted.")
        }
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        when (permission) {
            "call" -> {
                if (isGranted) {
                    pendingCall?.let { call ->
                        CallUtils.makePhoneCall(context, call.number) {
                            // Permission callback not needed here as we already have permission
                        }
                        addAssistantMessage("Calling ${call.displayName}")
                    }
                } else {
                    addAssistantMessage("Call permission was denied. I cannot make calls without this permission.")
                }
                pendingCall = null
            }
            // ... handle other permissions ...
        }
        _requestPermission.value = null
    }

    fun onVoiceInput(text: String) {
        viewModelScope.launch {
            _inputText.value = text
            // Auto-send after voice input
            delay(300) // Small delay for UI update
            processCommand(text)
            _inputText.value = "" // Clear input
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun setSelectedImage(uri: Uri?) {
        _selectedImage.value = uri
    }
} 