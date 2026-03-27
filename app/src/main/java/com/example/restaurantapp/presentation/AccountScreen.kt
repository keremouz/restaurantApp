package com.example.restaurantapp.presentation.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    innerPadding: PaddingValues,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hesap") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Henüz giriş yapmadınız")

            Text(
                text = "Devam etmek için giriş yapabilir veya üye olabilirsiniz.",
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text("Giriş Yap")
            }

            OutlinedButton(onClick = onNavigateToRegister) {
                Text("Üye Ol")
            }
        }
    }
}