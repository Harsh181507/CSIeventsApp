package com.example.csievent.presentation.organizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.example.csievent.presentation.components.StatTile
import com.example.csievent.presentation.components.firstName
import com.example.csievent.presentation.components.formatDate
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.ControlShape
import com.example.csievent.ui.theme.CsiTheme

@Composable
fun OrganizerHomeScreen(
    navController: NavHostController,
    viewModel: OrganizerHomeViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()

    // Reloads when returning from an event (it may have been created / deleted)
    LaunchedEffect(Unit) { viewModel.load() }

    val open = state.events.count { !it.scoringLocked }
    val locked = state.events.size - open

    CsiScreen(
        topBar = {
            BrandTopBar(
                userName = state.name,
                onProfile = { navController.navigate(Routes.PROFILE) },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.ROLE_MANAGEMENT) }) {
                        Icon(Icons.Rounded.Groups, contentDescription = "People and roles", tint = c.text)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.CREATE_EVENT) },
                containerColor = c.accent,
                contentColor = c.onAccent,
                shape = ControlShape,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text("New event", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
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
                            Text("Organizer", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                        }
                    }

                    item(key = "stats") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatTile(state.events.size.toString(), "Events", Modifier.weight(1f))
                            StatTile(open.toString(), "Open", Modifier.weight(1f))
                            StatTile(locked.toString(), "Completed", Modifier.weight(1f))
                        }
                    }

                    item(key = "header") { SectionHeader("Events", modifier = Modifier.padding(top = 8.dp)) }

                    if (state.events.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                icon = Icons.AutoMirrored.Rounded.EventNote,
                                title = "No events yet",
                                message = "Create your first event, add judging criteria and assign judges.",
                                actionLabel = "Create event",
                                onAction = { navController.navigate(Routes.CREATE_EVENT) }
                            )
                        }
                    }

                    items(state.events, key = { it.id }) { event ->
                        EventRow(
                            title = event.title,
                            detail = "${formatDate(event.eventDate)} · teams of up to ${event.maxTeamSize}",
                            chipText = if (event.scoringLocked) "Locked" else "Open",
                            chipKind = if (event.scoringLocked) ChipKind.NEUTRAL else ChipKind.SUCCESS,
                            onClick = { navController.navigate("${Routes.ORGANIZER_EVENT}/${event.id}") }
                        )
                    }

                    // Room for the floating button
                    item(key = "bottom") { BottomSpacer(88) }
                }
            }
        }
    }
}
