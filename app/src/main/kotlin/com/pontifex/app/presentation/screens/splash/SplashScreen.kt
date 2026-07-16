package com.pontifex.app.presentation.screens.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pontifex.app.R

@Composable
fun SplashScreen(
    onReady: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isReady by viewModel.isReady.collectAsState()
    val shouldNavigateToOnboarding by viewModel.shouldNavigateToOnboarding.collectAsState()

    var logoScale by remember { mutableFloatStateOf(0f) }
    val scale by animateFloatAsState(
        targetValue = logoScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logoScale"
    )

    LaunchedEffect(Unit) {
        logoScale = 1f
    }

    LaunchedEffect(isReady, shouldNavigateToOnboarding) {
        if (shouldNavigateToOnboarding) {
            onNavigateToOnboarding()
        } else if (isReady) {
            onReady()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Pontifex",
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
            tint = androidx.compose.ui.graphics.Color.Unspecified
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pontifex",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Portable ADB/Fastboot Terminal",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (val currentState = state) {
            is SplashState.Checking -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxSize(0.6f))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Checking setup...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is SplashState.Extracting -> {
                LinearProgressIndicator(
                    progress = { currentState.progress },
                    modifier = Modifier.fillMaxSize(0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentState.step,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is SplashState.Verifying -> {
                LinearProgressIndicator(
                    progress = { currentState.progress },
                    modifier = Modifier.fillMaxSize(0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Verifying integrity...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is SplashState.Error -> {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Setup Error") },
                    text = { Text(currentState.message) },
                    confirmButton = {
                        if (currentState.retryable) {
                            TextButton(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                )
            }
            is SplashState.Ready -> { }
        }
    }
}
