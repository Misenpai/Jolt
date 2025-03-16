package com.example.jolt

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val showDialog = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the AccidentDetectionService
        startService(Intent(this, AccidentDetectionService::class.java))

        // Check if launched due to an accident
        if (intent?.getBooleanExtra("accident_detected", false) == true) {
            showDialog.value = true
        }

        setContent {
            val detectionState = remember { mutableStateOf("Monitoring...") }
            AccidentDetectionScreen(
                detectionText = detectionState.value,
                showDialog = showDialog.value,
                onDismiss = { showDialog.value = false }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle case where activity is already running and a new intent arrives
        if (intent.getBooleanExtra("accident_detected", false)) {
            showDialog.value = true
        }
    }
}

@Composable
fun AccidentDetectionScreen(
    detectionText: String,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = detectionText,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = if (detectionText == "Accident Happened") {
                MaterialTheme.colors.error
            } else {
                MaterialTheme.colors.onSurface
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Accident Detected") },
            text = { Text("Click to dismiss.") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        )
    }
}