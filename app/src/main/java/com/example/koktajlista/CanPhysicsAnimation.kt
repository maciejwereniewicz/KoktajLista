package com.example.koktajlista

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

@Composable
fun FreeCansAnimation(
    cansCount: Int = 5,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surfaceVariant)
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) }

    val density = LocalContext.current.resources.displayMetrics.density
    val canSizeDp = 48.dp
    val canSizePx = (canSizeDp.value * density).toInt()

    val physics = remember {
        PhysicsSettings(
            damping = 0.98f,
            gravityScale = 150f,
            maxSpeed = 500f
        )
    }

    data class CanState(
        val x: Animatable<Float, *>,
        val y: Animatable<Float, *>,
        var vx: Float = 0f,
        var vy: Float = 0f
    )

    val cans = remember {
        List(cansCount) {
            CanState(
                x = Animatable((0..1000).random().toFloat()),
                y = Animatable((0..2000).random().toFloat())
            )
        }
    }

    var gravityX by remember { mutableStateOf(0f) }
    var gravityY by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                gravityX = -event.values[0]
                gravityY = event.values[1]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    LaunchedEffect(Unit) {
        var previousTime = System.currentTimeMillis()
        while (true) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = (currentTime - previousTime).coerceAtLeast(1L)
            val deltaTimeSeconds = deltaTime / 1000f
            previousTime = currentTime

            cans.forEach { can ->
                can.vx += gravityX * physics.gravityScale * deltaTimeSeconds
                can.vy += gravityY * physics.gravityScale * deltaTimeSeconds

                // Ograniczenie prędkości
                can.vx = can.vx.coerceIn(-physics.maxSpeed, physics.maxSpeed)
                can.vy = can.vy.coerceIn(-physics.maxSpeed, physics.maxSpeed)

                // Pozycja bez żadnych kolizji
                val newX = can.x.value + can.vx * deltaTimeSeconds
                val newY = can.y.value + can.vy * deltaTimeSeconds

                can.vx *= physics.damping
                can.vy *= physics.damping

                can.x.snapTo(newX)
                can.y.snapTo(newY)
            }

            delay(16L)
        }
    }

    Box(modifier = modifier) {
        cans.forEach { can ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (can.x.value / density).dp,
                        y = (can.y.value / density).dp
                    )
                    .size(canSizeDp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83C\uDF7E", fontSize = 28.sp)
            }
        }
    }
}

data class PhysicsSettings(
    val damping: Float,
    val gravityScale: Float,
    val maxSpeed: Float
)
