package com.example.csievent.presentation.organizer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerDashboardScreen(
    navController: NavHostController,
    viewModel:     OrganizerEventsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { viewModel.fetchEvents() }

    // Lock / unlock / delete all ask for confirmation first
    var pendingAction by remember { mutableStateOf<Pair<EventAction, EventResponseDto>?>(null) }

    val inf = rememberInfiniteTransition(label = "war_room")

    // Nebula + atmosphere
    val nebX by inf.animateFloat(0f, 40f,
        infiniteRepeatable(tween(12000, easing = EaseInOutSine), RepeatMode.Reverse), "nx")
    val nebY by inf.animateFloat(0f, 25f,
        infiniteRepeatable(tween(9000, easing = EaseInOutSine), RepeatMode.Reverse), "ny")
    val twinkle by inf.animateFloat(0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(5000, easing = LinearEasing)), "tw")

    // Console cursor blink
    val cursor by inf.animateFloat(1f, 0f,
        infiniteRepeatable(tween(600), RepeatMode.Reverse), "cur")

    // Grid pulse
    val gridPulse by inf.animateFloat(0.3f, 0.6f,
        infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse), "gp")

    // Header
    var headerIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); headerIn = true }
    val headerA by animateFloatAsState(
        targetValue = if (headerIn) 1f else 0f, animationSpec = tween(800), label = "ha")
    val headerY by animateFloatAsState(
        targetValue = if (headerIn) 0f else -60f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "hy")

    // Count up
    var displayCount by remember { mutableStateOf(0) }
    LaunchedEffect(state.events.size) {
        val t = state.events.size; val s = displayCount
        repeat(20) { i -> delay(25); displayCount = s + ((t - s) * (i + 1) / 20) }
        displayCount = state.events.size
    }

    // Live console log lines
    val consoleLines = remember {
        listOf(
            "SYS > All systems nominal",
            "NET > Backend connection stable",
            "DB  > Supabase pool: 5/5 active",
            "AUTH> Session valid · ORGANIZER",
            "EVT > Fetching mission roster...",
        )
    }
    var consoleIdx by remember { mutableStateOf(0) }
    LaunchedEffect(state.events.size) {
        while (true) {
            delay(2500)
            consoleIdx = (consoleIdx + 1) % consoleLines.size
        }
    }

    // FAB
    val fabPulse by inf.animateFloat(0.93f, 1f,
        infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse), "fab")

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(Color(0xFF020818), Color(0xFF050215), Color(0xFF020A10))
            ))
    ) {
        // ── BACKGROUND ────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Nebulae
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF1B3A6B).copy(alpha = 0.4f), Color.Transparent),
                    radius = 500f,
                    center = Offset(size.width * 0.15f + nebX, size.height * 0.2f + nebY)
                ), radius = 500f,
                center = Offset(size.width * 0.15f + nebX, size.height * 0.2f + nebY)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF3B1B6B).copy(alpha = 0.35f), Color.Transparent),
                    radius = 400f,
                    center = Offset(size.width * 0.85f, size.height * 0.6f)
                ), radius = 400f,
                center = Offset(size.width * 0.85f, size.height * 0.6f)
            )
            // Perspective grid — bottom portion only
            val gridY = size.height * 0.55f
            val vanishX = size.width / 2f
            val cols = 10
            for (i in 0..cols) {
                val t = i.toFloat() / cols
                val bx = t * size.width
                drawLine(
                    color = Color(0xFF4B3CC8).copy(alpha = gridPulse * (1f - abs(t - 0.5f) * 1.5f).coerceIn(0f, 1f)),
                    start = Offset(vanishX + (bx - vanishX) * 0.01f, gridY),
                    end   = Offset(bx, size.height),
                    strokeWidth = 0.6f
                )
            }
            val rows = 6
            for (i in 0..rows) {
                val t = i.toFloat() / rows
                val y = gridY + (size.height - gridY) * t
                val scale = 0.01f + t * 0.99f
                drawLine(
                    color = Color(0xFF4B3CC8).copy(alpha = gridPulse * t * 0.6f),
                    start = Offset(vanishX - (vanishX) * scale, y),
                    end   = Offset(vanishX + (size.width - vanishX) * scale, y),
                    strokeWidth = 0.5f
                )
            }
            // Stars
            val stars = listOf(
                0.06f to 0.03f, 0.91f to 0.06f, 0.43f to 0.02f,
                0.16f to 0.13f, 0.77f to 0.10f, 0.02f to 0.27f,
                0.97f to 0.21f, 0.30f to 0.34f, 0.83f to 0.39f,
                0.10f to 0.49f, 0.68f to 0.54f, 0.54f to 0.77f,
                0.21f to 0.73f, 0.87f to 0.67f, 0.47f to 0.91f,
                0.72f to 0.87f, 0.34f to 0.59f, 0.61f to 0.19f,
                0.50f to 0.44f, 0.79f to 0.30f, 0.14f to 0.66f
            )
            stars.forEachIndexed { i, (x, y) ->
                val tw = (sin(twinkle + i * 0.65f) * 0.4f + 0.6f).toFloat()
                drawCircle(
                    color = Color(0xFFF0EEFF).copy(alpha = tw * 0.5f),
                    radius = if (i % 4 == 0) 2.2f else 1.3f,
                    center = Offset(size.width * x, size.height * y)
                )
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.ROLE_MANAGEMENT) }) {
                            Icon(Icons.Default.Groups, "Manage roles", tint = Color(0xFF8C83E4))
                        }
                        IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                            Icon(Icons.Default.AccountCircle, "Profile", tint = Color(0xFF8C83E4))
                        }
                        IconButton(onClick = { viewModel.fetchEvents() }) {
                            Icon(Icons.Default.Refresh, null, tint = Color(0xFF8C83E4))
                        }
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.logout()
                                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, null, tint = Color(0xFF8C83E4))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                Box(
                    modifier = Modifier.scale(fabPulse).size(60.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFF7C3AED), Color(0xFF4B3CC8))))
                        .border(1.dp, Color(0xFF9B6DFF).copy(alpha = 0.5f), CircleShape)
                        .clickable { navController.navigate(Routes.CREATE_EVENT) },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(60.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color(0xFF7C3AED).copy(alpha = 0.5f), Color.Transparent))
                        )
                    }
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            },
            containerColor = Color.Transparent
        ) { pad ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── WAR ROOM HEADER ───────────────────────────────────
                item {
                    Column(
                        modifier = Modifier.offset(y = headerY.dp).alpha(headerA)
                            .padding(bottom = 4.dp)
                    ) {
                        // Top status line
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(3) { i ->
                                Box(Modifier.size(5.dp).clip(CircleShape)
                                    .background(
                                        when(i) {
                                            0 -> Color(0xFF10B981)
                                            1 -> Color(0xFF4B3CC8)
                                            else -> Color(0xFFD4AF37)
                                        }
                                    ))
                                if (i < 2) Spacer(Modifier.width(3.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "WAR ROOM  ·  ORGANIZER COMMAND",
                                color = Color(0xFF8C83E4).copy(alpha = 0.7f),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Title with glitch potential
                        Text(
                            "War\nRoom",
                            color = Color.White, fontSize = 44.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = (-2).sp,
                            lineHeight = 50.sp
                        )

                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Command your missions from here",
                            color = Color(0xFF8C83E4).copy(alpha = 0.55f), fontSize = 13.sp
                        )

                        Spacer(Modifier.height(20.dp))

                        // Stats + live console side by side
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Stats column
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (state.events.isNotEmpty()) {
                                    WarStat(displayCount.toString(), "MISSIONS",
                                        Color(0xFF8C83E4))
                                    WarStat(
                                        state.events.count { !it.scoringLocked }.toString(),
                                        "ACTIVE", Color(0xFF10B981)
                                    )
                                    WarStat(
                                        state.events.count { it.scoringLocked }.toString(),
                                        "LOCKED", Color(0xFFEF4444)
                                    )
                                }
                            }

                            // Live console terminal
                            Box(
                                modifier = Modifier.weight(1.4f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF020C08))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    // Terminal header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(Modifier.size(5.dp).clip(CircleShape)
                                            .background(Color(0xFFEF4444)))
                                        Box(Modifier.size(5.dp).clip(CircleShape)
                                            .background(Color(0xFFFFB800)))
                                        Box(Modifier.size(5.dp).clip(CircleShape)
                                            .background(Color(0xFF10B981)))
                                        Spacer(Modifier.width(4.dp))
                                        Text("LIVE CONSOLE",
                                            color = Color(0xFF10B981).copy(alpha = 0.5f),
                                            fontSize = 8.sp, fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    // Show last 4 console lines
                                    val startIdx = (consoleIdx - 3).coerceAtLeast(0)
                                    for (i in startIdx..consoleIdx) {
                                        val line = consoleLines[i % consoleLines.size]
                                        val isLatest = i == consoleIdx
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                if (isLatest) "> " else "  ",
                                                color = Color(0xFF10B981).copy(
                                                    alpha = if (isLatest) 1f else 0.3f
                                                ),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                line,
                                                color = Color(0xFF10B981).copy(
                                                    alpha = if (isLatest) 0.9f else 0.3f
                                                ),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isLatest) {
                                                Text(
                                                    "█",
                                                    color = Color(0xFF10B981).copy(alpha = cursor),
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section divider
                if (state.events.isNotEmpty() && !state.isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(Modifier.size(6.dp).clip(CircleShape)
                                .background(Color(0xFF4B3CC8)))
                            Text(
                                "ACTIVE MISSION ROSTER",
                                color = Color(0xFF4B3CC8).copy(alpha = 0.7f),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Box(Modifier.weight(1f).height(1.dp)
                                .background(Brush.horizontalGradient(
                                    listOf(Color(0xFF4B3CC8).copy(alpha = 0.4f), Color.Transparent)
                                )))
                        }
                    }
                }

                // Loading
                if (state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().height(260.dp),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CSILoader(size = LoaderSize.LARGE)
                                Spacer(Modifier.height(16.dp))
                                Text("Downloading mission roster...",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.7f),
                                    fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Error
                if (!state.isLoading && state.error != null) {
                    item {
                        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1A0808))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()) {
                                Text("⚠️", fontSize = 32.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(state.error ?: "", color = Color(0xFFFCA5A5), fontSize = 13.sp)
                                Spacer(Modifier.height(12.dp))
                                Box(Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                    .clickable { viewModel.clearMessage() }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Retry", color = Color(0xFFEF4444),
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Empty
                if (!state.isLoading && state.error == null && state.events.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(280.dp),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🚀", fontSize = 52.sp)
                                Spacer(Modifier.height(14.dp))
                                Text("No missions deployed", color = Color.White,
                                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text("Tap + to launch the first mission",
                                    color = Color(0xFF8C83E4), fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Success
                state.successMessage?.let { msg ->
                    item {
                        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A1A0F))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("✅", fontSize = 14.sp)
                                Text(msg, color = Color(0xFF6EE7B7),
                                    fontSize = 13.sp, modifier = Modifier.weight(1f))
                                TextButton(onClick = { viewModel.clearMessage() }) {
                                    Text("✕", color = Color(0xFF10B981).copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }

                // ── HOLOGRAPHIC MISSION CARDS ─────────────────────────
                itemsIndexed(state.events, key = { _, e -> e.id }) { index, event ->
                    HolographicMissionCard(
                        event           = event,
                        index           = index,
                        navController   = navController,
                        onLockScoring   = { pendingAction = EventAction.LOCK to event },
                        onUnlockScoring = { pendingAction = EventAction.UNLOCK to event },
                        onDelete        = { pendingAction = EventAction.DELETE to event }
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    pendingAction?.let { (action, event) ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            containerColor   = Color(0xFF0D0B1F),
            title = {
                Text(
                    when (action) {
                        EventAction.LOCK   -> "Lock scoring?"
                        EventAction.UNLOCK -> "Unlock scoring?"
                        EventAction.DELETE -> "Delete event?"
                    },
                    color = Color(0xFFECEBF7)
                )
            },
            text = {
                Text(
                    when (action) {
                        EventAction.LOCK   -> "Judges won't be able to change scores for \"${event.title}\", and the final results become visible to everyone."
                        EventAction.UNLOCK -> "Judges will be able to change scores for \"${event.title}\" again, and results are hidden until you lock it."
                        EventAction.DELETE -> "\"${event.title}\" and all its teams, criteria, judges and scores will be permanently deleted. This can't be undone."
                    },
                    color = Color(0xFF9C99B8)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    when (action) {
                        EventAction.LOCK   -> viewModel.lockScoring(event.id)
                        EventAction.UNLOCK -> viewModel.unlockScoring(event.id)
                        EventAction.DELETE -> viewModel.deleteEvent(event.id)
                    }
                    pendingAction = null
                }) {
                    Text(
                        when (action) {
                            EventAction.LOCK   -> "Lock"
                            EventAction.UNLOCK -> "Unlock"
                            EventAction.DELETE -> "Delete"
                        },
                        color = if (action == EventAction.UNLOCK) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancel", color = Color(0xFF8C83E4))
                }
            }
        )
    }
}

private enum class EventAction { LOCK, UNLOCK, DELETE }

// =============================================================================
// HOLOGRAPHIC MISSION CARD
// =============================================================================

@Composable
private fun HolographicMissionCard(
    event:           EventResponseDto,
    index:           Int,
    navController:   NavHostController,
    onLockScoring:   () -> Unit,
    onUnlockScoring: () -> Unit,
    onDelete:        () -> Unit
) {
    val isLocked = event.scoringLocked
    val inf      = rememberInfiniteTransition(label = "holo_$index")

    // Entrance — cards drop from top with stagger
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 120L + 400L); visible = true }
    val dropY by animateFloatAsState(
        targetValue   = if (visible) 0f else -80f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label         = "dy$index"
    )
    val cardA by animateFloatAsState(
        targetValue = if (visible) 1f else 0f, animationSpec = tween(400), label = "ca$index")

    // Press + expand
    var pressed  by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh), label = "ps$index"
    )
    val drawerAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f, animationSpec = tween(300), label = "da$index")
    val drawerScale by animateFloatAsState(
        targetValue   = if (expanded) 1f else 0.95f,
        animationSpec = tween(300, easing = EaseOutCubic), label = "ds$index"
    )

    // Holographic flicker
    val holoFlicker by inf.animateFloat(
        0.85f, 1f,
        infiniteRepeatable(tween(150 + index * 50, easing = LinearEasing), RepeatMode.Reverse),
        "hf"
    )

    // Scan line across card
    val cardScan by inf.animateFloat(
        -0.1f, 1.1f,
        infiniteRepeatable(tween(3000 + index * 400, easing = LinearEasing)), "cs"
    )

    // Energy pulse
    val energyPulse by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2500, easing = LinearEasing)), "ep"
    )

    // Beacon
    val beacon by inf.animateFloat(
        0.2f, 1f,
        infiniteRepeatable(tween(800), RepeatMode.Reverse), "bc"
    )

    // Accent color cycling
    val accentColors = listOf(
        Color(0xFF4B3CC8), Color(0xFF06B6D4), Color(0xFF10B981),
        Color(0xFFD4AF37), Color(0xFFEC4899), Color(0xFF8B5CF6)
    )
    val accent = accentColors[index % accentColors.size]

    Box(
        modifier = Modifier.fillMaxWidth()
            .offset(y = dropY.dp)
            .alpha(cardA)
            .scale(pressScale)
    ) {
        // Outer glow — stronger for active events
        if (!isLocked) {
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent,
                            accent.copy(alpha = 0.06f * holoFlicker),
                            Color.Transparent)
                    )
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            if (isLocked) Color(0xFF0A0510) else Color(0xFF080620),
                            if (isLocked) Color(0xFF080408) else Color(0xFF050418)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = if (isLocked) 0.15f else 0.5f * holoFlicker),
                            accent.copy(alpha = 0.08f),
                            accent.copy(alpha = if (isLocked) 0.15f else 0.3f * holoFlicker)
                        )
                    ),
                    RoundedCornerShape(22.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                        onTap   = { expanded = !expanded }
                    )
                }
        ) {
            // Internal scan line
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val sy = size.height * cardScan
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent,
                            accent.copy(alpha = 0.06f),
                            accent.copy(alpha = 0.12f),
                            accent.copy(alpha = 0.06f),
                            Color.Transparent),
                        startY = sy - 20f, endY = sy + 20f
                    ), size = size
                )
            }

            Column {
                // ── MAIN CARD CONTENT ─────────────────────────────────
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left column: holographic index + beacon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.width(44.dp)
                    ) {
                        // Holographic number display
                        Box(
                            modifier = Modifier.size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(accent.copy(alpha = 0.2f), Color(0xFF060412))
                                    )
                                )
                                .border(1.dp, accent.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Inner rings
                            Canvas(Modifier.size(44.dp)) {
                                drawCircle(
                                    color  = accent.copy(alpha = 0.2f),
                                    radius = size.minDimension / 2.8f,
                                    style  = Stroke(0.8f)
                                )
                            }
                            Text(
                                String.format("%02d", index + 1),
                                color      = accent.copy(alpha = holoFlicker),
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        // Status beacon
                        Box(
                            Modifier.size(8.dp).clip(CircleShape)
                                .background(
                                    if (isLocked) Color(0xFFEF4444).copy(alpha = 0.5f)
                                    else Color(0xFF10B981).copy(alpha = beacon)
                                )
                        )
                        // Expand arrow
                        Text(
                            if (expanded) "▲" else "▼",
                            color = accent.copy(alpha = 0.4f),
                            fontSize = 9.sp
                        )
                    }

                    // Right: content
                    Column(modifier = Modifier.weight(1f)) {
                        // Status tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                Modifier.clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isLocked) Color(0xFFEF4444).copy(alpha = 0.1f)
                                        else accent.copy(alpha = 0.1f)
                                    )
                                    .border(
                                        0.5.dp,
                                        if (isLocked) Color(0xFFEF4444).copy(alpha = 0.3f)
                                        else accent.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    if (isLocked) "LOCKED" else "ACTIVE",
                                    color = if (isLocked) Color(0xFFEF4444).copy(alpha = 0.7f)
                                    else accent,
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            // Tap hint
                            Text(
                                if (expanded) "CLOSE ▲" else "ACTIONS ▼",
                                color = accent.copy(alpha = 0.35f),
                                fontSize = 9.sp, letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Title
                        Text(
                            event.title,
                            color = Color.White.copy(alpha = if (isLocked) 0.45f else holoFlicker),
                            fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )

                        // Description
                        event.description?.takeIf { it.isNotBlank() }?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                it,
                                color = Color(0xFF9B94C4).copy(alpha = if (isLocked) 0.25f else 0.65f),
                                fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Animated energy bar
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Text(
                                    "ENERGY",
                                    color = accent.copy(alpha = 0.4f),
                                    fontSize = 8.sp, fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    if (isLocked) "OFFLINE" else "NOMINAL",
                                    color = if (isLocked) Color(0xFFEF4444).copy(alpha = 0.4f)
                                    else Color(0xFF10B981).copy(alpha = 0.6f),
                                    fontSize = 8.sp, fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            // Multi-segment energy bar
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val segments = 12
                                repeat(segments) { seg ->
                                    val segProgress = seg.toFloat() / segments
                                    val isActive = if (isLocked) false
                                    else ((energyPulse + segProgress) % 1f) < 0.7f
                                    Box(
                                        Modifier.weight(1f).height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                if (isActive) accent.copy(alpha = 0.8f)
                                                else if (isLocked) Color(0xFFEF4444).copy(alpha = 0.15f)
                                                else accent.copy(alpha = 0.12f)
                                            )
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Meta chips row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            HoloChip("📅 ${event.eventDate ?: "TBD"}", accent, isLocked)
                            HoloChip("👥 Max ${event.maxTeamSize}", accent, isLocked)
                        }
                    }
                }

                // ── EXPANDABLE ACTION CONSOLE ─────────────────────────
                AnimatedVisibility(
                    visible = expanded,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .alpha(drawerAlpha)
                            .scale(drawerScale)
                    ) {
                        // Separator
                        Box(
                            Modifier.fillMaxWidth().height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            accent.copy(alpha = 0.5f),
                                            accent.copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Console header
                        Box(
                            Modifier.fillMaxWidth()
                                .background(accent.copy(alpha = 0.05f))
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                ">> COMMAND CONSOLE  [MISSION ${String.format("%02d", index + 1)}]",
                                color = accent.copy(alpha = 0.5f),
                                fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // 4 action buttons in 2x2 grid
                        Column(
                            modifier = Modifier.padding(
                                start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HoloActionButton(
                                    icon = "📋", label = "CRITERIA",
                                    accent = Color(0xFF8B5CF6),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    navController.navigate("${Routes.ORGANIZER_EVENT_DETAILS}/${event.id}")
                                }
                                HoloActionButton(
                                    icon = "⚖️", label = "ASSIGN JUDGE",
                                    accent = Color(0xFF06B6D4),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    navController.navigate("${Routes.ASSIGN_JUDGE}/${event.id}")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HoloActionButton(
                                    icon = "🏆", label = "LEADERBOARD",
                                    accent = Color(0xFFD4AF37),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    navController.navigate("${Routes.LEADERBOARD}/${event.id}")
                                }
                                if (!isLocked) {
                                    HoloActionButton(
                                        icon = "🔒", label = "LOCK SCORING",
                                        accent = Color(0xFFEF4444),
                                        modifier = Modifier.weight(1f),
                                        onClick  = onLockScoring
                                    )
                                } else {
                                    HoloActionButton(
                                        icon = "🔓", label = "UNLOCK",
                                        accent = Color(0xFF10B981),
                                        modifier = Modifier.weight(1f),
                                        onClick  = onUnlockScoring
                                    )
                                }
                            }
                            HoloActionButton(
                                icon = "🗑", label = "DELETE EVENT",
                                accent = Color(0xFFEF4444),
                                modifier = Modifier.fillMaxWidth(),
                                onClick  = onDelete
                            )
                        }
                    }
                }
            }
        }

        // HUD corner brackets
        Canvas(modifier = Modifier.matchParentSize()) {
            val s = 16f; val w = 1.5f
            val c = accent.copy(alpha = if (isLocked) 0.2f else 0.55f)
            drawLine(c, Offset(0f, s), Offset(0f, 0f), w)
            drawLine(c, Offset(0f, 0f), Offset(s, 0f), w)
            drawLine(c, Offset(size.width - s, 0f), Offset(size.width, 0f), w)
            drawLine(c, Offset(size.width, 0f), Offset(size.width, s), w)
            drawLine(c, Offset(0f, size.height - s), Offset(0f, size.height), w)
            drawLine(c, Offset(0f, size.height), Offset(s, size.height), w)
            drawLine(c, Offset(size.width - s, size.height), Offset(size.width, size.height), w)
            drawLine(c, Offset(size.width, size.height - s), Offset(size.width, size.height), w)
        }
    }
}

