package com.example.csievent.presentation.student.events

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
import com.example.csievent.presentation.components.SuccessBanner

@Composable
fun StudentEventsScreen(
    viewModel: StudentEventsViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.error != null -> {
                ErrorBanner(
                    message = state.error ?: "",
                    onDismiss = { viewModel.clearMessage() }
                )
            }

            else -> {

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // Success Banner
                    state.successMessage?.let {
                        SuccessBanner(
                            message = it,
                            onDismiss = { viewModel.clearMessage() }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(state.events) { event ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(text = event.description ?: "")

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(text = "📅 Date: ${event.eventDate}")
                                    Text(text = "👥 Max Team Size: ${event.maxTeamSize}")

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            viewModel.registerForEvent(event.id)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Register")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
