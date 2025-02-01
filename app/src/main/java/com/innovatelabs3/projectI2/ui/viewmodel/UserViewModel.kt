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

class UserViewModel : ViewModel() {
    private val TAG = "UserViewModel"
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
    private val chat = generativeModel.startChat()
    
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
                    handleGeneralQuery(command)
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

    private suspend fun handleGeneralQuery(command: String) {
        try {
            val identityAnalysisPrompt = """
                Analyze if this is a question about your identity (who you are, who made you, etc).
                Respond with only YES or NO:
                "$command"
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

                    val summaryResponse = chat.sendMessage(summaryPrompt)
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
} 