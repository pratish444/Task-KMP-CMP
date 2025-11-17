package com.example.androidinterntask.screens

import androidx.compose.runtime.Composable

@Composable
expect fun TextReadingTaskScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
)

@Composable
expect fun ImageDescriptionTaskScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
)

@Composable
expect fun PhotoCaptureTaskScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
)