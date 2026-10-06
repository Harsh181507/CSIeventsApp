package com.example.csievent.presentation.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.BrandTopBar
import com.example.csievent.presentation.components.ChipKind
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.EmptyState
import com.example.csievent.presentation.components.ErrorState
import com.example.csievent.presentation.components.EventRow
import com.example.csievent.presentation.components.JoinCodeBox
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.MessageEffect
import com.example.csievent.presentation.components.RefreshableBox
import com.example.csievent.presentation.components.SegmentedTabs
import com.example.csievent.presentation.components.StatusChip
import com.example.csievent.presentation.components.firstName
import com.example.csievent.presentation.components.formatDate
import com.example.csievent.presentation.components.shouldConfirmCopy
import com.example.csievent.presentation.components.formatDayMonth
import com.example.csievent.presentation.components.shareText
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle

@Composable
fun StudentHomeScreen(
    navController: NavHostController,
    viewModel: StudentHomeViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }

    // Reloads each time the screen is shown again (e.g. after joining a team)
    LaunchedEffect(Unit) { viewModel.load() }
    MessageEffect(state.message, snackbar, viewModel::clearMessage)

    val upcoming = state.events.filter { !it.scoringLocked }.sortedBy { it.eventDate ?: "9999" }
    val past = state.events.filter { it.scoringLocked }
    val hero = upcoming.firstOrNull { state.myTeams.containsKey(it.id) }
    val shown = if (tab == 0) upcoming else past

    fun openEvent(event: EventResponseDto) {
        if (event.scoringLocked) navController.navigate("${Routes.RESULTS}/${event.id}")
        else navController.navigate("${Routes.STUDENT_EVENT}/${event.id}")
    }

    CsiScreen(
        snackbarHostState = snackbar,
        topBar = { BrandTopBar(userName = state.name, onProfile = { navController.navigate(Routes.PROFILE) }) }
    ) { padding ->
        when {
            state.isLoading && state.events.isEmpty() -> LoadingState(Modifier.padding(padding))
            state.error != null && state.events.isEmpty() ->
                ErrorState(state.error!!, onRetry = viewModel::load, modifier = Modifier.padding(padding))
            else -> RefreshableBox(
                refreshing = state.isLoading,
                onRefresh = viewModel::load,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "greeting") {
                        Text(
                            "Hey ${firstName(state.name)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = c.text,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (hero != null) {
                        item(key = "hero") {
                            val team = state.myTeams.getValue(hero.id)
                            NextUpCard(
                                event = hero,
                                team = team,
                                onOpen = { openEvent(hero) },
                                onCopy = { code ->
                                    clipboard.setText(AnnotatedString(code))
                                    if (shouldConfirmCopy()) viewModel.showMessage("Join code copied")
                                },
                                onShare = { code ->
                                    shareText(
                                        context,
                                        "Join my team \"${team.teamName}\" for ${hero.title} on CSI Events. Code: $code"
                                    )
                                }
                            )
                        }
                    }

                    item(key = "tabs") {
                        SegmentedTabs(
                            options = listOf("Upcoming · ${upcoming.size}", "Past · ${past.size}"),
                            selected = tab,
                            onSelect = { tab = it },
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (shown.isEmpty()) {
                        item(key = "empty") {
                            if (tab == 0) {
                                EmptyState(
                                    icon = Icons.Rounded.EventAvailable,
                                    title = "No upcoming events",
                                    message = "New events from CSI will show up here. Pull down to refresh."
                                )
                            } else {
                                EmptyState(
                                    icon = Icons.Rounded.EmojiEvents,
                                    title = "No results yet",
                                    message = "Events appear here once judging is complete."
                                )
                            }
                        }
                    } else {
                        items(shown, key = { it.id }) { event ->
                            val team = state.myTeams[event.id]
                            EventRow(
                                title = event.title,
                                detail = "${formatDate(event.eventDate)} · teams of up to ${event.maxTeamSize}",
                                chipText = when {
                                    event.scoringLocked -> "Results"
                                    team != null -> "You're in"
                                    else -> "Join"
                                },
                                chipKind = when {
                                    event.scoringLocked -> ChipKind.HIGHLIGHT
                                    team != null -> ChipKind.SUCCESS
                                    else -> ChipKind.OUTLINE
                                },
                                onClick = { openEvent(event) }
                            )
                        }
                    }

                    item(key = "bottom") { BottomSpacer() }
                }
            }
        }
    }
}

/** The next event the student has a team for, with their join code. */
@Composable
private fun NextUpCard(
    event: EventResponseDto,
    team: TeamResponseDto,
    onOpen: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    val c = CsiTheme.colors
    CsiCard(highlighted = true, onClick = onOpen, onClickLabel = "Open event") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                formatDayMonth(event.eventDate),
                style = monoStyle(13.sp, letterSpacing = 0.5.sp),
                color = c.highlight,
                modifier = Modifier.weight(1f)
            )
            StatusChip("You're in", ChipKind.SUCCESS)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(event.title, style = MaterialTheme.typography.headlineSmall, color = c.text)
            Text(
                "${team.teamName} · ${team.members.size} of ${event.maxTeamSize} members",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMuted
            )
        }
        team.joinCode?.let { code ->
            JoinCodeBox(code = code, onCopy = { onCopy(code) }, onShare = { onShare(code) })
        }
    }
}
