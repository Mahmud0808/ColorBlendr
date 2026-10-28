package com.drdisagree.colorblendr.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.drdisagree.colorblendr.utils.wallpaper.WallpaperShortcuts
import com.drdisagree.colorblendr.utils.wallpaper.WallpaperShortcuts.ACTION_APPLY_WALLPAPER_COLOR
import com.drdisagree.colorblendr.utils.wallpaper.WallpaperShortcuts.EXTRA_SEED_COLOR

class ShortcutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val applied = intent
            ?.takeIf { it.action == ACTION_APPLY_WALLPAPER_COLOR && it.hasExtra(EXTRA_SEED_COLOR) }
            ?.let { WallpaperShortcuts.applySeed(it.getIntExtra(EXTRA_SEED_COLOR, 0)) } == true

        if (!applied) {
            startActivity(
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        finish()
    }
}
