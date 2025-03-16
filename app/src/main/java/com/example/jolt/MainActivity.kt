package com.example.jolt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccidentDetectionApp()
        }
    }
}

@Composable
fun AccidentDetectionApp() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Normal") }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val sensorEventListener = remember {
        object : SensorEventListener {
            private var lastX = 0f
            private var lastY = 0f
            private var lastZ = 0f
            private val SHAKE_THRESHOLD = 8f  // Lowered threshold for better sensitivity
            private val TIME_THRESHOLD = 500L // Minimum time between shakes in milliseconds
            private var lastShakeTime = 0L

            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate the difference from the last reading
                val diffX = Math.abs(x - lastX)
                val diffY = Math.abs(y - lastY)
                val diffZ = Math.abs(z - lastZ)

                // Update last values
                lastX = x
                lastY = y
                lastZ = z

                // Calculate the overall acceleration
                val acceleration = Math.sqrt((diffX * diffX + diffY * diffY + diffZ * diffZ).toDouble()).toFloat()

                // Check if the acceleration exceeds the threshold
                if (acceleration > SHAKE_THRESHOLD) {
                    val currentTime = System.currentTimeMillis()

                    // Only consider it a shake if enough time has passed since the last one
                    if (currentTime - lastShakeTime > TIME_THRESHOLD) {
                        status = "Accident happened!"
                        lastShakeTime = currentTime
                    }
                } else {
                    status = "Normal"
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    sensorManager.registerListener(
        sensorEventListener,
        accelerometer,
        SensorManager.SENSOR_DELAY_NORMAL
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            fontSize = 36.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AccidentDetectionApp()
}