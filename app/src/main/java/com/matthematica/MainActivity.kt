package com.matthematica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.matthematica.ui.auth.AuthViewModel
import com.matthematica.ui.auth.LoginScreen
import com.matthematica.ui.calculator.CalculatorScreen
import com.matthematica.ui.calculator.CalculatorViewModel
import com.matthematica.ui.theme.MattematicaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val calculatorViewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MattematicaTheme {
                val currentUserId by authViewModel.currentUserId.collectAsState(initial = null)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (currentUserId != null) {
                        CalculatorScreen(viewModel = calculatorViewModel)
                    } else {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                // Navigate to calculator
                            }
                        )
                    }
                }
            }
        }
    }
}

