package com.example.jolt.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.jolt.R
import com.example.jolt.AccidentDetectionService
import com.example.jolt.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun MainScreen(showDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val contactsString = sharedPreferences.getString("emergency_contacts", "") ?: ""
    val contactsList = contactsString.split(", ").filter { it.isNotEmpty() }
    val showEmergencyNumberDialog = remember { mutableStateOf(contactsString.isEmpty()) }
    val isMonitoring = remember { mutableStateOf(false) }

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.alarm_sound).apply { isLooping = true }
    }
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    // Permission launcher for SEND_SMS
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sendEmergencySMS(context) // Proceed with SMS sending if granted
            showDialog.value = false
        } else {
            Toast.makeText(context, "SMS permission denied. Cannot send emergency message.", Toast.LENGTH_LONG).show()
        }
    }

    // Check and request SMS permission
    val hasSmsPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(isMonitoring.value) {
        val intent = Intent(context, AccidentDetectionService::class.java)
        if (isMonitoring.value) {
            context.startService(intent)
        } else {
            context.stopService(intent)
        }
    }

    if (showEmergencyNumberDialog.value) {
        // Existing emergency number dialog code (unchanged)
        val nameInput = remember { mutableStateOf("") }
        val numberInput = remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { /* Non-dismissible */ },
            title = { Text("Enter Emergency Contact") },
            text = {
                Column {
                    Text("Please enter the contact's name and a valid 10-digit Indian mobile number:")
                    TextField(
                        value = nameInput.value,
                        onValueChange = { nameInput.value = it },
                        label = { Text("Contact Name") },
                        placeholder = { Text("e.g., Sanya Kalra") },
                        singleLine = true,
                        modifier = Modifier.padding(bottom = AppTheme.dimens.paddingSmall)
                    )
                    TextField(
                        value = numberInput.value,
                        onValueChange = { newValue ->
                            numberInput.value = newValue.filter { it.isDigit() }.take(10)
                        },
                        label = { Text("Emergency Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g., 8638144632") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.value.isBlank()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        } else if (!isValidIndianPhoneNumber(numberInput.value)) {
                            Toast.makeText(context, "Please enter a valid Indian mobile number", Toast.LENGTH_SHORT).show()
                        } else {
                            val contact = "${nameInput.value}: ${numberInput.value}"
                            val existingContacts = sharedPreferences.getString("emergency_contacts", "") ?: ""
                            val updatedContacts = if (existingContacts.isEmpty()) contact else "$existingContacts, $contact"
                            sharedPreferences.edit().putString("emergency_contacts", updatedContacts).apply()
                            showEmergencyNumberDialog.value = false
                        }
                    }
                ) { Text("Save") }
            }
        )
    }

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
                            if (hasSmsPermission.value) {
                                sendEmergencySMS(context)
                            } else {
                                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                            }
                        }
                    }
                } finally {
                    if (mediaPlayer.isPlaying) mediaPlayer.stop()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Accident Detected") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Emergency Contacts")
                    contactsList.forEach { contact ->
                        Text(text = contact.split(": ")[0], style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "(${contact.split(": ")[1].substring(0, 3)}) XXX-XXXX",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Message to send:")
                    Text("Emergency message", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("seconds remaining: ${countdown.value}")
                }
            },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("Cancel Text")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        if (hasSmsPermission.value) {
                            sendEmergencySMS(context)
                            showDialog.value = false
                        } else {
                            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                        }
                    }
                ) { Text("Send Text") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.dimens.paddingLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emergency Contacts",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = AppTheme.dimens.paddingNormal)
        )
        contactsList.forEach { contact ->
            Text(text = contact.split(": ")[0], style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "(${contact.split(": ")[1].substring(0, 3)}) XXX-XXXX",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = AppTheme.dimens.paddingSmall)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = if (showDialog.value) "Accident Happened" else if (isMonitoring.value) "Monitoring..." else "Not Monitoring",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = if (showDialog.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { isMonitoring.value = true }, enabled = !isMonitoring.value) {
                Text("Start Monitoring")
            }
            Button(onClick = { isMonitoring.value = false }, enabled = isMonitoring.value) {
                Text("Stop Monitoring")
            }
        }
    }
}

private fun isValidIndianPhoneNumber(number: String): Boolean {
    val cleanNumber = number.replace(Regex("[\\s-]"), "")
    return cleanNumber.matches(Regex("^[6-9]\\d{9}$"))
}

private fun sendEmergencySMS(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val contactsString = sharedPreferences.getString("emergency_contacts", "") ?: ""
    val contactsList = contactsString.split(", ").filter { it.isNotEmpty() }

    if (contactsList.isEmpty()) {
        Toast.makeText(context, "No emergency contacts set", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val smsManager = SmsManager.getDefault()
        val message = "Emergency message: I may have been in an accident. Please help!"
        contactsList.forEach { contact ->
            val number = contact.split(": ")[1]
            smsManager.sendTextMessage(number, null, message, null, null)
        }
        Toast.makeText(context, "Emergency SMS sent", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("SMS_ERROR", "Failed to send SMS: ${e.message}")
    }
}