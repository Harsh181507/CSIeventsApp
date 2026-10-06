package com.example.csievent.presentation.student.teams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.presentation.components.Avatar
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.ChipKind
import com.example.csievent.presentation.components.ConfirmDialog
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTextField
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.EmptyState
import com.example.csievent.presentation.components.ErrorState
import com.example.csievent.presentation.components.GhostButton
import com.example.csievent.presentation.components.JoinCodeBox
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.MessageEffect
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.PrimaryButton
import com.example.csievent.presentation.components.RefreshableBox
import com.example.csievent.presentation.components.SecondaryButton
import com.example.csievent.presentation.components.SectionHeader
import com.example.csievent.presentation.components.StatusChip
import com.example.csievent.presentation.components.formatDate
import com.example.csievent.presentation.components.shouldConfirmCopy
import com.example.csievent.presentation.components.shareText
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle

/**
 * One event for a student: their team (members, join code, leave) or the
 * ways to get into one (join with a code, create a team), plus the other
 * teams in the event.
 */
@Composable
fun StudentEventScreen(
    eventId: Long,
    navController: NavHostController,
    viewModel: StudentTeamsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var confirmLeave by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) { viewModel.loadTeams(eventId) }
    MessageEffect(state.message, snackbar, viewModel::clearMessage)

    val event = state.event
    val myTeam = state.myTeam
    val locked = event?.scoringLocked == true

    CsiScreen(
        snackbarHostState = snackbar,
        topBar = {
            CsiTopBar(
                title = event?.title ?: "Event",
                subtitle = event?.let { formatDate(it.eventDate) },
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        when {
            state.isLoading && event == null -> LoadingState(Modifier.padding(padding))
            state.error != null && event == null ->
                ErrorState(state.error!!, onRetry = { viewModel.loadTeams(eventId) }, modifier = Modifier.padding(padding))
            else -> RefreshableBox(
                refreshing = state.isLoading,
                onRefresh = { viewModel.loadTeams(eventId) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (event != null) {
                        item(key = "info") { EventInfo(event, state.teams.size) }
                    }

                    if (locked) {
                        item(key = "locked") {
                            NoticeBanner(
                                text = "Judging is complete for this event.",
                                icon = Icons.Rounded.EmojiEvents,
                                actionLabel = "Results",
                                onAction = { navController.navigate("${Routes.RESULTS}/$eventId") }
                            )
                        }
                    }

                    when {
                        myTeam != null -> item(key = "myteam") {
                            MyTeamCard(
                                team = myTeam,
                                maxSize = event?.maxTeamSize ?: myTeam.members.size,
                                myUserId = state.myUserId,
                                locked = locked,
                                working = state.isWorking,
                                onCopy = { code ->
                                    clipboard.setText(AnnotatedString(code))
                                    if (shouldConfirmCopy()) viewModel.showMessage("Join code copied")
                                },
                                onShare = { code ->
                                    shareText(
                                        context,
                                        "Join my team \"${myTeam.teamName}\" for ${event?.title ?: "the event"} on CSI Events. Code: $code"
                                    )
                                },
                                onLeave = { confirmLeave = true }
                            )
                        }
                        !locked -> item(key = "getin") {
                            GetIntoTeam(
                                working = state.isWorking,
                                onJoin = { code -> viewModel.joinTeamByCode(code, eventId) },
                                onCreate = { name -> viewModel.createTeam(eventId, name) }
                            )
                        }
                        else -> item(key = "noteam") {
                            EmptyState(
                                icon = Icons.Rounded.Lock,
                                title = "You weren't in a team",
                                message = "Teams can't be created or joined after judging is complete."
                            )
                        }
                    }

                    val others = state.teams.filter { it.id != myTeam?.id }
                    item(key = "others_header") {
                        SectionHeader(
                            title = if (myTeam != null) "Other teams" else "Teams in this event",
                            trailing = others.size.toString(),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (others.isEmpty()) {
                        item(key = "others_empty") {
                            Text(
                                if (myTeam != null) "No other teams yet." else "No teams yet — be the first to create one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CsiTheme.colors.textMuted
                            )
                        }
                    } else {
                        items(others, key = { it.id }) { team ->
                            OtherTeamRow(team, event?.maxTeamSize)
                        }
                    }

                    item(key = "bottom") { BottomSpacer() }
                }
            }
        }
    }

    if (confirmLeave && myTeam != null) {
        ConfirmDialog(
            title = "Leave ${myTeam.teamName}?",
            message = "You can join another team afterwards with its code.",
            confirmLabel = "Leave team",
            destructive = true,
            onConfirm = { viewModel.leaveTeam(myTeam.id, eventId) },
            onDismiss = { confirmLeave = false }
        )
    }
}

@Composable
private fun EventInfo(event: EventResponseDto, teamCount: Int) {
    val c = CsiTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("Teams of up to ${event.maxTeamSize}", ChipKind.NEUTRAL)
            StatusChip("$teamCount teams", ChipKind.NEUTRAL)
        }
        if (!event.description.isNullOrBlank()) {
            Text(event.description, style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
        }
    }
}

@Composable
private fun MyTeamCard(
    team: TeamResponseDto,
    maxSize: Int,
    myUserId: Long?,
    locked: Boolean,
    working: Boolean,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onLeave: () -> Unit
) {
    val c = CsiTheme.colors
    val iAmLeader = myUserId != null && team.leaderId == myUserId

    CsiCard(highlighted = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("YOUR TEAM", style = MaterialTheme.typography.labelSmall, color = c.textMuted, modifier = Modifier.weight(1f))
            StatusChip("${team.members.size} / $maxSize members", ChipKind.SUCCESS)
        }
        Text(team.teamName, style = MaterialTheme.typography.headlineSmall, color = c.text)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            team.members.forEach { member ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Avatar(member, size = 32, emphasized = member == team.leaderName)
                    Text(member, style = MaterialTheme.typography.bodyLarge, color = c.text, modifier = Modifier.weight(1f))
                    if (member == team.leaderName) StatusChip("Leader", ChipKind.HIGHLIGHT)
                }
            }
        }

        team.joinCode?.let { code ->
            JoinCodeBox(code = code, onCopy = { onCopy(code) }, onShare = { onShare(code) })
        }

        if (!locked) {
            if (iAmLeader) {
                Text(
                    "Share the code to add teammates. As the leader you stay in the team.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSubtle
                )
            } else {
                GhostButton(text = "Leave team", color = c.danger, onClick = onLeave, enabled = !working)
            }
        }
    }
}

