package com.nexussync.datausage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexussync.datausage.ui.navigation.DataSyncAppRoot
import com.nexussync.datausage.ui.theme.DataSyncTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as DataSyncApplication).container

        setContent {
            val prefs by container.preferencesRepository.preferencesFlow.collectAsStateWithLifecycle()
            DataSyncTheme(themeMode = prefs.themeMode) {
                DataSyncAppRoot(container = container)
            }
        }
    }
}
