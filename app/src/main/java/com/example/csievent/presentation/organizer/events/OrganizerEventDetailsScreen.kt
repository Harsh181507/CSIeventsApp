package com.example.csievent.presentation.organizer.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.presentation.components.ErrorBanner
import com.example.csievent.presentation.components.SuccessBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerEventDetailsScreen(
    eventId: Long,
    navController: NavHostController,
    viewModel: OrganizerEventDetailsViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()

    var title by remember { mutableStateOf("") }
    var maxScore by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchCriteria(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                actions = {
                    TextButton(
                        onClick = { viewModel.fetchCriteria(eventId) }
                    ) {
                        Text("Refresh")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {

                    Column {

                        Text(
                            text = "Add Criteria",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Criteria Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = maxScore,
                            onValueChange = { maxScore = it },
                            label = { Text("Max Score") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (title.isNotBlank() && maxScore.isNotBlank()) {
                                    viewModel.createCriteria(
                                        eventId,
                                        title,
                                        maxScore.toInt()
                                    )
                                    title = ""
                                    maxScore = ""
                                }
                            }
                        ) {
                            Text("Add Criteria")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Criteria List",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn {

                            items(state.criteria) { criteria ->

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(text = criteria.title)
                                        Text(text = "Max Score: ${criteria.maxScore}")
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
                    onDismiss = { viewModel.clearMessage() }
                )
            }

            state.successMessage?.let {
                SuccessBanner(
                    message = it,
                    onDismiss = { viewModel.clearMessage() }
                )
            }
        }
    }
}
