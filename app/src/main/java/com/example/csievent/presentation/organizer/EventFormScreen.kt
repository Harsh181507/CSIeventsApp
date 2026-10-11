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
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTextField
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.ErrorState
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.OutlinedIconButton
import com.example.csievent.presentation.components.PickerField
import com.example.csievent.presentation.components.PrimaryButton
import com.example.csievent.presentation.components.formatLongDate
import com.example.csievent.presentation.components.formatTime
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Create an event ([eventId] null) or edit one. Date, start time and venue are
 * required so students know when and where to turn up.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    navController: NavHostController,
    eventId: Long? = null,
    viewModel: EventFormViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val focus = LocalFocusManager.current
    val editing = eventId != null

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf<String?>(null) }   // yyyy-MM-dd
    var time by rememberSaveable { mutableStateOf<String?>(null) }   // HH:mm
    var venue by rememberSaveable { mutableStateOf("") }
    var teamSize by rememberSaveable { mutableIntStateOf(4) }
    var prefilled by rememberSaveable { mutableStateOf(!editing) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) { eventId?.let(viewModel::loadForEdit) }

    // Fill the form once with the event being edited
    LaunchedEffect(state.original) {
        val event = state.original ?: return@LaunchedEffect
        if (!prefilled) {
            title = event.title
            description = event.description.orEmpty()
            date = event.eventDate
            time = event.eventTime?.take(5)
            venue = event.venue.orEmpty()
            teamSize = event.maxTeamSize
            prefilled = true
        }
    }

    LaunchedEffect(state.savedEventId) {
        val id = state.savedEventId ?: return@LaunchedEffect
        if (editing) {
            navController.popBackStack()
        } else {
            // Open the new event right away so the organizer can add criteria
            navController.navigate("${Routes.ORGANIZER_EVENT}/$id") {
                popUpTo(Routes.CREATE_EVENT) { inclusive = true }
            }
        }
    }

    CsiScreen(
        topBar = {
            CsiTopBar(title = if (editing) "Edit event" else "New event", onBack = { navController.popBackStack() })
        }
    ) { padding ->
        when {
            editing && state.loadError != null && state.original == null ->
                ErrorState(state.loadError!!, onRetry = { viewModel.loadForEdit(eventId!!) }, modifier = Modifier.padding(padding))
            !prefilled -> LoadingState(Modifier.padding(padding))
            else -> Column(
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

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerField(
                        label = "Date",
                        value = date?.let { formatLongDate(it) },
                        placeholder = "Pick a date",
                        icon = Icons.Rounded.CalendarMonth,
                        onClick = {
                            focus.clearFocus()
                            showDatePicker = true
                        },
                        modifier = Modifier.weight(1.4f)
                    )
                    PickerField(
                        label = "Starts at",
                        value = formatTime(time),
                        placeholder = "Time",
                        icon = Icons.Rounded.Schedule,
                        onClick = {
                            focus.clearFocus()
                            showTimePicker = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                CsiTextField(
                    value = venue,
                    onValueChange = { venue = it.take(200) },
                    label = "Venue",
                    placeholder = "AB-1, Seminar Hall 2",
                    capitalization = KeyboardCapitalization.Words
                )

                CsiTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (optional)",
                    placeholder = "Theme, rules, what to bring…",
                    singleLine = false,
                    minLines = 3,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
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
                    text = if (editing) "Save changes" else "Create event",
                    onClick = {
                        focus.clearFocus()
                        viewModel.save(
                            eventId,
                            EventFormInput(
                                title = title,
                                description = description,
                                date = date ?: return@PrimaryButton,
                                time = time ?: return@PrimaryButton,
                                venue = venue,
                                maxTeamSize = teamSize
                            )
                        )
                    },
                    enabled = title.isNotBlank() && date != null && time != null && venue.isNotBlank(),
                    loading = state.isSaving
                )
                if (!editing) {
                    Text(
                        "Next you'll add judging criteria and assign judges.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSubtle
                    )
                }
                BottomSpacer(8)
            }
        }
    }

    if (showDatePicker) {
        val todayUtc = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        // An edited event may keep its original date even if that has passed
        val originalUtc = state.original?.eventDate
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
                ?: todayUtc,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis >= todayUtc || utcTimeMillis == originalUtc
                override fun isSelectableYear(year: Int) =
                    year >= LocalDate.now().year || originalUtc != null
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text("OK", color = c.highlight, style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
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

    if (showTimePicker) {
        val initial = time?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: LocalTime.of(10, 0)
        val pickerState = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = false)
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(shape = MaterialTheme.shapes.extraLarge, color = c.surface) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("Start time", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
                    TimePicker(
                        state = pickerState,
                        colors = TimePickerDefaults.colors(
                            containerColor = c.surface,
                            clockDialColor = c.raised,
                            clockDialSelectedContentColor = c.onAccent,
                            clockDialUnselectedContentColor = c.text,
                            selectorColor = c.accent,
                            periodSelectorBorderColor = c.line,
                            periodSelectorSelectedContainerColor = c.highlightContainer,
                            periodSelectorUnselectedContainerColor = Color.Transparent,
                            periodSelectorSelectedContentColor = c.onHighlightContainer,
                            periodSelectorUnselectedContentColor = c.textMuted,
                            timeSelectorSelectedContainerColor = c.highlightContainer,
                            timeSelectorUnselectedContainerColor = c.raised,
                            timeSelectorSelectedContentColor = c.onHighlightContainer,
                            timeSelectorUnselectedContentColor = c.text
                        )
                    )
                    // Not fillMaxWidth: the dialog should wrap the clock, not span the screen
                    Row(Modifier.align(Alignment.End)) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel", color = c.textMuted, style = MaterialTheme.typography.labelLarge)
                        }
                        TextButton(onClick = {
                            time = LocalTime.of(pickerState.hour, pickerState.minute).toString() // HH:mm
                            showTimePicker = false
                        }) { Text("OK", color = c.highlight, style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }
        }
    }
}
