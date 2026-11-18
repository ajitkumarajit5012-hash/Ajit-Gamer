package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.SlateContainer
import com.example.ui.theme.SnowText
import com.example.ui.theme.SubtleBorder

@Composable
fun VoiceListeningOverlay(
    visible: Boolean,
    partialTranscript: String,
    rmsDb: Float,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = (rmsDb / 10f).coerceIn(0.12f, 1f),
        animationSpec = spring(),
        label = "mic_rms_pulse"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateContainer,
            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(92.dp)
                ) {
                    Canvas(modifier = Modifier.size(92.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxR = size.minDimension / 2f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    CoralPulse.copy(alpha = 0.35f),
                                    ElectricCyan.copy(alpha = 0.08f)
                                )
                            ),
                            radius = maxR * (0.58f + 0.42f * animatedLevel),
                            center = center
                        )
                        drawCircle(
                            color = SaffronGold.copy(alpha = 0.32f),
                            radius = maxR * (0.48f + 0.28f * animatedLevel),
                            center = center
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = CoralPulse,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = stringResource(R.string.listening_label),
                                tint = SnowText,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (partialTranscript.isBlank()) {
                        stringResource(R.string.listening_label)
                    } else {
                        "“$partialTranscript”"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (partialTranscript.isBlank()) MutedSlate else SnowText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        border = BorderStroke(1.dp, SubtleBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CoralContainer.copy(alpha = 0.45f),
                            contentColor = SnowText
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("cancel_listening_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("रद्द करें")
                    }

                    FilledTonalButton(
                        onClick = onDone,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CyanContainer,
                            contentColor = ElectricCyan
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("done_listening_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("हो गया")
                    }
                }
            }
        }
    }
}
