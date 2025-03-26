package com.example.jolt

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jolt.screens.LoginScreen
import com.example.jolt.screens.MainScreen
import com.example.jolt.screens.SignupScreen
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val showDialog = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        if (intent?.getBooleanExtra("accident_detected", false) == true) {
            showDialog.value = true
        }

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginScreen(navController) }
                composable("signup") { SignupScreen(navController) }
                composable("main") { MainScreen(showDialog) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("accident_detected", false)) {
            showDialog.value = true
        }
    }
}