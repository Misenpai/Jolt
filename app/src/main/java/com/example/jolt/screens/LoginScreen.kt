package com.example.jolt.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jolt.R
import com.example.jolt.ui.common.*
import com.example.jolt.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf("") }
    val passwordError = remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Centers content vertically
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.paddingLarge)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = AppTheme.dimens.paddingLarge)
                    .padding(bottom = AppTheme.dimens.paddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MediumTitleText(
                    modifier = Modifier
                        .padding(top = AppTheme.dimens.paddingLarge)
                        .fillMaxWidth(),
                    text = "Jolt App",
                    textAlign = TextAlign.Center
                )
                Image(
                    painter = painterResource(id = R.drawable.disaster),
                    contentDescription = "Disaster Image",
                    modifier = Modifier
                        .size(128.dp)
                        .padding(top = AppTheme.dimens.paddingSmall)
                )
                TitleText(
                    modifier = Modifier.padding(top = AppTheme.dimens.paddingLarge),
                    text = "LOGIN"
                )
                EmailTextField(
                    modifier = Modifier
                        .padding(top = AppTheme.dimens.paddingLarge),
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = "Email",
                    isError = emailError.value.isNotEmpty(),
                    errorText = emailError.value,
                    imeAction = ImeAction.Next
                )
                PasswordTextField(
                    modifier = Modifier
                        .padding(top = AppTheme.dimens.paddingLarge),
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = "Password",
                    isError = passwordError.value.isNotEmpty(),
                    errorText = passwordError.value,
                    imeAction = ImeAction.Done
                )
                Text(
                    modifier = Modifier
                        .padding(top = AppTheme.dimens.paddingSmall)
                        .align(Alignment.End)
                        .clickable { /* TODO: Implement forgot password */ },
                    text = "Forgot Password?",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                NormalButton(
                    modifier = Modifier.padding(top = AppTheme.dimens.paddingLarge),
                    text = "Login",
                    onClick = {
                        if (email.value.isEmpty()) {
                            emailError.value = "Email cannot be empty"
                        } else {
                            emailError.value = ""
                        }
                        if (password.value.isEmpty()) {
                            passwordError.value = "Password cannot be empty"
                        } else {
                            passwordError.value = ""
                        }
                        if (emailError.value.isEmpty() && passwordError.value.isEmpty()) {
                            auth.signInWithEmailAndPassword(email.value, password.value)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        navController.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                )
            }
        }
        Row(
            modifier = Modifier.padding(AppTheme.dimens.paddingNormal),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't have an account?")
            Text(
                modifier = Modifier
                    .padding(start = AppTheme.dimens.paddingExtraSmall)
                    .clickable { navController.navigate("signup") },
                text = "Signup",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}