package com.example.koktajlista

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.BassBoost
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


@Composable
fun ShowTimer(
    timeR: Long,
    isRunningR: Boolean,
    isCountdownR: Boolean,
    timeStartR: Long,
    storedTimeR: Long,
    onUpdate: (Long, Boolean, Boolean, Long, Long) -> Unit
) {
    var isCountdown by remember { mutableStateOf(isCountdownR) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 🔁 Przełącznik jako przyciski

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            if (isCountdown) {
                CountdownTimer(
                    timeR = timeR,
                    isCountDownR = isCountdownR,
                    timeStartR = timeStartR,
                    storedTimeR = storedTimeR,
                    onUpdate = onUpdate
                )
            } else {
                StopwatchTimer(
                    timeR = timeR,
                    isRunningR = isRunningR,
                    timeStartR = timeStartR,
                    storedTimeR = storedTimeR,
                    onUpdate = onUpdate
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { isCountdown = false },
                enabled = isCountdown && !isCountdownR,
            ) {
                Icon(Icons.Filled.Timer, contentDescription = "Stoper")
            }
            Button(
                onClick = { isCountdown = true },
                enabled = !isCountdown && !isRunningR,
            ) {
                Icon(Icons.Filled.HourglassBottom, contentDescription = "Odliczanie")
            }

        }
    }
}


@Composable
fun CountdownTimer(
    timeR: Long,
    isCountDownR: Boolean,
    timeStartR: Long,
    storedTimeR: Long,
    onUpdate: (Long, Boolean, Boolean, Long, Long) -> Unit
) {
    var time by remember { mutableStateOf(timeR) }
    var isCountDown by remember { mutableStateOf(isCountDownR) }
    var timeStart by remember { mutableStateOf(timeStartR) }
    var storedTime by remember { mutableStateOf(storedTimeR) }


    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }

    val context = LocalContext.current

    LaunchedEffect(isCountDown) {
        while (isCountDown && time > 0) {
            delay(16)
            time = timeStart - System.currentTimeMillis() + storedTime
            onUpdate(time, false, isCountDown, timeStart, storedTime)
        }
        if (time <= 0) {
            isCountDown = false
            time = 0
            storedTime = 0
        }
        time = storedTime
    }

    TimerCard(
        label = "Odliczanie",
        time = time,
        onStart = {
            timeStart = System.currentTimeMillis()
            isCountDown = true
            onUpdate(time, false, isCountDown, timeStart, storedTime)

            val triggerTime = System.currentTimeMillis() + storedTime
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                }
            }

            if (canScheduleExactAlarms(context)) {
                setAlarm(context, triggerTime)
            } else {
                // pokaż dialog z prośbą o ręczne włączenie
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }

        },
        onStop = {
            storedTime = storedTime - System.currentTimeMillis() + timeStart
            isCountDown = false
            onUpdate(time, false, isCountDown, timeStart, storedTime)
            cancelAlarm(context)
        },
        onReset = {
            isCountDown = false
            storedTime = (minutes*60+seconds)*1000.toLong()
            time = storedTime
            onUpdate(time, false, isCountDown, timeStart, storedTime)
        },
        isRunning = isCountDown,
        countdownInputs = {
            CountdownInputs(
                selectedMinutes = minutes,
                selectedSeconds = seconds,
                onMinutesChange = { minutes = it },
                onSecondsChange = { seconds = it }
            )
        }

    )
}


@Composable
fun StopwatchTimer(
    timeR: Long,
    isRunningR: Boolean,
    timeStartR: Long,
    storedTimeR: Long,
    onUpdate: (Long, Boolean, Boolean, Long, Long) -> Unit
) {
    var time by remember { mutableStateOf(timeR) }
    var isRunning by remember { mutableStateOf(isRunningR) }
    var timeStart by remember { mutableStateOf(timeStartR) }
    var storedTime by remember { mutableStateOf(storedTimeR) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(16)
            time = System.currentTimeMillis() - timeStart + storedTime
            onUpdate(time, isRunning, false, timeStart, storedTime)
        }
        time = storedTime
    }

    TimerCard(
        label = "Stoper",
        time = time,
        onStart = {
            timeStart = System.currentTimeMillis()
            isRunning = true
            storedTime = time
            onUpdate(time, isRunning, false, timeStart, storedTime)
        },
        onStop = {
            storedTime += System.currentTimeMillis() - timeStart
            isRunning = false
            onUpdate(time, isRunning, false, timeStart, storedTime)
        },
        onReset = {
            time = 0L
            isRunning = false
            timeStart = 0L
            storedTime = 0L
            onUpdate(time, isRunning, false, timeStart, storedTime)
        },
        isRunning = isRunning
    )
}


@Composable
fun TimerCard(
    label: String,
    time: Long,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    isRunning: Boolean,
    countdownInputs: (@Composable RowScope.() -> Unit)? = null
) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
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
                Button(onClick = onStart, enabled = !isRunning) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
                }
                Button(onClick = onStop, enabled = isRunning) {
                    Icon(Icons.Filled.Stop, contentDescription = "Stop")
                }
                Button(onClick = onReset, enabled = !isRunning) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = "Reset")
                }

            }

            countdownInputs?.let {
                Row { it() }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
}

@Composable
fun CountdownInputs(
    selectedMinutes: Int,
    selectedSeconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        ScrollBoxSelector(
            label = "Minuty",
            selected = selectedMinutes,
            onSelectedChange = onMinutesChange
        )
        Spacer(modifier = Modifier.width(16.dp))
        ScrollBoxSelector(
            label = "Sekundy",
            selected = selectedSeconds,
            onSelectedChange = onSecondsChange
        )
    }
}

@Composable
fun ScrollBoxSelector(
    label: String,
    selected: Int,
    onSelectedChange: (Int) -> Unit,
    range: IntRange = 0..60
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(150.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                range.forEach { value ->
                    val isSelected = value == selected
                    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectedChange(value) }
                            .background(backgroundColor)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.bodyLarge.copy(color = textColor)
                        )
                    }
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
