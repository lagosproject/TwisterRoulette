package com.lakescorp.twisterroulette.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lakescorp.twisterroulette.R
import com.lakescorp.twisterroulette.domain.model.AppLanguage
import com.lakescorp.twisterroulette.domain.model.AppTheme
import com.lakescorp.twisterroulette.domain.model.ColorSet
import com.lakescorp.twisterroulette.domain.model.TtsPitch
import com.lakescorp.twisterroulette.domain.model.TtsSpeed
import com.lakescorp.twisterroulette.domain.model.TwisterColor
import com.lakescorp.twisterroulette.presentation.main.LanguageFlagShortcut
import com.lakescorp.twisterroulette.presentation.theme.GameBlack
import com.lakescorp.twisterroulette.presentation.theme.GameBlue
import com.lakescorp.twisterroulette.presentation.theme.GameGreen
import com.lakescorp.twisterroulette.presentation.theme.GameOrange
import com.lakescorp.twisterroulette.presentation.theme.GamePurple
import com.lakescorp.twisterroulette.presentation.theme.GameRed
import com.lakescorp.twisterroulette.presentation.theme.GameYellow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var triggerText by remember(settings.triggerWord) { mutableStateOf(settings.triggerWord) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- SECTION: GENERAL SETTINGS ---
        SectionHeader(title = stringResource(R.string.settings_language))
            LanguageFlagShortcut(
                currentLanguage = settings.language,
                onLanguageChange = { viewModel.updateLanguage(it) }
            )

            // --- SECTION: COLOR CONFIGURATION ---
            SectionHeader(title = stringResource(R.string.settings_color_set))
            ColorSetSelector(
                selectedSet = settings.colorSet,
                onSetSelected = { viewModel.updateColorSet(it) }
            )

            Text(
                text = stringResource(R.string.settings_color_selection),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TwisterColor.entries.forEach { color ->
                    val isEnabled = settings.enabledColors.contains(color)
                    val composeColor = when (color) {
                        TwisterColor.RED -> GameRed
                        TwisterColor.BLUE -> GameBlue
                        TwisterColor.YELLOW -> GameYellow
                        TwisterColor.GREEN -> GameGreen
                        TwisterColor.PURPLE -> GamePurple
                        TwisterColor.ORANGE -> GameOrange
                        TwisterColor.BLACK -> GameBlack
                    }

                    FilterChip(
                        selected = isEnabled,
                        onClick = {
                            val success = viewModel.toggleColor(color)
                            if (!success) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.validation_min_colors),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        label = { Text(stringResource(color.nameResId)) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(composeColor)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = composeColor.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                            selectedLeadingIconColor = composeColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isEnabled,
                            selectedBorderColor = composeColor,
                            selectedBorderWidth = 2.dp
                        )
                    )
                }
            }

            // --- SECTION: GAMEPLAY CONFIGURATION ---
            SectionHeader(title = stringResource(R.string.settings_timer_interval))
            TimerIntervalSelector(
                selectedInterval = settings.timerIntervalSecs,
                onIntervalSelected = { viewModel.updateTimerInterval(it) }
            )

            // --- SECTION: SPEECH / VOICE TRIGGER ---
            SectionHeader(title = stringResource(R.string.settings_trigger_word))
            OutlinedTextField(
                value = triggerText,
                onValueChange = {
                    triggerText = it
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.updateTriggerWord(triggerText)
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary
                )
            )

            // --- SECTION: TTS SPEECH SYNTHESIS CONFIG ---
            SectionHeader(title = stringResource(R.string.settings_tts_speed))
            TtsSpeedSelector(
                selectedSpeed = settings.ttsSpeed,
                onSpeedSelected = { viewModel.updateTtsSpeed(it) }
            )

            Text(
                text = stringResource(R.string.settings_tts_pitch),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            TtsPitchSelector(
                selectedPitch = settings.ttsPitch,
                onPitchSelected = { viewModel.updateTtsPitch(it) }
            )

            // --- SECTION: SOUND EFFECTS ---
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
                            painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_volume_2),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.settings_sound_effects),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Switch(
                        checked = settings.soundEffectsEnabled,
                        onCheckedChange = { viewModel.updateSoundEffects(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.secondary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSetSelector(
    selectedSet: ColorSet,
    onSetSelected: (ColorSet) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        ColorSet.entries.forEachIndexed { index, set ->
            val label = when (set) {
                ColorSet.CLASSIC -> stringResource(R.string.color_set_classic)
                ColorSet.EXTENDED -> stringResource(R.string.color_set_extended)
                ColorSet.DARK_EDITION -> stringResource(R.string.color_set_dark)
                ColorSet.CUSTOM -> stringResource(R.string.color_set_custom)
            }
            SegmentedButton(
                selected = selectedSet == set,
                onClick = { onSetSelected(set) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ColorSet.entries.size)
            ) {
                Text(label, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerIntervalSelector(
    selectedInterval: Int,
    onIntervalSelected: (Int) -> Unit
) {
    val intervals = listOf(5, 10, 15, 20, 30, 45, 60)
    
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        intervals.forEach { sec ->
            val isSelected = selectedInterval == sec
            FilterChip(
                selected = isSelected,
                onClick = { onIntervalSelected(sec) },
                label = { Text(stringResource(R.string.seconds_unit, sec)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.onBackground
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = MaterialTheme.colorScheme.secondary,
                    selectedBorderWidth = 2.dp
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsSpeedSelector(
    selectedSpeed: TtsSpeed,
    onSpeedSelected: (TtsSpeed) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        TtsSpeed.entries.forEachIndexed { index, speed ->
            val label = when (speed) {
                TtsSpeed.SLOW -> stringResource(R.string.val_slow)
                TtsSpeed.NORMAL -> stringResource(R.string.val_normal)
                TtsSpeed.FAST -> stringResource(R.string.val_fast)
            }
            SegmentedButton(
                selected = selectedSpeed == speed,
                onClick = { onSpeedSelected(speed) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = TtsSpeed.entries.size)
            ) {
                Text(label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsPitchSelector(
    selectedPitch: TtsPitch,
    onPitchSelected: (TtsPitch) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        TtsPitch.entries.forEachIndexed { index, pitch ->
            val label = when (pitch) {
                TtsPitch.LOW -> stringResource(R.string.val_low)
                TtsPitch.NORMAL -> stringResource(R.string.val_normal)
                TtsPitch.HIGH -> stringResource(R.string.val_high)
            }
            SegmentedButton(
                selected = selectedPitch == pitch,
                onClick = { onPitchSelected(pitch) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = TtsPitch.entries.size)
            ) {
                Text(label)
            }
        }
    }
}
