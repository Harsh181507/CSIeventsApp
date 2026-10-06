package com.example.csievent.presentation.judge.scoring

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.automirrored.rounded.Rule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.ConfirmDialog
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.EmptyState
import com.example.csievent.presentation.components.ErrorState
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.MessageEffect
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.OutlinedIconButton
import com.example.csievent.presentation.components.PrimaryButton
import com.example.csievent.ui.theme.ControlShape
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle

/**
 * Score one team at a time. The strip at the top switches teams (green =
 * scored), each criterion has a slider plus −/+ for precision, and
 * "Save & next" saves everything in one request and moves to the next
 * unscored team.
 */
@Composable
fun JudgeScoringScreen(
    eventId: Long,
    teamId: Long,
    navController: NavHostController,
    viewModel: JudgeScoringViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    // Pending action that would throw away unsaved edits
    var pendingTeamSwitch by remember { mutableStateOf<Long?>(null) }
    var confirmLeave by remember { mutableStateOf(false) }

    LaunchedEffect(eventId, teamId) { viewModel.load(eventId, teamId) }
    MessageEffect(state.message, snackbar, viewModel::clearMessage)

    BackHandler(enabled = state.dirty) { confirmLeave = true }

    val scoredIds = state.scoredTeamIds
    val team = state.currentTeam

    CsiScreen(
        snackbarHostState = snackbar,
        topBar = {
            CsiTopBar(
                title = state.event?.title ?: "Scoring",
                subtitle = if (state.teams.isNotEmpty()) "${scoredIds.size} of ${state.teams.size} teams scored" else null,
                onBack = { if (state.dirty) confirmLeave = true else navController.popBackStack() }
            )
        },
        bottomBar = {
            if (team != null && state.criteria.isNotEmpty() && !state.locked) {
                Column(
                    Modifier
                        .background(c.background)
                        .navigationBarsPadding()
                ) {
                    HorizontalDivider(color = c.line)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = c.textMuted)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${state.total}", style = monoStyle(24.sp), color = c.text)
                                Text(" /${state.maxTotal}", style = monoStyle(14.sp), color = c.textSubtle)
                            }
                        }
                        val othersLeft = state.teams.count { it.id != team.id && it.id !in scoredIds }
                        PrimaryButton(
                            text = if (othersLeft == 0) "Save" else "Save & next team",
                            onClick = { viewModel.save(goToNext = true) },
                            enabled = state.allSet,
                            loading = state.isSaving,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(
                state.error!!,
                onRetry = { viewModel.retry(eventId, teamId) },
                modifier = Modifier.padding(padding)
            )
            state.criteria.isEmpty() -> EmptyState(
                icon = Icons.AutoMirrored.Rounded.Rule,
                title = "No judging criteria yet",
                message = "The organizer needs to add criteria before teams can be scored.",
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "strip") {
                    TeamStrip(
                        teamIds = state.teams.map { it.id },
                        currentId = state.currentTeamId,
                        scoredIds = scoredIds,
                        onSelect = { id ->
                            if (id == state.currentTeamId) return@TeamStrip
                            if (state.dirty) pendingTeamSwitch = id else viewModel.selectTeam(id)
                        }
                    )
                }

                if (team != null) {
                    item(key = "team") {
                        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(team.teamName, style = MaterialTheme.typography.headlineMedium, color = c.text)
                            if (team.members.isNotEmpty()) {
                                Text(team.members.joinToString(" · "), style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                            }
                        }
                    }
                }

                if (state.locked) {
                    item(key = "locked") {
                        NoticeBanner(
                            "Scoring is locked. Scores can't be changed.",
                            icon = Icons.Rounded.Lock,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                items(state.criteria, key = { it.id }) { criterion ->
                    CriterionCard(
                        criterion = criterion,
                        value = state.draft[criterion.id],
                        enabled = !state.locked && !state.isSaving,
                        onChange = { viewModel.setScore(criterion.id, it) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                if (!state.allSet && !state.locked) {
                    item(key = "hint") {
                        Text(
                            "Set a score for every criterion to save.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSubtle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                    }
                }

                item(key = "bottom") { BottomSpacer(8) }
            }
        }
    }

    pendingTeamSwitch?.let { target ->
        ConfirmDialog(
            title = "Discard unsaved scores?",
            message = "You changed scores for ${team?.teamName ?: "this team"} without saving.",
            confirmLabel = "Discard",
            destructive = true,
            onConfirm = { viewModel.selectTeam(target) },
            onDismiss = { pendingTeamSwitch = null }
        )
    }
    if (confirmLeave) {
        ConfirmDialog(
            title = "Leave without saving?",
            message = "Your changes for ${team?.teamName ?: "this team"} will be lost.",
            confirmLabel = "Leave",
            destructive = true,
            onConfirm = { navController.popBackStack() },
            onDismiss = { confirmLeave = false }
        )
    }
}

/** Numbered team buttons: green = scored, gold = current. */
@Composable
private fun TeamStrip(
    teamIds: List<Long>,
    currentId: Long?,
    scoredIds: Set<Long>,
    onSelect: (Long) -> Unit
) {
    val c = CsiTheme.colors
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(teamIds, key = { _, id -> id }) { index, id ->
            val current = id == currentId
            val scored = id in scoredIds
            val bg = when {
                current -> c.accent
                scored -> c.successContainer
                else -> c.surface
            }
            val fg = when {
                current -> c.onAccent
                scored -> c.success
                else -> c.textMuted
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(ControlShape)
                    .background(bg)
                    .then(if (!current && !scored) Modifier.border(1.dp, c.line, ControlShape) else Modifier)
                    .clickable { onSelect(id) }
                    .semantics {
                        contentDescription = "Team ${index + 1}" +
                            (if (scored) ", scored" else "") + (if (current) ", current" else "")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("${index + 1}", style = monoStyle(14.sp), color = fg)
            }
        }
    }
}

@Composable
private fun CriterionCard(
    criterion: CriteriaResponseDto,
    value: Int?,
    enabled: Boolean,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = CsiTheme.colors
    val max = criterion.maxScore.coerceAtLeast(1)

    CsiCard(modifier = modifier, highlighted = value == null && enabled) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                criterion.title,
                style = MaterialTheme.typography.titleSmall,
                color = c.text,
                modifier = Modifier.weight(1f)
            )
            Text(
                value?.toString() ?: "–",
                style = monoStyle(24.sp),
                color = if (value == null) c.textSubtle else c.highlight
            )
            Text(" /$max", style = monoStyle(14.sp, FontWeight.Medium), color = c.textSubtle)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedIconButton(
                icon = Icons.Rounded.Remove,
                contentDescription = "Decrease ${criterion.title}",
                onClick = { onChange((value ?: 0) - 1) },
                enabled = enabled && (value ?: 0) > 0
            )
            Slider(
                value = (value ?: 0).toFloat(),
                onValueChange = { onChange(Math.round(it)) },
                valueRange = 0f..max.toFloat(),
                steps = (max - 1).coerceAtLeast(0),
                enabled = enabled,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = c.text,
                    activeTrackColor = c.highlight,
                    inactiveTrackColor = c.line,
                    activeTickColor = c.highlight.copy(alpha = 0f),
                    inactiveTickColor = c.line.copy(alpha = 0f),
                    disabledThumbColor = c.textSubtle,
                    disabledActiveTrackColor = c.textSubtle,
                    disabledInactiveTrackColor = c.line
                )
            )
            OutlinedIconButton(
                icon = Icons.Rounded.Add,
                contentDescription = "Increase ${criterion.title}",
                onClick = { onChange(if (value == null) 0 else value + 1) },
                enabled = enabled && (value ?: -1) < max
            )
        }
    }
}
