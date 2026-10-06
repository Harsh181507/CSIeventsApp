package com.example.csievent.presentation.judge.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.ChipKind
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.EmptyState
import com.example.csievent.presentation.components.ErrorState
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.PrimaryButton
import com.example.csievent.presentation.components.ProgressBar
import com.example.csievent.presentation.components.RefreshableBox
import com.example.csievent.presentation.components.StatusChip
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.ControlShape
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle

/** Teams a judge has to score in one event, with progress. */
@Composable
fun JudgeTeamsScreen(
    eventId: Long,
    navController: NavHostController,
    viewModel: JudgeTeamsViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()

    // Reloads when coming back from scoring, so progress is up to date
    LaunchedEffect(eventId) { viewModel.loadTeams(eventId) }

    val scored = state.teams.count { it.id in state.scoredTeamIds }
    val total = state.teams.size
    val locked = state.event?.scoringLocked == true
    val nextTeam = state.teams.firstOrNull { it.id !in state.scoredTeamIds }

    fun openTeam(teamId: Long) = navController.navigate("${Routes.JUDGE_SCORING}/$eventId/$teamId")

    CsiScreen(
        topBar = {
            CsiTopBar(
                title = state.event?.title ?: "Teams",
                subtitle = if (total > 0) "$scored of $total teams scored" else null,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.teams.isEmpty() -> LoadingState(Modifier.padding(padding))
            state.error != null && state.teams.isEmpty() ->
                ErrorState(state.error!!, onRetry = { viewModel.loadTeams(eventId) }, modifier = Modifier.padding(padding))
            else -> RefreshableBox(
                refreshing = state.isLoading,
                onRefresh = { viewModel.loadTeams(eventId) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (total > 0) {
                        item(key = "progress") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ProgressBar(fraction = scored.toFloat() / total)
                                when {
                                    locked -> NoticeBanner("Scoring is locked for this event.", icon = Icons.Rounded.Lock)
                                    state.criteriaCount == 0 -> NoticeBanner(
                                        "The organizer hasn't added judging criteria yet. Check back soon.",
                                        icon = Icons.Rounded.Lock
                                    )
                                    nextTeam != null -> PrimaryButton(
                                        text = if (scored == 0) "Start scoring" else "Continue with ${nextTeam.teamName}",
                                        icon = Icons.Rounded.PlayArrow,
                                        onClick = { openTeam(nextTeam.id) }
                                    )
                                    else -> NoticeBanner("All teams scored. You can still edit any score.", icon = Icons.Rounded.Groups)
                                }
                            }
                        }
                    }

                    if (state.teams.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                icon = Icons.Rounded.Groups,
                                title = "No teams yet",
                                message = "Teams appear here once students form them."
                            )
                        }
                    }

                    itemsIndexed(state.teams, key = { _, t -> t.id }) { index, team ->
                        val isScored = team.id in state.scoredTeamIds
                        CsiCard(onClick = { openTeam(team.id) }, onClickLabel = "Score ${team.teamName}", padding = 14) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(ControlShape)
                                        .background(if (isScored) c.successContainer else c.raised),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        style = monoStyle(15.sp),
                                        color = if (isScored) c.success else c.textMuted
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(team.teamName, style = MaterialTheme.typography.titleSmall, color = c.text)
                                    Text(
                                        "${team.members.size} member" + if (team.members.size == 1) "" else "s",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textMuted
                                    )
                                }
                                if (isScored) StatusChip("Scored", ChipKind.SUCCESS)
                                else StatusChip("To score", ChipKind.OUTLINE)
                            }
                        }
                    }

                    item(key = "bottom") { BottomSpacer() }
                }
            }
        }
    }
}
