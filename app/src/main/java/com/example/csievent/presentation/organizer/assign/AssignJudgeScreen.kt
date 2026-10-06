package com.example.csievent.presentation.organizer.assign

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.judge.JudgeAssignmentDto
import com.example.csievent.presentation.components.Avatar
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.ConfirmDialog
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.ErrorState
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.MessageEffect
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.PrimaryButton
import com.example.csievent.presentation.components.SectionHeader
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CardShape
import com.example.csievent.ui.theme.CsiTheme

/**
 * Lets organizers choose which judges score an event and which teams each
 * judge scores. "All teams" (the default) also covers teams formed later.
 *
 * File: app/src/main/java/com/example/csievent/presentation/organizer/assign/AssignJudgeScreen.kt
 */
@Composable
fun AssignJudgeScreen(
    eventId: Long,
    navController: NavHostController,
    viewModel: AssignJudgeViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var selectedJudgeId by remember { mutableStateOf<Long?>(null) }
    var allTeams by remember { mutableStateOf(true) }
    val selectedTeamIds = remember { mutableStateListOf<Long>() }
    var confirmRemove by remember { mutableStateOf<JudgeAssignmentDto?>(null) }

    LaunchedEffect(eventId) { viewModel.loadData(eventId) }

    // Reset the form after a successful save; show messages in the snackbar
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            selectedJudgeId = null
            allTeams = true
            selectedTeamIds.clear()
        }
    }
    MessageEffect(state.successMessage ?: state.error, snackbar, viewModel::clearMessage)

    fun startEditing(a: JudgeAssignmentDto) {
        selectedJudgeId = a.judgeId
        allTeams = a.allTeams
        selectedTeamIds.clear()
        selectedTeamIds.addAll(a.teamIds)
    }

    val canSave = selectedJudgeId != null && (allTeams || selectedTeamIds.isNotEmpty())

    CsiScreen(
        snackbarHostState = snackbar,
        topBar = {
            CsiTopBar(
                title = "Judges",
                subtitle = "Who scores which teams",
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .background(c.background)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = c.line)
                PrimaryButton(
                    text = when {
                        selectedJudgeId == null -> "Select a judge"
                        allTeams -> "Save — scores all teams"
                        selectedTeamIds.isEmpty() -> "Select at least one team"
                        else -> "Save — scores ${selectedTeamIds.size} team" + if (selectedTeamIds.size == 1) "" else "s"
                    },
                    onClick = {
                        selectedJudgeId?.let { judgeId ->
                            viewModel.assignJudge(eventId, judgeId, if (allTeams) emptyList() else selectedTeamIds.toList())
                        }
                    },
                    enabled = canSave,
                    loading = state.isSaving,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading && state.judges.isEmpty() -> LoadingState(Modifier.padding(padding))
            state.loadError != null && state.judges.isEmpty() && state.assignments.isEmpty() && !state.isLoading ->
                ErrorState(state.loadError!!, onRetry = { viewModel.loadData(eventId) }, modifier = Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Current judges ─────────────────────────────────────
                item(key = "current_header") {
                    SectionHeader("On this event", trailing = state.assignments.size.toString())
                }
                if (state.assignments.isEmpty()) {
                    item(key = "current_empty") {
                        Text("No judges yet. Pick one below and save.", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                    }
                } else {
                    items(state.assignments, key = { "a_${it.judgeId}" }) { a ->
                        CsiCard(padding = 12) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Avatar(a.judgeName, size = 36, emphasized = true)
                                Column(Modifier.weight(1f)) {
                                    Text(a.judgeName, style = MaterialTheme.typography.titleSmall, color = c.text)
                                    Text(
                                        if (a.allTeams) "Scores all teams" else "Scores: " + a.teamNames.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (a.allTeams) c.success else c.textMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { startEditing(a) }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Change ${a.judgeName}'s teams", tint = c.textMuted)
                                }
                                IconButton(onClick = { confirmRemove = a }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove ${a.judgeName}", tint = c.textMuted)
                                }
                            }
                        }
                    }
                }

                // ── Pick judge ─────────────────────────────────────────
                item(key = "judge_header") {
                    SectionHeader("1 · Choose judge", modifier = Modifier.padding(top = 12.dp))
                }
                if (state.judges.isEmpty()) {
                    item(key = "no_judges") {
                        NoticeBanner(
                            "No users have the Judge role yet. Promote someone in People first.",
                            icon = Icons.Rounded.Info,
                            actionLabel = "People",
                            onAction = { navController.navigate(Routes.ROLE_MANAGEMENT) }
                        )
                    }
                } else {
                    items(state.judges, key = { "j_${it.id}" }) { judge ->
                        SelectRow(
                            title = judge.name,
                            subtitle = judge.email,
                            selected = selectedJudgeId == judge.id,
                            onClick = {
                                val existing = state.assignments.firstOrNull { it.judgeId == judge.id }
                                if (existing != null) startEditing(existing) else {
                                    selectedJudgeId = judge.id
                                    allTeams = true
                                    selectedTeamIds.clear()
                                }
                            }
                        )
                    }
                }

                // ── Pick teams ─────────────────────────────────────────
                item(key = "team_header") {
                    SectionHeader("2 · Teams to score", trailing = "${state.teams.size} teams", modifier = Modifier.padding(top = 12.dp))
                }
                item(key = "all_teams") {
                    SelectRow(
                        title = "All teams",
                        subtitle = "Recommended — also covers teams formed later",
                        selected = allTeams,
                        onClick = { allTeams = true; selectedTeamIds.clear() }
                    )
                }
                item(key = "some_teams") {
                    SelectRow(
                        title = "Only specific teams",
                        subtitle = "Split teams between judges",
                        selected = !allTeams,
                        onClick = { allTeams = false }
                    )
                }
                if (!allTeams) {
                    if (state.teams.isEmpty()) {
                        item(key = "no_teams") {
                            Text("No teams have formed for this event yet.", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                        }
                    } else {
                        items(state.teams, key = { "t_${it.id}" }) { team ->
                            val checked = team.id in selectedTeamIds
                            SelectRow(
                                title = team.teamName,
                                subtitle = listOfNotNull(
                                    team.leaderName?.let { "Led by $it" },
                                    "${team.members.size} member" + if (team.members.size == 1) "" else "s"
                                ).joinToString(" · "),
                                selected = checked,
                                checkbox = true,
                                onClick = { if (checked) selectedTeamIds.remove(team.id) else selectedTeamIds.add(team.id) }
                            )
                        }
                    }
                }

                item(key = "bottom") { BottomSpacer(8) }
            }
        }
    }

    confirmRemove?.let { a ->
        ConfirmDialog(
            title = "Remove ${a.judgeName}?",
            message = "They will no longer see this event. Scores they already gave stay on the leaderboard.",
            confirmLabel = "Remove",
            destructive = true,
            onConfirm = {
                viewModel.removeJudge(eventId, a.judgeId)
                if (selectedJudgeId == a.judgeId) selectedJudgeId = null
            },
            onDismiss = { confirmRemove = null }
        )
    }
}

@Composable
private fun SelectRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    checkbox: Boolean = false,
    onClick: () -> Unit
) {
    val c = CsiTheme.colors
    val markShape = if (checkbox) RoundedCornerShape(6.dp) else CircleShape
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (selected) c.highlightContainer else c.surface)
            .border(1.dp, if (selected) c.highlightBorder else if (c.isDark) Color.Transparent else c.line, CardShape)
            .clickable(role = if (checkbox) Role.Checkbox else Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(
            Modifier
                .size(24.dp)
                .clip(markShape)
                .background(if (selected) c.accent else Color.Transparent)
                .border(1.5.dp, if (selected) c.accent else c.textSubtle, markShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = c.onAccent, modifier = Modifier.size(16.dp))
        }
    }
}