// =============================================================================
// HOLO ACTION BUTTON
// =============================================================================

@Composable
private fun HoloActionButton(
    icon:     String,
    label:    String,
    accent:   Color,
    modifier: Modifier = Modifier,
    onClick:  () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)
    val s by animateFloatAsState(
        targetValue   = if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh), label = "hab_s"
    )

    Box(
        modifier = modifier.scale(s)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap   = { currentOnClick() }
                )
            }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 18.sp)
            Text(
                label,
                color      = accent.copy(alpha = 0.85f),
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// =============================================================================
// HOLO CHIP
// =============================================================================

@Composable
private fun HoloChip(text: String, accent: Color, isLocked: Boolean) {
    Box(
        Modifier.clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = if (isLocked) 0.04f else 0.08f))
            .border(0.5.dp, accent.copy(alpha = if (isLocked) 0.15f else 0.25f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = accent.copy(alpha = if (isLocked) 0.3f else 0.7f),
            fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

// =============================================================================
// WAR STAT
// =============================================================================

@Composable
private fun WarStat(value: String, label: String, color: Color) {
    var vis by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(400); vis = true }
    val s by animateFloatAsState(
        targetValue   = if (vis) 1f else 0.8f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy), label = "ws_s"
    )
    val a by animateFloatAsState(
        targetValue   = if (vis) 1f else 0f,
        animationSpec = tween(400), label = "ws_a"
    )

    Row(
        modifier = Modifier.scale(s).alpha(a)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(value, color = color, fontSize = 22.sp,
            fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
        Text(label, color = color.copy(alpha = 0.5f), fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}