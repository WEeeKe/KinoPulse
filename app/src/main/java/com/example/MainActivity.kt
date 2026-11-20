package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.KinoPulseAppRoot
import com.example.ui.KinoPulseViewModel
import com.example.ui.theme.KinoPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as KinoPulseApp
        val factory = KinoPulseViewModel.Factory(app.repository, applicationContext)

        setContent {
            val viewModel: KinoPulseViewModel = viewModel(factory = factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            KinoPulseTheme(
                themeMode = uiState.preferences.themeMode,
                amoledNightMode = uiState.preferences.amoledNightMode
            ) {
                KinoPulseAppRoot(viewModel = viewModel)
            }
        }
    }
}
