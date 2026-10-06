package com.example.csievent.presentation.judge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.BrandTopBar
import com.example.csievent.presentation.components.ChipKind
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.EmptyState
import com.example.csievent.presentation.components.ErrorState
import com.example.csievent.presentation.components.EventRow
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.RefreshableBox
import com.example.csievent.presentation.components.SectionHeader
import com.example.csievent.presentation.components.firstName
import com.example.csievent.presentation.components.formatDate
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CsiTheme

@Composable
fun JudgeHomeScreen(
    navController: NavHostController,
    viewModel: JudgeHomeViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    val open = state.events.filter { !it.scoringLocked }
    val locked = state.events.filter { it.scoringLocked }

    CsiScreen(
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
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Hey ${firstName(state.name)}", style = MaterialTheme.typography.headlineMedium, color = c.text)
                            Text("Here are the events you're judging.", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                        }
                    }

                    if (state.events.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                icon = Icons.Rounded.Gavel,
                                title = "No events assigned yet",
                                message = "When an organizer assigns you to an event it will appear here. Pull down to refresh."
                            )
                        }
                    }

                    if (open.isNotEmpty()) {
                        item(key = "open_header") { SectionHeader("Scoring open", trailing = open.size.toString(), modifier = Modifier.padding(top = 8.dp)) }
                        items(open, key = { "o${it.id}" }) { event ->
                            EventRow(
                                title = event.title,
                                detail = formatDate(event.eventDate),
                                chipText = "Score teams",
                                chipKind = ChipKind.OUTLINE,
                                highlighted = true,
                                onClick = { navController.navigate("${Routes.JUDGE_TEAMS}/${event.id}") }
                            )
                        }
                    }

                    if (locked.isNotEmpty()) {
                        item(key = "locked_header") { SectionHeader("Completed", trailing = locked.size.toString(), modifier = Modifier.padding(top = 8.dp)) }
                        items(locked, key = { "l${it.id}" }) { event ->
                            EventRow(
                                title = event.title,
                                detail = formatDate(event.eventDate),
                                chipText = "Results",
                                chipKind = ChipKind.HIGHLIGHT,
                                onClick = { navController.navigate("${Routes.RESULTS}/${event.id}") }
                            )
                        }
                    }

                    item(key = "bottom") { BottomSpacer() }
                }
            }
        }
    }
}
