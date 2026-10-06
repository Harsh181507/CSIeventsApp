package com.example.csievent.presentation.organizer.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.automirrored.rounded.Rule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.ChipKind
import com.example.csievent.presentation.components.ConfirmDialog
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.ErrorState
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.MessageEffect
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.PrimaryButton
import com.example.csievent.presentation.components.RefreshableBox
import com.example.csievent.presentation.components.SecondaryButton
import com.example.csievent.presentation.components.SectionHeader
import com.example.csievent.presentation.components.StatTile
import com.example.csievent.presentation.components.StatusChip
import com.example.csievent.presentation.components.formatDate
import com.example.csievent.presentation.components.shouldConfirmCopy
import com.example.csievent.presentation.components.formatJoinCode
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.ControlShape
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle

private enum class PendingAction { LOCK, UNLOCK, DELETE }

/**
 * Everything about one event for an organizer: status, setup checklist,
 * shortcuts to criteria / judges / results, and every team with its members.
 */
@Composable
fun OrganizerEventScreen(
    eventId: Long,
    navController: NavHostController,
    viewModel: OrganizerEventViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    var menuOpen by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PendingAction?>(null) }

    // Reloads when returning from criteria / judges screens
    LaunchedEffect(eventId) { viewModel.load(eventId) }
    LaunchedEffect(state.deleted) { if (state.deleted) navController.popBackStack() }
    MessageEffect(state.message, snackbar, viewModel::clearMessage)

    val event = state.event
    val locked = event?.scoringLocked == true
    val totalPoints = state.criteria.sumOf { it.maxScore }
    val students = state.teams.sumOf { it.members.size }

    CsiScreen(
        snackbarHostState = snackbar,
        topBar = {
            CsiTopBar(
                title = event?.title ?: "Event",
                subtitle = event?.let { formatDate(it.eventDate) },
                onBack = { navController.popBackStack() },
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More options", tint = c.text)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Delete event", color = c.danger) },
                                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = c.danger) },
                                onClick = { menuOpen = false; pending = PendingAction.DELETE }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading && event == null -> LoadingState(Modifier.padding(padding))
            state.error != null && event == null ->
                ErrorState(state.error!!, onRetry = { viewModel.load(eventId) }, modifier = Modifier.padding(padding))
            event != null -> RefreshableBox(
                refreshing = state.isLoading,
                onRefresh = { viewModel.load(eventId) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "status") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (locked) StatusChip("Scoring locked", ChipKind.NEUTRAL)
                                else StatusChip("Open", ChipKind.SUCCESS)
                                StatusChip("Teams of up to ${event.maxTeamSize}", ChipKind.NEUTRAL)
                            }
                            if (!event.description.isNullOrBlank()) {
                                Text(event.description, style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                            }
                        }
                    }

                    item(key = "stats") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatTile(state.teams.size.toString(), "Teams", Modifier.weight(1f))
                            StatTile(students.toString(), "Students", Modifier.weight(1f))
                            StatTile(state.judges.size.toString(), "Judges", Modifier.weight(1f))
                        }
                    }

                    // Setup checklist
                    if (!locked && state.criteria.isEmpty()) {
                        item(key = "need_criteria") {
                            NoticeBanner(
                                "Add judging criteria so judges can score teams.",
                                icon = Icons.Rounded.Info,
                                actionLabel = "Add",
                                onAction = { navController.navigate("${Routes.CRITERIA}/$eventId") }
                            )
                        }
                    }
                    if (!locked && state.judges.isEmpty()) {
                        item(key = "need_judges") {
                            NoticeBanner(
                                "Assign at least one judge to this event.",
                                icon = Icons.Rounded.Info,
                                actionLabel = "Assign",
                                onAction = { navController.navigate("${Routes.ASSIGN_JUDGE}/$eventId") }
                            )
                        }
                    }

                    item(key = "links") {
                        CsiCard(padding = 4) {
                            LinkRow(
                                icon = Icons.AutoMirrored.Rounded.Rule,
                                title = "Judging criteria",
                                detail = if (state.criteria.isEmpty()) "None yet" else "${state.criteria.size} criteria · $totalPoints points",
                                onClick = { navController.navigate("${Routes.CRITERIA}/$eventId") }
                            )
                            LinkRow(
                                icon = Icons.Rounded.Gavel,
                                title = "Judges",
                                detail = if (state.judges.isEmpty()) "None assigned" else "${state.judges.size} assigned",
                                onClick = { navController.navigate("${Routes.ASSIGN_JUDGE}/$eventId") }
                            )
                            LinkRow(
                                icon = Icons.Rounded.EmojiEvents,
                                title = "Results",
                                detail = if (locked) "Final results" else "Live standings",
                                onClick = { navController.navigate("${Routes.RESULTS}/$eventId") }
                            )
                        }
                    }

                    item(key = "lock") {
                        if (locked) {
                            SecondaryButton(
                                text = "Reopen scoring",
                                icon = Icons.Rounded.LockOpen,
                                onClick = { pending = PendingAction.UNLOCK },
                                enabled = !state.isWorking,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            PrimaryButton(
                                text = "Lock scoring & publish results",
                                icon = Icons.Rounded.Lock,
                                onClick = { pending = PendingAction.LOCK },
                                loading = state.isWorking
                            )
                        }
                    }

                    item(key = "teams_header") {
                        SectionHeader("Teams", trailing = state.teams.size.toString(), modifier = Modifier.padding(top = 8.dp))
                    }
                    if (state.teams.isEmpty()) {
                        item(key = "teams_empty") {
                            Text(
                                "No teams yet. Students create or join teams from the app.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textMuted
                            )
                        }
                    }
                    items(state.teams, key = { it.id }) { team ->
                        TeamCard(
                            team = team,
                            maxSize = event.maxTeamSize,
                            onCopyCode = { code ->
                                clipboard.setText(AnnotatedString(code))
                                if (shouldConfirmCopy()) viewModel.showMessage("Join code copied")
                            }
                        )
                    }

                    item(key = "bottom") { BottomSpacer() }
                }
            }
        }
    }

    when (pending) {
        PendingAction.LOCK -> ConfirmDialog(
            title = "Lock scoring?",
            message = "Judges won't be able to change scores, and the final results become visible to everyone.",
            confirmLabel = "Lock & publish",
            onConfirm = { viewModel.setLocked(eventId, true) },
            onDismiss = { pending = null }
        )
        PendingAction.UNLOCK -> ConfirmDialog(
            title = "Reopen scoring?",
            message = "Judges can change scores again, and results are hidden until you lock it.",
            confirmLabel = "Reopen",
            onConfirm = { viewModel.setLocked(eventId, false) },
            onDismiss = { pending = null }
        )
        PendingAction.DELETE -> ConfirmDialog(
            title = "Delete event?",
            message = "\"${event?.title}\" and all its teams, criteria, judges and scores will be permanently deleted. This can't be undone.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = { viewModel.delete(eventId) },
            onDismiss = { pending = null }
        )
        null -> Unit
    }
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, detail: String, onClick: () -> Unit) {
    val c = CsiTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = c.highlight, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.text)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = c.textSubtle)
    }
}

@Composable
private fun TeamCard(team: TeamResponseDto, maxSize: Int, onCopyCode: (String) -> Unit) {
    val c = CsiTheme.colors
    CsiCard(padding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(team.teamName, style = MaterialTheme.typography.titleSmall, color = c.text)
                Text(
                    team.leaderName?.let { "Led by $it" } ?: "No leader",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted
                )
            }
            StatusChip(
                "${team.members.size}/$maxSize",
                if (team.members.size >= maxSize) ChipKind.NEUTRAL else ChipKind.OUTLINE
            )
        }
        if (team.members.isNotEmpty()) {
            Text(team.members.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        }
        team.joinCode?.let { code ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatJoinCode(code), style = monoStyle(14.sp, letterSpacing = 1.sp), color = c.highlight, modifier = Modifier.weight(1f))
                IconButton(onClick = { onCopyCode(code) }) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy ${team.teamName}'s join code", tint = c.textMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

