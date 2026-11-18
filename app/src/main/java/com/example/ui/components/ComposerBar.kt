package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElevatedContainer
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.SlateContainer
import com.example.ui.theme.SnowText
import com.example.ui.theme.SubtleBorder

@Composable
fun ComposerBar(
    inputText: String,
    pendingImageUri: Uri?,
    isGenerating: Boolean,
    isListening: Boolean,
    onInputChanged: (String) -> Unit,
    onRemovePendingImage: () -> Unit,
    onPhotoCameraClick: () -> Unit,
    onMicClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSend = (inputText.isNotBlank() || pendingImageUri != null) && !isGenerating

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MidnightSurface.copy(alpha = 0.96f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            AnimatedVisibility(visible = pendingImageUri != null) {
                if (pendingImageUri != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SlateContainer,
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = pendingImageUri,
                                contentDescription = "चुनी हुई फ़ोटो",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "फ़ोटो विश्लेषण के लिए तैयार है",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SnowText
                                )
                                Text(
                                    text = "बोलें या लिखें, या सीधे भेजें दबाएं",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan
                                )
                            }
                            IconButton(
                                onClick = onRemovePendingImage,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("remove_pending_image_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.cd_remove_image),
                                    tint = MutedSlate
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onPhotoCameraClick,
                    shape = CircleShape,
                    color = ElevatedContainer,
                    border = BorderStroke(1.dp, SubtleBorder),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("photo_camera_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AddAPhoto,
                            contentDescription = stringResource(R.string.cd_attach_photo),
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = SlateContainer,
                    border = BorderStroke(1.dp, SubtleBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 6.dp, end = 4.dp)
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = onInputChanged,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.input_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSlate
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = SnowText),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = { if (canSend) onSendClick() }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = SaffronGold
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field")
                        )

                        if (canSend) {
                            IconButton(
                                onClick = onSendClick,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = stringResource(R.string.cd_send_button),
                                    tint = SaffronGold
                                )
                            }
                        }
                    }
                }

                Surface(
                    onClick = onMicClick,
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            brush = if (isListening) {
                                Brush.linearGradient(listOf(CoralPulse, SaffronGold))
                            } else {
                                Brush.linearGradient(listOf(SaffronGold, ElectricCyan))
                            }
                        )
                        .testTag("mic_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = if (isListening) {
                                stringResource(R.string.cd_stop_mic)
                            } else {
                                stringResource(R.string.cd_mic_button)
                            },
                            tint = ObsidianVoid,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
