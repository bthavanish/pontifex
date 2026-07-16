package com.pontifex.app.presentation.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding)
        ) { page ->
            when (page) {
                0 -> WelcomePage(
                    onNext = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
                1 -> ContainerSetupPage(
                    onNext = {
                        scope.launch { pagerState.animateScrollToPage(2) }
                    }
                )
                2 -> BinarySetupPage(
                    onFinished = {
                        viewModel.completeOnboarding()
                        onFinished()
                    }
                )
            }
        }
    }
}

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "Pontifex",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Portable ADB/Fastboot Terminal",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Text(
            text = "Turn your Android device into a portable ADB workstation with a Termux-style terminal experience.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = onNext) {
            Text("Get Started")
        }
    }
}

@Composable
private fun ContainerSetupPage(onNext: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "Container Setup",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Pontifex uses a virtual container to keep your ADB and fastboot operations isolated. Choose a location on your device to store the container.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = onNext) {
            Text("Choose Directory")
        }
        TextButton(onClick = onNext) {
            Text("Skip for now")
        }
    }
}

@Composable
private fun BinarySetupPage(onFinished: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "Binary Setup",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "ADB and fastboot binaries are bundled with the app. They will be extracted and verified on first launch.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = onFinished) {
            Text("Complete Setup")
        }
    }
}
