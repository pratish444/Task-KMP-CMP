package com.example.androidinterntask.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSelectionScreen(
    onTaskSelected: (String) -> Unit,
    onViewHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Task Type") },
                actions = {
                    IconButton(onClick = onViewHistory) {
                        Icon(Icons.Default.History, "View History")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Choose a sample task",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TaskCard(
                title = "Text Reading Task",
                description = "Read a passage aloud in your language",
                icon = Icons.Default.Description,
                onClick = { onTaskSelected("text_reading") }
            )

            TaskCard(
                title = "Image Description Task",
                description = "Describe what you see in an image",
                icon = Icons.Default.Image,
                onClick = { onTaskSelected("image_description") }
            )

            TaskCard(
                title = "Photo Capture Task",
                description = "Take a photo and describe it",
                icon = Icons.Default.CameraAlt,
                onClick = { onTaskSelected("photo_capture") }
            )
        }
    }
}

@Composable
private fun TaskCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}