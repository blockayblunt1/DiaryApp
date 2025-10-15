package com.example.diaryapp

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.example.diaryapp.prefs.Settings

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val s = Settings(newBase)
        val cfg = Configuration(newBase.resources.configuration)
        cfg.fontScale = s.fontScale
        val ctx = newBase.createConfigurationContext(cfg)
        super.attachBaseContext(ctx)
    }
}
