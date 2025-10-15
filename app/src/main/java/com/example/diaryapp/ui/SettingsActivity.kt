package com.example.diaryapp.ui

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.widget.SwitchCompat
import com.example.diaryapp.BaseActivity
import com.example.diaryapp.R
import com.example.diaryapp.prefs.Settings

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val settings = Settings(this)
        val themeSwitch = findViewById<SwitchCompat>(R.id.switchTheme)
        val seek = findViewById<SeekBar>(R.id.seekFont)

        themeSwitch.isChecked = settings.isDark
        seek.max = 60
        seek.progress = ((settings.fontScale - 0.8f) * 100).toInt()

        themeSwitch.setOnCheckedChangeListener { _, checked -> 
            settings.isDark = checked
            // Theme change will automatically recreate the activity
        }
        
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) {
                    settings.fontScale = 0.8f + (p / 100f)
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {
                // Only recreate when user stops dragging
                recreate()
            }
        })
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
