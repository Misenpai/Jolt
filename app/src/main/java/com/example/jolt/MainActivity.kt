package com.example.jolt

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jolt.screens.ContactsScreen
import com.example.jolt.screens.LoginScreen
import com.example.jolt.screens.MainScreen
import com.example.jolt.screens.SignupScreen
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val showDialog = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        if (intent?.getBooleanExtra("accident_detected", false) == true) {
            showDialog.value = true
        }

        setContent {
            val navController = rememberNavController()
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Text(
                            "Jolt Menu",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Divider()
                        NavigationDrawerItem(
                            label = { Text("Home Screen") },
                            selected = navController.currentDestination?.route == "main",
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("main") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Contacts") },
                            selected = navController.currentDestination?.route == "contacts",
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("contacts") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Jolt") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        )
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("login") { LoginScreen(navController) }
                        composable("signup") { SignupScreen(navController) }
                        composable("main") { MainScreen(showDialog) }
                        composable("contacts") { ContactsScreen() }
                    }
                }
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