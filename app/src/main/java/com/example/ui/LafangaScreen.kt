package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.ComposerBar
import com.example.ui.components.CreatorBottomSheet
import com.example.ui.components.EmptyWelcomeView
import com.example.ui.components.LafangaTopBar
import com.example.ui.components.VoiceListeningOverlay
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.SlateContainer
import com.example.ui.theme.SnowText
import com.example.ui.theme.SubtleBorder
import com.example.viewmodel.LafangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LafangaScreen(
    viewModel: LafangaViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var showImageSourceSheet by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.text?.length) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.attachImage(uri)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUriString?.let { uriStr ->
                viewModel.attachImage(Uri.parse(uriStr))
            }
        }
        pendingCameraUriString = null
    }

    val launchCameraCapture = {
        val photoUri = viewModel.createCameraPhotoUri()
        pendingCameraUriString = photoUri.toString()
        takePictureLauncher.launch(photoUri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        } else {
            viewModel.emitSnackbar(context.getString(R.string.camera_permission_denied))
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startHindiVoiceInput()
        } else {
            viewModel.emitSnackbar(context.getString(R.string.mic_permission_denied))
        }
    }

    val handleMicClick = {
        if (uiState.isListening) {
            viewModel.stopListeningAndSend()
        } else {
            val hasAudioPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasAudioPerm) {
                viewModel.startHindiVoiceInput()
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val handleCameraSelect = {
        showImageSourceSheet = false
        val hasCameraPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPerm) {
            launchCameraCapture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val handleGallerySelect = {
        showImageSourceSheet = false
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    BackHandler(enabled = uiState.isListening || showImageSourceSheet || uiState.showCreatorSheet) {
        when {
            uiState.isListening -> viewModel.cancelListening()
            showImageSourceSheet -> showImageSourceSheet = false
            uiState.showCreatorSheet -> viewModel.setCreatorSheetVisible(false)
        }
    }

    Scaffold(
        containerColor = ObsidianVoid,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SlateContainer,
                    contentColor = SnowText,
                    actionColor = SaffronGold
                )
            }
        },
        topBar = {
            LafangaTopBar(
                isListening = uiState.isListening,
                isGenerating = uiState.isGenerating,
                isSpeaking = uiState.speakingMessageId != null,
                isAutoSpeakEnabled = uiState.isAutoSpeakEnabled,
                hasMessages = uiState.messages.isNotEmpty(),
                onToggleAutoSpeak = viewModel::toggleAutoSpeak,
                onStopSpeaking = viewModel::stopSpeaking,
                onOpenCreatorInfo = { viewModel.setCreatorSheetVisible(true) },
                onClearChatClick = { viewModel.setClearDialogVisible(true) }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.widthIn(max = 680.dp)) {
                    VoiceListeningOverlay(
                        visible = uiState.isListening,
                        partialTranscript = uiState.partialTranscript,
                        rmsDb = uiState.micRmsDb,
                        onCancel = viewModel::cancelListening,
                        onDone = viewModel::stopListeningAndSend
                    )
                }

                ComposerBar(
                    inputText = uiState.inputText,
                    pendingImageUri = uiState.pendingImageUri,
                    isGenerating = uiState.isGenerating,
                    isListening = uiState.isListening,
                    onInputChanged = viewModel::onInputTextChanged,
                    onRemovePendingImage = viewModel::clearPendingImage,
                    onPhotoCameraClick = { showImageSourceSheet = true },
                    onMicClick = handleMicClick,
                    onSendClick = { viewModel.sendMessage() },
                    modifier = Modifier.widthIn(max = 680.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ObsidianVoid,
                            MidnightSurface.copy(alpha = 0.7f),
                            ObsidianVoid
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.messages.isEmpty()) {
                EmptyWelcomeView(
                    onQuickPromptClick = viewModel::sendQuickPrompt,
                    onQuickPhotoClick = { showImageSourceSheet = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                        .testTag("chat_messages_list")
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        ChatMessageBubble(
                            message = message,
                            isCurrentlySpeaking = uiState.speakingMessageId == message.id,
                            onSpeakClick = viewModel::speakMessage
                        )
                    }
                }
            }
        }
    }

    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            containerColor = MidnightSurface,
            contentColor = SnowText
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "फ़ोटो या कैमरा चुनें",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SnowText
                )
                Text(
                    text = "कोई भी तस्वीर भेजें — Lafanga AI उसे देखकर हिंदी में समझाएगी।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate
                )
                Spacer(modifier = Modifier.height(4.dp))

                ImageSourceOptionCard(
                    icon = Icons.Rounded.CameraAlt,
                    title = stringResource(R.string.action_camera),
                    subtitle = "कैमरे से तुरंत नई फ़ोटो खींचें",
                    tint = SaffronGold,
                    iconBg = SaffronContainer,
                    testTag = "option_camera",
                    onClick = handleCameraSelect
                )

                ImageSourceOptionCard(
                    icon = Icons.Rounded.PhotoLibrary,
                    title = stringResource(R.string.action_gallery),
                    subtitle = "फ़ोन की गैलरी से फ़ोटो चुनें",
                    tint = ElectricCyan,
                    iconBg = CyanContainer,
                    testTag = "option_gallery",
                    onClick = handleGallerySelect
                )
            }
        }
    }

    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setClearDialogVisible(false) },
            containerColor = SlateContainer,
            titleContentColor = SnowText,
            textContentColor = MutedSlate,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = null,
                    tint = CoralPulse
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.clear_chat_title),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.clear_chat_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::clearChat,
                    colors = ButtonDefaults.textButtonColors(contentColor = CoralPulse),
                    modifier = Modifier.testTag("confirm_clear_chat_button")
                ) {
                    Text(stringResource(R.string.action_clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.setClearDialogVisible(false) },
                    colors = ButtonDefaults.textButtonColors(contentColor = SnowText),
                    modifier = Modifier.testTag("cancel_clear_chat_button")
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (uiState.showCreatorSheet) {
        CreatorBottomSheet(
            onDismiss = { viewModel.setCreatorSheetVisible(false) },
            onSpeakCreatorIntro = viewModel::speakCreatorIntroduction
        )
    }
}

@Composable
private fun ImageSourceOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    iconBg: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SlateContainer,
        border = BorderStroke(1.dp, SubtleBorder),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SnowText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate
                )
            }
        }
    }
}
