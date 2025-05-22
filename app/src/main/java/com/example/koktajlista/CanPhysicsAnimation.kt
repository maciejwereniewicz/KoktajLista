package com.example.koktajlista

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun FloatToDp(px: Float): Dp {
    val density = LocalDensity.current
    return with(density) { px.toDp() }
}

@Composable
fun DpToPx(dpValue: Dp): Float {
    val density = LocalDensity.current
    return with(density) { dpValue.toPx() }
}

@Composable
fun FreeCansAnimation(
    cansCount: Int = 5,
    modifier: Modifier = Modifier
        .padding(20.dp)
        .fillMaxSize()
        .border(2.dp, Color.Red)
        .background(MaterialTheme.colorScheme.surfaceVariant)

) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) }

    val density = LocalContext.current.resources.displayMetrics.density
    val orientation = LocalContext.current.resources.configuration.orientation
    val canSizeDp = 48.dp

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
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // zamień osie
                    val temp = gravityX
                    gravityX = gravityY
                    gravityY = -temp
                }

            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    BoxWithConstraints(modifier = modifier) {
        val minX = 0.dp
        val minY = 0.dp

        val fminX = DpToPx(minX)
        val fminY = DpToPx(minY)
        val density = LocalDensity.current
        LaunchedEffect(constraints) {
            var previousTime = System.currentTimeMillis()

            while (true) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - previousTime).coerceAtLeast(1L)
                val deltaTimeSeconds = deltaTime / 1000f
                previousTime = currentTime


                val fmaxX = with(density) { (constraints.maxWidth.toDp() - 50.dp).toPx() }
                val fmaxY = with(density) { (constraints.minHeight.toDp() - 50.dp).toPx() }

                val fminX = with(density) { 0.dp.toPx() }
                val fminY = with(density) { 0.dp.toPx() }


                cans.forEach { can ->
                    can.vx += gravityX * physics.gravityScale * deltaTimeSeconds
                    can.vy += gravityY * physics.gravityScale * deltaTimeSeconds

                    can.vx = can.vx.coerceIn(-physics.maxSpeed, physics.maxSpeed)
                    can.vy = can.vy.coerceIn(-physics.maxSpeed, physics.maxSpeed)

                    var newX = can.x.value + can.vx * deltaTimeSeconds
                    var newY = can.y.value + can.vy * deltaTimeSeconds

                    if (newX < fminX) {
                        newX = fminX
                        can.vx = -can.vx
                    }
                    if (newX > fmaxX) {
                        newX = fmaxX
                        can.vx = -can.vx
                    }

                    if (newY < fminY) {
                        newY = fminY
                        can.vy = -can.vy
                    }
                    if (newY > fmaxY) {
                        newY = fmaxY
                        can.vy = -can.vy
                    }

                    can.vx *= physics.damping
                    can.vy *= physics.damping

                    can.x.snapTo(newX)
                    can.y.snapTo(newY)
                }

                delay(16L)
            }
        }

        // Drawing cans stays unchanged
        cans.forEach { can ->
            Box(
                modifier = Modifier
                    .offset(
                        x = FloatToDp(can.x.value),
                        y = FloatToDp(can.y.value)
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
