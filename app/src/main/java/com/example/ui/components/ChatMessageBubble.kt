package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ChatMessage
import com.example.model.MessageRole
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElevatedContainer
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.SlateContainer
import com.example.ui.theme.SnowText
import com.example.ui.theme.SubtleBorder

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isCurrentlySpeaking: Boolean,
    onSpeakClick: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.img_lafanga_avatar),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(SaffronGold, ElectricCyan)),
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        val bubbleShape = if (isUser) {
            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
        } else {
            RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        }

        val containerColor = when {
            message.isError -> CoralContainer
            isUser -> ElevatedContainer
            else -> SlateContainer
        }

        val borderColor = when {
            message.isError -> CoralPulse.copy(alpha = 0.55f)
            isCurrentlySpeaking -> ElectricCyan.copy(alpha = 0.8f)
            isUser -> SaffronGold.copy(alpha = 0.35f)
            else -> SubtleBorder
        }

        Surface(
            shape = bubbleShape,
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = if (isUser) 2.dp else 1.dp,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (message.imageUri != null) {
                    AsyncImage(
                        model = message.imageUri,
                        contentDescription = "भेजी गई फ़ोटो",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!isUser && message.isStreaming && message.text.isBlank()) {
                    ThinkingDotsIndicator()
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (message.isError) SnowText else SnowText
                    )
                }

                if (!isUser && !message.isStreaming && !message.isError && message.text.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        onClick = { onSpeakClick(message) },
                        shape = RoundedCornerShape(50),
                        color = if (isCurrentlySpeaking) CoralContainer else CyanContainer.copy(alpha = 0.7f),
                        border = BorderStroke(
                            1.dp,
                            if (isCurrentlySpeaking) CoralPulse.copy(alpha = 0.6f)
                            else ElectricCyan.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("speak_message_${message.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrentlySpeaking) {
                                    Icons.Rounded.Stop
                                } else {
                                    Icons.AutoMirrored.Rounded.VolumeUp
                                },
                                contentDescription = stringResource(R.string.cd_replay_voice),
                                tint = if (isCurrentlySpeaking) CoralPulse else ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isCurrentlySpeaking) "रोकें" else "आवाज़ में सुनें",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrentlySpeaking) CoralPulse else ElectricCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingDotsIndicator() {
    val transition = rememberInfiniteTransition(label = "thinking_dots")
    val alpha1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "dot1"
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, delayMillis = 150), RepeatMode.Reverse),
        label = "dot2"
    )
    val alpha3 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, delayMillis = 300), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        listOf(alpha1, alpha2, alpha3).forEach { dotAlpha ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(SaffronGold)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.thinking_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MutedSlate
        )
    }
}
