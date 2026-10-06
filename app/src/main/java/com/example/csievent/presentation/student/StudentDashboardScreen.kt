package com.example.csievent.presentation.student

import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.presentation.student.events.StudentEventsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

// =============================================================================
// LOADER SIZE ENUM — defined at file top level (not inside a function)
// =============================================================================

enum class LoaderSize { SMALL, MEDIUM, LARGE }

// =============================================================================
// PLANET PALETTE — defined at file top level
// =============================================================================

data class PlanetPalette(
    val core:    Color,
    val mid:     Color,
    val glow:    Color,
    val ring:    Color,
    val surface: Color,
    val name:    String
)

val PLANET_PALETTES = listOf(
    PlanetPalette(Color(0xFF4B3CC8), Color(0xFF7C3AED), Color(0xFF9B6DFF).copy(alpha = 0.5f),
        Color(0xFF6B5FD6).copy(alpha = 0.4f), Color(0xFF3829A6), "NEBULA"),
    PlanetPalette(Color(0xFF0891B2), Color(0xFF06B6D4), Color(0xFF00D4FF).copy(alpha = 0.5f),
        Color(0xFF0EA5E9).copy(alpha = 0.4f), Color(0xFF075985), "AQUA"),
    PlanetPalette(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399).copy(alpha = 0.5f),
        Color(0xFF047857).copy(alpha = 0.4f), Color(0xFF065F46), "VERDANT"),
    PlanetPalette(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFFB800).copy(alpha = 0.5f),
        Color(0xFFD4AF37).copy(alpha = 0.4f), Color(0xFF92400E), "SOLAR"),
    PlanetPalette(Color(0xFFDB2777), Color(0xFFEC4899), Color(0xFFF472B6).copy(alpha = 0.5f),
        Color(0xFFBE185D).copy(alpha = 0.4f), Color(0xFF9D174D), "CRIMSON"),
    PlanetPalette(Color(0xFF7C3AED), Color(0xFF8B5CF6), Color(0xFFA78BFA).copy(alpha = 0.5f),
        Color(0xFF6D28D9).copy(alpha = 0.4f), Color(0xFF4C1D95), "VIOLET"),
)

