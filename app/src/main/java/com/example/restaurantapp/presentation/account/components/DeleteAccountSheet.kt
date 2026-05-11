package com.example.restaurantapp.presentation.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountSheet(
    sheetState: SheetState,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissClick,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiConstants.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(UiConstants.ContentSpacing)
        ) {
            Text(
                text = stringResource(R.string.delete_account),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(R.string.delete_account_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onConfirmClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF64B5F6)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(UiConstants.LoginButtonHeight),
                shape = RoundedCornerShape(UiConstants.ButtonRadius)
            ) {
                Text(
                    text = stringResource(R.string.delete_account)
                )
            }

            OutlinedButton(
                onClick = onDismissClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(UiConstants.ButtonRadius)
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        }
    }
}