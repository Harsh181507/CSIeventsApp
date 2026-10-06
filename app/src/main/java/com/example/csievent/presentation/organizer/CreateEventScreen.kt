package com.example.csievent.presentation.organizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTextField
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.OutlinedIconButton
import com.example.csievent.presentation.components.PickerField
import com.example.csievent.presentation.components.PrimaryButton
import com.example.csievent.presentation.components.formatDate
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavHostController,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val focus = LocalFocusManager.current

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf<String?>(null) }   // yyyy-MM-dd
    var teamSize by rememberSaveable { mutableIntStateOf(4) }
    var showPicker by remember { mutableStateOf(false) }

    // Open the new event right away so the organizer can add criteria
    LaunchedEffect(state.createdEventId) {
        state.createdEventId?.let { id ->
            navController.navigate("${Routes.ORGANIZER_EVENT}/$id") {
                popUpTo(Routes.CREATE_EVENT) { inclusive = true }
            }
        }
    }

    CsiScreen(
        topBar = { CsiTopBar(title = "New event", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CsiTextField(
                value = title,
                onValueChange = { title = it.take(255) },
                label = "Event name",
                placeholder = "HackVerse 2026",
                capitalization = KeyboardCapitalization.Words
            )
            CsiTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                placeholder = "Theme, rules, venue…",
                singleLine = false,
                minLines = 3,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default
            )

            PickerField(
                label = "Date",
                value = date?.let { formatDate(it) },
                placeholder = "Pick a date",
                icon = Icons.Rounded.CalendarMonth,
                onClick = {
                    focus.clearFocus()
                    showPicker = true
                }
            )

            CsiCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Max team size", style = MaterialTheme.typography.titleSmall, color = c.text)
                        Text("Students per team", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    }
                    OutlinedIconButton(
                        icon = Icons.Rounded.Remove,
                        contentDescription = "Smaller teams",
                        onClick = { teamSize = (teamSize - 1).coerceAtLeast(1) },
                        enabled = teamSize > 1
                    )
                    Text(
                        "$teamSize",
                        style = monoStyle(22.sp),
                        color = c.text,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    OutlinedIconButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = "Bigger teams",
                        onClick = { teamSize = (teamSize + 1).coerceAtMost(50) },
                        enabled = teamSize < 50
                    )
                }
            }

            state.error?.let { NoticeBanner(text = it, icon = Icons.Rounded.ErrorOutline, danger = true) }

            PrimaryButton(
                text = "Create event",
                onClick = {
                    focus.clearFocus()
                    viewModel.createEvent(title, description, date ?: return@PrimaryButton, teamSize)
                },
                enabled = title.isNotBlank() && date != null,
                loading = state.isLoading
            )
            Text(
                "Next you'll add judging criteria and assign judges.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSubtle
            )
            BottomSpacer(8)
        }
    }

    if (showPicker) {
        val todayUtc = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
                ?: todayUtc,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtc
                override fun isSelectableYear(year: Int) = year >= LocalDate.now().year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showPicker = false
                }) { Text("OK", color = c.highlight, style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = c.textMuted, style = MaterialTheme.typography.labelLarge)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = c.surface)
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = c.surface,
                    titleContentColor = c.textMuted,
                    headlineContentColor = c.text,
                    weekdayContentColor = c.textMuted,
                    dayContentColor = c.text,
                    disabledDayContentColor = c.textSubtle,
                    selectedDayContainerColor = c.accent,
                    selectedDayContentColor = c.onAccent,
                    todayContentColor = c.highlight,
                    todayDateBorderColor = c.highlight,
                    yearContentColor = c.text,
                    selectedYearContainerColor = c.accent,
                    selectedYearContentColor = c.onAccent,
                    navigationContentColor = c.text,
                    subheadContentColor = c.textMuted,
                    dividerColor = c.line
                )
            )
        }
    }
}
