package com.example.csievent.presentation.organizer.assign

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.judge.JudgeAssignmentDto
import com.example.csievent.presentation.navigation.Routes
import kotlinx.coroutines.delay

private val Bg        = Color(0xFF05040D)
private val Card      = Color(0xFF0D0B1F)
private val Line      = Color(0xFF2A2650)
private val Accent    = Color(0xFF8C83E4)
private val Cyan      = Color(0xFF06B6D4)
private val Danger    = Color(0xFFEF4444)
private val TextMain  = Color(0xFFECEBF7)
private val TextMuted = Color(0xFF9C99B8)

/**
 * Lets organizers choose which judges score an event and which teams each
 * judge scores. "All teams" (the default) also covers teams formed later.
 *
 * Layout: one LazyColumn with sections (current judges, pick judge, pick
 * teams) and a fixed Save button — avoids nested scrollable containers.
 *
 * File: app/src/main/java/com/example/csievent/presentation/organizer/assign/AssignJudgeScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignJudgeScreen(
    eventId:       Long,
    navController: NavHostController,
    viewModel:     AssignJudgeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var selectedJudgeId  by remember { mutableStateOf<Long?>(null) }
    var allTeams         by remember { mutableStateOf(true) }
    val selectedTeamIds  = remember { mutableStateListOf<Long>() }
    var confirmRemove    by remember { mutableStateOf<JudgeAssignmentDto?>(null) }

    LaunchedEffect(eventId) { viewModel.loadData(eventId) }

    // Reset the form after a successful save and auto-hide messages
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            selectedJudgeId = null
            allTeams = true
            selectedTeamIds.clear()
            delay(2500)
            viewModel.clearMessage()
        }
    }

    fun startEditing(a: JudgeAssignmentDto) {
        selectedJudgeId = a.judgeId
        allTeams = a.allTeams
        selectedTeamIds.clear()
        selectedTeamIds.addAll(a.teamIds)
    }

    val canSave = selectedJudgeId != null && (allTeams || selectedTeamIds.isNotEmpty()) && !state.isSaving

    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ASSIGN JUDGES", color = TextMain, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
                        Text("Who scores which teams", color = Accent.copy(alpha = 0.6f), fontSize = 10.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Accent)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData(eventId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg)
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Bg)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        selectedJudgeId?.let { judgeId ->
                            viewModel.assignJudge(eventId, judgeId, if (allTeams) emptyList() else selectedTeamIds.toList())
                        }
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = Color(0xFF0A0820),
                        disabledContainerColor = Line,
                        disabledContentColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color(0xFF0A0820), strokeWidth = 2.dp)
                    } else {
                        Text(
                            when {
                                selectedJudgeId == null -> "Select a judge"
                                allTeams -> "Save — judge scores all teams"
                                selectedTeamIds.isEmpty() -> "Select at least one team"
                                else -> "Save — judge scores ${selectedTeamIds.size} team" + if (selectedTeamIds.size == 1) "" else "s"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->

        Box(Modifier.fillMaxSize().padding(padding)) {

            if (state.isLoading && state.judges.isEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Accent)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // ── Current judges ─────────────────────────────────────
                    item(key = "current_header") {
                        SectionHeader("JUDGES ON THIS EVENT", "${state.assignments.size} assigned")
                    }
                    if (state.assignments.isEmpty()) {
                        item(key = "current_empty") {
                            Hint("No judges yet. Pick a judge below and save.")
                        }
                    } else {
                        items(state.assignments, key = { "a_${it.judgeId}" }) { a ->
                            AssignmentRow(
                                assignment = a,
                                onEdit = { startEditing(a) },
                                onRemove = { confirmRemove = a }
                            )
                        }
                    }

                    // ── Pick judge ─────────────────────────────────────────
                    item(key = "judge_header") {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader("1 · CHOOSE JUDGE", "Required")
                    }
                    if (state.judges.isEmpty()) {
                        item(key = "no_judges") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Hint("No users have the Judge role yet. Promote a user to Judge first.")
                                OutlinedButton(
                                    onClick = { navController.navigate(Routes.ROLE_MANAGEMENT) },
                                    border = BorderStroke(1.dp, Accent)
                                ) { Text("Open Role Management", color = Accent) }
                            }
                        }
                    } else {
                        items(state.judges, key = { "j_${it.id}" }) { judge ->
                            SelectRow(
                                title = judge.name,
                                subtitle = judge.email,
                                selected = selectedJudgeId == judge.id,
                                accent = Accent,
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
                        Spacer(Modifier.height(8.dp))
                        SectionHeader("2 · TEAMS TO SCORE", "${state.teams.size} teams in event")
                    }
                    item(key = "all_teams") {
                        SelectRow(
                            title = "All teams",
                            subtitle = "Recommended — also covers teams formed later",
                            selected = allTeams,
                            accent = Cyan,
                            onClick = { allTeams = true; selectedTeamIds.clear() }
                        )
                    }
                    item(key = "some_teams") {
                        SelectRow(
                            title = "Only specific teams",
                            subtitle = "Split teams between judges",
                            selected = !allTeams,
                            accent = Cyan,
                            onClick = { allTeams = false }
                        )
                    }
                    if (!allTeams) {
                        if (state.teams.isEmpty()) {
                            item(key = "no_teams") { Hint("No teams have formed for this event yet.") }
                        } else {
                            items(state.teams, key = { "t_${it.id}" }) { team ->
                                val checked = team.id in selectedTeamIds
                                SelectRow(
                                    title = team.teamName,
                                    subtitle = buildString {
                                        team.leaderName?.let { append("Led by $it") }
                                        if (team.members.isNotEmpty()) {
                                            if (isNotEmpty()) append(" · ")
                                            append("${team.members.size} members")
                                        }
                                    },
                                    selected = checked,
                                    accent = Cyan,
                                    checkbox = true,
                                    onClick = {
                                        if (checked) selectedTeamIds.remove(team.id) else selectedTeamIds.add(team.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Messages
            Column(
                Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp)
            ) {
                state.error?.let { Banner(it, Danger) { viewModel.clearMessage() } }
                state.successMessage?.let { Banner(it, Color(0xFF22C55E)) { viewModel.clearMessage() } }
            }
        }
    }

    confirmRemove?.let { a ->
        AlertDialog(
            onDismissRequest = { confirmRemove = null },
            containerColor = Card,
            title = { Text("Remove ${a.judgeName}?", color = TextMain) },
            text = {
                Text(
                    "They will no longer see this event. Scores they already gave stay on the leaderboard.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeJudge(eventId, a.judgeId)
                    if (selectedJudgeId == a.judgeId) selectedJudgeId = null
                    confirmRemove = null
                }) { Text("Remove", color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = null }) { Text("Cancel", color = Accent) }
            }
        )
    }
}

// =============================================================================
// PRIVATE COMPOSABLES
// =============================================================================

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Accent, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(subtitle, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun SelectRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    accent: Color,
    checkbox: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) accent.copy(alpha = 0.12f) else Card)
            .border(1.dp, if (selected) accent else Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(
            Modifier
                .size(24.dp)
                .clip(if (checkbox) RoundedCornerShape(6.dp) else CircleShape)
                .background(if (selected) accent else Color.Transparent)
                .border(1.5.dp, if (selected) accent else TextMuted, if (checkbox) RoundedCornerShape(6.dp) else CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF0A0820), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun AssignmentRow(
    assignment: JudgeAssignmentDto,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(assignment.judgeName, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(
                if (assignment.allTeams) "Scores all teams"
                else "Scores: " + assignment.teamNames.joinToString(", "),
                color = if (assignment.allTeams) Cyan else TextMuted,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Change teams", tint = Accent)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove judge", tint = Danger.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun Banner(message: String, color: Color, onDismiss: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF120F26))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(message, color = TextMain, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text("✕", color = TextMuted, fontSize = 14.sp)
    }
}
