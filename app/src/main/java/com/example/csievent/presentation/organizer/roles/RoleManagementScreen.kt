package com.example.csievent.presentation.organizer.roles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.user.UserResponseDto
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
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.MessageEffect
import com.example.csievent.presentation.components.RefreshableBox
import com.example.csievent.presentation.components.SecondaryButton
import com.example.csievent.presentation.components.SegmentedTabs
import com.example.csievent.presentation.components.StatusChip
import com.example.csievent.ui.theme.CsiTheme

/** Organizers promote students to judges (and back). */
@Composable
fun RoleManagementScreen(
    navController: NavHostController,
    viewModel: RoleManagementViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var pending by remember { mutableStateOf<Pair<UserResponseDto, String>?>(null) }

    LaunchedEffect(Unit) { viewModel.loadUsers() }
    MessageEffect(state.successMessage ?: state.error?.takeIf { state.users.isNotEmpty() }, snackbar, viewModel::clearMessage)

    val students = state.users.count { it.role == "STUDENT" }
    val judges = state.users.count { it.role == "JUDGE" }
    val q = query.trim().lowercase()
    val shown = state.users
        .filter {
            when (filter) {
                1 -> it.role == "STUDENT"
                2 -> it.role == "JUDGE"
                else -> true
            }
        }
        .filter { q.isEmpty() || it.name.lowercase().contains(q) || it.email.lowercase().contains(q) }

    CsiScreen(
        snackbarHostState = snackbar,
        topBar = {
            CsiTopBar(
                title = "People",
                subtitle = "Make students judges for events",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.users.isEmpty() -> LoadingState(Modifier.padding(padding))
            state.error != null && state.users.isEmpty() ->
                ErrorState(state.error!!, onRetry = viewModel::loadUsers, modifier = Modifier.padding(padding))
            else -> RefreshableBox(
                refreshing = state.isLoading,
                onRefresh = viewModel::loadUsers,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(key = "search") {
                        CsiTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = "Search",
                            placeholder = "Name or email",
                            imeAction = ImeAction.Search,
                            trailing = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear search", tint = c.textMuted)
                                    }
                                } else {
                                    Icon(Icons.Rounded.Search, contentDescription = null, tint = c.textMuted)
                                }
                            }
                        )
                    }
                    item(key = "filter") {
                        SegmentedTabs(
                            options = listOf("All · ${state.users.size}", "Students · $students", "Judges · $judges"),
                            selected = filter,
                            onSelect = { filter = it }
                        )
                    }

                    if (shown.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                icon = Icons.Rounded.PersonSearch,
                                title = if (state.users.isEmpty()) "No users yet" else "No matches",
                                message = if (state.users.isEmpty()) "People appear here after they sign up in the app."
                                else "Try a different name or filter."
                            )
                        }
                    }

                    items(shown, key = { it.id }) { user ->
                        UserRow(
                            user = user,
                            enabled = !state.isLoading,
                            onChangeRole = { newRole -> pending = user to newRole }
                        )
                    }

                    item(key = "bottom") { BottomSpacer() }
                }
            }
        }
    }

    pending?.let { (user, newRole) ->
        val toJudge = newRole == "JUDGE"
        ConfirmDialog(
            title = if (toJudge) "Make ${user.name} a judge?" else "Make ${user.name} a student?",
            message = if (toJudge)
                "They'll see the judge screens next time they open the app. You can then assign them to events."
            else
                "They'll be removed from any events they're judging. Scores they already gave are kept.",
            confirmLabel = if (toJudge) "Make judge" else "Make student",
            onConfirm = { viewModel.updateRole(user.id, newRole) },
            onDismiss = { pending = null }
        )
    }
}

@Composable
private fun UserRow(user: UserResponseDto, enabled: Boolean, onChangeRole: (String) -> Unit) {
    val c = CsiTheme.colors
    val isJudge = user.role == "JUDGE"
    CsiCard(padding = 12) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(user.name, size = 40, emphasized = isJudge)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        user.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = c.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isJudge) StatusChip("Judge", ChipKind.HIGHLIGHT)
                }
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (user.role == "STUDENT" || isJudge) {
                SecondaryButton(
                    text = if (isJudge) "Make student" else "Make judge",
                    onClick = { onChangeRole(if (isJudge) "STUDENT" else "JUDGE") },
                    enabled = enabled,
                    height = 40
                )
            }
        }
    }
}
