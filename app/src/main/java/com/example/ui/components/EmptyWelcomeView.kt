package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PersonPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.SaffronSoft
import com.example.ui.theme.SlateContainer
import com.example.ui.theme.SnowText
import com.example.ui.theme.SubtleBorder

private data class QuickStarterItem(
    val title: String,
    val prompt: String,
    val icon: ImageVector,
    val isPhotoAction: Boolean = false,
    val tag: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmptyWelcomeView(
    onQuickPromptClick: (String) -> Unit,
    onQuickPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val starters = listOf(
        QuickStarterItem(
            title = "तुम्हें किसने बनाया है?",
            prompt = "तुम्हें किसने बनाया है और तुम्हारा मालिक कहाँ से है?",
            icon = Icons.Rounded.PersonPin,
            tag = "quick_prompt_creator"
        ),
        QuickStarterItem(
            title = "आज का कोई रोचक तथ्य सुनाओ",
            prompt = "मुझे आज का कोई मज़ेदार और अनोखा ज्ञानवर्धक तथ्य हिंदी में सुनाओ।",
            icon = Icons.Rounded.AutoAwesome,
            tag = "quick_prompt_fact"
        ),
        QuickStarterItem(
            title = "बिहार की खासियत बताओ",
            prompt = "बिहार और सहरसा की संस्कृति और खास बातों के बारे में संक्षेप में बताओ।",
            icon = Icons.Rounded.Explore,
            tag = "quick_prompt_bihar"
        ),
        QuickStarterItem(
            title = "मेरी फ़ोटो देखकर बताओ",
            prompt = "",
            icon = Icons.Rounded.CameraAlt,
            isPhotoAction = true,
            tag = "quick_prompt_photo"
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(148.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SaffronGold.copy(alpha = 0.25f),
                                ElectricCyan.copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.img_lafanga_avatar),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(SaffronGold, ElectricCyan)),
                        shape = CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SnowText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MutedSlate,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 420.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CapabilityBadge(
                icon = Icons.Rounded.Mic,
                label = "हिंदी वॉइस इनपुट",
                tint = SaffronSoft,
                container = SaffronContainer
            )
            CapabilityBadge(
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                label = "महिला आवाज़ जवाब",
                tint = ElectricCyan,
                container = CyanContainer
            )
            CapabilityBadge(
                icon = Icons.Rounded.CameraAlt,
                label = "फ़ोटो / कैमरा AI",
                tint = SaffronGold,
                container = SlateContainer
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        Column(
            modifier = Modifier.widthIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            starters.forEach { item ->
                Surface(
                    onClick = {
                        if (item.isPhotoAction) onQuickPhotoClick()
                        else onQuickPromptClick(item.prompt)
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = SlateContainer,
                    border = BorderStroke(1.dp, SubtleBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag(item.tag)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (item.isPhotoAction) CyanContainer else SaffronContainer
                                )
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (item.isPhotoAction) ElectricCyan else SaffronGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = SnowText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.creator_tagline),
            style = MaterialTheme.typography.labelSmall,
            color = MutedSlate.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CapabilityBadge(
    icon: ImageVector,
    label: String,
    tint: Color,
    container: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = container,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SnowText
            )
        }
    }
}
