package com.innovatelabs3.projectI2.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.graphicsLayer
import com.innovatelabs3.projectI2.data.model.SearchSource
import com.innovatelabs3.projectI2.ui.components.SearchSourcesRow
import com.innovatelabs3.projectI2.util.WelcomePrompts
import com.innovatelabs3.projectI2.ui.components.SuggestionsCard
import com.innovatelabs3.projectI2.ui.components.TypewriterText

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserScreen(viewModel: UserViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading by viewModel.isLoading.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    var command by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isSearchMode by viewModel.searchMode.collectAsState()
    val searchSources by viewModel.searchSources.collectAsState()
    val placeholderText by remember {
        mutableStateOf(WelcomePrompts.getRandomPrompt())
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
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
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            modifier = Modifier.size(22.dp),
                            color = if (isSearchMode) Primary else TextSecondary
                        )
                    }

                    // Input TextField
                    OutlinedTextField(
                        value = command,
                        onValueChange = { command = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp, max = 120.dp),
                        placeholder = {
                            Text(
                                if (isSearchMode) "Search the web..." else placeholderText,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
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

                    // Send button with keyboard handling
                    IconButton(
                        onClick = {
                            if (command.isNotBlank()) {
                                viewModel.processCommand(command)
                                command = ""
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (command.isNotBlank()) Primary else Primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp),
                            tint = if (command.isNotBlank()) Color.White else Primary
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
                        command = prompt
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
                    items(chatMessages.chunked(2)) { messagePair ->
                        // User message
                        messagePair.firstOrNull()?.let { userMessage ->
                            ChatMessageItem(
                                message = userMessage,
                                viewModel = viewModel
                            )
                        }

                        // Assistant message with sources if available
                        messagePair.getOrNull(1)?.let { assistantMessage ->
                            ChatMessageItem(
                                message = assistantMessage,
                                viewModel = viewModel,
                                showSources = searchSources.isNotEmpty() && messagePair.first().content == chatMessages[chatMessages.lastIndex - 1].content,
                                sources = searchSources
                            )
                        }
                    }

                    item {
                        if (isLoading) {
                            ShimmerEffect(isWebSearch = isSearchMode)
                        }
                    }
                }
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
                    .animateContentSize(),
                shadowElevation = 6.dp,
                tonalElevation = 2.dp
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
                    if (message.isUser) {
                        MessageContent(message)
                    } else {
                        val segments = TextFormatter.formatText(message.content)
                        AnimatedVisibility(
                            visible = true,
                            enter = enterTransition
                        ) {
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
                                            modifier = Modifier.animateContentSize(),
                                            shouldAnimate = shouldAnimate
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