package com.example.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.BuildConfig
import com.example.app.data.ai.FunctionTools
import com.example.app.data.model.User
import com.example.app.data.model.ChatMessage
import com.example.app.data.repository.UserRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.example.app.data.network.TavilyApiService
import com.example.app.data.network.SearchRequest
import com.example.app.data.network.RetrofitClient
import kotlinx.coroutines.flow.asStateFlow
import com.example.app.data.model.SearchSource

class UserViewModel : ViewModel() {
    private val TAG = "UserViewModel"
    private val repository = UserRepository()
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = listOf(
            SafetySetting(harmCategory = HarmCategory.HARASSMENT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.HATE_SPEECH, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.SEXUALLY_EXPLICIT, threshold = BlockThreshold.NONE),
            SafetySetting(harmCategory = HarmCategory.DANGEROUS_CONTENT, threshold = BlockThreshold.NONE)
        )
    )
    private val chat = generativeModel.startChat()
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _response = MutableStateFlow<String>("")
    val response: StateFlow<String> = _response
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val tavilyApiService = RetrofitClient.tavilyRetrofit.create(TavilyApiService::class.java)
    private var isSearchMode = MutableStateFlow(false)
    val searchMode: StateFlow<Boolean> = isSearchMode.asStateFlow()

    // Add this to track animated messages
    private val animatedMessages = mutableSetOf<Long>()

    // Add this state
    private val _searchSources = MutableStateFlow<List<SearchSource>>(emptyList())
    val searchSources: StateFlow<List<SearchSource>> = _searchSources.asStateFlow()

    // Add this function to check and mark messages as animated
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
                _error.value = null
                
                _chatMessages.value = _chatMessages.value + ChatMessage(command, true)

                if (isSearchMode.value) {
                    handleSearchQuery(command)
                } else {
                    val analysisPrompt = """
                        Analyze this query and respond with only one of these categories:
                        LIST_USERS - if asking to see or show all users
                        FIND_USER - if asking about specific user(s) or user details
                        GENERAL_QUERY - for any other topics
                        
                        Query: "$command"
                    """.trimIndent()

                    val response = chat.sendMessage(analysisPrompt).text?.trim() ?: "GENERAL_QUERY"
                    
                    when {
                        response.contains("LIST_USERS", ignoreCase = true) -> handleListUsersQuery()
                        response.contains("FIND_USER", ignoreCase = true) -> handleSpecificUserQuery(command)
                        else -> handleGeneralQuery(command)
                    }
                }

            } catch (e: Exception) {
                addAssistantMessage("Sorry, I encountered an error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun addAssistantMessage(message: String) {
        _chatMessages.value = _chatMessages.value + ChatMessage(message, false)
    }

    private suspend fun handleSpecificUserQuery(command: String) {
        try {
            val result = repository.getUsers(2)
            
            val searchPrompt = """
                System info: ${result.total} total users, page ${result.page}/${result.total_pages}
                Current users:
                ${result.data.joinToString("\n") { 
                    "${it.first_name} ${it.last_name} (${it.email})" 
                }}
                
                Query: "$command"
                Give a natural response about the requested user info.
                If info not found, mention other pages might have it.
            """.trimIndent()

            val searchResponse = chat.sendMessage(searchPrompt)
            addAssistantMessage(searchResponse.text ?: "Sorry, couldn't process query.")

        } catch (e: Exception) {
            addAssistantMessage("Couldn't find user info: ${e.localizedMessage}")
        }
    }

    private suspend fun handleListUsersQuery() {
        try {
            val result = repository.getUsers(2)
            
            val listPrompt = """
                Users (${result.page}/${result.total_pages}):
                ${result.data.joinToString("\n") { 
                    "${it.first_name} ${it.last_name} - ${it.email}" 
                }}
                Present this list naturally.
            """.trimIndent()
            
            val formattedResponse = chat.sendMessage(listPrompt)
            addAssistantMessage(formattedResponse.text ?: "Couldn't format user list.")
            
        } catch (e: Exception) {
            addAssistantMessage("Couldn't fetch users: ${e.localizedMessage}")
        }
    }

    private suspend fun handleGeneralQuery(command: String) {
        try {
            // First analyze if it's an identity question
            val identityAnalysisPrompt = """
                Analyze if this query is asking about my identity, name, creator, origin, or development.
                Return only YES or NO.
                Query: "$command"
            """.trimIndent()
            
            val isIdentityQuestion = chat.sendMessage(identityAnalysisPrompt).text?.trim()?.equals("YES", ignoreCase = true) ?: false
            
            if (isIdentityQuestion) {
                val identityResponse = """
                    Let me introduce myself! 
                    
                    I'm <b>Project I</b>, an AI assistant developed by <b>Somesh Sampat</b> at <b>Innovate Labs</b> in <b>India</b>. 
                    I'm built on top of Google's Gemini model to help users with various tasks and queries.
                    
                    While I leverage Gemini's powerful language understanding capabilities, my interface and specific functionalities 
                    were custom-developed to provide a unique and helpful experience.
                    
                    Is there anything specific you'd like to know about my capabilities?
                """.trimIndent()
                
                addAssistantMessage(identityResponse)
            } else {
                val newChat = generativeModel.startChat()
                val response = newChat.sendMessage(command).text
                addAssistantMessage(response ?: "Couldn't process request.")
            }
        } catch (e: Exception) {
            addAssistantMessage("Couldn't process request: ${e.localizedMessage}")
        }
    }

    private suspend fun handleSearchQuery(query: String) {
        try {
            val searchRequest = SearchRequest(query = query)
            val searchResponse = tavilyApiService.search(searchRequest)
            
            // Update search sources
            _searchSources.value = searchResponse.results.take(3).map { result ->
                SearchSource(
                    title = result.title,
                    url = result.url,
                    content = result.content
                )
            }

            // Updated prompt for more natural responses
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

            val summaryResponse = chat.sendMessage(summaryPrompt)
            addAssistantMessage(summaryResponse.text ?: "Sorry, I couldn't find relevant information.")

        } catch (e: Exception) {
            addAssistantMessage("Error performing search: ${e.localizedMessage}")
            _searchSources.value = emptyList()
        }
    }
} 