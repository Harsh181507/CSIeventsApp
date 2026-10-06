package com.example.csievent.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { DARK, LIGHT, SYSTEM }

/**
 * The user's appearance choice. Stored in SharedPreferences (separate from the
 * login data, so logging out keeps it) and read synchronously so the first
 * frame already uses the right theme.
 */
@Singleton
class ThemeSettings @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(read())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY, mode.name).apply()
        _mode.value = mode
    }

    private fun read(): ThemeMode =
        prefs.getString(KEY, null)
            ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
            ?: ThemeMode.DARK

    private companion object {
        const val KEY = "theme_mode"
    }
}
