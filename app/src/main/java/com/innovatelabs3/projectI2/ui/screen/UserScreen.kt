package com.innovatelabs3.projectI2.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.innovatelabs3.projectI2.data.model.ChatMessage
import com.innovatelabs3.projectI2.ui.viewmodel.UserViewModel
import com.innovatelabs3.projectI2.util.TextFormatter
import com.innovatelabs3.projectI2.ui.components.CodeBlock
import com.innovatelabs3.projectI2.ui.components.ShimmerEffect
import com.innovatelabs3.projectI2.ui.theme.Background
import com.innovatelabs3.projectI2.ui.theme.Primary
import com.innovatelabs3.projectI2.ui.theme.TextPrimary
import com.innovatelabs3.projectI2.ui.theme.TextSecondary
import com.innovatelabs3.projectI2.ui.theme.UserMessageBg
import com.innovatelabs3.projectI2.ui.theme.BotMessageBg
import com.innovatelabs3.projectI2.ui.theme.InputBg
import androidx.compose.ui.graphics.Color
import com.innovatelabs3.projectI2.ui.theme.OpenSansFont
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import com.innovatelabs3.projectI2.ui.components.GlobeIcon
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.graphicsLayer
import com.innovatelabs3.projectI2.data.model.SearchSource
import com.innovatelabs3.projectI2.domain.QueryType
import com.innovatelabs3.projectI2.ui.components.FileSearchResults
import com.innovatelabs3.projectI2.ui.components.SearchSourcesRow
import com.innovatelabs3.projectI2.util.WelcomePrompts
import com.innovatelabs3.projectI2.ui.components.SuggestionsCard
import com.innovatelabs3.projectI2.ui.components.TypewriterText
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import android.speech.RecognizerIntent
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource
import com.innovatelabs3.projectI2.R
import android.speech.SpeechRecognizer
import android.os.Bundle
import android.speech.RecognitionListener
import androidx.activity.compose.rememberLauncherForActivityResult
import com.innovatelabs3.projectI2.utils.GenericUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

