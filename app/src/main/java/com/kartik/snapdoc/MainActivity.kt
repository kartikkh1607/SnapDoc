package com.kartik.snapdoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.kartik.snapdoc.ui.navigation.SnapDocNavGraph
import com.kartik.snapdoc.ui.theme.SnapDocTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() must be called before super.onCreate so the
        // platform splash theme (Theme.SnapDoc.Splash) transitions cleanly
        // into the post-splash theme (Theme.SnapDoc) when the activity is ready.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnapDocTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    SnapDocNavGraph(navController = navController)
                }
            }
        }
    }
}
