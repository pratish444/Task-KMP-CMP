package com.example.androidinterntask.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidinterntask.LocalMainActivity
import com.example.androidinterntask.data.TaskRepository
import com.example.androidinterntask.models.Product
import com.example.androidinterntask.models.Task
import com.example.androidinterntask.models.TaskType
import com.example.androidinterntask.utils.AudioRecorderHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TextReadingTaskScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalMainActivity.current
    val scope = rememberCoroutineScope()

    var product by remember { mutableStateOf<Product?>(null) }
    var passage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var hasRecorded by remember { mutableStateOf(false) }
    var recordedDuration by remember { mutableStateOf(0) }
    var recordedFilePath by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    var noBackgroundNoise by remember { mutableStateOf(false) }
    var noMistakes by remember { mutableStateOf(false) }
    var noPauses by remember { mutableStateOf(false) }

    val audioRecorder = remember { AudioRecorderHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.release()
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        val fetchedProduct = TaskRepository.fetchProduct()
        product = fetchedProduct
        passage = fetchedProduct?.description ?: ""
        isLoading = false
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording && recordingDuration <= 20) {
                delay(1000)
                recordingDuration++
            }
            if (recordingDuration > 20) {
                isRecording = false
                audioRecorder.stopRecording()
                errorMessage = "Recording too long (max 20 s)."
                recordingDuration = 0
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Microphone Permission Required") },
            text = { Text("This app needs microphone access to record audio.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    activity.permissionHandler.requestAudioPermission { granted ->
                        if (!granted) {
                            errorMessage = "Microphone permission denied."
                        }
                    }
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text Reading Task") },
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
            Text(
                text = "Read the passage aloud in your native language",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator()
                        Text("Loading passage...", modifier = Modifier.padding(top = 8.dp))
                    } else {
                        Text(passage)
                    }
                }
            }

            if (!isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = { },
                            modifier = Modifier
                                .size(80.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            activity.permissionHandler.requestAudioPermission { granted ->
                                                if (granted) {
                                                    val fileName = "reading_audio_${Clock.System.now().toEpochMilliseconds()}.mp3"
                                                    val started = audioRecorder.startRecording(fileName)

                                                    if (started) {
                                                        isRecording = true
                                                        recordingDuration = 0
                                                        errorMessage = null
                                                    } else {
                                                        errorMessage = "Failed to start recording"
                                                    }
                                                } else {
                                                    showPermissionDialog = true
                                                }
                                            }

                                            tryAwaitRelease()

                                            if (isRecording) {
                                                isRecording = false
                                                val filePath = audioRecorder.stopRecording()

                                                if (recordingDuration < 10) {
                                                    errorMessage = "Recording too short (min 10 s)."
                                                    recordingDuration = 0
                                                } else {
                                                    hasRecorded = true
                                                    recordedDuration = recordingDuration
                                                    recordedFilePath = filePath
                                                }
                                            }
                                        }
                                    )
                                },
                            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = if (isRecording) "Release to stop" else "Hold to record",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                if (isRecording) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🎤 Recording: ${recordingDuration}s",
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                if (hasRecorded) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Recording Complete: ${recordedDuration}s",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Audio saved: ${recordedFilePath?.substringAfterLast("/") ?: "unknown"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Column {
                        CheckboxWithLabel(noBackgroundNoise, { noBackgroundNoise = it }, "No background noise")
                        CheckboxWithLabel(noMistakes, { noMistakes = it }, "No mistakes while reading")
                        CheckboxWithLabel(noPauses, { noPauses = it }, "Beech me koi galti nahi hai")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                hasRecorded = false
                                recordedDuration = 0
                                recordedFilePath = null
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Record Again")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val task = Task(
                                        id = "task_${Clock.System.now().toEpochMilliseconds()}",
                                        taskType = TaskType.TEXT_READING,
                                        timestamp = Clock.System.now().toString(),
                                        durationSec = recordedDuration,
                                        text = passage,
                                        audioPath = recordedFilePath
                                    )
                                    TaskRepository.addTask(task)
                                    onComplete()
                                }
                            },
                            enabled = noBackgroundNoise && noMistakes && noPauses,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckboxWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}