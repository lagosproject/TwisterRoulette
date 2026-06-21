package com.lakescorp.twisterroulette.presentation.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lakescorp.twisterroulette.R
import com.lakescorp.twisterroulette.domain.model.AppLanguage
import com.lakescorp.twisterroulette.domain.model.BodyPart
import com.lakescorp.twisterroulette.domain.model.GameResult
import com.lakescorp.twisterroulette.domain.model.Turn
import com.lakescorp.twisterroulette.domain.model.TurnVerb
import com.lakescorp.twisterroulette.domain.model.TwisterColor
import com.lakescorp.twisterroulette.presentation.theme.DeepViolet
import com.lakescorp.twisterroulette.presentation.theme.GameBlack
import com.lakescorp.twisterroulette.presentation.theme.GameBlue
import com.lakescorp.twisterroulette.presentation.theme.GameGreen
import com.lakescorp.twisterroulette.presentation.theme.GameOrange
import com.lakescorp.twisterroulette.presentation.theme.GamePurple
import com.lakescorp.twisterroulette.presentation.theme.GameRed
import com.lakescorp.twisterroulette.presentation.theme.GameYellow
import com.lakescorp.twisterroulette.service.speech.SpeechState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val currentTurn by viewModel.currentTurn.collectAsState()
    val modeMessage by viewModel.modeMessage.collectAsState()
    val activeMode by viewModel.activeMode.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val timerProgress by viewModel.timerProgress.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val showMicRationale by viewModel.showMicrophoneRationale.collectAsState()
    val history by viewModel.history.collectAsState()

    // Whether the Timer play/stop overlay is visible (toggled by tapping the card).
    var showTimerControls by remember { mutableStateOf(false) }
    // Whether the Voice mic on/off overlay is visible (toggled by tapping the card).
    var showVoiceControls by remember { mutableStateOf(false) }

    val isListening = speechState == SpeechState.LISTENING || speechState == SpeechState.TRIGGER_DETECTED

    val context = LocalContext.current

    // Context localized to the audio/TTS language so that card text matches what is spoken.
    val audioContext = remember(settings.language, context) {
        val locale = when (settings.language) {
            AppLanguage.ENGLISH -> Locale.US
            AppLanguage.SPANISH -> Locale("es", "ES")
            AppLanguage.FRENCH -> Locale.FRANCE
            AppLanguage.PORTUGUESE -> Locale("pt", "PT")
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
        }
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
    }

    // Handle audio permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceListening()
        } else {
            // Revert back to Manual if permission is denied
            viewModel.setPlayMode(PlayMode.MANUAL)
        }
    }

    // Keep screen active if timer or voice is running
    KeepScreenOnDisposableEffect(activeMode = activeMode, isTimerRunning = isTimerRunning, context = context)

    // Mode listener to trigger/stop background tasks
    LaunchedEffect(activeMode) {
        showTimerControls = false // hide the overlays when switching modes
        showVoiceControls = false
        when (activeMode) {
            PlayMode.TIMER -> {
                viewModel.stopVoiceListening()
            }
            PlayMode.VOICE -> {
                viewModel.stopTimer()
                checkAndRequestPermission(
                    context = context,
                    onGranted = { viewModel.startVoiceListening() },
                    onShowRationale = { viewModel.setMicrophoneRationaleVisible(true) },
                    onRequest = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )
            }
            PlayMode.MANUAL -> {
                viewModel.cleanupActiveMode()
            }
        }
    }

    // Auto cleanup when the screen is destroyed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanupActiveMode()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
            // 1. Play Mode Selector (top of screen, icon + text)
            Spacer(modifier = Modifier.height(8.dp))
            PlayModeSelector(
                activeMode = activeMode,
                onModeSelected = { mode -> viewModel.setPlayMode(mode) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Result Card — header. Timer: border = countdown, tap = play/stop
            //    overlay. Voice: dimmed when off / neon + soundwave when listening,
            //    tap = mic on/off overlay. Manual: tap = spin.
            Box(modifier = Modifier.fillMaxWidth()) {
                // Soundwave ripples around the card while listening.
                if (activeMode == PlayMode.VOICE && isListening) {
                    VoiceSoundwave()
                }
                ResultCard(
                    turn = currentTurn,
                    audioContext = audioContext,
                    borderProgress = when {
                        activeMode == PlayMode.TIMER -> timerProgress
                        activeMode == PlayMode.VOICE && isListening -> 1f // full neon = enabled
                        else -> null
                    },
                    dimmed = activeMode == PlayMode.VOICE && !isListening,
                    onClick = {
                        when (activeMode) {
                            PlayMode.TIMER -> showTimerControls = !showTimerControls
                            PlayMode.VOICE -> showVoiceControls = !showVoiceControls
                            PlayMode.MANUAL -> viewModel.spin()
                        }
                    }
                )
                if (activeMode == PlayMode.TIMER && showTimerControls) {
                    TimerOverlayControls(
                        isRunning = isTimerRunning,
                        onStart = { viewModel.startTimer() },
                        onPause = { viewModel.pauseTimer() },
                        modifier = Modifier.matchParentSize()
                    )
                }
                if (activeMode == PlayMode.VOICE && showVoiceControls) {
                    VoiceMicOverlay(
                        isListening = isListening,
                        onToggle = {
                            if (isListening) {
                                viewModel.stopVoiceListening()
                            } else {
                                checkAndRequestPermission(
                                    context = context,
                                    onGranted = { viewModel.startVoiceListening() },
                                    onShowRationale = { viewModel.setMicrophoneRationaleVisible(true) },
                                    onRequest = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                                )
                            }
                        },
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Transient banner shown when Reducing mode drops a color.
            modeMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2500)
                    viewModel.clearModeMessage()
                }
                val droppedColor = currentTurn?.droppedColor
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (droppedColor != null) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(gameColor(droppedColor))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Icon(
                        painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_circle_minus),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // 3. Dynamic Mode Controls Area (wrap height)
            when (activeMode) {
                PlayMode.TIMER -> {
                    // Timer controls now live on the result card (border + tap overlay).
                }
                PlayMode.VOICE -> {
                    VoiceStatus(
                        speechState = speechState,
                        triggerWord = settings.triggerWord
                    )
                }
                PlayMode.MANUAL -> {
                    // Show the hint only before the first spin; afterwards the
                    // history list fills this space instead.
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
            }

            // 4. Recent Moves — non-scrollable, fills the remaining space
            RecentMovesList(
                history = history,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

    // Permission Rationale Dialog
    if (showMicRationale) {
        AlertDialog(
            onDismissRequest = { viewModel.setMicrophoneRationaleVisible(false) },
            title = { Text(stringResource(R.string.permission_rationale_title)) },
            text = { Text(stringResource(R.string.permission_rationale_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setMicrophoneRationaleVisible(false)
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                ) {
                    Text(stringResource(R.string.control_start))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setMicrophoneRationaleVisible(false)
                        viewModel.setPlayMode(PlayMode.MANUAL)
                    }
                ) {
                    Text(stringResource(R.string.permission_cancel))
                }
            }
        )
    }
}

/** Maps a game color to its vivid Compose color. */
fun gameColor(color: TwisterColor): Color = when (color) {
    TwisterColor.RED -> GameRed
    TwisterColor.BLUE -> GameBlue
    TwisterColor.YELLOW -> GameYellow
    TwisterColor.GREEN -> GameGreen
    TwisterColor.PURPLE -> GamePurple
    TwisterColor.ORANGE -> GameOrange
    TwisterColor.BLACK -> GameBlack
}

@Composable
fun RecentMovesList(
    history: List<HistoryEntry>,
    modifier: Modifier = Modifier
) {
    // Index 0 is the current move (already shown in the card); list the rest.
    val recent = history.drop(1)
    if (recent.isEmpty()) {
        Box(modifier) // hold the weighted space; nothing to show yet
        return
    }
    Column(modifier) {
        Text(
            text = stringResource(R.string.recent_moves).uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                recent.forEach { entry -> RecentMoveRow(entry) }
            }
            // Bottom fade: clipped overflow reads as "more before".
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                        )
                    )
            )
        }
    }
}

