@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.lakescorp.twisterroulette.presentation.previews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lakescorp.twisterroulette.R
import com.lakescorp.twisterroulette.domain.model.AppSettings
import com.lakescorp.twisterroulette.domain.model.AppTheme
import com.lakescorp.twisterroulette.domain.model.BodyPart
import com.lakescorp.twisterroulette.domain.model.ColorSet
import com.lakescorp.twisterroulette.domain.model.GameModeType
import com.lakescorp.twisterroulette.domain.model.GameResult
import com.lakescorp.twisterroulette.domain.model.Turn
import com.lakescorp.twisterroulette.domain.model.TwisterColor
import com.lakescorp.twisterroulette.presentation.main.HistoryEntry
import com.lakescorp.twisterroulette.presentation.main.PlayMode
import com.lakescorp.twisterroulette.presentation.main.RecentMovesList
import com.lakescorp.twisterroulette.presentation.main.ResultCard
import com.lakescorp.twisterroulette.presentation.main.VoiceSoundwave
import com.lakescorp.twisterroulette.presentation.main.VoiceStatus
import com.lakescorp.twisterroulette.presentation.settings.ColorSetSelector
import com.lakescorp.twisterroulette.presentation.settings.SectionHeader
import com.lakescorp.twisterroulette.presentation.settings.TimerIntervalSelector
import com.lakescorp.twisterroulette.presentation.settings.TtsPitchSelector
import com.lakescorp.twisterroulette.presentation.settings.TtsSpeedSelector
import com.lakescorp.twisterroulette.presentation.theme.DeepViolet
import com.lakescorp.twisterroulette.presentation.theme.TwisterRouletteTheme
import com.lakescorp.twisterroulette.service.speech.SpeechState


// ── Shared scaffold with bottom nav ──────────────────────────────────────────

@Composable
private fun PreviewScaffold(
    selectedTab: Int = 0,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0, onClick = {},
                    icon = { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_play), contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_play)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1, onClick = {},
                    icon = { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_dices), contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_modes)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2, onClick = {},
                    icon = { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_settings_2), contentDescription = null) },
                    label = { Text(stringResource(R.string.settings)) }
                )
            }
        },
        content = content
    )
}

// ── Play-mode segmented selector ─────────────────────────────────────────────

@Composable
private fun PlayModeSelector(active: PlayMode) {
    com.lakescorp.twisterroulette.presentation.main.PlayModeSelector(
        activeMode = active,
        onModeSelected = {}
    )
}

// ── Inline mode card (mirrors private ModeCard in ModesScreen) ───────────────

@Composable
private fun PreviewModeCard(title: String, description: String, selected: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

// ── Main column content helper ────────────────────────────────────────────────

@Composable
private fun MainContent(
    padding: PaddingValues,
    activeMode: PlayMode,
    turn: Turn?,
    history: List<HistoryEntry>,
    borderProgress: Float? = null,
    dimmed: Boolean = false,
    voiceListening: Boolean = false
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))
        PlayModeSelector(active = activeMode)
        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            if (activeMode == PlayMode.VOICE && voiceListening) {
                VoiceSoundwave()
            }
            ResultCard(
                turn = turn,
                onClick = {},
                audioContext = context,
                borderProgress = borderProgress,
                dimmed = dimmed
            )
        }

        Spacer(Modifier.height(16.dp))

        when (activeMode) {
            PlayMode.VOICE -> VoiceStatus(
                speechState = if (voiceListening) SpeechState.LISTENING else SpeechState.IDLE,
                triggerWord = "Twister"
            )
            PlayMode.MANUAL -> {
                if (history.isEmpty()) {
                    Text(
                        text = stringResource(R.string.main_manual_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
            else -> Unit
        }

        RecentMovesList(
            history = history,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

// ── Public preview scenes ─────────────────────────────────────────────────────

/** Play tab · Manual mode · no result yet */
@Composable
fun PreviewScene_MainIdle() {
    TwisterRouletteTheme(theme = AppTheme.DARK) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            PreviewScaffold(selectedTab = 0) { padding ->
                MainContent(
                    padding = padding,
                    activeMode = PlayMode.MANUAL,
                    turn = null,
                    history = emptyList()
                )
            }
        }
    }
}

/** Play tab · Manual mode · Red Right-Hand result + 3 history rows */
@Composable
fun PreviewScene_MainResult_Red() {
    val turn = Turn(moves = listOf(GameResult(BodyPart.RIGHT_HAND, TwisterColor.RED)))
    val history = listOf(
        HistoryEntry(GameResult(BodyPart.RIGHT_HAND, TwisterColor.RED),    3),
        HistoryEntry(GameResult(BodyPart.LEFT_FOOT,  TwisterColor.BLUE),   2),
        HistoryEntry(GameResult(BodyPart.RIGHT_FOOT, TwisterColor.GREEN),  1)
    )
    TwisterRouletteTheme(theme = AppTheme.DARK) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            PreviewScaffold(selectedTab = 0) { padding ->
                MainContent(padding, PlayMode.MANUAL, turn, history)
            }
        }
    }
}

/** Play tab · Manual mode · Yellow Left-Foot result + fuller history */
@Composable
fun PreviewScene_MainResult_Yellow() {
    val turn = Turn(moves = listOf(GameResult(BodyPart.LEFT_FOOT, TwisterColor.YELLOW)))
    val history = listOf(
        HistoryEntry(GameResult(BodyPart.LEFT_FOOT,   TwisterColor.YELLOW), 5),
        HistoryEntry(GameResult(BodyPart.RIGHT_HAND,  TwisterColor.RED),    4),
        HistoryEntry(GameResult(BodyPart.LEFT_HAND,   TwisterColor.PURPLE), 3),
        HistoryEntry(GameResult(BodyPart.RIGHT_FOOT,  TwisterColor.BLUE),   2),
        HistoryEntry(GameResult(BodyPart.LEFT_FOOT,   TwisterColor.GREEN),  1)
    )
    TwisterRouletteTheme(theme = AppTheme.DARK) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            PreviewScaffold(selectedTab = 0) { padding ->
                MainContent(padding, PlayMode.MANUAL, turn, history)
            }
        }
    }
}

