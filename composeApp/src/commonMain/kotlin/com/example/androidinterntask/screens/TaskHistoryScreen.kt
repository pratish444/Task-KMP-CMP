package com.example.androidinterntask.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidinterntask.data.TaskRepository
import com.example.androidinterntask.models.Task
import com.example.androidinterntask.models.TaskType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskHistoryScreen(
    onBack: () -> Unit
) {
    val tasks = remember { TaskRepository.getAllTasks() }
    val totalDuration = remember { TaskRepository.getTotalDuration() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task History") },
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
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Tasks",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${tasks.size}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Duration",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${totalDuration}s",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks completed yet",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks) { task ->
                        TaskHistoryItem(task)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskHistoryItem(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Task ${task.id.takeLast(6)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = getTaskTypeLabel(task.taskType),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Duration: ${task.durationSec}s",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatTimestamp(task.timestamp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (task.text != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.text.take(100) + if (task.text.length > 100) "..." else "",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (task.imageUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Image: ${task.imageUrl.takeLast(30)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (task.imagePath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Photo: ${task.imagePath.takeLast(30)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getTaskTypeLabel(type: TaskType): String {
    return when (type) {
        TaskType.TEXT_READING -> "Text Reading"
        TaskType.IMAGE_DESCRIPTION -> "Image Description"
        TaskType.PHOTO_CAPTURE -> "Photo Capture"
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        timestamp.substring(0, 19).replace("T", " ")
    } catch (e: Exception) {
        timestamp
    }
}