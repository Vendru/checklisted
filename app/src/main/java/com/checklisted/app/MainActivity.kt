package com.checklisted.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.checklisted.app.ui.gallery.ComponentGalleryScreen
import com.checklisted.app.ui.theme.NeoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NeoTheme {
                // Phase 1 entry point: the design system gallery. Replaced by the
                // navigation graph once the Today screen lands in phase 3.
                ComponentGalleryScreen()
            }
        }
    }
}
