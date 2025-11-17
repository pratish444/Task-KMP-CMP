package com.example.androidinterntask.screens

import android.net.Uri
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.androidinterntask.LocalMainActivity
import com.example.androidinterntask.data.TaskRepository
import com.example.androidinterntask.models.Task
import com.example.androidinterntask.models.TaskType
import com.example.androidinterntask.utils.AudioRecorderHelper
import com.example.androidinterntask.utils.CameraHelper
import com.example.androidinterntask.utils.PermissionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

private const val TAG = "PhotoCaptureTask"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun PhotoCaptureTaskScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalMainActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasPhoto by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var textDescription by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var hasRecorded by remember { mutableStateOf(false) }
    var recordedDuration by remember { mutableStateOf(0) }
    var recordedFilePath by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCameraOpen by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionType by remember { mutableStateOf("") }
    var hasAudioPermission by remember { mutableStateOf(false) }

    val audioRecorder = remember { AudioRecorderHelper(context) }
    val cameraHelper = remember { CameraHelper(context) }
    val previewView = remember { PreviewView(context) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        hasAudioPermission = PermissionHandler.hasAudioPermission(context)
        Log.d(TAG, "Initial permission check: $hasAudioPermission")
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    Log.d(TAG, "Press detected")
                    if (!hasAudioPermission) {
                        permissionType = "Microphone"
                        showPermissionDialog = true
                    } else {
                        val fileName = "photo_audio_${Clock.System.now().toEpochMilliseconds()}.mp3"
                        val started = audioRecorder.startRecording(fileName)
                        if (started) {
                            isRecording = true
                            recordingDuration = 0
                            errorMessage = null
                            hasRecorded = false
                        } else {
                            errorMessage = "Failed to start recording"
                        }
                    }
                }
                is PressInteraction.Release -> {
                    Log.d(TAG, "Release detected")
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
                            errorMessage = null
                        }
                    }
                }
                is PressInteraction.Cancel -> {
                    Log.d(TAG, "Press cancelled")
                    if (isRecording) {
                        isRecording = false
                        audioRecorder.stopRecording()
                        errorMessage = "Recording cancelled"
                        recordingDuration = 0
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Disposing screen")
            if (isRecording) {
                audioRecorder.stopRecording()
            }
            audioRecorder.release()
            cameraHelper.shutdown()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            Log.d(TAG, "Recording timer started")
            while (isRecording && recordingDuration <= 20) {
                delay(1000)
                recordingDuration++
            }
            if (recordingDuration > 20) {
                Log.d(TAG, "Recording exceeded 20 seconds")
                isRecording = false
                audioRecorder.stopRecording()
                errorMessage = "Recording too long (max 20 s)."
                recordingDuration = 0
            }
        }
    }

    fun checkAndRequestAudioPermission() {
        Log.d(TAG, "Requesting audio permission")
        activity.permissionHandler.requestAudioPermission { granted ->
            hasAudioPermission = granted
            Log.d(TAG, "Audio permission granted: $granted")
            if (!granted) {
                errorMessage = "Microphone permission denied."
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDialog = false
                Log.d(TAG, "Permission dialog dismissed")
            },
            title = { Text("$permissionType Permission Required") },
            text = { Text("This app needs $permissionType access for this feature.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    Log.d(TAG, "Requesting $permissionType permission from dialog")
                    if (permissionType == "Camera") {
                        activity.permissionHandler.requestCameraPermission { granted ->
                            if (!granted) {
                                errorMessage = "Camera permission denied."
                            }
                        }
                    } else {
                        checkAndRequestAudioPermission()
                    }
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    Log.d(TAG, "Permission dialog cancelled")
                }) {
                    Text("Cancel")
                }
            }
        )
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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCameraOpen) {
                            AndroidView(
                                factory = { previewView },
                                modifier = Modifier.fillMaxSize()
                            )
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
                                    text = "Tap 'Open Camera' to start",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                if (!isCameraOpen) {
                    Button(
                        onClick = {
                            Log.d(TAG, "Open Camera clicked")
                            activity.permissionHandler.requestCameraPermission { granted ->
                                if (granted) {
                                    Log.d(TAG, "Camera permission granted, opening camera")
                                    isCameraOpen = true
                                    cameraHelper.startCamera(
                                        previewView,
                                        lifecycleOwner,
                                        onError = { e ->
                                            Log.e(TAG, "Camera error", e)
                                            errorMessage = "Camera error: ${e.message}"
                                            isCameraOpen = false
                                        }
                                    )
                                } else {
                                    Log.d(TAG, "Camera permission denied")
                                    permissionType = "Camera"
                                    showPermissionDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Open Camera", fontSize = 18.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            Log.d(TAG, "Capture Photo clicked")
                            cameraHelper.takePhoto(
                                onPhotoSaved = { uri ->
                                    Log.d(TAG, "Photo saved: $uri")
                                    photoUri = uri
                                    hasPhoto = true
                                    isCameraOpen = false
                                    errorMessage = null
                                },
                                onError = { e ->
                                    Log.e(TAG, "Photo capture error", e)
                                    errorMessage = "Failed to capture photo: ${e.message}"
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Capture Photo", fontSize = 18.sp)
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
                    photoUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Captured photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                OutlinedTextField(
                    value = textDescription,
                    onValueChange = { textDescription = it },
                    label = { Text("Describe the photo in your language") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Enter a detailed description...") }
                )

                Text(
                    text = "Or record audio description (optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = {
                                Log.d(TAG, "FAB onClick triggered (backup)")
                            },
                            modifier = Modifier.size(64.dp),
                            containerColor = if (isRecording)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary,
                            interactionSource = interactionSource
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record",
                                modifier = Modifier.size(28.dp)
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
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🎤 Recording: ${recordingDuration}s / 20s",
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✅ Recording Complete: ${recordedDuration}s",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Audio saved: ${recordedFilePath?.substringAfterLast("/") ?: "unknown"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(top = 4.dp)
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
                            Log.d(TAG, "Retake Photo clicked")
                            hasPhoto = false
                            photoUri = null
                            isCameraOpen = false
                            textDescription = ""
                            hasRecorded = false
                            recordedDuration = 0
                            recordedFilePath = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retake Photo")
                    }

                    Button(
                        onClick = {
                            Log.d(TAG, "Submit clicked")
                            scope.launch {
                                val task = Task(
                                    id = "task_${Clock.System.now().toEpochMilliseconds()}",
                                    taskType = TaskType.PHOTO_CAPTURE,
                                    timestamp = Clock.System.now().toString(),
                                    durationSec = recordedDuration,
                                    imagePath = photoUri?.toString(),
                                    audioPath = recordedFilePath,
                                    textDescription = textDescription.ifBlank { null }
                                )
                                TaskRepository.addTask(task)
                                onComplete()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}