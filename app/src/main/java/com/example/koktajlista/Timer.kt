import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun showTimer(timeR: Long, isRunningR: Boolean, timeStartR: Long, storedTimeR: Long) {
    var time by remember { mutableStateOf(timeR) }
    var isRunning by remember { mutableStateOf(isRunningR) }
    var timeStart by remember { mutableStateOf( timeStartR ) }
    var storedTime by remember { mutableStateOf( storedTimeR ) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1)
            time = System.currentTimeMillis() - timeStart + storedTime
        }
        time = storedTime
    }

    ModalDrawerSheet {
        Text("Stoper", modifier = Modifier.padding(16.dp))
        HorizontalDivider()

        Text(
            text = formatTime(time),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                if (!isRunning)
                {
                    isRunning = true
                    timeStart = System.currentTimeMillis()
                }

            }) {
                Text("Start")
            }
            Button(onClick = {
                if (isRunning)
                {
                    isRunning = false
                    storedTime = time
                    time = 0
                }

            }) {
                Text("Stop")
            }
            Button(onClick = {
                if(!isRunning)
                {
                    storedTime = 0
                    timeStart = 0
                    time = 0
                }
            }) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return "%02d:%02d:%02d:%03d".format(hours,minutes%60,seconds %60, millis%1000)
}