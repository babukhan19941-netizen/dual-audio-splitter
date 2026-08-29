package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.AudioMode
import com.example.model.BluetoothDeviceItem
import com.example.model.CallAudioType
import com.example.model.ConnectionStatus
import com.example.model.DeviceType
import com.example.model.MediaTrack
import com.example.ui.components.AudioChannelVisualizerSection
import com.example.ui.components.BluetoothDeviceSection
import com.example.ui.components.CallAudioSection
import com.example.ui.components.MediaPlayerSection
import com.example.ui.components.ModeSwitchSection
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          ModeSwitchSection(
            currentMode = AudioMode.SPLIT_CHANNELS,
            onToggleMode = {},
            onSelectMode = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

