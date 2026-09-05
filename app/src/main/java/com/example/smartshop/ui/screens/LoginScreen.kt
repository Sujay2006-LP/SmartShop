package com.example.smartshop.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.smartshop.ui.components.LogoView
import com.example.smartshop.ui.theme.IndigoPrimary
import com.example.smartshop.ui.theme.LightGraySurface
import com.example.smartshop.ui.theme.PureWhite
import com.example.smartshop.ui.theme.DeepBlack
import com.example.smartshop.ui.theme.MutedZinc

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onSignUpSuccess: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Centered Logo
        LogoView(size = 140.dp)

        Spacer(modifier = Modifier.height(24.dp))

        // 2. App Branding
        Text(
            text = "SMART SHOP",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = DeepBlack,
            letterSpacing = 4.sp
        )
        Text(
            text = "FOR FASTER SHOPPING",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MutedZinc,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 3. Auth Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightGraySurface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (isSignUp) "Create Account" else "Welcome Back",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = DeepBlack
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = MutedZinc) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = IndigoPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = DeepBlack,
                        unfocusedTextColor = DeepBlack
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = MutedZinc) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = DeepBlack,
                        unfocusedTextColor = DeepBlack
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { 
                        if (isSignUp) onSignUpSuccess(email, password) 
                        else onLoginSuccess(email, password) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary, contentColor = PureWhite)
                ) {
                    Text(
                        text = if (isSignUp) "SIGN UP" else "LOGIN",
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Toggle Auth Mode
        TextButton(onClick = { isSignUp = !isSignUp }) {
            Text(
                text = if (isSignUp) "Already a member? Login" else "New user? Create an account",
                color = IndigoPrimary
            )
        }
    }
}
