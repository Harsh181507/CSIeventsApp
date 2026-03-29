package com.example.csievent.presentation.organizer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ── constants ────────────────────────────────────────────────────────────────
private val NF_MS =
    arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private val NF_MF = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private fun nfDays(y: Int, m: Int) = when (m) {
    1, 3, 5, 7, 8, 10, 12 -> 31; 4, 6, 9, 11 -> 30
    2 -> if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 29 else 28; else -> 30
}

// ── fixed star positions ─────────────────────────────────────────────────────
private val NF_STARS = listOf(
    0.05f to 0.04f, 0.88f to 0.07f, 0.42f to 0.02f, 0.16f to 0.15f, 0.74f to 0.11f,
    0.02f to 0.30f, 0.96f to 0.23f, 0.30f to 0.38f, 0.81f to 0.44f, 0.09f to 0.52f,
    0.66f to 0.58f, 0.52f to 0.80f, 0.21f to 0.76f, 0.85f to 0.69f, 0.46f to 0.92f,
    0.70f to 0.88f, 0.33f to 0.64f, 0.60f to 0.21f, 0.49f to 0.47f, 0.24f to 0.33f,
    0.78f to 0.72f, 0.37f to 0.55f, 0.91f to 0.85f, 0.13f to 0.68f, 0.57f to 0.96f
)

// ── screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavHostController,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxTeamSize by remember { mutableStateOf(4) }
    var selYear by remember { mutableStateOf(2026) }
    var selMonth by remember { mutableStateOf(6) }
    var selDay by remember { mutableStateOf(15) }
    var showDate by remember { mutableStateOf(false) }

    val eventDate = "%04d-%02d-%02d".format(selYear, selMonth, selDay)
    val displayDate = "${NF_MS[selMonth - 1]} $selDay, $selYear"

    val f1 = title.isNotBlank()
    val f2 = description.isNotBlank()
    val f3 = true
    val f4 = maxTeamSize > 0
    val filledCount = listOf(f1, f2, f3, f4).count { it }
    val charge = filledCount / 4f          // 0 → 1

    // launch sequence
    var lPhase by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(3) }
    LaunchedEffect(state.isLoading, state.success) {
        when {
            state.success -> {
                lPhase = 4; delay(1200); navController.popBackStack()
            }

            state.isLoading -> {
                lPhase = 1
                for (i in 3 downTo 1) {
                    countdown = i; delay(380)
                }
                lPhase = 2; delay(500); lPhase = 3
            }

            !state.isLoading && lPhase == 3 -> lPhase = 0
        }
    }

    // ── infinite animations ───────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "nf")

    // core pulse — slow, majestic, 3-second cycle
    val corePulse by inf.animateFloat(
        0.88f, 1f,
        infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse), "cp"
    )

    // nebula slow drift
    val nebDrift by inf.animateFloat(
        0f, 22f,
        infiniteRepeatable(tween(14000, easing = EaseInOutSine), RepeatMode.Reverse), "nd"
    )

    // star twinkle — very slow
    val twinkle by inf.animateFloat(
        0f, (2f * PI).toFloat(),
        infiniteRepeatable(tween(9000, easing = LinearEasing)), "tw"
    )

    // plasma streams rotate slowly around core
    val streamAngle by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(18000, easing = LinearEasing)), "sa"
    )

    // outer ring rotation — opposite direction, even slower
    val outerRing by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(25000, easing = LinearEasing)), "or"
    )

    // middle ring
    val midRing by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(15000, easing = LinearEasing)), "mr"
    )

    // warp particles on launch
    val warpP by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(600, easing = LinearEasing)), "wp"
    )

    // beacon blink for status
    val beacon by inf.animateFloat(
        0.35f, 1f,
        infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse), "bc"
    )

    // animated charge fill for individual field indicators
    val chargeAnim by animateFloatAsState(
        targetValue = charge,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "ca"
    )

    // burst when a field completes
    var burstOn by remember { mutableStateOf(false) }
    var prevFill by remember { mutableStateOf(filledCount) }
    LaunchedEffect(filledCount) {
        if (filledCount > prevFill) {
            burstOn = true; delay(1000); burstOn = false
        }
        prevFill = filledCount
    }
    val burstP by animateFloatAsState(
        targetValue = if (burstOn) 1f else 0f,
        animationSpec = tween(1000, easing = EaseOutExpo), label = "bp"
    )

    // card entrance
    var screenIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); screenIn = true }

    if (showDate) {
        Dialog(
            onDismissRequest = { showDate = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            NfDatePicker(
                selYear,
                selMonth,
                selDay,
                { y, m, d -> selYear = y; selMonth = m; selDay = d; showDate = false },
                { showDate = false })
        }
    }

    // ── root ─────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020912))
    ) {

        // ── DEEP SPACE BG — drawn once, only twinkling moves ─────────
        Canvas(Modifier.fillMaxSize()) {
            // Vertical gradient atmosphere
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF030A18),
                        Color(0xFF020810),
                        Color(0xFF020612)
                    )
                )
            )

            // Nebula cloud 1 — large, deep purple, drifts slowly
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF1A0A50).copy(alpha = 0.5f), Color.Transparent),
                    radius = 480f,
                    center = Offset(size.width * 0.75f + nebDrift, size.height * 0.2f)
                ),
                radius = 480f, center = Offset(size.width * 0.75f + nebDrift, size.height * 0.2f)
            )
            // Nebula cloud 2 — teal-blue, stationary
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF041830).copy(alpha = 0.45f), Color.Transparent),
                    radius = 350f,
                    center = Offset(size.width * 0.12f, size.height * 0.72f - nebDrift * 0.5f)
                ),
                radius = 350f,
                center = Offset(size.width * 0.12f, size.height * 0.72f - nebDrift * 0.5f)
            )
            // Nebula cloud 3 — faint, bottom right
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF0A1040).copy(alpha = 0.3f), Color.Transparent),
                    radius = 280f, center = Offset(size.width * 0.88f, size.height * 0.8f)
                ),
                radius = 280f, center = Offset(size.width * 0.88f, size.height * 0.8f)
            )

            // Stars — 25 positions, varied sizes, slow twinkle
            NF_STARS.forEachIndexed { i, (x, y) ->
                val tw = (sin(twinkle + i * 0.55f) * 0.3f + 0.7f).toFloat()
                val rad = when (i % 5) {
                    0 -> 2.4f; 1 -> 1.8f; 2 -> 1.3f; else -> 0.9f
                }
                drawCircle(
                    Color.White.copy(alpha = tw * 0.65f),
                    rad,
                    Offset(size.width * x, size.height * y)
                )
                // Diffraction cross on bright stars
                if (i % 5 == 0) {
                    val sx = size.width * x;
                    val sy = size.height * y
                    drawLine(
                        Color.White.copy(alpha = tw * 0.2f),
                        Offset(sx - 7f, sy),
                        Offset(sx + 7f, sy),
                        0.6f
                    )
                    drawLine(
                        Color.White.copy(alpha = tw * 0.2f),
                        Offset(sx, sy - 7f),
                        Offset(sx, sy + 7f),
                        0.6f
                    )
                }
            }

            // Warp streaks during launch
            if (lPhase >= 3) {
                NF_STARS.forEachIndexed { i, (x, y) ->
                    val len = 55f * warpP
                    drawLine(
                        brush = Brush.verticalGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.7f)),
                            startY = size.height * y - len, endY = size.height * y
                        ),
                        start = Offset(size.width * x, size.height * y - len),
                        end = Offset(size.width * x, size.height * y),
                        strokeWidth = 0.9f
                    )
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "NEBULA FORGE", color = Color.White,
                                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace, letterSpacing = 2.sp
                            )
                            Text(
                                "Mission Configuration Console",
                                color = Color(0xFF8C83E4).copy(alpha = 0.55f), fontSize = 10.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF8C83E4))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { pad ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // ── STELLAR CORE CANVAS ───────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier
                        .fillMaxWidth()
                        .height(280.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val cr = 52.dp.toPx()   // core radius

                        // ── Deep glow layers (size grows with charge) ─
                        val g1 = cr * 4f * corePulse * (0.4f + chargeAnim * 0.6f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFF3B1B8B).copy(alpha = 0.25f * chargeAnim),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy), radius = g1
                            ), radius = g1, center = Offset(cx, cy)
                        )
                        val g2 = cr * 2.6f * corePulse
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFF5B2BAB).copy(alpha = 0.35f * chargeAnim),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy), radius = g2
                            ), radius = g2, center = Offset(cx, cy)
                        )

                        // ── Field burst ripple (fires on completion) ──
                        if (burstP > 0f) {
                            for (i in 0..4) {
                                val delay = i * 0.15f
                                val p = ((burstP - delay).coerceIn(0f, 1f))
                                if (p > 0f) {
                                    drawCircle(
                                        color = Color(0xFF8B6DFF).copy(alpha = (1f - p) * 0.6f),
                                        radius = cr + p * cr * 3.5f,
                                        center = Offset(cx, cy),
                                        style = Stroke((2.5f - p * 2f).coerceAtLeast(0.3f))
                                    )
                                }
                            }
                        }

                        // ── Plasma streams (only appear when fields filled)
                        // 4 streams, one per field, rotate slowly
                        val streamColors = listOf(
                            Color(0xFF5B3FD8) to f1,
                            Color(0xFF7C3AED) to f2,
                            Color(0xFF06B6D4) to f3,
                            Color(0xFF10B981) to f4
                        )
                        streamColors.forEachIndexed { i, (col, active) ->
                            if (active) {
                                val baseAngle =
                                    streamAngle * PI.toFloat() / 180f + i * PI.toFloat() / 2f
                                val streamLen = cr * (1.4f + chargeAnim * 1.2f)
                                // Draw curved plasma stream
                                for (s in 0..20) {
                                    val t = s / 20f
                                    val a = baseAngle + t * 0.4f
                                    val r = cr * 1.05f + t * streamLen
                                    val px = cx + r * cos(a)
                                    val py = cy + r * sin(a)
                                    val alph = (1f - t) * 0.7f * corePulse
                                    drawCircle(
                                        col.copy(alpha = alph),
                                        (4f - t * 3f).coerceAtLeast(0.5f),
                                        Offset(px, py)
                                    )
                                }
                                // Endpoint glowing dot
                                val endA = baseAngle + 0.4f
                                val endR = cr * 1.05f + streamLen
                                val ex = cx + endR * cos(endA);
                                val ey = cy + endR * sin(endA)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(
                                            col,
                                            Color.Transparent
                                        ), center = Offset(ex, ey), radius = 12f
                                    ), radius = 12f, center = Offset(ex, ey)
                                )
                                drawCircle(
                                    Color.White.copy(alpha = 0.8f * corePulse),
                                    3.5f,
                                    Offset(ex, ey)
                                )
                            }
                        }

                        // ── Outer orbital ring — slow, dashed, always visible ─
                        rotate(outerRing, Offset(cx, cy)) {
                            drawCircle(
                                color = Color(0xFF4B3CC8).copy(alpha = 0.25f),
                                radius = cr * 2.4f, center = Offset(cx, cy),
                                style = Stroke(
                                    1f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f))
                                )
                            )
                            // 2 rider dots
                            drawCircle(
                                Color(0xFF8B6DFF).copy(alpha = 0.7f),
                                3f,
                                Offset(cx + cr * 2.4f, cy)
                            )
                            drawCircle(
                                Color(0xFF8B6DFF).copy(alpha = 0.7f),
                                3f,
                                Offset(cx - cr * 2.4f, cy)
                            )
                        }

                        // ── Middle ring — charge-dependent opacity ────
                        rotate(midRing, Offset(cx, cy)) {
                            val midAlpha = 0.15f + chargeAnim * 0.45f
                            drawCircle(
                                color = Color(0xFF7C3AED).copy(alpha = midAlpha),
                                radius = cr * 1.7f, center = Offset(cx, cy),
                                style = Stroke(1.2f)
                            )
                            drawCircle(
                                Color(0xFFD4AF37).copy(alpha = midAlpha * 1.2f),
                                4f,
                                Offset(cx, cy - cr * 1.7f)
                            )
                            drawCircle(
                                Color(0xFFD4AF37).copy(alpha = midAlpha * 0.5f),
                                8f,
                                Offset(cx, cy - cr * 1.7f),
                                style = Stroke(1f)
                            )
                        }

                        // ── Inner charge arc ──────────────────────────
                        if (chargeAnim > 0f) {
                            // background track
                            drawCircle(
                                color = Color(0xFF1A1040).copy(alpha = 0.8f),
                                radius = cr * 1.22f, center = Offset(cx, cy), style = Stroke(3.5f)
                            )
                            // charge fill
                            val arcColor = when {
                                chargeAnim < 0.5f -> Color(0xFF4B3CC8)
                                chargeAnim < 0.75f -> Color(0xFF06B6D4)
                                else -> Color(0xFF10B981)
                            }
                            drawArc(
                                color = arcColor.copy(alpha = 0.9f),
                                startAngle = -90f,
                                sweepAngle = chargeAnim * 360f,
                                useCenter = false,
                                topLeft = Offset(cx - cr * 1.22f, cy - cr * 1.22f),
                                size = Size(cr * 2.44f, cr * 2.44f),
                                style = Stroke(3.5f, cap = StrokeCap.Round)
                            )
                        }

                        // ── Stellar core body ─────────────────────────
                        // Outer corona
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFF9B6DFF).copy(alpha = 0.4f * corePulse),
                                    Color(0xFF4B3CC8).copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy), radius = cr * 1.05f
                            ),
                            radius = cr * 1.05f, center = Offset(cx, cy)
                        )
                        // Main sphere
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFFE8E0FF),
                                    Color(0xFF9B6DFF),
                                    Color(0xFF4B3CC8),
                                    Color(0xFF1A0A40)
                                ),
                                center = Offset(cx - cr * 0.22f, cy - cr * 0.28f),
                                radius = cr * 1.6f
                            ),
                            radius = cr, center = Offset(cx, cy)
                        )
                        // Surface texture swirls
                        for (j in 0..5) {
                            val a = streamAngle * PI.toFloat() / 180f + j * PI.toFloat() / 3f
                            drawLine(
                                Color(0xFFFFFFFF).copy(alpha = 0.06f * corePulse),
                                Offset(cx, cy),
                                Offset(cx + cr * 0.65f * cos(a), cy + cr * 0.65f * sin(a)),
                                0.8f
                            )
                        }
                        // Specular highlight
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.65f), Color.Transparent),
                                center = Offset(cx - cr * 0.3f, cy - cr * 0.35f),
                                radius = cr * 0.35f
                            ),
                            radius = cr * 0.35f, center = Offset(cx - cr * 0.3f, cy - cr * 0.35f)
                        )
                    }

                    // Charge text inside core — Compose text avoids nativeCanvas
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${(charge * 100).toInt()}%",
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            if (charge < 1f) "CHARGING" else "PRIMED",
                            color = if (charge < 1f) Color(0xFF8B6DFF).copy(alpha = 0.8f) else Color(
                                0xFF10B981
                            ).copy(alpha = 0.9f),
                            fontSize = 8.sp, fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace, letterSpacing = 2.sp
                        )
                    }

                    // Status label bottom
                    Box(Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            val stColor = if (charge >= 1f) Color(0xFF10B981) else Color(0xFF4B3CC8)
                            Box(
                                Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(stColor.copy(alpha = beacon))
                            )
                            Text(
                                "$filledCount/4 SYSTEMS ONLINE",
                                color = stColor.copy(alpha = 0.75f), fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // ── FORM ─────────────────────────────────────────────
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Section label
                    NfFade(0, screenIn) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4B3CC8)))
                            Box(
                                Modifier
                                    .width(14.dp)
                                    .height(1.dp)
                                    .background(Color(0xFF4B3CC8).copy(alpha = 0.4f))
                            )
                            Text(
                                "MISSION PARAMETERS", color = Color(0xFF8C83E4).copy(alpha = 0.5f),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                            )
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF4B3CC8).copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // SYS-01 — Title
                    NfFade(80, screenIn) {
                        NfPanel(
                            "SYS-01",
                            "MISSION TITLE",
                            "Names the stellar event",
                            Color(0xFF5B3FD8),
                            f1
                        ) {
                            OutlinedTextField(
                                value = title, onValueChange = { title = it },
                                placeholder = {
                                    Text(
                                        "e.g. Hackathon 2026",
                                        color = Color(0xFF2A2050),
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF5B3FD8),
                                    unfocusedBorderColor = Color(0xFF5B3FD8).copy(alpha = 0.28f),
                                    focusedContainerColor = Color(0xFF04020E),
                                    unfocusedContainerColor = Color(0xFF03010A),
                                    focusedLabelColor = Color(0xFF5B3FD8),
                                    unfocusedLabelColor = Color(0xFF5B3FD8).copy(alpha = 0.5f)
                                ), modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // SYS-02 — Description
                    NfFade(160, screenIn) {
                        NfPanel(
                            "SYS-02",
                            "MISSION BRIEF",
                            "Operational directives",
                            Color(0xFF7C3AED),
                            f2
                        ) {
                            OutlinedTextField(
                                value = description, onValueChange = { description = it },
                                placeholder = {
                                    Text(
                                        "Describe the mission objectives...",
                                        color = Color(0xFF32205A),
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = false, minLines = 3,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF7C3AED),
                                    unfocusedBorderColor = Color(0xFF7C3AED).copy(alpha = 0.28f),
                                    focusedContainerColor = Color(0xFF06020E),
                                    unfocusedContainerColor = Color(0xFF04010A),
                                    focusedLabelColor = Color(0xFF7C3AED),
                                    unfocusedLabelColor = Color(0xFF7C3AED).copy(alpha = 0.5f)
                                ), modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // SYS-03 — Date
                    NfFade(240, screenIn) {
                        NfPanel(
                            "SYS-03",
                            "DEPARTURE DATE",
                            "Mission launch timestamp",
                            Color(0xFF06B6D4),
                            f3
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF020C12))
                                    .border(
                                        1.dp, Brush.linearGradient(
                                            listOf(
                                                Color(0xFF06B6D4).copy(alpha = 0.55f),
                                                Color(0xFF4B3CC8).copy(alpha = 0.3f)
                                            )
                                        ), RoundedCornerShape(10.dp)
                                    )
                                    .clickable { showDate = true }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    Arrangement.SpaceBetween,
                                    Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "DEPARTURE TIME",
                                            color = Color(0xFF06B6D4).copy(alpha = 0.5f),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            displayDate,
                                            color = Color.White,
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            eventDate,
                                            color = Color(0xFF06B6D4).copy(alpha = 0.45f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("📅", fontSize = 22.sp)
                                        Text(
                                            "MODIFY",
                                            color = Color(0xFF06B6D4).copy(alpha = 0.4f),
                                            fontSize = 7.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SYS-04 — Crew
                    NfFade(320, screenIn) {
                        NfPanel(
                            "SYS-04",
                            "CREW MANIFEST",
                            "Personnel capacity",
                            Color(0xFF10B981),
                            f4
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (s in 1..10) {
                                        val on = s <= maxTeamSize;
                                        val exact = s == maxTeamSize
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(
                                                    if (exact) Brush.radialGradient(
                                                        listOf(
                                                            Color(
                                                                0xFF10B981
                                                            ), Color(0xFF059669)
                                                        )
                                                    )
                                                    else if (on) Brush.radialGradient(
                                                        listOf(
                                                            Color(
                                                                0xFF10B981
                                                            ).copy(alpha = 0.2f),
                                                            Color(0xFF10B981).copy(alpha = 0.06f)
                                                        )
                                                    )
                                                    else Brush.radialGradient(
                                                        listOf(
                                                            Color(
                                                                0xFF0A140E
                                                            ), Color(0xFF06100A)
                                                        )
                                                    )
                                                )
                                                .border(
                                                    0.5.dp,
                                                    if (exact) Color(0xFF10B981)
                                                    else if (on) Color(0xFF10B981).copy(alpha = 0.25f)
                                                    else Color(0xFF0E180E), RoundedCornerShape(7.dp)
                                                )
                                                .clickable { maxTeamSize = s },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                if (on) "★" else "$s",
                                                color = if (exact) Color.White else if (on) Color(
                                                    0xFF10B981
                                                ) else Color(0xFF1A2A1A),
                                                fontSize = if (on) 10.sp else 11.sp
                                            )
                                        }
                                    }
                                }
                                Row(
                                    Modifier.fillMaxWidth(),
                                    Arrangement.SpaceBetween,
                                    Alignment.CenterVertically
                                ) {
                                    Text(
                                        "CREW SIZE", color = Color(0xFF10B981).copy(alpha = 0.4f),
                                        fontSize = 9.sp, fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        "$maxTeamSize personnel",
                                        color = Color(0xFF10B981),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Slider(
                                    value = maxTeamSize.toFloat(),
                                    onValueChange = { maxTeamSize = it.toInt() },
                                    valueRange = 1f..20f,
                                    steps = 18,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF10B981),
                                        activeTrackColor = Color(0xFF10B981),
                                        inactiveTrackColor = Color(0xFF0A180E)
                                    )
                                )
                            }
                        }
                    }

                    // Mission preview
                    AnimatedVisibility(
                        charge >= 1f,
                        enter = fadeIn(tween(700)) + expandVertically(tween(700))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD4AF37).copy(alpha = 0.7f))
                                )
                                Text(
                                    "MISSION BRIEFING",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.35f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF080C1E),
                                                Color(0xFF04060E)
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp, Brush.linearGradient(
                                            listOf(
                                                Color(0xFFD4AF37).copy(alpha = 0.4f),
                                                Color(0xFF4B3CC8).copy(alpha = 0.2f)
                                            )
                                        ), RoundedCornerShape(14.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981).copy(alpha = 0.7f))
                                        )
                                        Text(
                                            "ALL SYSTEMS PRIMED",
                                            color = Color(0xFF10B981).copy(alpha = 0.6f),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        title,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (description.isNotBlank()) Text(
                                        description,
                                        color = Color(0xFF9B94C4).copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        maxLines = 2
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        NfChip("📅 $displayDate", Color(0xFF06B6D4))
                                        NfChip("👥 $maxTeamSize crew", Color(0xFF10B981))
                                    }
                                }
                            }
                        }
                    }

                    // ── LAUNCH BUTTON ─────────────────────────────────
                    Spacer(Modifier.height(6.dp))
                    val canGo = f1 && lPhase == 0
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (canGo) Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1A0A50),
                                        Color(0xFF2A1070)
                                    )
                                )
                                else Brush.linearGradient(
                                    listOf(
                                        Color(0xFF080810),
                                        Color(0xFF080810)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                if (canGo) Brush.linearGradient(
                                    listOf(
                                        Color(0xFF5B3FD8).copy(alpha = 0.8f),
                                        Color(0xFF8B6DFF).copy(alpha = 0.4f),
                                        Color(0xFF5B3FD8).copy(alpha = 0.8f)
                                    )
                                )
                                else Brush.linearGradient(
                                    listOf(
                                        Color(0xFF151520),
                                        Color(0xFF151520)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = canGo) {
                                viewModel.createEvent(title, description, eventDate, maxTeamSize)
                            }
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            lPhase,
                            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                            label = "nf_btn"
                        ) { p ->
                            when {
                                p == 0 && canGo -> Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("✦", fontSize = 18.sp, color = Color(0xFFD4AF37))
                                    Text(
                                        "FORGE MISSION",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                    Text("✦", fontSize = 18.sp, color = Color(0xFFD4AF37))
                                }

                                p == 0 -> Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "ENTER MISSION TITLE FIRST",
                                        color = Color(0xFF2A2040), fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp
                                    )
                                }

                                p == 1 -> Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        "T-$countdown",
                                        color = Color(0xFFD4AF37),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        "IGNITION SEQUENCE",
                                        color = Color(0xFFD4AF37).copy(alpha = 0.75f),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                p == 2 -> Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("◉", fontSize = 20.sp, color = Color(0xFF9B2AFF))
                                    Text(
                                        "FORGING NEBULA",
                                        color = Color(0xFF9B2AFF),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 2.sp
                                    )
                                }

                                p == 3 -> Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CSILoader(size = LoaderSize.SMALL)
                                    Text(
                                        "CRYSTALLISING",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                else -> Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("✦", fontSize = 18.sp, color = Color(0xFF10B981))
                                    Text(
                                        "MISSION FORGED",
                                        color = Color(0xFF10B981),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // error
                    state.error?.let { err ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1A0808))
                                .border(
                                    1.dp,
                                    Color(0xFFEF4444).copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                                Text(
                                    err,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearState() }) {
                                    Text("✕", color = Color(0xFFEF4444).copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

// =============================================================================
// NF PANEL — mission parameter card
// =============================================================================
@Composable
private fun NfPanel(
    code: String,
    label: String,
    sub: String,
    accent: Color,
    active: Boolean,
    content: @Composable () -> Unit
) {
    Box(Modifier.fillMaxWidth()) {
        // Bottom glow line on completion
        if (active) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, accent.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            if (active) accent.copy(alpha = 0.06f) else Color(0xFF06070F),
                            Color(0xFF04050A)
                        )
                    )
                )
                .border(
                    1.dp, Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = if (active) 0.6f else 0.18f),
                            accent.copy(alpha = if (active) 0.18f else 0.05f),
                            accent.copy(alpha = if (active) 0.6f else 0.18f)
                        )
                    ), RoundedCornerShape(14.dp)
                )
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Code badge
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(accent.copy(alpha = if (active) 0.18f else 0.07f))
                                .border(
                                    0.5.dp,
                                    accent.copy(alpha = if (active) 0.55f else 0.18f),
                                    RoundedCornerShape(5.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                code, color = accent.copy(alpha = if (active) 1f else 0.4f),
                                fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                            )
                        }
                        Column {
                            Text(
                                label,
                                color = Color.White.copy(alpha = if (active) 0.9f else 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                sub, color = accent.copy(alpha = if (active) 0.4f else 0.2f),
                                fontSize = 9.sp, fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    // Online/Standby pill
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) accent.copy(alpha = 0.12f) else Color(0xFF0A0A14))
                            .border(
                                0.5.dp,
                                accent.copy(alpha = if (active) 0.45f else 0.12f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (active) "● ONLINE" else "○ STANDBY",
                            color = if (active) accent else Color(0xFF252535),
                            fontSize = 8.sp, fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
                        )
                    }
                }
                content()
            }
        }
        // Corner HUD brackets
        Canvas(Modifier.matchParentSize()) {
            val s = 11f;
            val w = 1.1f;
            val c = accent.copy(alpha = if (active) 0.55f else 0.16f)
            drawLine(c, Offset(0f, s), Offset(0f, 0f), w); drawLine(
            c,
            Offset(0f, 0f),
            Offset(s, 0f),
            w
        )
            drawLine(c, Offset(size.width - s, 0f), Offset(size.width, 0f), w); drawLine(
            c,
            Offset(size.width, 0f),
            Offset(size.width, s),
            w
        )
            drawLine(c, Offset(0f, size.height - s), Offset(0f, size.height), w); drawLine(
            c,
            Offset(0f, size.height),
            Offset(s, size.height),
            w
        )
            drawLine(
                c,
                Offset(size.width - s, size.height),
                Offset(size.width, size.height),
                w
            ); drawLine(c, Offset(size.width, size.height - s), Offset(size.width, size.height), w)
        }
    }
}

