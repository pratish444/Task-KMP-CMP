package com.example.androidinterntask

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.androidinterntask.screens.*

@Composable
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf("start") }

        when (currentScreen) {
            "start" -> {
                StartScreen(onStartClick = {
                    currentScreen = "noise_test"
                })
            }

            "noise_test" -> {
                NoiseTestScreen(
                    onTestPassed = {
                        currentScreen = "task_selection"
                    },
                    onBack = {
                        currentScreen = "start"
                    }
                )
            }

            "task_selection" -> {
                TaskSelectionScreen(
                    onTaskSelected = { taskType ->
                        currentScreen = taskType
                    },
                    onViewHistory = {
                        currentScreen = "history"
                    }
                )
            }

            "text_reading" -> {
                TextReadingTaskScreen(
                    onComplete = {
                        currentScreen = "task_selection"
                    },
                    onBack = {
                        currentScreen = "task_selection"
                    }
                )
            }

            "image_description" -> {
                ImageDescriptionTaskScreen(
                    onComplete = {
                        currentScreen = "task_selection"
                    },
                    onBack = {
                        currentScreen = "task_selection"
                    }
                )
            }

            "photo_capture" -> {
                PhotoCaptureTaskScreen(
                    onComplete = {
                        currentScreen = "task_selection"
                    },
                    onBack = {
                        currentScreen = "task_selection"
                    }
                )
            }

            "history" -> {
                TaskHistoryScreen(
                    onBack = {
                        currentScreen = "task_selection"
                    }
                )
            }
        }
    }
}