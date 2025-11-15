package com.example.androidinterntask.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidinterntask.data.TaskRepository
import com.example.androidinterntask.models.Task
import com.example.androidinterntask.models.TaskType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureTaskScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var hasPhoto by remember { mutableStateOf(false) }
    var photoPath by remember { mutableStateOf("") }
    var textDescription by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var hasRecorded by remember { mutableStateOf(false) }
    var recordedDuration by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCameraOpen by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording && recordingDuration <= 20) {
                delay(1000)
                recordingDuration++
            }
            if (recordingDuration > 20) {
                isRecording = false
                errorMessage = "Recording too long (max 20 s)."
                recordingDuration = 0
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Capture Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!hasPhoto) {
                Text(
                    text = "Take a photo to describe",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "This task requires camera permission to capture photos.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCameraOpen) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "📷 Camera Opening...",
                                    modifier = Modifier.padding(top = 16.dp),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Simulating camera access",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Camera Preview",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                                Text(
                                    text = "In a real app, this would show the camera viewfinder",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        isCameraOpen = true
                        scope.launch {
                            delay(2000) // Simulate camera opening time
                            hasPhoto = true
                            photoPath = "photo_${Clock.System.now().toEpochMilliseconds()}.jpg"
                            isCameraOpen = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isCameraOpen
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (isCameraOpen) "Opening Camera..." else "Capture Image",
                        fontSize = 18.sp
                    )
                }
            } else {
                Text(
                    text = "Photo captured successfully! ✅",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "📸 Photo Preview",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "File: $photoPath",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "Timestamp: ${Clock.System.now()}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = textDescription,
                    onValueChange = { textDescription = it },
                    label = { Text("Describe the photo in your language") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Enter a detailed description of what you see in the photo...") }
                )

                Text(
                    text = "Or record audio description (optional):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Mic button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = { },
                            modifier = Modifier
                                .size(70.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isRecording = true
                                            recordingDuration = 0
                                            errorMessage = null
                                            tryAwaitRelease()
                                            isRecording = false

                                            if (recordingDuration < 10) {
                                                errorMessage = "Recording too short (min 10 s)."
                                                recordingDuration = 0
                                            } else {
                                                hasRecorded = true
                                                recordedDuration = recordingDuration
                                            }
                                        }
                                    )
                                },
                            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = if (isRecording) "Release to stop" else "Hold to record (optional)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                if (isRecording) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🎤 Recording Audio: ${recordingDuration}s",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            LinearProgressIndicator(
                                progress = { recordingDuration / 20f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "⚠️ $errorMessage",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                if (hasRecorded) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🎵 Audio Recording: ${recordedDuration}s",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            LinearProgressIndicator(
                                progress = { recordedDuration / 20f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )

                            Text(
                                text = "Audio file: photo_audio_${
                                    Clock.System.now().toEpochMilliseconds()
                                }.mp3",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            hasPhoto = false
                            textDescription = ""
                            hasRecorded = false
                            recordedDuration = 0
                            errorMessage = null
                            photoPath = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retake Photo")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val task = Task(
                                    id = "task_${Clock.System.now().toEpochMilliseconds()}",
                                    taskType = TaskType.PHOTO_CAPTURE,
                                    timestamp = Clock.System.now().toString(),
                                    durationSec = if (hasRecorded) recordedDuration else 0,
                                    imagePath = photoPath,
                                    text = textDescription.ifEmpty { null },
                                    audioPath = if (hasRecorded) "photo_audio_${
                                        Clock.System.now().toEpochMilliseconds()
                                    }.mp3" else null
                                )
                                TaskRepository.addTask(task)
                                onComplete()
                            }
                        },
                        enabled = textDescription.isNotEmpty() || hasRecorded,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}