// =============================================================================
// HELPERS
// =============================================================================
@Composable
private fun NfFade(delayMs: Int, trigger: Boolean, content: @Composable () -> Unit) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(trigger) {
        if (trigger) {
            kotlinx.coroutines.delay(delayMs.toLong()); show = true
        }
    }
    val a by animateFloatAsState(if (show) 1f else 0f, tween(500), label = "nfa")
    val y by animateFloatAsState(
        if (show) 0f else 18f,
        tween(500, easing = EaseOutCubic),
        label = "nfy"
    )
    Box(Modifier
        .alpha(a)
        .offset(y = y.dp)) { content() }
}

@Composable
private fun NfChip(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(color.copy(alpha = 0.1f))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(7.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            color = color.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================================
// DATE PICKER
// =============================================================================
@Composable
private fun NfDatePicker(
    year: Int, month: Int, day: Int,
    onConfirm: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var sY by remember { mutableStateOf(year) }
    var sM by remember { mutableStateOf(month) }
    var sD by remember { mutableStateOf(day) }
    val md = nfDays(sY, sM); if (sD > md) sD = md

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF06060E))
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(Color(0xFF5B3FD8), Color(0xFF06B6D4))),
                    RoundedCornerShape(22.dp)
                )
                .clickable { }
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF06B6D4)))
                    Text(
                        "DEPARTURE DATE",
                        color = Color(0xFF06B6D4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF03040A))
                        .border(
                            1.dp,
                            Color(0xFF06B6D4).copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp), contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${NF_MF[sM - 1]} %02d, %d".format(sD, sY),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "%04d-%02d-%02d".format(sY, sM, sD),
                            color = Color(0xFF06B6D4).copy(alpha = 0.5f),
                            fontSize = 11.sp, fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    "MONTH",
                    color = Color(0xFF5B3FD8).copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    NF_MS.forEachIndexed { i, mo ->
                        val sel = i == sM - 1
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (sel) Color(0xFF5B3FD8).copy(alpha = 0.25f) else Color(
                                        0xFF08080E
                                    )
                                )
                                .border(
                                    0.5.dp,
                                    if (sel) Color(0xFF5B3FD8).copy(alpha = 0.7f) else Color(
                                        0xFF181828
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { sM = i + 1 }
                                .padding(horizontal = 11.dp, vertical = 7.dp)
                        ) {
                            Text(
                                mo,
                                color = if (sel) Color(0xFF8B6DFF) else Color(0xFF3A3A55),
                                fontSize = 12.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            "DAY",
                            color = Color(0xFF06B6D4).copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..md).forEach { n ->
                                val sel = n == sD
                                Box(
                                    Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(
                                            if (sel) Color(0xFF06B6D4).copy(alpha = 0.22f) else Color(
                                                0xFF08080E
                                            )
                                        )
                                        .border(
                                            0.5.dp,
                                            if (sel) Color(0xFF06B6D4).copy(alpha = 0.7f) else Color(
                                                0xFF181828
                                            ),
                                            RoundedCornerShape(7.dp)
                                        )
                                        .clickable { sD = n }, contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        n.toString(),
                                        color = if (sel) Color(0xFF06B6D4) else Color(0xFF3A3A55),
                                        fontSize = 11.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            "YEAR",
                            color = Color(0xFF7C3AED).copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (2025..2030).forEach { n ->
                                val sel = n == sY
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(
                                            if (sel) Color(0xFF7C3AED).copy(alpha = 0.22f) else Color(
                                                0xFF08080E
                                            )
                                        )
                                        .border(
                                            0.5.dp,
                                            if (sel) Color(0xFF7C3AED).copy(alpha = 0.7f) else Color(
                                                0xFF181828
                                            ),
                                            RoundedCornerShape(7.dp)
                                        )
                                        .clickable { sY = n }
                                        .padding(horizontal = 10.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        n.toString(),
                                        color = if (sel) Color(0xFF7C3AED) else Color(0xFF3A3A55),
                                        fontSize = 11.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10101E))
                            .clickable { onDismiss() }
                            .padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "CANCEL",
                            color = Color(0xFF8C83E4),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF5B3FD8),
                                        Color(0xFF06B6D4)
                                    )
                                )
                            )
                            .clickable { onConfirm(sY, sM, sD) }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "CONFIRM",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}