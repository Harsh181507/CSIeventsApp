package com.example.csievent.presentation.organizer.roles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
fun RoleManagementScreen(
    navController: NavHostController,
    viewModel:     RoleManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👥 Manage Roles") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers() }) {
                        Icon(
                            imageVector        = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.users.isEmpty() -> {
                    Text(
                        text     = "No users found.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        // Section: Judges
                        val judges   = state.users.filter { it.role == "JUDGE" }
                        val students = state.users.filter { it.role == "STUDENT" }

                        if (judges.isNotEmpty()) {
                            item(key = "judges_header") {
                                Text(
                                    text  = "⚖️ Judges (${judges.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(items = judges, key = { "judge_${it.id}" }) { user ->
                                UserRoleCard(
                                    name        = user.name,
                                    email       = user.email,
                                    currentRole = "JUDGE",
                                    actionLabel = "Demote to Student",
                                    onAction    = { viewModel.updateRole(user.id, "STUDENT") }
                                )
                            }

                            item(key = "spacer") { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        // Section: Students
                        if (students.isNotEmpty()) {
                            item(key = "students_header") {
                                Text(
                                    text  = "🎓 Students (${students.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            items(items = students, key = { "student_${it.id}" }) { user ->
                                UserRoleCard(
                                    name        = user.name,
                                    email       = user.email,
                                    currentRole = "STUDENT",
                                    actionLabel = "Promote to Judge",
                                    onAction    = { viewModel.updateRole(user.id, "JUDGE") }
                                )
                            }
                        }
                    }
                }
            }

            // Banners
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                state.error?.let {
                    ErrorBanner(message = it, onDismiss = { viewModel.clearMessage() })
                }
                state.successMessage?.let {
                    SuccessBanner(message = it, onDismiss = { viewModel.clearMessage() })
                }
            }
        }
    }
}


@Composable
private fun UserRoleCard(
    name:        String,
    email:       String,
    currentRole: String,
    actionLabel: String,
    onAction:    () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text  = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onAction,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}