@Composable
private fun RecentMoveRow(entry: HistoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(gameColor(entry.result.color))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(entry.result.bodyPart.nameResId),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(entry.result.color.nameResId),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.turn_number, entry.turn),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun ResultCard(
    turn: Turn?,
    onClick: () -> Unit,
    audioContext: Context,
    borderProgress: Float? = null,
    dimmed: Boolean = false
) {
    val cardAlpha by animateFloatAsState(
        targetValue = if (dimmed) 0.4f else 1f,
        animationSpec = tween(300),
        label = "card_dim"
    )
    val primaryColor = turn?.primaryMove?.color
    val composeColor = when {
        turn?.challengeResId != null -> DeepViolet
        primaryColor == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        else -> gameColor(primaryColor)
    }
    val animatedColor by animateColorAsState(
        targetValue = composeColor,
        animationSpec = tween(500),
        label = "color_swatch"
    )

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = animatedColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .graphicsLayer { alpha = cardAlpha }
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = animatedColor.copy(alpha = 0.4f),
                spotColor = animatedColor.copy(alpha = 0.8f)
            )
            .then(
                if (borderProgress != null) {
                    Modifier.countdownBorder(
                        progress = borderProgress,
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 5.dp,
                        cornerRadius = 32.dp
                    )
                } else {
                    Modifier.border(2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                }
            )
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = turn,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300, delayMillis = 90)) +
                     androidx.compose.animation.scaleIn(initialScale = 0.92f, animationSpec = tween(300, delayMillis = 90)))
                    .togetherWith(fadeOut(animationSpec = tween(150)))
                },
                label = "result_animation"
            ) { t ->
                when {
                    t == null -> IdleCardContent()
                    t.challengeResId != null -> ChallengeCardContent(t.challengeResId, audioContext)
                    t.primaryMove != null -> MoveCardContent(t, audioContext)
                }
            }
        }
    }
}

