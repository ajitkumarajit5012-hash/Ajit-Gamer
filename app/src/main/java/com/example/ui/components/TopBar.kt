package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.SnowText
import com.example.ui.theme.SubtleBorder

@Composable
fun LafangaTopBar(
    isListening: Boolean,
    isGenerating: Boolean,
    isSpeaking: Boolean,
    isAutoSpeakEnabled: Boolean,
    hasMessages: Boolean,
    onToggleAutoSpeak: () -> Unit,
    onStopSpeaking: () -> Unit,
    onOpenCreatorInfo: () -> Unit,
    onClearChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        isListening -> stringResource(R.string.listening_label)
        isSpeaking -> stringResource(R.string.speaking_label)
        isGenerating -> stringResource(R.string.thinking_label)
        isAutoSpeakEnabled -> "हिंदी महिला आवाज़ • चालू"
        else -> "हिंदी वॉइस • म्यूट"
    }

    val statusColor by animateColorAsState(
        targetValue = when {
            isListening -> CoralPulse
            isSpeaking -> ElectricCyan
            isGenerating -> SaffronGold
            isAutoSpeakEnabled -> ElectricCyan
            else -> MutedSlate
        },
        label = "status_color"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MidnightSurface.copy(alpha = 0.94f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenCreatorInfo)
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .testTag("top_bar_brand")
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    listOf(SaffronGold, ElectricCyan)
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lafanga_avatar),
                            contentDescription = stringResource(R.string.app_name),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = SnowText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (isSpeaking) {
                        IconButton(
                            onClick = onStopSpeaking,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.StopCircle,
                                contentDescription = stringResource(R.string.cd_stop_speaking),
                                tint = CoralPulse
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onToggleAutoSpeak,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("voice_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isAutoSpeakEnabled) {
                                    Icons.AutoMirrored.Rounded.VolumeUp
                                } else {
                                    Icons.AutoMirrored.Rounded.VolumeOff
                                },
                                contentDescription = stringResource(R.string.cd_voice_toggle),
                                tint = if (isAutoSpeakEnabled) ElectricCyan else MutedSlate
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenCreatorInfo,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("creator_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.cd_creator_info),
                            tint = SaffronGold
                        )
                    }

                    IconButton(
                        onClick = onClearChatClick,
                        enabled = hasMessages,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = stringResource(R.string.cd_clear_chat),
                            tint = if (hasMessages) SnowText else MutedSlate.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }
    androidx.compose.material3.HorizontalDivider(
        color = SubtleBorder.copy(alpha = 0.6f),
        thickness = 1.dp
    )
}
