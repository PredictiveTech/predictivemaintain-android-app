package pe.edu.upc.predictivemaintain.app.iam.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.iam.presentation.state.LoginUiEvent
import pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUiEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    val navy = Color(0xFF0F172A)
    val accent = Color(0xFF0EA5E9)
    val textPrimary = Color(0xFFF8FAFC)
    val textSecondary = Color(0xFF94A3B8)
    val fieldBg = Color(0xFF1E293B)
    val fieldBorder = Color(0xFF334155)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = fieldBg,
        unfocusedContainerColor = fieldBg,
        errorContainerColor = fieldBg,
        focusedBorderColor = accent,
        unfocusedBorderColor = fieldBorder,
        focusedTextColor = textPrimary,
        unfocusedTextColor = textPrimary,
        errorTextColor = textPrimary,
        focusedLabelColor = accent,
        unfocusedLabelColor = textSecondary,
        cursorColor = accent
    )
    val fieldShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(navy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(22.dp))
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.app_name),
            color = textPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.login_subtitle),
            color = textSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text(text = stringResource(R.string.login_email_hint)) },
            isError = uiState.fieldErrors.containsKey("email"),
            singleLine = true,
            shape = fieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.fieldErrors.containsKey("email")) {
            Text(
                text = uiState.fieldErrors["email"]!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text(text = stringResource(R.string.login_password_hint)) },
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.fieldErrors.containsKey("password"),
            singleLine = true,
            shape = fieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.fieldErrors.containsKey("password")) {
            Text(
                text = uiState.fieldErrors["password"]!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 4.dp)
            )
        }

        if (uiState.generalError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            val errorText = when (uiState.generalError) {
                "NETWORK" -> stringResource(R.string.error_network)
                "INVALID_RESPONSE" -> stringResource(R.string.error_invalid_response)
                "UNKNOWN" -> stringResource(R.string.error_unknown)
                else -> uiState.generalError!!
            }
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.login() },
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = textPrimary,
                disabledContainerColor = accent.copy(alpha = 0.6f),
                disabledContentColor = textPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = textPrimary
                )
            } else {
                Text(text = stringResource(R.string.login_button))
            }
        }
    }
}
