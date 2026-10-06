package com.example.csievent.presentation.organizer.criteria

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.ConfirmDialog
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTextField
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.ErrorState
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.MessageEffect
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.PrimaryButton
import com.example.csievent.presentation.components.SectionHeader
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle

@Composable
fun CriteriaScreen(
    eventId: Long,
    navController: NavHostController,
    viewModel: CriteriaViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val focus = LocalFocusManager.current

    var title by rememberSaveable { mutableStateOf("") }
    var maxScore by rememberSaveable { mutableStateOf("10") }
    var toDelete by remember { mutableStateOf<CriteriaResponseDto?>(null) }

    LaunchedEffect(eventId) { viewModel.load(eventId) }
    MessageEffect(state.message, snackbar, viewModel::clearMessage)
    LaunchedEffect(state.addedCount) {
        if (state.addedCount > 0) {
            title = ""
            maxScore = "10"
        }
    }

    val locked = state.event?.scoringLocked == true
    val max = maxScore.toIntOrNull()
    val maxError = when {
        maxScore.isBlank() -> null
        max == null || max < 1 -> "Enter a number from 1 to 1000"
        max > 1000 -> "Max is 1000"
        else -> null
    }
    val canAdd = title.isNotBlank() && max != null && maxError == null && !locked

    CsiScreen(
        snackbarHostState = snackbar,
        topBar = {
            CsiTopBar(
                title = "Judging criteria",
                subtitle = state.event?.title,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.criteria.isEmpty() && state.event == null -> LoadingState(Modifier.padding(padding))
            state.error != null && state.criteria.isEmpty() ->
                ErrorState(state.error!!, onRetry = { viewModel.load(eventId) }, modifier = Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (locked) {
                    item(key = "locked") {
                        NoticeBanner("Scoring is locked. Reopen scoring to change criteria.", icon = Icons.Rounded.Lock)
                    }
                } else {
                    item(key = "form") {
                        CsiCard {
                            Text("Add a criterion", style = MaterialTheme.typography.titleMedium, color = c.text)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CsiTextField(
                                    value = title,
                                    onValueChange = { title = it.take(255) },
                                    label = "Name",
                                    placeholder = "Innovation",
                                    capitalization = KeyboardCapitalization.Sentences,
                                    modifier = Modifier.weight(1f)
                                )
                                CsiTextField(
                                    value = maxScore,
                                    onValueChange = { maxScore = it.filter(Char::isDigit).take(4) },
                                    label = "Out of",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                    onImeAction = {
                                        if (canAdd) { focus.clearFocus(); viewModel.add(eventId, title, max!!) }
                                    },
                                    modifier = Modifier.width(96.dp)
                                )
                            }
                            maxError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.danger) }
                            PrimaryButton(
                                text = "Add criterion",
                                onClick = { focus.clearFocus(); viewModel.add(eventId, title, max ?: return@PrimaryButton) },
                                enabled = canAdd,
                                loading = state.isSubmitting
                            )
                        }
                    }
                }

                item(key = "header") {
                    SectionHeader(
                        "Criteria",
                        trailing = if (state.criteria.isEmpty()) null else "Total ${state.criteria.sumOf { it.maxScore }} points",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (state.criteria.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "No criteria yet. Typical ones: Innovation, Design, Technical depth, Presentation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textMuted
                        )
                    }
                }

                itemsIndexed(state.criteria, key = { _, item -> item.id }) { index, criterion ->
                    CsiCard(padding = 12) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${index + 1}", style = monoStyle(14.sp), color = c.textSubtle, modifier = Modifier.width(20.dp))
                            Column(Modifier.weight(1f)) {
                                Text(criterion.title, style = MaterialTheme.typography.titleSmall, color = c.text)
                                Text("out of ${criterion.maxScore}", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                            }
                            if (!locked) {
                                IconButton(onClick = { toDelete = criterion }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove ${criterion.title}", tint = c.textMuted)
                                }
                            }
                        }
                    }
                }

                item(key = "bottom") { BottomSpacer() }
            }
        }
    }

    toDelete?.let { criterion ->
        ConfirmDialog(
            title = "Remove \"${criterion.title}\"?",
            message = "Any scores judges already gave for it will be deleted too.",
            confirmLabel = "Remove",
            destructive = true,
            onConfirm = { viewModel.delete(criterion.id) },
            onDismiss = { toDelete = null }
        )
    }
}
