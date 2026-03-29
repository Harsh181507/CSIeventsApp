package com.example.csievent.presentation.organizer.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.csievent.presentation.components.ErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    eventId: Long,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadLeaderboard(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏆 Leaderboard") }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Button(
                    onClick = { viewModel.loadLeaderboard(eventId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔄 Refresh")
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.leaderboard.isEmpty() && state.error == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No scores submitted yet.")
                        }
                    }

                    else -> {
                        LazyColumn {
                            items(state.leaderboard) { item ->

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Column {
                                            Text(
                                                text = "#${item.rank} ${item.teamName}",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Total Score: ${item.totalScore}"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            state.error?.let {
                ErrorBanner(
                    message = it,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}
