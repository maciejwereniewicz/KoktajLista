package com.example.koktajlista

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ShowTimer(
    timeR: Long,
    isRunningR: Boolean,
    timeStartR: Long,
    storedTimeR: Long,
    onUpdate: (Long, Boolean, Long, Long) -> Unit
) {
    var time by remember { mutableStateOf(timeR) }
    var isRunning by remember { mutableStateOf(isRunningR) }
    var timeStart by remember { mutableStateOf(timeStartR) }
    var storedTime by remember { mutableStateOf(storedTimeR) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            time = System.currentTimeMillis() - timeStart + storedTime
            onUpdate(time, isRunning, timeStart, storedTime)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Stoper",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Czas: ${formatTime(time)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        timeStart = System.currentTimeMillis()
                        isRunning = true
                        onUpdate(time, isRunning, timeStart, storedTime)
                    },
                    enabled = !isRunning
                ) {
                    Text("Start")
                }

                Button(
                    onClick = {
                        storedTime += System.currentTimeMillis() - timeStart
                        isRunning = false
                        onUpdate(time, isRunning, timeStart, storedTime)
                    },
                    enabled = isRunning
                ) {
                    Text("Stop")
                }

                Button(
                    onClick = {
                        time = 0L
                        isRunning = false
                        timeStart = 0L
                        storedTime = 0L
                        onUpdate(time, isRunning, timeStart, storedTime)
                    }
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    return "%02d:%02d".format(minutes,seconds %60)
}