@Composable
private fun IdleCardContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.main_idle_subtitle),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ChallengeCardContent(challengeResId: Int, audioContext: Context) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(text = "⭐", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.challenge_label).uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = audioContext.getString(challengeResId),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp
        )
    }
}

@Composable
private fun MoveCardContent(turn: Turn, audioContext: Context) {
    val move = turn.primaryMove ?: return
    val textColor = when (move.color) {
        TwisterColor.YELLOW, TwisterColor.GREEN, TwisterColor.ORANGE -> Color.Black
        else -> Color.White
    }
    val isLeft = move.bodyPart == BodyPart.LEFT_HAND || move.bodyPart == BodyPart.LEFT_FOOT
    val isHand = move.bodyPart == BodyPart.LEFT_HAND || move.bodyPart == BodyPart.RIGHT_HAND
    val emoji = if (isHand) "✋" else "🦶"
    val scaleX = if (isLeft) -1f else 1f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        if (turn.verb == TurnVerb.LIFT) {
            Text(
                text = stringResource(R.string.verb_lift).uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = textColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(textColor.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(textColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 38.sp, modifier = Modifier.graphicsLayer(scaleX = scaleX))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = audioContext.getString(move.bodyPart.nameResId).uppercase(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = audioContext.getString(move.color.nameResId),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor.copy(alpha = 0.8f),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(textColor.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Sequence: the extra moves to perform in order.
        if (turn.moves.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            turn.moves.drop(1).forEachIndexed { index, m ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(gameColor(m.color))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${index + 2}. " +
                            audioContext.getString(m.bodyPart.nameResId) + " · " +
                            audioContext.getString(m.color.nameResId),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

/**
 * Draws a neon countdown stroke around a rounded-rectangle perimeter.
 * [progress] 1f = full border, 0f = empty.
 */
fun Modifier.countdownBorder(
    progress: Float,
    color: Color,
    strokeWidth: Dp,
    cornerRadius: Dp
): Modifier = drawWithCache {
    val sw = strokeWidth.toPx()
    val corner = cornerRadius.toPx()
    val inset = sw / 2f
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = inset,
                top = inset,
                right = size.width - inset,
                bottom = size.height - inset,
                cornerRadius = CornerRadius(corner, corner)
            )
        )
    }
    val measure = PathMeasure().apply { setPath(path, true) }
    val length = measure.length
    val segment = Path()
    onDrawWithContent {
        drawContent()
        val p = progress.coerceIn(0f, 1f)
        if (p > 0f) {
            segment.reset()
            measure.getSegment(0f, length * p, segment, true)
            drawPath(segment, color = color, style = Stroke(width = sw, cap = StrokeCap.Round))
        }
    }
}

/** Play/Pause controls overlaid on the result card in Timer mode. */
@Composable
fun TimerOverlayControls(
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = if (isRunning) onPause else onStart,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
                .size(72.dp)
        ) {
            Icon(
                painter = painterResource(if (isRunning) com.composables.icons.lucide.R.drawable.lucide_ic_pause else com.composables.icons.lucide.R.drawable.lucide_ic_play),
                contentDescription = if (isRunning) stringResource(R.string.control_pause) else stringResource(R.string.control_start),
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/** Animated "soundwave" ripples expanding outward around the card while listening. */
@Composable
fun BoxScope.VoiceSoundwave() {
    val transition = rememberInfiniteTransition(label = "soundwave")
    repeat(3) { i ->
        val t by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, delayMillis = i * 600, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ripple_$i"
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    val scale = 1f + 0.18f * t
                    scaleX = scale
                    scaleY = scale
                    alpha = (1f - t) * 0.5f
                }
                .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(32.dp))
        )
    }
}

/** Mic on/off control overlaid on the result card in Voice mode. */
@Composable
fun VoiceMicOverlay(
    isListening: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isListening) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                .size(72.dp)
        ) {
            Icon(
                painter = painterResource(if (isListening) com.composables.icons.lucide.R.drawable.lucide_ic_mic else com.composables.icons.lucide.R.drawable.lucide_ic_mic_off),
                contentDescription = stringResource(R.string.cd_microphone),
                tint = if (isListening) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/** The listening status line shown just below the card in Voice mode. */
@Composable
fun VoiceStatus(
    speechState: SpeechState,
    triggerWord: String
) {
    Text(
        text = when (speechState) {
            SpeechState.LISTENING -> stringResource(R.string.voice_listening, triggerWord)
            SpeechState.TRIGGER_DETECTED -> stringResource(R.string.voice_trigger_detected)
            SpeechState.UNSUPPORTED -> stringResource(R.string.voice_unsupported)
            SpeechState.ERROR -> stringResource(R.string.voice_error)
            else -> stringResource(R.string.voice_idle_hint)
        },
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}

@Composable
fun LanguageFlagShortcut(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    // Flag emojis row for rapid switching
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val flags = listOf(
            AppLanguage.ENGLISH to "🇬🇧",
            AppLanguage.SPANISH to "🇪🇸",
            AppLanguage.FRENCH to "🇫🇷",
            AppLanguage.PORTUGUESE to "🇵🇹",
            AppLanguage.HINDI to "🇮🇳",
            AppLanguage.CHINESE to "🇨🇳"
        )
        flags.forEach { (lang, flag) ->
            val isSelected = currentLanguage == lang
            Text(
                text = flag,
                fontSize = 20.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f) else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onLanguageChange(lang) }
                    .padding(4.dp)
                    .width(24.dp) // Maintain spacing stability
                    .height(24.dp)
            )
        }
    }
}

@Composable
fun KeepScreenOnDisposableEffect(
    activeMode: PlayMode,
    isTimerRunning: Boolean,
    context: Context
) {
    DisposableEffect(activeMode, isTimerRunning) {
        val activity = context as? android.app.Activity
        val keepScreenOn = (activeMode == PlayMode.TIMER && isTimerRunning) || activeMode == PlayMode.VOICE
        
        if (keepScreenOn) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

fun checkAndRequestPermission(
    context: Context,
    onGranted: () -> Unit,
    onShowRationale: () -> Unit,
    onRequest: () -> Unit
) {
    when {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
            onGranted()
        }
        (context as? android.app.Activity)?.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) == true -> {
            onShowRationale()
        }
        else -> {
            onRequest()
        }
    }
}

@Composable
fun PlayModeSelector(
    activeMode: PlayMode,
    onModeSelected: (PlayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayMode.entries.forEach { mode ->
            val isSelected = activeMode == mode
            val label = when (mode) {
                PlayMode.MANUAL -> stringResource(R.string.mode_manual)
                PlayMode.TIMER -> stringResource(R.string.mode_timer)
                PlayMode.VOICE -> stringResource(R.string.mode_voice)
            }
            val modeIconRes = when (mode) {
                PlayMode.MANUAL -> com.composables.icons.lucide.R.drawable.lucide_ic_hand
                PlayMode.TIMER -> com.composables.icons.lucide.R.drawable.lucide_ic_timer
                PlayMode.VOICE -> com.composables.icons.lucide.R.drawable.lucide_ic_mic
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        color = if (isSelected) DeepViolet else Color.Transparent,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable { onModeSelected(mode) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(modeIconRes),
                        contentDescription = label,
                        modifier = Modifier.size(18.dp),
                        tint = if (isSelected) Color.White else Color(0xFFB0B0B0)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFFB0B0B0),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
