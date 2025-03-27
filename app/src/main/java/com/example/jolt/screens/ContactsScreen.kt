package com.example.jolt.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.jolt.ui.theme.AppTheme

@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val contactsString = sharedPreferences.getString("emergency_contacts", "") ?: ""
    val contactsList = remember { mutableStateListOf<String>().apply { addAll(contactsString.split(", ").filter { it.isNotEmpty() }) } }
    val showAddDialog = remember { mutableStateOf(false) }
    val editContact = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emergency Contacts",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = AppTheme.dimens.paddingLarge)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(contactsList) { contact ->
                ContactItem(
                    contact = contact,
                    onEdit = { oldContact ->
                        editContact.value = oldContact
                        showAddDialog.value = true
                    },
                    onDelete = { contactToDelete ->
                        contactsList.remove(contactToDelete)
                        sharedPreferences.edit()
                            .putString("emergency_contacts", contactsList.joinToString(", "))
                            .apply()
                    }
                )
            }
        }

        Button(
            onClick = {
                editContact.value = null
                showAddDialog.value = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.paddingLarge)
        ) {
            Text("Add New Contact")
        }
    }

    if (showAddDialog.value) {
        val nameInput = remember { mutableStateOf(editContact.value?.split(": ")?.get(0) ?: "") }
        val numberInput = remember { mutableStateOf(editContact.value?.split(": ")?.get(1) ?: "") }
        AlertDialog(
            onDismissRequest = { showAddDialog.value = false },
            title = { Text(if (editContact.value != null) "Edit Emergency Contact" else "Add Emergency Contact") },
            text = {
                Column {
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
                        if (nameInput.value.isBlank()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        } else if (!isValidIndianPhoneNumber(numberInput.value)) {
                            Toast.makeText(context, "Please enter a valid Indian mobile number", Toast.LENGTH_SHORT).show()
                        } else {
                            val newContact = "${nameInput.value}: ${numberInput.value}"
                            if (editContact.value != null) {
                                contactsList.remove(editContact.value)
                            }
                            if (contactsList.contains(newContact) && editContact.value != newContact) {
                                Toast.makeText(context, "Contact already exists", Toast.LENGTH_SHORT).show()
                            } else {
                                contactsList.add(newContact)
                                sharedPreferences.edit()
                                    .putString("emergency_contacts", contactsList.joinToString(", "))
                                    .apply()
                                showAddDialog.value = false
                                editContact.value = null
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog.value = false
                    editContact.value = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ContactItem(
    contact: String,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.dimens.paddingSmall),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = contact.split(": ")[0],
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "(${contact.split(": ")[1].substring(0, 3)}) XXX-XXXX",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                TextButton(onClick = { onEdit(contact) }) {
                    Text("Edit", color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = { onDelete(contact) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun isValidIndianPhoneNumber(number: String): Boolean {
    val cleanNumber = number.replace(Regex("[\\s-]"), "")
    return cleanNumber.matches(Regex("^[6-9]\\d{9}$"))
}