package com.example.jolt.screens

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.jolt.R
import com.example.jolt.AccidentDetectionService
import kotlinx.coroutines.delay

@Composable
fun MainScreen(showDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val emergencyNumber = sharedPreferences.getString("emergency_number", null)
    val showEmergencyNumberDialog = remember { mutableStateOf(emergencyNumber == null) }

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.alarm_sound).apply { isLooping = true }
    }
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    // Start the AccidentDetectionService
    LaunchedEffect(Unit) {
        val intent = Intent(context, AccidentDetectionService::class.java)
        context.startService(intent)
    }

// Emergency number dialog
    if (showEmergencyNumberDialog.value) {
        val numberInput = remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { /* Non-dismissible */ },
            title = { Text("Enter Emergency Number") },
            text = {
                Column {
                    Text("Please enter a valid 10-digit Indian mobile number:")
                    TextField(
                        value = numberInput.value,
                        onValueChange = { newValue ->
                            // Allow only numeric input and limit to 10 characters
                            numberInput.value = newValue
                                .filter { it.isDigit() }
                                .take(10)
                        },
                        label = { Text("Emergency Number") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        placeholder = { Text("e.g., 8638144632") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isValidIndianPhoneNumber(numberInput.value)) {
                            sharedPreferences.edit()
                                .putString("emergency_number", numberInput.value)
                                .apply()
                            showEmergencyNumberDialog.value = false
                        } else {
                            Toast.makeText(context, "Please enter a valid Indian mobile number", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        )
    }

    // Accident detection dialog
    if (showDialog.value) {
        val countdown = remember { mutableStateOf(15) }
        LaunchedEffect(showDialog.value) {
            if (showDialog.value) {
                mediaPlayer.start()
                try {
                    for (i in 15 downTo 0) {
                        countdown.value = i
                        val volume = 0.1f + 0.9f * (1.0f - i / 15.0f)
                        mediaPlayer.setVolume(volume, volume)
                        delay(1000L)
                        if (i == 0) {
                            mediaPlayer.stop()
                            callEmergencyNumber(context)
                        }
                    }
                } finally {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.stop()
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Accident Detected") },
            text = { Text("Calling emergency in ${countdown.value} seconds") },
            confirmButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Main content
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (showDialog.value) "Accident Happened" else "Monitoring...",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = if (showDialog.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun isValidIndianPhoneNumber(number: String): Boolean {
    // Remove any spaces or dashes
    val cleanNumber = number.replace(Regex("[\\s-]"), "")

    // Check if the number starts with valid Indian mobile prefixes and is 10 digits
    return cleanNumber.matches(Regex("^[6-9]\\d{9}$"))
}

private fun callEmergencyNumber(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val emergencyNumber = sharedPreferences.getString("emergency_number", null)

    if (emergencyNumber != null) {
        try {
            // Use ACTION_DIAL instead of ACTION_CALL to avoid runtime permission
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$emergencyNumber"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle any potential exceptions
            Toast.makeText(context, "Unable to make emergency call", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Emergency number not set", Toast.LENGTH_SHORT).show()
    }
}