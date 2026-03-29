package com.example.csievent.presentation.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ── star field ────────────────────────────────────────────────────────────────
private val REG_STARS = listOf(
    0.06f to 0.03f, 0.90f to 0.08f, 0.43f to 0.02f, 0.17f to 0.16f, 0.76f to 0.09f,
    0.03f to 0.31f, 0.96f to 0.23f, 0.31f to 0.39f, 0.83f to 0.42f, 0.09f to 0.54f,
    0.68f to 0.58f, 0.54f to 0.81f, 0.23f to 0.76f, 0.87f to 0.70f, 0.47f to 0.92f,
    0.71f to 0.86f, 0.35f to 0.64f, 0.62f to 0.22f, 0.50f to 0.47f, 0.26f to 0.35f,
    0.80f to 0.73f, 0.39f to 0.57f, 0.93f to 0.83f, 0.14f to 0.69f, 0.59f to 0.96f,
    0.67f to 0.40f, 0.42f to 0.72f, 0.85f to 0.55f, 0.21f to 0.50f, 0.56f to 0.17f
)

// ── constellation lines connecting the 3 field stars ─────────────────────────
private val CONSTELLATION_LINES = listOf(
    // (starIndex1, starIndex2) — decorative bg constellation
    0 to 4, 4 to 7, 7 to 9, 9 to 11, 11 to 14,
    1 to 5, 5 to 8, 8 to 12, 2 to 6, 6 to 10
)

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var enlisted by remember { mutableStateOf(false) }

    // Navigate on success
    LaunchedEffect(state.role) {
        state.role?.let { role ->
            enlisted = true
            delay(1400)
            val dest = when (role) {
                "STUDENT" -> Routes.STUDENT_DASHBOARD
                "JUDGE" -> Routes.JUDGE_DASHBOARD
                "ORGANIZER" -> Routes.ORGANIZER_DASHBOARD
                else -> Routes.LOGIN
            }
            navController.navigate(dest) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    val f1 = name.isNotBlank()
    val f2 = email.isNotBlank()
    val f3 = password.isNotBlank()
    val filledCount = listOf(f1, f2, f3).count { it }
    val charge = filledCount / 3f

    // ── infinite animations ───────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "reg")

    val twinkle by inf.animateFloat(
        0f, (2f * PI).toFloat(),
        infiniteRepeatable(tween(9000, easing = LinearEasing)), "tw"
    )
    val nebDrift by inf.animateFloat(
        0f, 22f,
        infiniteRepeatable(tween(14000, easing = EaseInOutSine), RepeatMode.Reverse), "nd"
    )
    val starBreath by inf.animateFloat(
        0.90f, 1.10f,
        infiniteRepeatable(tween(2800, easing = EaseInOutSine), RepeatMode.Reverse), "sb"
    )
    val coronaRot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)), "cr"
    )
    val outerRing by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(22000, easing = LinearEasing)), "or"
    )
    val midRing by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(13000, easing = LinearEasing)), "mr"
    )
    val particleFlow by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing)), "pf"
    )
    val beacon by inf.animateFloat(
        0.25f, 1f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), "bc"
    )
    val dataStream by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1400, easing = LinearEasing)), "ds"
    )

    // Animated star radius — grows as fields fill
    val starR by animateFloatAsState(
        targetValue = 28.dp.value + charge * 28.dp.value,
        animationSpec = tween(800, easing = EaseOutCubic), label = "sr"
    )

    // Nova on success
    val novaP by animateFloatAsState(
        targetValue = if (enlisted) 1f else 0f,
        animationSpec = tween(1200, easing = EaseOutExpo), label = "np"
    )

    // Form entrance
    var formIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); formIn = true }
    val formA by animateFloatAsState(if (formIn) 1f else 0f, tween(700), label = "fa")
    val formY by animateFloatAsState(
        if (formIn) 0f else 70f,
        spring(0.65f, Spring.StiffnessMediumLow), label = "fy"
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF020912))) {

        // ── BACKGROUND ────────────────────────────────────────────────
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF030A18), Color(0xFF020810), Color(0xFF040614))
                )
            )

            // Constellation lines — faint background
            val pts = REG_STARS.map { (x, y) -> Offset(size.width * x, size.height * y) }
            CONSTELLATION_LINES.forEach { (a, b) ->
                if (a < pts.size && b < pts.size)
                    drawLine(Color(0xFF4B3CC8).copy(alpha = 0.07f), pts[a], pts[b], 0.6f)
            }

            // Nebulae
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF1A0A50).copy(alpha = 0.5f), Color.Transparent),
                    radius = 450f,
                    center = Offset(size.width * 0.82f + nebDrift, size.height * 0.2f)
                ),
                radius = 450f, center = Offset(size.width * 0.82f + nebDrift, size.height * 0.2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF041830).copy(alpha = 0.4f), Color.Transparent),
                    radius = 360f, center = Offset(size.width * 0.1f, size.height * 0.7f)
                ),
                radius = 360f, center = Offset(size.width * 0.1f, size.height * 0.7f)
            )
            // Charge-reactive nebula — grows around centre as fields fill
            if (charge > 0f) drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF4B3CC8).copy(alpha = 0.15f * charge), Color.Transparent),
                    radius = 280f * charge, center = Offset(size.width * 0.5f, size.height * 0.21f)
                ),
                radius = 280f * charge, center = Offset(size.width * 0.5f, size.height * 0.21f)
            )

            // Stars + constellation dots
            REG_STARS.forEachIndexed { i, (x, y) ->
                val tw = (sin(twinkle + i * 0.6f) * 0.3f + 0.7f).toFloat()
                val r = when (i % 5) {
                    0 -> 2.3f; 1 -> 1.7f; else -> 1.1f
                }
                drawCircle(
                    Color.White.copy(alpha = tw * 0.6f),
                    r,
                    Offset(size.width * x, size.height * y)
                )
                if (i % 5 == 0) {
                    val sx = size.width * x;
                    val sy = size.height * y
                    drawLine(
                        Color.White.copy(alpha = tw * 0.18f),
                        Offset(sx - 7f, sy),
                        Offset(sx + 7f, sy),
                        0.5f
                    )
                    drawLine(
                        Color.White.copy(alpha = tw * 0.18f),
                        Offset(sx, sy - 7f),
                        Offset(sx, sy + 7f),
                        0.5f
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── STELLAR BIRTH HERO ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier
                    .fillMaxWidth()
                    .height(260.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val sR = starR.dp.toPx() * starBreath

                    // ── Deep atmospheric glow — expands with charge ──
                    if (charge > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFF4B3CC8).copy(alpha = 0.3f * charge),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy), radius = sR * 3.5f
                            ),
                            radius = sR * 3.5f, center = Offset(cx, cy)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFF7C3AED).copy(alpha = 0.2f * charge),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy), radius = sR * 2.2f
                            ),
                            radius = sR * 2.2f, center = Offset(cx, cy)
                        )
                    }

                    // ── Outer orbital ring — always visible, slow ────
                    rotate(outerRing, Offset(cx, cy)) {
                        drawCircle(
                            Color(0xFF4B3CC8).copy(alpha = 0.3f), sR * 2.8f, Offset(cx, cy),
                            style = Stroke(
                                1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 9f))
                            )
                        )
                        // 3 rider dots
                        for (i in 0..2) {
                            val a = i * 2f * PI.toFloat() / 3f
                            drawCircle(
                                Color(0xFFD4AF37).copy(alpha = 0.5f + charge * 0.4f), 3.5f,
                                Offset(cx + sR * 2.8f * cos(a), cy + sR * 2.8f * sin(a))
                            )
                            drawCircle(
                                Color(0xFFD4AF37).copy(alpha = 0.2f * charge),
                                7f,
                                Offset(cx + sR * 2.8f * cos(a), cy + sR * 2.8f * sin(a)),
                                style = Stroke(1f)
                            )
                        }
                    }

                    // ── Mid ring — charge dependent ──────────────────
                    if (charge > 0f) {
                        rotate(midRing, Offset(cx, cy)) {
                            drawCircle(
                                Color(0xFF7C3AED).copy(alpha = 0.45f * charge),
                                sR * 1.9f, Offset(cx, cy), style = Stroke(1.3f)
                            )
                            drawCircle(
                                Color(0xFF9B6DFF).copy(alpha = 0.6f * charge), 4f,
                                Offset(cx + sR * 1.9f, cy)
                            )
                            drawCircle(
                                Color(0xFF9B6DFF).copy(alpha = 0.25f), 8f,
                                Offset(cx + sR * 1.9f, cy), style = Stroke(1f)
                            )
                        }
                    }

                    // ── Charge arc track ─────────────────────────────
                    if (charge > 0f) {
                        drawCircle(
                            Color(0xFF1A1040),
                            sR * 1.32f,
                            Offset(cx, cy),
                            style = Stroke(3f)
                        )
                        val arcCol = when {
                            charge < 0.4f -> Color(0xFF4B3CC8)
                            charge < 0.8f -> Color(0xFF7C3AED)
                            else -> Color(0xFF10B981)
                        }
                        drawArc(
                            arcCol.copy(alpha = 0.9f), -90f, charge * 360f, false,
                            Offset(cx - sR * 1.32f, cy - sR * 1.32f), Size(sR * 2.64f, sR * 2.64f),
                            style = Stroke(3f, cap = StrokeCap.Round)
                        )
                    }

                    // ── Orbiting particles — 16 dots circling ────────
                    if (charge > 0f) {
                        for (i in 0..15) {
                            val t = (particleFlow + i * 0.0625f) % 1f
                            val a = t * 2f * PI.toFloat()
                            val pR = sR * 1.6f
                            val px = cx + pR * cos(a);
                            val py = cy + pR * sin(a) * 0.5f
                            val pSz = if (i % 4 == 0) 3f else 1.8f
                            val pC = if (i % 3 == 0) Color(0xFFD4AF37) else Color(0xFF8B6DFF)
                            drawCircle(pC.copy(alpha = 0.7f * charge), pSz, Offset(px, py))
                        }
                    }

                    // ── Corona flares — only when charge > 0 ─────────
                    if (charge > 0f) {
                        rotate(coronaRot, Offset(cx, cy)) {
                            for (flare in 0..7) {
                                val a = flare * PI.toFloat() / 4f
                                val fL = sR * (0.3f + charge * 0.45f)
                                for (s in 0..12) {
                                    val t2 = s / 12f
                                    val fA = a + t2 * 0.18f
                                    val r2 = sR * 1.05f + t2 * fL
                                    val col =
                                        Color(0xFFE8D0FF).copy(alpha = (1f - t2) * 0.35f * charge)
                                    drawCircle(
                                        col, (2.5f - t2 * 2f).coerceAtLeast(0.3f),
                                        Offset(cx + r2 * cos(fA), cy + r2 * sin(fA))
                                    )
                                }
                            }
                        }
                    }

                    // ── Star body — grows with charge ─────────────────
                    // Only show core when name is filled
                    if (sR > 1f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFFFFF), Color(0xFFE8D8FF),
                                    Color(0xFF9B6DFF), Color(0xFF4B3CC8), Color(0xFF0A0420)
                                ),
                                center = Offset(cx - sR * 0.2f, cy - sR * 0.25f), radius = sR * 1.8f
                            ),
                            radius = sR, center = Offset(cx, cy)
                        )
                        // Specular
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.75f), Color.Transparent),
                                center = Offset(cx - sR * 0.28f, cy - sR * 0.32f),
                                radius = sR * 0.32f
                            ),
                            radius = sR * 0.32f, center = Offset(cx - sR * 0.28f, cy - sR * 0.32f)
                        )
                        // Star border glow
                        drawCircle(
                            Color(0xFFD4AF37).copy(alpha = 0.45f * starBreath),
                            sR, Offset(cx, cy), style = Stroke(1.5f)
                        )
                    } else {
                        // Empty state — just a tiny seed dot
                        drawCircle(Color(0xFF4B3CC8).copy(alpha = 0.4f), 6f, Offset(cx, cy))
                        drawCircle(
                            Color(0xFF4B3CC8).copy(alpha = 0.15f), 14f, Offset(cx, cy),
                            style = Stroke(1f)
                        )
                    }

                    // ── Data streams — 3 vertical beams to fields ────
                    if (charge > 0f) {
                        val beamW = 2.5f
                        // Upward beam from star
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF8B6DFF).copy(alpha = 0.3f * charge)
                                ),
                                startY = 30f, endY = cy - sR
                            ),
                            topLeft = Offset(cx - beamW / 2f, 30f),
                            size = Size(beamW, (cy - sR - 30f).coerceAtLeast(0f))
                        )
                        // Data packets
                        for (p in 0..2) {
                            val pY = cy - sR - ((dataStream + p * 0.33f) % 1f) * (cy - sR - 50f)
                            if (pY > 0f) drawCircle(
                                Color(0xFF8B6DFF).copy(alpha = 0.7f * charge),
                                2.5f, Offset(cx, pY)
                            )
                        }
                    }

                    // ── Supernova on success ──────────────────────────
                    if (novaP > 0f) {
                        val cols = listOf(
                            Color.White, Color(0xFF9B6DFF), Color(0xFFD4AF37),
                            Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFFFFB800)
                        )
                        for (i in 0..29) {
                            val a = i * 12f * PI.toFloat() / 180f
                            val dist = novaP * sR * 5.5f
                            val px = cx + dist * cos(a);
                            val py = cy + dist * sin(a)
                            val pA = (1f - novaP).coerceIn(0f, 1f)
                            drawCircle(
                                cols[i % cols.size].copy(alpha = pA),
                                (6f - novaP * 5.5f).coerceAtLeast(0.4f), Offset(px, py)
                            )
                            drawLine(
                                cols[i % cols.size].copy(alpha = pA * 0.3f),
                                Offset(cx, cy), Offset(px, py), 0.6f
                            )
                        }
                        for (ring in 0..2) {
                            drawCircle(
                                Color(0xFF9B6DFF).copy(alpha = (1f - novaP) * 0.5f),
                                novaP * sR * (3.5f + ring * 1.8f), Offset(cx, cy),
                                style = Stroke((2.5f - ring * 0.6f).coerceAtLeast(0.3f))
                            )
                        }
                    }
                }

                // Text in star centre when visible
                if (charge > 0.3f) {
                    Text(
                        "${(charge * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                    )
                }
            }

            // ── TITLE ─────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    "CREW ENLISTMENT",
                    color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp, textAlign = TextAlign.Center
                )
                Text(
                    "TERMINAL",
                    color = Color(0xFF8C83E4).copy(alpha = 0.7f), fontSize = 13.sp,
                    letterSpacing = 6.sp, fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(4.dp))
                // Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    enlisted -> Color(0xFF10B981).copy(alpha = beacon)
                                    charge >= 1f -> Color(0xFF10B981).copy(alpha = beacon)
                                    charge > 0f -> Color(0xFFD4AF37).copy(alpha = beacon)
                                    else -> Color(0xFF4B3CC8).copy(alpha = beacon)
                                }
                            )
                    )
                    Text(
                        when {
                            enlisted -> "ENLISTED — WELCOME ABOARD"
                            charge >= 1f -> "READY FOR ENLISTMENT"
                            charge > 0f -> "REGISTRATION IN PROGRESS"
                            else -> "AWAITING CREW DATA"
                        },
                        color = when {
                            enlisted -> Color(0xFF10B981)
                            charge >= 1f -> Color(0xFF10B981)
                            charge > 0f -> Color(0xFFD4AF37)
                            else -> Color(0xFF4B3CC8).copy(alpha = 0.7f)
                        },
                        fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── ENLISTMENT FORM ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = formY.dp)
                    .alpha(formA)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF08081A), Color(0xFF050510))
                            )
                        )
                        .border(
                            1.dp, Brush.linearGradient(
                                listOf(
                                    Color(0xFF4B3CC8).copy(alpha = 0.5f),
                                    Color(0xFF7C3AED).copy(alpha = 0.2f),
                                    Color(0xFF4B3CC8).copy(alpha = 0.5f)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // Panel header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Canvas(Modifier.size(10.dp)) {
                                drawCircle(
                                    Color(0xFF4B3CC8),
                                    size.minDimension / 2f,
                                    style = Stroke(1.5f)
                                )
                                drawCircle(
                                    Color(0xFF4B3CC8).copy(alpha = 0.25f),
                                    size.minDimension / 2f + 3f, style = Stroke(0.7f)
                                )
                            }
                            Text(
                                "CREW MANIFEST", color = Color(0xFF8C83E4).copy(alpha = 0.55f),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                            )
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF4B3CC8).copy(alpha = 0.35f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            Text(
                                "$filledCount/3",
                                color = Color(0xFF4B3CC8).copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // SYS-01 Crew Name
                        RegField(
                            value = name, onValueChange = { name = it },
                            label = "CREW NAME", placeholder = "Full name",
                            icon = Icons.Default.Person, accent = Color(0xFF4B3CC8), active = f1,
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                            visualTransformation = VisualTransformation.None
                        )

                        // SYS-02 Email
                        RegField(
                            value = email,
                            onValueChange = { email = it },
                            label = "CREW EMAIL",
                            placeholder = "your@email.com",
                            icon = Icons.Default.MailOutline,
                            accent = Color(0xFF7C3AED),
                            active = f2,
                            keyboardType = KeyboardType.Email,
                            capitalization = KeyboardCapitalization.None,
                            visualTransformation = VisualTransformation.None
                        )

                        // SYS-03 Password
                        RegField(
                            value = password, onValueChange = { password = it },
                            label = "ACCESS CODE", placeholder = "••••••••",
                            icon = Icons.Default.Lock, accent = Color(0xFF06B6D4), active = f3,
                            keyboardType = KeyboardType.Password,
                            capitalization = KeyboardCapitalization.None,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        // Enlistment charge bar
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                Modifier.fillMaxWidth(), Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Text(
                                    "ENLISTMENT CHARGE",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.4f), fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace, letterSpacing = 1.sp
                                )
                                val col = if (charge >= 1f) Color(0xFF10B981) else Color(0xFF4B3CC8)
                                Text(
                                    "${(charge * 100).toInt()}%",
                                    color = col, fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold
                                )
                            }
                            // Segmented bar — 3 segments, one per field
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    f1 to Color(0xFF4B3CC8),
                                    f2 to Color(0xFF7C3AED),
                                    f3 to Color(0xFF06B6D4)
                                ).forEach { (filled, col) ->
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (filled) col
                                                else Color(0xFF0A0A1A)
                                            )
                                            .border(
                                                0.5.dp,
                                                col.copy(alpha = 0.25f), RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }

                        // ENLIST button
                        val canEnlist = f1 && f2 && f3 && !state.isLoading
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (canEnlist) Brush.linearGradient(
                                        listOf(Color(0xFF1A0A50), Color(0xFF2A1070))
                                    )
                                    else Brush.linearGradient(
                                        listOf(Color(0xFF0A0A14), Color(0xFF0A0A14))
                                    )
                                )
                                .border(
                                    1.dp,
                                    if (canEnlist) Brush.linearGradient(
                                        listOf(
                                            Color(0xFF5B3FD8).copy(alpha = 0.8f),
                                            Color(0xFF8B6DFF).copy(alpha = 0.4f),
                                            Color(0xFF5B3FD8).copy(alpha = 0.8f)
                                        )
                                    )
                                    else Brush.linearGradient(
                                        listOf(
                                            Color(0xFF151525), Color(0xFF151525)
                                        )
                                    ),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = canEnlist) {
                                    viewModel.register(name, email, password)
                                }
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = when {
                                    enlisted -> 2
                                    state.isLoading -> 1
                                    else -> 0
                                },
                                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                                label = "enlist_btn"
                            ) { s ->
                                when (s) {
                                    0 -> Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            "✦", fontSize = 18.sp,
                                            color = if (canEnlist) Color(0xFFD4AF37) else Color(
                                                0xFF252535
                                            )
                                        )
                                        Text(
                                            if (canEnlist) "ENLIST IN CREW" else "COMPLETE ALL FIELDS",
                                            color = if (canEnlist) Color(0xFF8B6DFF) else Color(
                                                0xFF252535
                                            ),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    1 -> Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CSILoader(size = LoaderSize.SMALL)
                                        Text(
                                            "REGISTERING CREW MEMBER",
                                            color = Color(0xFF8B6DFF),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    else -> Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("✦", fontSize = 18.sp, color = Color(0xFF10B981))
                                        Text(
                                            "WELCOME ABOARD",
                                            color = Color(0xFF10B981), fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // Error
                        state.error?.let { err ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1A0808))
                                    .border(
                                        1.dp, Color(0xFFEF4444).copy(alpha = 0.35f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("⚠️", fontSize = 14.sp)
                                    Text(
                                        err, color = Color(0xFFFCA5A5), fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Divider
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0xFF4B3CC8).copy(alpha = 0.25f), Color.Transparent
                                        )
                                    )
                                )
                        )


                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Routes.LOGIN) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("‹", color = Color(0xFF8B6DFF), fontSize = 14.sp)
                                Text(
                                    "Already enlisted?",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.5f), fontSize = 13.sp
                                )
                                Text(
                                    "RETURN TO DOCK",
                                    color = Color(0xFF8B6DFF), fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // HUD brackets
                Canvas(Modifier.matchParentSize()) {
                    val s = 18f;
                    val w = 1.5f;
                    val c = Color(0xFF4B3CC8).copy(alpha = 0.4f)
                    drawLine(c, Offset(0f, s), Offset(0f, 0f), w)
                    drawLine(c, Offset(0f, 0f), Offset(s, 0f), w)
                    drawLine(c, Offset(size.width - s, 0f), Offset(size.width, 0f), w)
                    drawLine(c, Offset(size.width, 0f), Offset(size.width, s), w)
                    drawLine(c, Offset(0f, size.height - s), Offset(0f, size.height), w)
                    drawLine(c, Offset(0f, size.height), Offset(s, size.height), w)
                    drawLine(
                        c,
                        Offset(size.width - s, size.height),
                        Offset(size.width, size.height),
                        w
                    )
                    drawLine(
                        c,
                        Offset(size.width, size.height - s),
                        Offset(size.width, size.height),
                        w
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// =============================================================================
// REG FIELD
// =============================================================================
@Composable
private fun RegField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    active: Boolean,
    keyboardType: KeyboardType,
    capitalization: KeyboardCapitalization,
    visualTransformation: VisualTransformation
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
                    .background(if (active) accent else Color(0xFF252535))
            )
            Text(
                label, color = if (active) accent.copy(alpha = 0.7f) else Color(0xFF252535),
                fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp
            )
            Box(
                Modifier
                    .width(12.dp)
                    .height(1.dp)
                    .background(if (active) accent.copy(alpha = 0.35f) else Color(0xFF1A1A28))
            )
            Text(
                if (active) "● ONLINE" else "○ EMPTY",
                color = if (active) accent.copy(alpha = 0.5f) else Color(0xFF252535),
                fontSize = 8.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp
            )
        }
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF252535), fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    icon,
                    null,
                    tint = if (active) accent else Color(0xFF252535),
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization
            ),
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = accent,
                unfocusedBorderColor = accent.copy(alpha = 0.25f),
                focusedContainerColor = Color(0xFF06060E),
                unfocusedContainerColor = Color(0xFF04040C),
                focusedLabelColor = accent,
                unfocusedLabelColor = accent.copy(alpha = 0.4f),
                focusedLeadingIconColor = accent,
                unfocusedLeadingIconColor = accent.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}