package com.example.androidinterntask.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoiseTestScreen(
    onTestPassed: () -> Unit,
    onBack: () -> Unit
) {
    var isTesting by remember { mutableStateOf(false) }
    var currentDb by remember { mutableStateOf(0f) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var averageDb by remember { mutableStateOf(0f) }

    LaunchedEffect(isTesting) {
        if (isTesting) {
            val readings = mutableListOf<Float>()
            repeat(30) {
                val db = Random.nextFloat() * 60f
                currentDb = db
                readings.add(db)
                delay(100)
            }
            averageDb = readings.average().toFloat()
            testResult = if (averageDb < 40f) {
                "Good to proceed"
            } else {
                "Please move to a quieter place"
            }
            isTesting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Noise Test") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ambient Noise Level",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Decibel meter
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val centerX = size.width / 2
                    val centerY = size.height / 2

                    // Background circle
                    drawCircle(
                        color = Color.LightGray,
                        radius = radius,
                        center = Offset(centerX, centerY)
                    )

                    // Level indicator
                    val level = currentDb / 60f
                    drawCircle(
                        color = if (currentDb < 40f) Color.Green else Color.Red,
                        radius = radius * level,
                        center = Offset(centerX, centerY)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${currentDb.toInt()}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "dB", fontSize = 20.sp)
                }
            }

            Text(
                text = "Range: 0 - 60 dB",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (testResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (averageDb < 40f)
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else
                            Color(0xFFF44336).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = testResult!!,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (averageDb < 40f) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        Text(
                            text = "Average: ${averageDb.toInt()} dB",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (testResult == null || averageDb >= 40f) {
                Button(
                    onClick = {
                        isTesting = true
                        testResult = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isTesting
                ) {
                    Text(if (isTesting) "Testing..." else "Start Test", fontSize = 18.sp)
                }
            } else {
                Button(
                    onClick = onTestPassed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Continue", fontSize = 18.sp)
                }
            }
        }
    }
}