@Composable
fun UserScreen(viewModel: UserViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading by viewModel.isLoading.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val listState = rememberLazyListState()
    val isSearchMode by viewModel.searchMode.collectAsState()
    val searchSources by viewModel.searchSources.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val placeholderText by remember { mutableStateOf(WelcomePrompts.getRandomPrompt()) }
    val lastQueryType by viewModel.lastQueryType.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val context = LocalContext.current
    val isListening = remember { mutableStateOf(false) }
    
    // Check if speech recognition is available
    val isSpeechAvailable = remember {
        SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    val speechRecognizer = remember { 
        if (isSpeechAvailable) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    // Setup speech recognizer
    DisposableEffect(speechRecognizer) {
        val listener = object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    viewModel.updateInputText(matches[0])
                    // Auto send after voice input
                    viewModel.processCommand(matches[0])
                }
                isListening.value = false
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission needed"
                    else -> "Error occurred"
                }
                GenericUtils.showToast(context, errorMessage)
                isListening.value = false
            }

            override fun onReadyForSpeech(params: Bundle?) {
                GenericUtils.showToast(context, "Listening...")
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    viewModel.updateInputText(matches[0])
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer?.setRecognitionListener(listener)

        onDispose {
            speechRecognizer?.destroy()
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val selectedImage by viewModel.selectedImage.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setSelectedImage(uri)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = Background,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 8.dp,top = 12.dp, bottom = 12.dp, end = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Web search toggle button
                    IconButton(
                        onClick = { viewModel.toggleSearchMode() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (isSearchMode) Primary.copy(alpha = 0.1f) else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        GlobeIcon(
                            modifier = Modifier.size(24.dp),
                            color = if (isSearchMode) Primary else TextSecondary
                        )
                    }

                    // New Image Picker Button
                    IconButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (selectedImage != null) Primary.copy(alpha = 0.1f) else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_image_placeholder),
                            contentDescription = "Add Image",
                            modifier = Modifier.size(24.dp),
                            tint = if (selectedImage != null) Primary else TextSecondary
                        )
                    }

                    // Input TextField with image preview
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { viewModel.updateInputText(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 40.dp, max = 120.dp),
                            placeholder = {
                                Text(
                                    text = when {
                                        selectedImage != null -> "Ask.."
                                        isSearchMode -> "Search on web..."
                                        else -> placeholderText
                                    },
                                    color = TextSecondary.copy(alpha = 0.6f)
                                )
                            },
                            trailingIcon = {
                                // Image Preview if selected
                                selectedImage?.let { uri ->
                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp)  // Small padding inside input box
                                            .size(56.dp)    // Slightly smaller to fit nicely
                                            .clip(RoundedCornerShape(6.dp))
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = uri,
                                                placeholder = painterResource(id = R.drawable.ic_image_placeholder)
                                            ),
                                            contentDescription = "Selected Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        
                                        // Close/Remove image button
                                        IconButton(
                                            onClick = { viewModel.setSelectedImage(null) },
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.TopEnd)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_close),
                                                contentDescription = "Remove Image",
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Primary,
                                focusedContainerColor = InputBg,
                                unfocusedContainerColor = InputBg
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = OpenSansFont,
                                color = Color.Black
                            ),
                            maxLines = 5,
                        )
                    }

                    // Send button with keyboard handling
                    IconButton(
                        onClick = {
                            if (inputText.isNotEmpty()) {
                                viewModel.processCommand(inputText)
                                viewModel.updateInputText("")
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .padding(start = 16.dp)
                            .background(
                                color = if (inputText.isNotEmpty()) Primary else Primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp),
                            tint = if (inputText.isNotEmpty()) Color.White else Primary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (chatMessages.isEmpty()) {
                // Show suggestions when no messages
                SuggestionsCard(
                    onSuggestionClick = { prompt ->
                        viewModel.updateInputText(prompt)
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Existing LazyColumn with messages
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(chatMessages.size) { index ->
                        val message = chatMessages[index]
                        val isLastMessage = index == chatMessages.lastIndex

                        // Hide the last assistant message placeholder while loading the first chunk
                        val shouldShowMessage = !(isLastMessage && !message.isUser && isLoading)

                        if (shouldShowMessage) {
                            Column {
                                ChatMessageItem(message, viewModel)
                                
                                // Show search results after assistant's message
                                if (!message.isUser && 
                                    lastQueryType == QueryType.SearchFiles && 
                                    searchResults.isNotEmpty() && 
                                    isLastMessage) {
                                    FileSearchResults(results = searchResults)
                                }
                                
                                // Show web search results if in search mode
                                if (!message.isUser && 
                                    isSearchMode && 
                                    searchSources.isNotEmpty() && 
                                    isLastMessage) {
                                    SearchSourcesRow(sources = searchSources)
                                }
                            }
                        }
                    }

                    item {
                        if (isLoading) {
                            ShimmerEffect(
                                isWebSearch = isSearchMode,
                                queryType = lastQueryType
                            )
                        }
                    }
                }
            }

            // Voice command FAB
            FloatingActionButton(
                onClick = {
                    if (!isListening.value && isSpeechAvailable && speechRecognizer != null) {
                        try {
                            isListening.value = true
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                            }
                            speechRecognizer.startListening(intent)
                        } catch (e: Exception) {
                            isListening.value = false
                            GenericUtils.showToast(context, "Error: ${e.localizedMessage}")
                        }
                    } else if (!isSpeechAvailable) {
                        GenericUtils.showToast(context, "Speech recognition not available on this device")
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 140.dp, end = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mic),
                    contentDescription = "Voice Command",
                    tint = if (isListening.value) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    viewModel: UserViewModel,
    showSources: Boolean = false,
    sources: List<SearchSource> = emptyList()
) {
    val shouldAnimate = remember(message.timestamp) {
        viewModel.shouldAnimateMessage(message.timestamp)
    }
    val enterTransition = remember {
        fadeIn(
            animationSpec = tween(
                durationMillis = 200,
                easing = LinearEasing
            )
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = 200,
                easing = LinearEasing
            ),
            expandFrom = Alignment.Top
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = if (message.isUser) "You" else "Project I",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = OpenSansFont,
                letterSpacing = 0.4.sp
            ),
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (!message.isUser && showSources) {
            SearchSourcesRow(
                sources = sources,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .then(
                    if (message.isUser) {
                        Modifier.wrapContentWidth()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 32.dp)
                    }
                )
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isUser) 16.dp else 4.dp,
                    topEnd = if (message.isUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (message.isUser) UserMessageBg else BotMessageBg,
                modifier = Modifier
                    .then(
                        if (message.isUser) {
                            Modifier.wrapContentWidth()
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )
                    .graphicsLayer {
                        shadowElevation = 8f
                        shape = RoundedCornerShape(
                            topStart = if (message.isUser) 16.dp else 4.dp,
                            topEnd = if (message.isUser) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                        clip = true
                    }
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                        .then(
                            if (message.isUser) {
                                Modifier.wrapContentWidth()
                            } else {
                                Modifier.fillMaxWidth()
                            }
                        )
                ) {
                    // Show image if present
                    message.imageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    if (message.isUser) {
                        MessageContent(message)
                    } else {
                        val segments = TextFormatter.formatText(message.content)
                        Column {
                            segments.forEach { segment ->
                                if (segment.isCodeBlock) {
                                    CodeBlock(
                                        code = segment.text,
                                        language = segment.language,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth()
                                    )
                                } else {
                                    TypewriterText(
                                        text = segment.text,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = OpenSansFont,
                                            lineHeight = 24.sp,
                                            letterSpacing = 0.2.sp,
                                            color = Color.Black
                                        ),
                                        shouldAnimate = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageContent(message: ChatMessage) {
    val segments = TextFormatter.formatText(message.content)
    Column(
        modifier = if (message.isUser) {
            Modifier.wrapContentWidth()
        } else {
            Modifier.fillMaxWidth()
        }
    ) {
        segments.forEach { segment ->
            if (segment.isCodeBlock) {
                CodeBlock(
                    code = segment.text,
                    language = segment.language,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                )
            } else {
                Text(
                    text = segment.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = OpenSansFont,
                        lineHeight = 24.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = if (message.isUser) Color.White else TextPrimary,
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
    }
}