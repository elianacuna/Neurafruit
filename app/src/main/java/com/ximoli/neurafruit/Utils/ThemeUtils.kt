package com.ximoli.neurafruit.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {

    const val PREFS_NAME = "theme_prefs"
    const val PREF_DARK_MODE = "isDarkMode"
    const val PREF_SYSTEM_MODE = "isSystemMode"

    fun applyTheme(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(PREF_DARK_MODE, false)
        val isSystemMode = sharedPreferences.getBoolean(PREF_SYSTEM_MODE, false)

        setAppTheme(isDarkMode, isSystemMode)
    }

    fun setAppTheme(isDarkMode: Boolean, isSystemMode: Boolean) {
        when {
            isSystemMode -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            isDarkMode -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    fun saveThemePreference(context: Context, isDarkMode: Boolean, isSystemMode: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(PREF_DARK_MODE, isDarkMode)
            putBoolean(PREF_SYSTEM_MODE, isSystemMode)
            apply()
        }
    }
}