// =============================================================================
// STUDENT DASHBOARD SCREEN
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    navController: NavHostController,
    viewModel:   StudentDashboardViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { viewModel.fetchEvents() }

    val inf = rememberInfiniteTransition(label = "galaxy")

    // Nebula drift
    val nebX by inf.animateFloat(
        initialValue  = 0f, targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(12000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "nx"
    )
    val nebY by inf.animateFloat(
        initialValue  = 0f, targetValue = 25f,
        animationSpec = infiniteRepeatable(tween(9000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ny"
    )
    // Star twinkle
    val twinkle by inf.animateFloat(
        initialValue  = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "tw"
    )

    // Shooting star
    var shootX   by remember { mutableStateOf(0f) }
    var shootY   by remember { mutableStateOf(0.2f) }
    var shootActive by remember { mutableStateOf(false) }
    val shootProgress by animateFloatAsState(
        targetValue   = if (shootActive) 1.3f else -0.1f,
        animationSpec = tween(900, easing = EaseInCubic),
        label         = "shoot"
    )
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L + (Math.random() * 5000).toLong())
            shootX      = -0.1f + (Math.random() * 0.4).toFloat()
            shootY      = 0.05f + (Math.random() * 0.3).toFloat()
            shootActive = true
            delay(1000)
            shootActive = false
        }
    }

    // Header entrance
    var headerIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(200); headerIn = true }
    val headerA by animateFloatAsState(
        targetValue   = if (headerIn) 1f else 0f,
        animationSpec = tween(800),
        label         = "ha"
    )
    val headerY by animateFloatAsState(
        targetValue   = if (headerIn) 0f else -40f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "hy"
    )

    // Count-up
    var displayCount by remember { mutableStateOf(0) }
    LaunchedEffect(state.events.size) {
        val target = state.events.size
        val start  = displayCount
        repeat(20) { i -> delay(25); displayCount = start + ((target - start) * (i + 1) / 20) }
        displayCount = state.events.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020818), Color(0xFF050215), Color(0xFF020A10))
                )
            )
    ) {
        // Space background canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Nebula 1 — purple
            drawCircle(
                brush  = Brush.radialGradient(
                    listOf(Color(0xFF2D1B69).copy(alpha = 0.4f), Color.Transparent),
                    radius = 500f,
                    center = Offset(size.width * 0.2f + nebX, size.height * 0.15f + nebY)
                ),
                radius = 500f,
                center = Offset(size.width * 0.2f + nebX, size.height * 0.15f + nebY)
            )
            // Nebula 2 — teal
            drawCircle(
                brush  = Brush.radialGradient(
                    listOf(Color(0xFF0D4040).copy(alpha = 0.3f), Color.Transparent),
                    radius = 400f,
                    center = Offset(size.width * 0.8f - nebX, size.height * 0.6f)
                ),
                radius = 400f,
                center = Offset(size.width * 0.8f - nebX, size.height * 0.6f)
            )
            // Stars
            val stars = listOf(
                0.06f to 0.03f, 0.91f to 0.06f, 0.43f to 0.02f,
                0.16f to 0.13f, 0.77f to 0.10f, 0.02f to 0.27f,
                0.97f to 0.21f, 0.30f to 0.34f, 0.83f to 0.39f,
                0.10f to 0.49f, 0.68f to 0.54f, 0.54f to 0.77f,
                0.21f to 0.73f, 0.87f to 0.67f, 0.47f to 0.91f,
                0.71f to 0.87f, 0.34f to 0.59f, 0.61f to 0.19f,
                0.50f to 0.44f, 0.25f to 0.82f, 0.79f to 0.30f,
                0.38f to 0.11f, 0.92f to 0.79f, 0.14f to 0.66f
            )
            stars.forEachIndexed { i, (x, y) ->
                val tw = (sin(twinkle + i * 0.65f) * 0.45f + 0.55f).toFloat()
                val r  = if (i % 4 == 0) 2.2f else 1.3f
                drawCircle(
                    color  = Color(0xFFF0EEFF).copy(alpha = tw * 0.55f),
                    radius = r,
                    center = Offset(size.width * x, size.height * y)
                )
            }
            // Shooting star
            if (shootProgress > 0f && shootProgress < 1.2f) {
                val sx      = size.width  * (shootX + shootProgress * 0.5f)
                val sy      = size.height * (shootY + shootProgress * 0.15f)
                val tailLen = 120f * minOf(shootProgress, 0.8f)
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.8f)),
                        start = Offset(sx - tailLen, sy - tailLen * 0.3f),
                        end   = Offset(sx, sy)
                    ),
                    start       = Offset(sx - tailLen, sy - tailLen * 0.3f),
                    end         = Offset(sx, sy),
                    strokeWidth = 1.5f
                )
                drawCircle(Color.White.copy(alpha = 0.9f), 2f, Offset(sx, sy))
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { viewModel.fetchEvents() }) {
                            Icon(Icons.Default.Refresh, null, tint = Color(0xFF8C83E4))
                        }
                        IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                            Icon(Icons.Default.AccountCircle, "Profile", tint = Color(0xFF8C83E4))
                        }
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, null, tint = Color(0xFF8C83E4))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { pad ->
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Header
                item {
                    Column(
                        modifier = Modifier
                            .offset(y = headerY.dp)
                            .alpha(headerA)
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(3) { i ->
                                Box(
                                    Modifier.size(4.dp).clip(CircleShape)
                                        .background(Color(0xFF8C83E4).copy(alpha = 0.6f))
                                )
                                if (i < 2) {
                                    Box(
                                        Modifier.width(12.dp).height(1.dp)
                                            .background(Color(0xFF4B3CC8).copy(alpha = 0.4f))
                                    )
                                }
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "GALAXY  ·  CSI VIT-AP",
                                color      = Color(0xFF8C83E4).copy(alpha = 0.7f),
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Event\nGalaxy",
                            color         = Color.White,
                            fontSize      = 42.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = (-2).sp,
                            lineHeight    = 48.sp
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "Tap a planet to enter",
                            color    = Color(0xFF8C83E4).copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(20.dp))

                        if (state.events.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatOrb(displayCount.toString(), "TOTAL",
                                    Color(0xFF4B3CC8), Modifier.weight(1f))
                                StatOrb(
                                    state.events.count { !it.scoringLocked }.toString(),
                                    "OPEN", Color(0xFF10B981), Modifier.weight(1f)
                                )
                                StatOrb(
                                    state.events.count { it.scoringLocked }.toString(),
                                    "LOCKED", Color(0xFFEF4444), Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Loading
                if (state.isLoading) {
                    item {
                        Box(
                            modifier         = Modifier.fillMaxWidth().height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CSILoader(size = LoaderSize.LARGE)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Scanning galaxy...",
                                    color    = Color(0xFF8C83E4).copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Error
                if (!state.isLoading && state.error != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1A0808))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f),
                                    RoundedCornerShape(20.dp))
                                .padding(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier            = Modifier.fillMaxWidth()
                            ) {
                                Text("⚠️", fontSize = 36.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(state.error ?: "", color = Color(0xFFFCA5A5), fontSize = 13.sp)
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                        .clickable { viewModel.fetchEvents() }
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
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
                        Box(
                            modifier         = Modifier.fillMaxWidth().height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌌", fontSize = 52.sp)
                                Spacer(Modifier.height(14.dp))
                                Text("Galaxy is empty", color = Color.White,
                                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text("New planets forming soon",
                                    color = Color(0xFF8C83E4), fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Planet cards
                itemsIndexed(state.events, key = { _, e -> e.id }) { index, event ->
                    PlanetEventCard(
                        event         = event,
                        index         = index,
                        navController = navController
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// =============================================================================
// PLANET EVENT CARD
// =============================================================================

@Composable
private fun PlanetEventCard(
    event:         EventResponseDto,
    index:         Int,
    navController: NavHostController
) {
    val palette  = PLANET_PALETTES[index % PLANET_PALETTES.size]
    val isLocked = event.scoringLocked
    val inf      = rememberInfiniteTransition(label = "planet_$index")

    val bob by inf.animateFloat(
        initialValue  = 0f, targetValue = 8f,
        animationSpec = infiniteRepeatable(
            tween(2500 + index * 200, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "bob"
    )
    val atmo by inf.animateFloat(
        initialValue  = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "atmo"
    )
    val ringAngle by inf.animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(4000 + index * 300, easing = LinearEasing)
        ), label = "ring"
    )
    val moonAngle by inf.animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(3000 + index * 400, easing = LinearEasing)
        ), label = "moon"
    )
    val statusBlink by inf.animateFloat(
        initialValue  = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "sb"
    )

    // Entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 120L + 400L); visible = true }
    val entranceX by animateFloatAsState(
        targetValue   = if (visible) 0f else 160f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "ex$index"
    )
    val entranceA by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label         = "ea$index"
    )

    // Tap ripple
    var rippleActive by remember { mutableStateOf(false) }
    val rippleScale by animateFloatAsState(
        targetValue   = if (rippleActive) 3f else 0f,
        animationSpec = tween(600, easing = EaseOutExpo),
        label         = "rp$index"
    )
    val rippleAlpha by animateFloatAsState(
        targetValue   = if (rippleActive) 0f else 0.6f,
        animationSpec = tween(600),
        label         = "ra$index"
    )

    // Press scale
    var pressedState by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressedState) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label         = "ps$index"
    )

    val dimFactor = if (isLocked) 0.35f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = entranceX.dp)
            .alpha(entranceA)
            .scale(pressScale)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF0A0A1E), Color(0xFF080818)))
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            palette.core.copy(alpha = 0.4f * dimFactor),
                            palette.mid.copy(alpha = 0.1f * dimFactor)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .pointerInput(event.id, isLocked) {
                    detectTapGestures(
                        onPress = {
                            pressedState = true
                            tryAwaitRelease()
                            pressedState = false
                        },
                        onTap = {
                            rippleActive = true
                            if (!isLocked) {
                                navController.navigate("${Routes.STUDENT_TEAMS}/${event.id}")
                            } else {
                                navController.navigate("${Routes.LEADERBOARD}/${event.id}")
                            }
                        }
                    )
                }
        ) {
            Row(
                modifier  = Modifier.padding(20.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Planet visual
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.size(100.dp).offset(y = (-bob).dp)
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val pr = size.minDimension / 2.8f

                        // Atmosphere glow
                        drawCircle(
                            brush  = Brush.radialGradient(
                                listOf(
                                    palette.glow.copy(alpha = atmo * dimFactor),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = size.minDimension / 1.5f
                            ),
                            radius = size.minDimension / 1.5f,
                            center = Offset(cx, cy)
                        )
                        // Planet body
                        drawCircle(
                            brush  = Brush.radialGradient(
                                listOf(
                                    palette.mid.copy(alpha = dimFactor),
                                    palette.core.copy(alpha = dimFactor),
                                    palette.surface.copy(alpha = dimFactor)
                                ),
                                center = Offset(cx - pr * 0.2f, cy - pr * 0.3f),
                                radius = pr * 2f
                            ),
                            radius = pr,
                            center = Offset(cx, cy)
                        )
                        // Surface stripes
                        if (!isLocked) {
                            listOf(-0.25f, 0f, 0.25f).forEach { offset ->
                                drawOval(
                                    color   = Color.White.copy(alpha = 0.05f),
                                    topLeft = Offset(cx - pr, cy - pr * 0.15f + offset * pr * 2),
                                    size    = Size(pr * 2, pr * 0.3f)
                                )
                            }
                        }
                        // Highlight
                        drawCircle(
                            color  = Color.White.copy(alpha = 0.25f * dimFactor),
                            radius = pr * 0.35f,
                            center = Offset(cx - pr * 0.3f, cy - pr * 0.35f)
                        )
                        // Orbital ring
                        rotate(ringAngle * 0.3f, Offset(cx, cy)) {
                            drawOval(
                                color   = palette.ring.copy(alpha = 0.6f * dimFactor),
                                topLeft = Offset(cx - pr * 1.4f, cy - pr * 0.25f),
                                size    = Size(pr * 2.8f, pr * 0.5f),
                                style   = Stroke(1.2f)
                            )
                        }
                        // Moon + trail
                        if (!isLocked) {
                            val mRad  = moonAngle * PI.toFloat() / 180f
                            val mOrb  = pr * 1.5f
                            val moonX = cx + mOrb * cos(mRad)
                            val moonY = cy + mOrb * 0.4f * sin(mRad)
                            for (t in 1..6) {
                                val trailAngle = mRad - t * 0.15f
                                drawCircle(
                                    color  = palette.mid.copy(
                                        alpha = (0.4f - t * 0.06f).coerceAtLeast(0f)
                                    ),
                                    radius = (3.5f - t * 0.4f).coerceAtLeast(0.5f),
                                    center = Offset(
                                        cx + mOrb * cos(trailAngle),
                                        cy + mOrb * 0.4f * sin(trailAngle)
                                    )
                                )
                            }
                            drawCircle(
                                brush  = Brush.radialGradient(
                                    listOf(palette.mid, Color.Transparent),
                                    radius = 8f, center = Offset(moonX, moonY)
                                ),
                                radius = 8f, center = Offset(moonX, moonY)
                            )
                            drawCircle(
                                color  = Color.White.copy(alpha = 0.8f),
                                radius = 3.5f,
                                center = Offset(moonX, moonY)
                            )
                        }
                        // Locked overlay
                        if (isLocked) {
                            drawCircle(
                                color  = Color(0xFF0A0A1E).copy(alpha = 0.6f),
                                radius = pr,
                                center = Offset(cx, cy)
                            )
                        }
                        // Ripple
                        if (rippleScale > 0f) {
                            drawCircle(
                                color  = palette.glow.copy(alpha = rippleAlpha),
                                radius = pr * rippleScale,
                                center = Offset(cx, cy),
                                style  = Stroke(2f)
                            )
                        }
                    }

                    // Planet type badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(palette.core.copy(alpha = 0.9f * dimFactor))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            palette.name,
                            color      = Color.White.copy(alpha = dimFactor),
                            fontSize   = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Info panel
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            Modifier.size(6.dp).clip(CircleShape)
                                .background(
                                    if (isLocked) Color(0xFFEF4444).copy(alpha = 0.5f)
                                    else Color(0xFF10B981).copy(alpha = statusBlink)
                                )
                        )
                        Text(
                            if (isLocked) "RESULTS OUT · TAP TO VIEW" else "OPEN FOR TEAMS",
                            color      = if (isLocked) Color(0xFFEF4444).copy(alpha = 0.5f)
                            else Color(0xFF10B981),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        event.title,
                        color      = Color.White.copy(alpha = if (isLocked) 0.4f else 1f),
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )

                    event.description?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(5.dp))
                        Text(
                            it,
                            color    = Color(0xFF9B94C4).copy(
                                alpha = if (isLocked) 0.3f else 0.75f
                            ),
                            fontSize   = 12.sp,
                            maxLines   = 2,
                            overflow   = TextOverflow.Ellipsis,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GlowChip("📅 ${event.eventDate ?: "TBD"}", palette.core, dimFactor)
                        GlowChip("👥 ${event.maxTeamSize} max", palette.core, dimFactor)
                    }

                    Spacer(Modifier.height(12.dp))

                    if (!isLocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(palette.surface, palette.core)
                                    )
                                )
                                .border(1.dp, palette.mid.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Land Here →",
                                color      = Color.White,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1A1020))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "🔒 Mission Closed",
                                color      = Color(0xFFEF4444).copy(alpha = 0.5f),
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// GLOW CHIP
// =============================================================================

@Composable
private fun GlowChip(text: String, color: Color, dimFactor: Float) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f * dimFactor))
            .border(1.dp, color.copy(alpha = 0.2f * dimFactor), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            color      = color.copy(alpha = 0.8f * dimFactor),
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================================
// STAT ORB
// =============================================================================

@Composable
private fun StatOrb(number: String, label: String, color: Color, modifier: Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(500); visible = true }
    val s by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.7f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label         = "os"
    )
    val a by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label         = "oa"
    )

    Box(
        modifier = modifier
            .scale(s)
            .alpha(a)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.05f))
                )
            )
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                number,
                color      = color,
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            Text(
                label,
                color      = color.copy(alpha = 0.55f),
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

// =============================================================================
// CSI LOADER — Reusable triple ring loader
// Move to presentation/components/ to use across all screens
// =============================================================================

@Composable
fun CSILoader(size: LoaderSize = LoaderSize.MEDIUM, modifier: Modifier = Modifier) {
    val dp      = when (size) {
        LoaderSize.SMALL  -> 28.dp
        LoaderSize.MEDIUM -> 52.dp
        LoaderSize.LARGE  -> 80.dp
    }
    val stroke1 = when (size) {
        LoaderSize.SMALL  -> 2f
        LoaderSize.MEDIUM -> 2.5f
        LoaderSize.LARGE  -> 3f
    }
    val stroke2 = when (size) {
        LoaderSize.SMALL  -> 1.5f
        LoaderSize.MEDIUM -> 2f
        LoaderSize.LARGE  -> 2.5f
    }
    val stroke3 = when (size) {
        LoaderSize.SMALL  -> 1f
        LoaderSize.MEDIUM -> 1.5f
        LoaderSize.LARGE  -> 2f
    }

    val inf = rememberInfiniteTransition(label = "csi_loader")
    val r1 by inf.animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "r1"
    )
    val r2 by inf.animateFloat(
        initialValue  = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "r2"
    )
    val r3 by inf.animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing)),
        label = "r3"
    )
    val goldAlpha by inf.animateFloat(
        initialValue  = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(850, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "ga"
    )

    Canvas(modifier = modifier.size(dp)) {
        val p1 = 4f
        val p2 = p1 + stroke1 * 3
        val p3 = p2 + stroke2 * 3

        rotate(r1) {
            drawArc(
                color      = Color(0xFF4B3CC8),
                startAngle = 0f, sweepAngle = 260f,
                useCenter  = false,
                style      = Stroke(stroke1, cap = StrokeCap.Round),
                topLeft    = Offset(p1, p1),
                size       = Size(this.size.width - p1 * 2, this.size.height - p1 * 2)
            )
        }
        rotate(r2) {
            drawArc(
                color      = Color(0xFF7C3AED),
                startAngle = 90f, sweepAngle = 200f,
                useCenter  = false,
                style      = Stroke(stroke2, cap = StrokeCap.Round),
                topLeft    = Offset(p2, p2),
                size       = Size(this.size.width - p2 * 2, this.size.height - p2 * 2)
            )
        }
        rotate(r3) {
            drawArc(
                color      = Color(0xFFD4AF37).copy(alpha = goldAlpha),
                startAngle = 180f, sweepAngle = 150f,
                useCenter  = false,
                style      = Stroke(stroke3, cap = StrokeCap.Round),
                topLeft    = Offset(p3, p3),
                size       = Size(this.size.width - p3 * 2, this.size.height - p3 * 2)
            )
        }
    }
}