/** Play tab · Timer mode · Blue Right-Foot result · countdown at 72 % */
@Composable
fun PreviewScene_MainTimer_Active() {
    val turn = Turn(moves = listOf(GameResult(BodyPart.RIGHT_FOOT, TwisterColor.BLUE)))
    TwisterRouletteTheme(theme = AppTheme.DARK) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            PreviewScaffold(selectedTab = 0) { padding ->
                MainContent(
                    padding = padding,
                    activeMode = PlayMode.TIMER,
                    turn = turn,
                    history = emptyList(),
                    borderProgress = 0.72f
                )
            }
        }
    }
}

/** Play tab · Voice mode · mic active · soundwave ripples · Purple Left-Hand result */
@Composable
fun PreviewScene_MainVoice_Listening() {
    val turn = Turn(moves = listOf(GameResult(BodyPart.LEFT_HAND, TwisterColor.PURPLE)))
    TwisterRouletteTheme(theme = AppTheme.DARK) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            PreviewScaffold(selectedTab = 0) { padding ->
                MainContent(
                    padding = padding,
                    activeMode = PlayMode.VOICE,
                    turn = turn,
                    history = emptyList(),
                    borderProgress = 1f,
                    voiceListening = true
                )
            }
        }
    }
}

/** Modes tab · Classic mode selected */
@Composable
fun PreviewScene_Modes_Classic() {
    TwisterRouletteTheme(theme = AppTheme.DARK) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            PreviewScaffold(selectedTab = 1) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GameModeType.entries.forEach { mode ->
                        PreviewModeCard(
                            title = stringResource(mode.nameResId),
                            description = stringResource(mode.descResId),
                            selected = mode == GameModeType.CLASSIC
                        )
                    }
                }
            }
        }
    }
}

/** Settings tab */
@Composable
fun PreviewScene_Settings() {
    val defaults = AppSettings()
    TwisterRouletteTheme(theme = AppTheme.DARK) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            PreviewScaffold(selectedTab = 2) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val currentLang = LocalContext.current.resources.configuration.locales[0].language
                    SectionHeader(title = stringResource(R.string.settings_language))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "en" to "🇬🇧",
                            "es" to "🇪🇸",
                            "fr" to "🇫🇷",
                            "pt" to "🇵🇹",
                            "hi" to "🇮🇳",
                            "zh" to "🇨🇳"
                        ).forEach { (langCode, flag) ->
                            val selected = currentLang.startsWith(langCode)
                            Text(
                                text = flag,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f) else Color.Transparent)
                                    .padding(6.dp)
                            )
                        }
                    }

                    SectionHeader(title = stringResource(R.string.settings_color_set))
                    ColorSetSelector(selectedSet = ColorSet.CLASSIC, onSetSelected = {})

                    SectionHeader(title = stringResource(R.string.settings_timer_interval))
                    TimerIntervalSelector(selectedInterval = defaults.timerIntervalSecs, onIntervalSelected = {})

                    SectionHeader(title = stringResource(R.string.settings_trigger_word))
                    OutlinedTextField(
                        value = "Twister",
                        onValueChange = {},
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary
                        )
                    )

                    SectionHeader(title = stringResource(R.string.settings_tts_speed))
                    TtsSpeedSelector(selectedSpeed = defaults.ttsSpeed, onSpeedSelected = {})

                    Text(
                        text = stringResource(R.string.settings_tts_pitch),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TtsPitchSelector(selectedPitch = defaults.ttsPitch, onPitchSelected = {})

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_volume_2),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.settings_sound_effects),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Switch(
                                checked = true,
                                onCheckedChange = {},
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                    checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