@Composable
private fun GetIntoTeam(
    working: Boolean,
    onJoin: (String) -> Unit,
    onCreate: (String) -> Unit
) {
    val c = CsiTheme.colors
    val focus = LocalFocusManager.current
    var code by rememberSaveable { mutableStateOf("") }
    var teamName by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CsiCard {
            Text("Join a team", style = MaterialTheme.typography.titleMedium, color = c.text)
            Text("Ask your team leader for their 8-character code.", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
            CsiTextField(
                value = code,
                onValueChange = { code = it.filter(Char::isLetterOrDigit).uppercase().take(8) },
                label = "Join code",
                placeholder = "A3F9B2C1",
                capitalization = KeyboardCapitalization.Characters,
                textStyle = monoStyle(18.sp, letterSpacing = 2.sp),
                imeAction = ImeAction.Done,
                onImeAction = { if (code.length == 8) { focus.clearFocus(); onJoin(code) } }
            )
            PrimaryButton(
                text = "Join team",
                onClick = { focus.clearFocus(); onJoin(code) },
                enabled = code.length == 8,
                loading = working
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("or", style = MaterialTheme.typography.bodyMedium, color = c.textSubtle)
        }

        CsiCard {
            Text("Start a new team", style = MaterialTheme.typography.titleMedium, color = c.text)
            Text("You'll be the leader and get a code to share.", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
            CsiTextField(
                value = teamName,
                onValueChange = { teamName = it.take(100) },
                label = "Team name",
                placeholder = "Byte Busters",
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
                onImeAction = { if (teamName.isNotBlank()) { focus.clearFocus(); onCreate(teamName) } }
            )
            SecondaryButton(
                text = "Create team",
                onClick = { focus.clearFocus(); onCreate(teamName) },
                enabled = teamName.isNotBlank() && !working,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OtherTeamRow(team: TeamResponseDto, maxSize: Int?) {
    val c = CsiTheme.colors
    CsiCard(padding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(team.teamName, size = 36)
            Column(Modifier.weight(1f)) {
                Text(team.teamName, style = MaterialTheme.typography.titleSmall, color = c.text)
                Text(
                    team.leaderName?.let { "Led by $it" } ?: "No leader",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted
                )
            }
            val full = maxSize != null && team.members.size >= maxSize
            StatusChip(
                if (maxSize != null) "${team.members.size}/$maxSize" else "${team.members.size}",
                if (full) ChipKind.NEUTRAL else ChipKind.OUTLINE
            )
        }
    }
}

