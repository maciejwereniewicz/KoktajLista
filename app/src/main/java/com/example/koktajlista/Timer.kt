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
    var isCountdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        if (isCountdown) {
            CountdownTimer(
                timeR = timeR,
                isRunningR = isRunningR,
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

        Text("Tryb odliczania")
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = isCountdown,
            onCheckedChange = { isCountdown = it }
        )
    }
}

@Composable
fun CountdownTimer(
    timeR: Long,
    isRunningR: Boolean,
    onUpdate: (Long, Boolean, Long, Long) -> Unit
) {
    var time by remember { mutableStateOf(timeR) }
    var isRunning by remember { mutableStateOf(isRunningR) }
    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(16)
            time -= 1000
            if (time <= 0L) {
                time = 0L
                isRunning = false
            }
            onUpdate(time, isRunning, 0L, 0L)
        }
    }

    TimerCard(
        label = "Odliczanie",
        time = time,
        onStart = {
            time = (minutes * 60 + seconds) * 1000L
            isRunning = true
            onUpdate(time, isRunning, 0L, 0L)
        },
        onStop = {
            isRunning = false
            onUpdate(time, isRunning, 0L, 0L)
        },
        onReset = {
            time = (minutes * 60 + seconds) * 1000L
            isRunning = false
            onUpdate(time, isRunning, 0L, 0L)
        },
        isRunning = isRunning,
        countdownInputs = {
            OutlinedTextField(
                value = minutes.toString(),
                onValueChange = { minutes = it.toIntOrNull() ?: 0 },
                label = { Text("Minuty") },
                modifier = Modifier.width(100.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = seconds.toString(),
                onValueChange = { seconds = it.toIntOrNull() ?: 0 },
                label = { Text("Sekundy") },
                modifier = Modifier.width(100.dp)
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
    onUpdate: (Long, Boolean, Long, Long) -> Unit
) {
    var time by remember { mutableStateOf(timeR) }
    var isRunning by remember { mutableStateOf(isRunningR) }
    var timeStart by remember { mutableStateOf(timeStartR) }
    var storedTime by remember { mutableStateOf(storedTimeR) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(16)
            time = System.currentTimeMillis() - timeStart + storedTime
            onUpdate(time, isRunning, timeStart, storedTime)
        }
    }

    TimerCard(
        label = "Stoper",
        time = time,
        onStart = {
            timeStart = System.currentTimeMillis()
            isRunning = true
            onUpdate(time, isRunning, timeStart, storedTime)
        },
        onStop = {
            storedTime += System.currentTimeMillis() - timeStart
            isRunning = false
            onUpdate(time, isRunning, timeStart, storedTime)
        },
        onReset = {
            time = 0L
            isRunning = false
            timeStart = 0L
            storedTime = 0L
            onUpdate(time, isRunning, timeStart, storedTime)
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
                Button(onClick = onStart, enabled = !isRunning) { Text("Start") }
                Button(onClick = onStop, enabled = isRunning) { Text("Stop") }
                Button(onClick = onReset) { Text("Reset") }
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
        DropdownSelector(
            label = "Minuty",
            selected = selectedMinutes,
            onSelectedChange = onMinutesChange
        )
        Spacer(modifier = Modifier.width(16.dp))
        DropdownSelector(
            label = "Sekundy",
            selected = selectedSeconds,
            onSelectedChange = onSecondsChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    selected: Int,
    onSelectedChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selected.toString(),
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..60).forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.toString()) },
                    onClick = {
                        onSelectedChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}



fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    return "%02d:%02d".format(minutes,seconds %60)
}
