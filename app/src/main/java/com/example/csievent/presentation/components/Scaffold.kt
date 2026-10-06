package com.example.csievent.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.csievent.R
import com.example.csievent.ui.theme.ControlShape
import com.example.csievent.ui.theme.CsiTheme

/**
 * Standard screen: themed background, optional top bar and bottom bar,
 * snackbar messages, and edge-to-edge insets handled.
 */
@Composable
fun CsiScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val c = CsiTheme.colors
    Scaffold(
        modifier = modifier,
        containerColor = c.background,
        contentColor = c.text,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = {
            if (snackbarHostState != null) {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = ControlShape,
                        containerColor = c.text,
                        contentColor = c.background,
                        actionColor = c.highlight
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = content
    )
}

/**
 * Simple top bar: optional back button, title + subtitle, trailing actions.
 * Draws below the status bar.
 */
@Composable
fun CsiTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val c = CsiTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.background)
            .statusBarsPadding()
            .padding(start = if (onBack != null) 4.dp else 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = c.text)
            }
            Spacer(Modifier.width(4.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = c.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        actions()
    }
}

/**
 * Header for the three home screens: logo + app name, optional actions and
 * the profile (initials) button.
 */
@Composable
fun BrandTopBar(
    userName: String,
    onProfile: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val c = CsiTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.csi_logo),
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "CSI Events",
            style = MaterialTheme.typography.titleLarge,
            color = c.text,
            modifier = Modifier.weight(1f)
        )
        actions()
        AvatarButton(name = userName, onClick = onProfile)
    }
}

/**
 * Pull-to-refresh wrapper. [refreshing] is the screen's loading flag; the
 * spinner shows only for user-started refreshes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableBox(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val c = CsiTheme.colors
    val state = rememberPullToRefreshState()

    if (state.isRefreshing) {
        LaunchedEffect(Unit) { onRefresh() }
    }
    LaunchedEffect(refreshing) {
        if (!refreshing) state.endRefresh()
    }

    Box(modifier.nestedScroll(state.nestedScrollConnection)) {
        content()
        PullToRefreshContainer(
            state = state,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = c.raised,
            contentColor = c.highlight
        )
    }
}

/** Bottom-of-list spacer that clears the navigation bar. */
@Composable
fun BottomSpacer(extra: Int = 24) {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(extra.dp)
    )
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
}
