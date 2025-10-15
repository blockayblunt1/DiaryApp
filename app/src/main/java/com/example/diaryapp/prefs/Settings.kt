package com.example.diaryapp.prefs

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.diaryapp.model.SortOption

class Settings(ctx: Context) {
    private val sp = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var isDark: Boolean
        get() = sp.getBoolean("isDark", false)
        set(value) {
            sp.edit().putBoolean("isDark", value).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (value) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

    /** 0.8f .. 1.4f typical range */
    var fontScale: Float
        get() = sp.getFloat("fontScale", 1.0f)
        set(value) { sp.edit().putFloat("fontScale", value).apply() }
        
    var sortOption: SortOption
        get() = SortOption.fromOrdinal(sp.getInt("sortOption", 0))
        set(value) { sp.edit().putInt("sortOption", value.ordinal).apply() }
}
