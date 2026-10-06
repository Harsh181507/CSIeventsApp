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
import androidx.compose.ui.res.painterResource
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
import com.example.csievent.R
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val RG_STARS = listOf(
    0.06f to 0.05f, 0.91f to 0.07f, 0.40f to 0.02f, 0.15f to 0.17f, 0.73f to 0.11f,
    0.03f to 0.33f, 0.97f to 0.26f, 0.29f to 0.44f, 0.81f to 0.46f, 0.10f to 0.56f,
    0.65f to 0.64f, 0.51f to 0.83f, 0.21f to 0.77f, 0.85f to 0.71f, 0.45f to 0.93f,
    0.69f to 0.89f, 0.33f to 0.67f, 0.59f to 0.25f, 0.47f to 0.51f, 0.23f to 0.37f,
    0.77f to 0.75f, 0.37f to 0.59f, 0.91f to 0.84f, 0.13f to 0.71f, 0.56f to 0.97f
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
    var success by remember { mutableStateOf(false) }

    LaunchedEffect(state.role) {
        state.role?.let { role ->
            success = true
            delay(1200)
            val dest = Routes.dashboardFor(role) ?: Routes.LOGIN
            navController.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
        }
    }

    val f1 = name.isNotBlank()
    val f2 = email.isNotBlank()
    val f3 = password.isNotBlank()
    val charge = listOf(f1, f2, f3).count { it } / 3f

    val inf = rememberInfiniteTransition(label = "rg")

    val ring1 by inf.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        "r1"
    )
    val ring2 by inf.animateFloat(
        360f,
        0f,
        infiniteRepeatable(tween(14000, easing = LinearEasing)),
        "r2"
    )
    val ring3 by inf.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(21000, easing = LinearEasing)),
        "r3"
    )
    val pulse by inf.animateFloat(
        0.92f,
        1f,
        infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse),
        "pu"
    )
    val twinkle by inf.animateFloat(
        0f,
        (2f * PI).toFloat(),
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        "tw"
    )
    val nebula by inf.animateFloat(
        0f,
        20f,
        infiniteRepeatable(tween(13000, easing = EaseInOutSine), RepeatMode.Reverse),
        "nb"
    )
    val beacon by inf.animateFloat(
        0.2f,
        1f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        "bc"
    )
    val particleAngle by inf.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(5000, easing = LinearEasing)),
        "pa"
    )

    val burstP by animateFloatAsState(
        targetValue = if (success) 1f else 0f,
        animationSpec = tween(1000, easing = EaseOutExpo), label = "bp"
    )

    var formIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(200); formIn = true }
    val formA by animateFloatAsState(if (formIn) 1f else 0f, tween(700), label = "fa")
    val formY by animateFloatAsState(
        if (formIn) 0f else 80f,
        spring(0.6f, Spring.StiffnessMediumLow), label = "fy"
    )

    Box(Modifier
        .fillMaxSize()
        .background(Color(0xFF020912))) {

        // ── BACKGROUND ────────────────────────────────────────────────
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF030B1A), Color(0xFF020810), Color(0xFF050618))
                )
            )

            // Nebulae
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF1E0E5A).copy(alpha = 0.55f), Color.Transparent),
                    radius = 500f,
                    center = Offset(size.width * 0.85f + nebula, size.height * 0.22f)
                ),
                radius = 500f,
                center = Offset(size.width * 0.85f + nebula, size.height * 0.22f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF041C30).copy(alpha = 0.45f), Color.Transparent),
                    radius = 380f,
                    center = Offset(size.width * 0.08f, size.height * 0.75f)
                ),
                radius = 380f,
                center = Offset(size.width * 0.08f, size.height * 0.75f)
            )
            // Charge reactive nebula centre
            if (charge > 0f) drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF4B3CC8).copy(alpha = 0.15f * charge), Color.Transparent),
                    radius = 260f * charge,
                    center = Offset(size.width * 0.5f, size.height * 0.2f)
                ),
                radius = 260f * charge,
                center = Offset(size.width * 0.5f, size.height * 0.2f)
            )

            // Stars
            RG_STARS.forEachIndexed { i, (x, y) ->
                val tw = (sin(twinkle + i * 0.58f) * 0.32f + 0.68f).toFloat()
                val r = when (i % 5) {
                    0 -> 2.4f; 1 -> 1.8f; else -> 1.1f
                }
                val pos = Offset(size.width * x, size.height * y)
                drawCircle(Color.White.copy(alpha = tw * 0.65f), r, pos)
                if (i % 5 == 0) {
                    drawLine(
                        Color.White.copy(alpha = tw * 0.18f),
                        Offset(pos.x - 8f, pos.y), Offset(pos.x + 8f, pos.y), 0.5f
                    )
                    drawLine(
                        Color.White.copy(alpha = tw * 0.18f),
                        Offset(pos.x, pos.y - 8f), Offset(pos.x, pos.y + 8f), 0.5f
                    )
                }
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))

            // ── LOGO + ORBITAL HERO ───────────────────────────────────
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
                    val logoR = 58.dp.toPx()

                    // Atmospheric halo — grows with charge
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xFF4B3CC8).copy(alpha = 0.12f + charge * 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy), radius = logoR * 3.5f
                        ),
                        radius = logoR * 3.5f, center = Offset(cx, cy)
                    )

                    // Ring 3 — outermost gold dashed
                    rotate(ring3, Offset(cx, cy)) {
                        drawCircle(
                            Color(0xFFD4AF37).copy(alpha = 0.3f + charge * 0.15f),
                            logoR * 2.7f, Offset(cx, cy),
                            style = Stroke(
                                1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 9f))
                            )
                        )
                        for (i in 0..2) {
                            val a = i * 2f * PI.toFloat() / 3f
                            val nx = cx + logoR * 2.7f * cos(a)
                            val ny = cy + logoR * 2.7f * sin(a)
                            drawCircle(
                                Color(0xFFD4AF37).copy(alpha = 0.5f + charge * 0.3f),
                                3.5f, Offset(nx, ny)
                            )
                        }
                    }

                    // Ring 2 — violet, CW
                    rotate(ring1, Offset(cx, cy)) {
                        drawCircle(
                            Color(0xFF7C3AED).copy(alpha = 0.5f),
                            logoR * 2.0f, Offset(cx, cy), style = Stroke(1.5f)
                        )
                        drawCircle(
                            Color(0xFF9B6DFF).copy(alpha = 0.9f), 5f,
                            Offset(cx + logoR * 2.0f, cy)
                        )
                        drawCircle(
                            Color(0xFF9B6DFF).copy(alpha = 0.3f), 11f,
                            Offset(cx + logoR * 2.0f, cy), style = Stroke(1f)
                        )
                    }

                    // Ring 1 — inner tilted ellipse, CCW
                    rotate(ring2 + 45f, Offset(cx, cy)) {
                        drawOval(
                            Color(0xFF4B3CC8).copy(alpha = 0.4f),
                            Offset(cx - logoR * 1.4f, cy - logoR * 0.36f),
                            Size(logoR * 2.8f, logoR * 0.72f),
                            style = Stroke(1.2f)
                        )
                        drawCircle(
                            Color(0xFFD4AF37).copy(alpha = 0.7f), 4f,
                            Offset(cx, cy - logoR * 0.36f)
                        )
                    }

                    // Orbiting particles — 12
                    for (i in 0..11) {
                        val t = (particleAngle / 360f + i / 12f) % 1f
                        val a = t * 2f * PI.toFloat()
                        val pR = logoR * 1.62f
                        val px = cx + pR * cos(a)
                        val py = cy + pR * 0.4f * sin(a)
                        val col = if (i % 3 == 0) Color(0xFFD4AF37) else Color(0xFF8B6DFF)
                        drawCircle(
                            col.copy(alpha = 0.65f), if (i % 3 == 0) 3f else 2f,
                            Offset(px, py)
                        )
                    }

                    // Charge arc — 3 segments
                    drawCircle(
                        Color(0xFF0E0C22), logoR * 1.15f, Offset(cx, cy),
                        style = Stroke(3.5f)
                    )
                    if (charge > 0f) {
                        val arcColor = when {
                            charge >= 1f -> Color(0xFF10B981)
                            charge > 0.5f -> Color(0xFF7C3AED)
                            else -> Color(0xFFD4AF37)
                        }
                        drawArc(
                            arcColor.copy(alpha = 0.9f), -90f, charge * 360f, false,
                            Offset(cx - logoR * 1.15f, cy - logoR * 1.15f),
                            Size(logoR * 2.3f, logoR * 2.3f),
                            style = Stroke(3.5f, cap = StrokeCap.Round)
                        )
                    }

                    // Success burst
                    if (burstP > 0f) {
                        val cols = listOf(
                            Color(0xFFD4AF37), Color(0xFF9B6DFF), Color.White,
                            Color(0xFF10B981), Color(0xFF06B6D4)
                        )
                        for (i in 0..19) {
                            val a = i * 18f * PI.toFloat() / 180f
                            val dist = burstP * logoR * 4f
                            val px = cx + dist * cos(a);
                            val py = cy + dist * sin(a)
                            val pA = (1f - burstP).coerceIn(0f, 1f)
                            drawCircle(
                                cols[i % cols.size].copy(alpha = pA),
                                (5f - burstP * 4.5f).coerceAtLeast(0.4f), Offset(px, py)
                            )
                        }
                        for (r in 0..1) {
                            drawCircle(
                                Color(0xFFD4AF37).copy(alpha = (1f - burstP) * 0.5f),
                                burstP * logoR * (2.5f + r * 1.5f), Offset(cx, cy),
                                style = Stroke(2f)
                            )
                        }
                    }
                }

                // CSI Logo — centrepiece
                Image(
                    painter = painterResource(id = R.drawable.csi_logo),
                    contentDescription = "CSI VIT-AP",
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFFD4AF37), Color(0xFF8B6DFF),
                                    Color(0xFF4B3CC8), Color(0xFFD4AF37)
                                )
                            ),
                            CircleShape
                        )
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── TITLE ─────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    "CSI VIT-AP",
                    color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp, textAlign = TextAlign.Center
                )
                Text(
                    "NEW MEMBER REGISTRATION",
                    color = Color(0xFFD4AF37).copy(alpha = 0.75f), fontSize = 11.sp,
                    letterSpacing = 3.sp, fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
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
                                    success -> Color(0xFF10B981).copy(alpha = beacon)
                                    charge >= 1f -> Color(0xFF10B981).copy(alpha = beacon)
                                    charge > 0f -> Color(0xFFD4AF37).copy(alpha = beacon)
                                    else -> Color(0xFF4B3CC8).copy(alpha = beacon)
                                }
                            )
                    )
                    Text(
                        when {
                            success -> "REGISTRATION COMPLETE"
                            charge >= 1f -> "READY TO REGISTER"
                            charge > 0f -> "FILLING CREW DATA"
                            else -> "ENTER YOUR DETAILS"
                        },
                        color = when {
                            success -> Color(0xFF10B981)
                            charge >= 1f -> Color(0xFF10B981)
                            charge > 0f -> Color(0xFFD4AF37)
                            else -> Color(0xFF4B3CC8).copy(alpha = 0.65f)
                        },
                        fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── FORM ──────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .offset(y = formY.dp)
                    .alpha(formA)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF090916), Color(0xFF060510))
                            )
                        )
                        .border(
                            1.dp, Brush.linearGradient(
                                listOf(
                                    Color(0xFFD4AF37).copy(alpha = 0.35f),
                                    Color(0xFF4B3CC8).copy(alpha = 0.25f),
                                    Color(0xFFD4AF37).copy(alpha = 0.35f)
                                )
                            ), RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD4AF37).copy(alpha = 0.6f))
                            )
                            Text(
                                "CREW REGISTRATION",
                                color = Color(0xFF8C83E4).copy(alpha = 0.55f),
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
                            Text(
                                "${listOf(f1, f2, f3).count { it }}/3",
                                color = Color(0xFF4B3CC8).copy(alpha = 0.5f),
                                fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Name
                        RgField(
                            name, { name = it }, "FULL NAME", "Your full name",
                            Icons.Default.Person, Color(0xFF4B3CC8), f1,
                            KeyboardType.Text, KeyboardCapitalization.Words,
                            VisualTransformation.None
                        )

                        // Email
                        RgField(
                            email, { email = it }, "EMAIL ADDRESS", "your@email.com",
                            Icons.Default.MailOutline, Color(0xFF7C3AED), f2,
                            KeyboardType.Email, KeyboardCapitalization.None,
                            VisualTransformation.None
                        )

                        // Password
                        RgField(
                            password, { password = it }, "PASSWORD", "••••••••",
                            Icons.Default.Lock, Color(0xFF06B6D4), f3,
                            KeyboardType.Password, KeyboardCapitalization.None,
                            PasswordVisualTransformation()
                        )

                        // 3-segment progress bar
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                            listOf(
                                f1 to Color(0xFF4B3CC8),
                                f2 to Color(0xFF7C3AED),
                                f3 to Color(0xFF06B6D4)
                            ).forEach { (on, col) ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (on) col else Color(0xFF0E0E20))
                                        .border(
                                            0.5.dp,
                                            col.copy(alpha = 0.22f),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }

                        // Register button
                        val canReg = f1 && f2 && f3 && !state.isLoading
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (canReg) Brush.linearGradient(
                                        listOf(Color(0xFF1C0D55), Color(0xFF2E1475))
                                    )
                                    else Brush.linearGradient(
                                        listOf(Color(0xFF0A0A16), Color(0xFF0A0A16))
                                    )
                                )
                                .border(
                                    1.dp,
                                    if (canReg) Brush.linearGradient(
                                        listOf(
                                            Color(0xFFD4AF37).copy(alpha = 0.6f),
                                            Color(0xFF7C3AED).copy(alpha = 0.3f),
                                            Color(0xFFD4AF37).copy(alpha = 0.6f)
                                        )
                                    )
                                    else Brush.linearGradient(
                                        listOf(
                                            Color(0xFF181828), Color(0xFF181828)
                                        )
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = canReg) {
                                    viewModel.register(name, email, password)
                                }
                                .padding(vertical = 17.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = when {
                                    success -> 2
                                    state.isLoading -> 1
                                    else -> 0
                                },
                                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                                label = "rg_btn"
                            ) { s ->
                                when (s) {
                                    0 -> Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            "✦", fontSize = 16.sp,
                                            color = if (canReg) Color(0xFFD4AF37) else Color(
                                                0xFF252535
                                            )
                                        )
                                        Text(
                                            if (canReg) "CREATE ACCOUNT" else "COMPLETE ALL FIELDS",
                                            color = if (canReg) Color.White else Color(0xFF252535),
                                            fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    1 -> Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CSILoader(size = LoaderSize.SMALL)
                                        Text(
                                            "REGISTERING",
                                            color = Color(0xFF8B6DFF), fontSize = 14.sp,
                                            fontFamily = FontFamily.Monospace, letterSpacing = 2.sp
                                        )
                                    }

                                    else -> Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("✦", fontSize = 16.sp, color = Color(0xFF10B981))
                                        Text(
                                            "WELCOME TO CSI",
                                            color = Color(0xFF10B981), fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
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
                                    .background(Color(0xFF180808))
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
                                            Color(0xFFD4AF37).copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Back to log in
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clickable { if (!navController.popBackStack()) navController.navigate(Routes.LOGIN) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("‹", color = Color(0xFF8B6DFF), fontSize = 15.sp)
                                Text(
                                    "Already a member?",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.5f), fontSize = 13.sp
                                )
                                Text(
                                    "SIGN IN",
                                    color = Color(0xFF8B6DFF), fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // HUD brackets
                Canvas(Modifier.matchParentSize()) {
                    val s = 16f;
                    val w = 1.4f
                    val c = Color(0xFFD4AF37).copy(alpha = 0.3f)
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

@Composable
private fun RgField(
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
                label,
                color = if (active) accent.copy(alpha = 0.75f) else Color(0xFF252535),
                fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp
            )
            Box(
                Modifier
                    .width(10.dp)
                    .height(1.dp)
                    .background(if (active) accent.copy(alpha = 0.3f) else Color(0xFF1A1A28))
            )
            Text(
                if (active) "● SET" else "○ EMPTY",
                color = if (active) accent.copy(alpha = 0.5f) else Color(0xFF252535),
                fontSize = 8.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF252535), fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    icon, null, tint = if (active) accent else Color(0xFF252535),
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