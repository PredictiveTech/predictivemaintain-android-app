package pe.edu.upc.predictivemaintain.app.iam.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.iam.presentation.state.SplashUiState
import pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is SplashUiState.Loading -> {
            SplashLoadingContent()
        }
        is SplashUiState.NavigateToHome -> {
            onNavigateToHome()
        }
        is SplashUiState.NavigateToLogin -> {
            onNavigateToLogin()
        }
        is SplashUiState.Error -> {
            SplashErrorContent(
                error = state.error,
                onRetry = { viewModel.checkSession() },
                onLoginAgain = { viewModel.logoutAndNavigateToLogin() }
            )
        }
    }
}

@Composable
fun SplashLoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.splash_checking), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SplashErrorContent(
    error: AppError,
    onRetry: () -> Unit,
    onLoginAgain: () -> Unit
) {
    val errorMessage = when (error) {
        is AppError.Api -> error.detail
        is AppError.Network -> stringResource(R.string.error_network)
        is AppError.InvalidResponse -> stringResource(R.string.error_invalid_response)
        is AppError.SessionExpired -> stringResource(R.string.error_invalid_response)
        is AppError.Unknown -> stringResource(R.string.error_unknown)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.splash_retry))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onLoginAgain) {
            Text(text = stringResource(R.string.splash_login_again))
        }
    }
}
