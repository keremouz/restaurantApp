package com.example.restaurantapp.presentation.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants

private val ChatBotBlue = Color(0xFF2F5BFF)
private val ChatBotBg = Color(0xFFF5F8FF)
private val ChatBotText = Color(0xFF162033)
private val ChatBotUserBubble = Color(0xFF2F5BFF)
private val ChatBotBotBubble = Color.White
private val ChatBotBorder = Color(0xFFD9E4FF)
private val ChatBotHint = Color(0xFF8A97B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    onBackClick: () -> Unit
) {
    val viewModel: ChatBotViewModel = viewModel(
        factory = ChatBotViewModelFactory()
    )

    val uiState by viewModel.uiState.collectAsState()

    val welcomeMessage = stringResource(R.string.chatbot_welcome_message)

    LaunchedEffect(Unit) {
        viewModel.addWelcomeMessageIfNeeded(welcomeMessage)
    }

    Scaffold(
        containerColor = ChatBotBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chatbot_title),
                        color = ChatBotText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = null,
                            tint = ChatBotBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChatBotBg
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = uiState.inputText,
                isLoading = uiState.isLoading,
                onInputChanged = viewModel::updateInputText,
                onSendClick = {
                    viewModel.sendMessage()
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = UiConstants.ScreenPadding),
            contentPadding = PaddingValues(
                top = UiConstants.ContentSpacing,
                bottom = UiConstants.ContentSpacing
            ),
            verticalArrangement = Arrangement.spacedBy(UiConstants.MediumSpacing)
        ) {
            items(uiState.messages) { message ->
                ChatMessageBubble(message = message)
            }
            if (uiState.isLoading) {
            item {
                ChatMessageBubble(
                    message = ChatMessage(
                        text = stringResource(R.string.chatbot_typing),
                        isFromUser = false
                    )
                )
            }
        }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage
) {
    val horizontalAlignment = if (message.isFromUser) {
        Alignment.End
    } else {
        Alignment.Start
    }

    val bubbleColor = if (message.isFromUser) {
        ChatBotUserBubble
    } else {
        ChatBotBotBubble
    }

    val textColor = if (message.isFromUser) {
        Color.White
    } else {
        ChatBotText
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.82f),
            shape = RoundedCornerShape(UiConstants.CardRadius),
            colors = CardDefaults.cardColors(
                containerColor = bubbleColor
            ),
            border = if (message.isFromUser) {
                null
            } else {
                androidx.compose.foundation.BorderStroke(
                    width = UiConstants.ReviewCardBorderWidth,
                    color = ChatBotBorder
                )
            },
            elevation = CardDefaults.cardElevation(
                defaultElevation = UiConstants.FavoriteCardElevation
            )
        ) {
            Row(
                modifier = Modifier.padding(UiConstants.ContentSpacing),
                horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing),
                verticalAlignment = Alignment.Top
            ) {
                if (!message.isFromUser) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ChatBotBlue
                    )
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}
@Composable
private fun ChatInputBar(
    inputText: String,
    isLoading: Boolean,
    onInputChanged: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatBotBg)
            .imePadding()
            .padding(UiConstants.ScreenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(R.string.chatbot_input_hint),
                        color = ChatBotHint
                    )
                },
                shape = RoundedCornerShape(UiConstants.TextFieldRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChatBotBlue,
                    unfocusedBorderColor = ChatBotBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = ChatBotBlue
                )
            )

            IconButton(
                onClick = onSendClick,
                enabled = inputText.isNotBlank() && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = stringResource(R.string.chatbot_send),
                    tint = if (inputText.isNotBlank() && !isLoading) {
                        ChatBotBlue
                    } else {
                        ChatBotHint
                    }
                )
            }
        }
    }
}