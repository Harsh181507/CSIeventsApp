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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
private val LOGIN_STARS = listOf(
    0.05f to 0.04f, 0.91f to 0.07f, 0.42f to 0.02f, 0.16f to 0.15f, 0.75f to 0.10f,
    0.03f to 0.30f, 0.97f to 0.22f, 0.30f to 0.38f, 0.82f to 0.44f, 0.09f to 0.53f,
    0.67f to 0.57f, 0.54f to 0.80f, 0.22f to 0.75f, 0.86f to 0.69f, 0.47f to 0.91f,
    0.71f to 0.88f, 0.34f to 0.63f, 0.61f to 0.21f, 0.49f to 0.46f, 0.25f to 0.34f,
    0.79f to 0.72f, 0.38f to 0.56f, 0.92f to 0.84f, 0.13f to 0.68f, 0.58f to 0.97f,
    0.66f to 0.39f, 0.41f to 0.71f, 0.84f to 0.56f, 0.20f to 0.49f, 0.55f to 0.16f
)

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var dockSuccess by remember { mutableStateOf(false) }

    // Navigate on role received
    LaunchedEffect(state.role) {
        state.role?.let { role ->
            dockSuccess = true
            delay(1400)
            val dest = when (role) {
                "STUDENT" -> Routes.STUDENT_DASHBOARD
                "JUDGE" -> Routes.JUDGE_DASHBOARD
                "ORGANIZER" -> Routes.ORGANIZER_DASHBOARD
                else -> Routes.LOGIN
            }
            navController.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
        }
    }

    val emailFilled = email.isNotBlank()
    val passwordFilled = password.isNotBlank()
    val formCharge = listOf(emailFilled, passwordFilled).count { it } / 2f

    // ── infinite animations ───────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "lg")

    val twinkle by inf.animateFloat(
        0f, (2f * PI).toFloat(),
        infiniteRepeatable(tween(9000, easing = LinearEasing)), "tw"
    )
    val nebDrift by inf.animateFloat(
        0f, 22f,
        infiniteRepeatable(tween(14000, easing = EaseInOutSine), RepeatMode.Reverse), "nd"
    )

    // Station rotation — slow, majestic
    val stationRot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(20000, easing = LinearEasing)), "sr"
    )

    // Station arm rotation — opposite direction, faster
    val armRot by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(12000, easing = LinearEasing)), "ar"
    )

    // Docking ring pulse
    val dockRing by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000, easing = LinearEasing)), "dr"
    )

    // Signal beam flicker
    val signalFlick by inf.animateFloat(
        0.6f, 1f,
        infiniteRepeatable(tween(120), RepeatMode.Reverse), "sf"
    )

    // Approach corridor perspective lines scroll
    val corridorScroll by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1800, easing = LinearEasing)), "cs"
    )

    // Beacon blink
    val beacon by inf.animateFloat(
        0.2f, 1f,
        infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse), "bc"
    )

    // Core breathe
    val stationBreath by inf.animateFloat(
        0.96f, 1.04f,
        infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse), "sb"
    )

    // Supernova on success
    val novaP by animateFloatAsState(
        targetValue = if (dockSuccess) 1f else 0f,
        animationSpec = tween(1200, easing = EaseOutExpo), label = "np"
    )

    // Form slide in
    var formIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); formIn = true }
    val formA by animateFloatAsState(if (formIn) 1f else 0f, tween(700), label = "fa")
    val formY by animateFloatAsState(
        if (formIn) 0f else 60f,
        spring(0.7f, Spring.StiffnessMediumLow), label = "fy"
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

            // Nebulae
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF1A0A50).copy(alpha = 0.5f), Color.Transparent),
                    radius = 480f,
                    center = Offset(size.width * 0.8f + nebDrift, size.height * 0.25f)
                ),
                radius = 480f, center = Offset(size.width * 0.8f + nebDrift, size.height * 0.25f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF041830).copy(alpha = 0.45f), Color.Transparent),
                    radius = 380f, center = Offset(size.width * 0.1f, size.height * 0.65f)
                ),
                radius = 380f, center = Offset(size.width * 0.1f, size.height * 0.65f)
            )

            // Stars
            LOGIN_STARS.forEachIndexed { i, (x, y) ->
                val tw = (sin(twinkle + i * 0.6f) * 0.3f + 0.7f).toFloat()
                val r = when (i % 5) {
                    0 -> 2.3f; 1 -> 1.7f; else -> 1.1f
                }
                drawCircle(
                    Color.White.copy(alpha = tw * 0.6f), r,
                    Offset(size.width * x, size.height * y)
                )
                if (i % 5 == 0) {
                    val sx = size.width * x;
                    val sy = size.height * y
                    drawLine(
                        Color.White.copy(alpha = tw * 0.18f),
                        Offset(sx - 7f, sy), Offset(sx + 7f, sy), 0.5f
                    )
                    drawLine(
                        Color.White.copy(alpha = tw * 0.18f),
                        Offset(sx, sy - 7f), Offset(sx, sy + 7f), 0.5f
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

            // ── SPACE STATION HERO ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier
                    .fillMaxWidth()
                    .height(300.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val base = 50.dp.toPx()

                    // ── Approach corridor — perspective rings ──────────
                    // Warp-tunnel effect: rings shrink toward center
                    for (ring in 1..6) {
                        val t = (ring / 6f + corridorScroll) % 1f
                        val ringR = base * (0.5f + t * 3.5f)
                        val alpha = (1f - t) * 0.25f
                        val ringOff = Offset(cx, cy)
                        drawCircle(
                            Color(0xFF4B3CC8).copy(alpha = alpha), ringR, ringOff,
                            style = Stroke(0.8f)
                        )
                    }

                    // ── Docking target grid lines ─────────────────────
                    val gridSize = base * 2.8f
                    for (line in -3..3) {
                        val alpha = (0.15f - abs(line) * 0.025f).coerceAtLeast(0.02f)
                        // Horizontal
                        drawLine(
                            Color(0xFF4B3CC8).copy(alpha = alpha),
                            Offset(cx - gridSize, cy + line * base * 0.38f),
                            Offset(cx + gridSize, cy + line * base * 0.38f), 0.5f
                        )
                        // Vertical
                        drawLine(
                            Color(0xFF4B3CC8).copy(alpha = alpha),
                            Offset(cx + line * base * 0.55f, cy - gridSize * 0.6f),
                            Offset(cx + line * base * 0.55f, cy + gridSize * 0.6f), 0.5f
                        )
                    }

                    // ── Station glow ──────────────────────────────────
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xFF6B3FD8).copy(alpha = 0.35f * stationBreath),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy), radius = base * 3f
                        ),
                        radius = base * 3f, center = Offset(cx, cy)
                    )

                    // ── Outer docking ring — slow rotation ────────────
                    rotate(stationRot, Offset(cx, cy)) {
                        // Main ring
                        drawCircle(
                            Color(0xFF5B3FD8).copy(alpha = 0.5f),
                            base * 2.2f, Offset(cx, cy),
                            style = Stroke(2.5f)
                        )
                        // 8 docking nodes on ring
                        for (n in 0..7) {
                            val a = n * PI.toFloat() / 4f
                            val nx = cx + base * 2.2f * cos(a)
                            val ny = cy + base * 2.2f * sin(a)
                            drawCircle(Color(0xFFD4AF37).copy(alpha = 0.7f), 4f, Offset(nx, ny))
                            drawCircle(
                                Color(0xFFD4AF37).copy(alpha = 0.2f), 8f, Offset(nx, ny),
                                style = Stroke(1f)
                            )
                        }
                        // Ring tick marks
                        for (tick in 0..23) {
                            val a = tick * PI.toFloat() / 12f
                            val r1 = base * 2.05f
                            val r2 = base * 2.35f
                            drawLine(
                                Color(0xFF8B6DFF).copy(alpha = 0.35f),
                                Offset(cx + r1 * cos(a), cy + r1 * sin(a)),
                                Offset(cx + r2 * cos(a), cy + r2 * sin(a)), 0.8f
                            )
                        }
                    }

                    // ── Solar arms — opposite direction ───────────────
                    rotate(armRot, Offset(cx, cy)) {
                        for (arm in 0..3) {
                            val a = arm * PI.toFloat() / 2f
                            // Arm shaft
                            drawLine(
                                Color(0xFF7C3AED).copy(alpha = 0.5f),
                                Offset(cx + base * 0.55f * cos(a), cy + base * 0.55f * sin(a)),
                                Offset(cx + base * 1.8f * cos(a), cy + base * 1.8f * sin(a)), 3f
                            )
                            // Solar panel
                            val panX = cx + base * 2.0f * cos(a)
                            val panY = cy + base * 2.0f * sin(a)
                            val pW = base * 0.55f;
                            val pH = base * 0.22f
                            val perpA = a + PI.toFloat() / 2f
                            drawRect(
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color(0xFF4B3CC8).copy(alpha = 0.6f),
                                        Color(0xFF7C3AED).copy(alpha = 0.4f)
                                    )
                                ),
                                topLeft = Offset(
                                    panX - pW / 2f * cos(perpA) - pH / 2f * cos(a),
                                    panY - pW / 2f * sin(perpA) - pH / 2f * sin(a)
                                ),
                                size = Size(pW, pH)
                            )
                            // Panel border
                            drawRect(
                                Color(0xFF9B6DFF).copy(alpha = 0.35f),
                                topLeft = Offset(
                                    panX - pW / 2f * cos(perpA) - pH / 2f * cos(a),
                                    panY - pW / 2f * sin(perpA) - pH / 2f * sin(a)
                                ),
                                size = Size(pW, pH), style = Stroke(0.6f)
                            )
                        }
                    }

                    // ── Station core hub ──────────────────────────────
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xFFE8E0FF), Color(0xFF9B6DFF),
                                Color(0xFF4B3CC8), Color(0xFF1A0A40)
                            ),
                            center = Offset(cx - base * 0.18f, cy - base * 0.22f),
                            radius = base * 1.5f
                        ),
                        radius = base * 0.55f, center = Offset(cx, cy)
                    )
                    // Core border
                    drawCircle(
                        Color(0xFFD4AF37).copy(alpha = 0.6f * stationBreath),
                        base * 0.55f, Offset(cx, cy), style = Stroke(1.5f)
                    )
                    // Specular
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
                            center = Offset(cx - base * 0.2f, cy - base * 0.22f),
                            radius = base * 0.22f
                        ),
                        radius = base * 0.22f,
                        center = Offset(cx - base * 0.2f, cy - base * 0.22f)
                    )

                    // ── Docking port ring — pulsing ────────────────────
                    drawCircle(
                        Color(0xFF00FF88).copy(alpha = (1f - dockRing) * 0.6f),
                        base * (0.65f + dockRing * 0.5f), Offset(cx, cy),
                        style = Stroke(1.5f)
                    )

                    // ── Signal beam — vertical, flickers ─────────────
                    if (formCharge > 0f) {
                        val beamW = 3f
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF00FF88).copy(alpha = 0.4f * formCharge * signalFlick)
                                ),
                                startY = 30f, endY = cy - base * 0.65f
                            ),
                            topLeft = Offset(cx - beamW / 2f, 30f),
                            size = Size(beamW, cy - base * 0.65f - 30f)
                        )
                        // Data packets on beam
                        for (p in 0..2) {
                            val pY = cy - base * 0.65f -
                                    ((corridorScroll + p * 0.33f) % 1f) * (cy - base * 0.65f - 40f)
                            drawCircle(
                                Color(0xFF00FF88).copy(alpha = 0.8f * formCharge),
                                3f, Offset(cx, pY)
                            )
                        }
                    }

                    // ── Supernova on success ──────────────────────────
                    if (novaP > 0f) {
                        val cols = listOf(
                            Color.White, Color(0xFF9B6DFF), Color(0xFFD4AF37),
                            Color(0xFF00FF88), Color(0xFF06B6D4), Color(0xFFEC4899)
                        )
                        for (i in 0..23) {
                            val a = i * 15f * PI.toFloat() / 180f
                            val dist = novaP * base * 5f
                            val px = cx + dist * cos(a);
                            val py = cy + dist * sin(a)
                            val pA = (1f - novaP).coerceIn(0f, 1f)
                            drawCircle(
                                cols[i % cols.size].copy(alpha = pA),
                                (6f - novaP * 5f).coerceAtLeast(0.5f), Offset(px, py)
                            )
                            drawLine(
                                cols[i % cols.size].copy(alpha = pA * 0.3f),
                                Offset(cx, cy), Offset(px, py), 0.7f
                            )
                        }
                        for (ring in 0..2) {
                            drawCircle(
                                Color(0xFF9B6DFF).copy(alpha = (1f - novaP) * 0.5f),
                                novaP * base * (3f + ring * 1.5f), Offset(cx, cy),
                                style = Stroke(2f - ring * 0.5f)
                            )
                        }
                    }
                }

                // "CSI" text in station core
                Text(
                    "CSI", color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, letterSpacing = 2.sp
                )
            }

            // ── TITLE ─────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    "CSI EVENTS",
                    color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Text(
                    "TECHNICAL CLUB",
                    color = Color(0xFF8C83E4).copy(alpha = 0.7f), fontSize = 12.sp,
                    letterSpacing = 5.sp, fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(4.dp))

                // Docking status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (formCharge >= 1f) Color(0xFF10B981).copy(alpha = beacon)
                                else Color(0xFF4B3CC8).copy(alpha = beacon)
                            )
                    )
                    Text(
                        when {
                            dockSuccess -> "DOCKING COMPLETE"
                            formCharge >= 1f -> "READY TO DOCK"
                            formCharge > 0f -> "APPROACH SEQUENCE ACTIVE"
                            else -> "AWAITING AUTHENTICATION"
                        },
                        color = when {
                            dockSuccess -> Color(0xFF10B981)
                            formCharge >= 1f -> Color(0xFF10B981)
                            formCharge > 0f -> Color(0xFFD4AF37)
                            else -> Color(0xFF4B3CC8).copy(alpha = 0.7f)
                        },
                        fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── AUTHENTICATION PANEL ──────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = formY.dp)
                    .alpha(formA)
            ) {
                // Panel
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
                                    Color(0xFF4B3CC8), size.minDimension / 2f,
                                    style = Stroke(1.5f)
                                )
                                drawCircle(
                                    Color(0xFF4B3CC8).copy(alpha = 0.3f),
                                    size.minDimension / 2f + 3f, style = Stroke(0.7f)
                                )
                            }
                            Text(
                                "AUTHENTICATION",
                                color = Color(0xFF8C83E4).copy(alpha = 0.55f), fontSize = 10.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 2.sp
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
                        }

                        // Email field
                        LoginField(
                            value = email,
                            onValueChange = { email = it },
                            label = "CREW EMAIL",
                            placeholder = "your@email.com",
                            icon = Icons.Default.MailOutline,
                            accent = Color(0xFF4B3CC8),
                            active = emailFilled,
                            keyboardType = KeyboardType.Email,
                            visualTransformation = VisualTransformation.None
                        )

                        // Password field
                        LoginField(
                            value = password,
                            onValueChange = { password = it },
                            label = "ACCESS CODE",
                            placeholder = "••••••••",
                            icon = Icons.Default.Lock,
                            accent = Color(0xFF7C3AED),
                            active = passwordFilled,
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (showPass) VisualTransformation.None
                            else PasswordVisualTransformation()
                        )

                        // Charge bar
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                Arrangement.SpaceBetween, Alignment.CenterVertically
                            ) {
                                Text(
                                    "DOCK READINESS",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.4f), fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace, letterSpacing = 1.sp
                                )
                                val pct = (formCharge * 100).toInt()
                                Text(
                                    "$pct%",
                                    color = if (formCharge >= 1f) Color(0xFF10B981)
                                    else Color(0xFF4B3CC8),
                                    fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF0A0A1A))
                            ) {
                                val animCharge by animateFloatAsState(
                                    formCharge,
                                    tween(600, easing = EaseOutCubic), label = "acl"
                                )
                                Box(
                                    Modifier
                                        .fillMaxWidth(animCharge)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color(0xFF4B3CC8), Color(0xFF7C3AED),
                                                    if (formCharge >= 1f) Color(0xFF10B981)
                                                    else Color(0xFF7C3AED)
                                                )
                                            )
                                        )
                                )
                            }
                        }

                        // LAUNCH / LOGIN button
                        val canDock = emailFilled && passwordFilled && !state.isLoading
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (canDock)
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF1A0A50), Color(0xFF2A1070)
                                            )
                                        )
                                    else
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF0A0A14), Color(0xFF0A0A14)
                                            )
                                        )
                                )
                                .border(
                                    1.dp,
                                    if (canDock) Brush.linearGradient(
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
                                .clickable(enabled = canDock) {
                                    viewModel.login(email, password)
                                }
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = when {
                                    dockSuccess -> 2
                                    state.isLoading -> 1
                                    else -> 0
                                },
                                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                                label = "btn"
                            ) { s ->
                                when (s) {
                                    0 -> Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("🚀", fontSize = 18.sp)
                                        Text(
                                            if (canDock) "INITIATE DOCKING" else "ENTER CREDENTIALS",
                                            color = if (canDock) Color(0xFF8B6DFF)
                                            else Color(0xFF252535),
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
                                            "AUTHENTICATING",
                                            color = Color(0xFF8B6DFF), fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace, letterSpacing = 1.sp
                                        )
                                    }

                                    else -> Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("✦", fontSize = 18.sp, color = Color(0xFF10B981))
                                        Text(
                                            "DOCKING COMPLETE",
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
                                            Color(0xFF4B3CC8).copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Register link
                        Box(Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Routes.REGISTER)
                            }, contentAlignment = Alignment.Center) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    "New crew member?",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.5f), fontSize = 13.sp
                                )
                                Text(
                                    "REQUEST ACCESS",
                                    color = Color(0xFF8B6DFF), fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
                                )
                                Text("›", color = Color(0xFF8B6DFF), fontSize = 14.sp)
                            }
                        }
                    }
                }

                // HUD corner brackets on panel
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
// LOGIN FIELD — constellation-styled input
// =============================================================================
@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    active: Boolean,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        // Label row
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
            if (active) {
                Text(
                    "● ONLINE", color = accent.copy(alpha = 0.5f), fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp
                )
            } else {
                Text(
                    "○ EMPTY", color = Color(0xFF252535), fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder, color = Color(0xFF252535), fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    icon, null, tint = if (active) accent else Color(0xFF252535),
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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