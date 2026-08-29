package com.example.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AudioChannelVisualizerSection
import com.example.ui.components.BluetoothDeviceSection
import com.example.ui.components.CallAudioSection
import com.example.ui.components.MediaPlayerSection
import com.example.ui.components.ModeSwitchSection
import com.example.ui.components.SpeakerBalanceSection
import com.example.ui.theme.CallEmerald
import com.example.ui.theme.PolishBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AudioSplitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isBtEnabled by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()
    val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val hasPermissions by viewModel.hasPermissions.collectAsStateWithLifecycle()

    val audioMode by viewModel.audioMode.collectAsStateWithLifecycle()
    val isMediaPlaying by viewModel.isMediaPlaying.collectAsStateWithLifecycle()
    val isCallPlaying by viewModel.isCallPlaying.collectAsStateWithLifecycle()
    val isMicMonitoring by viewModel.isMicMonitoring.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val currentCallType by viewModel.callAudioType.collectAsStateWithLifecycle()
    val mediaVolume by viewModel.mediaVolume.collectAsStateWithLifecycle()
    val callVolume by viewModel.callVolume.collectAsStateWithLifecycle()
    val panBalance by viewModel.panBalance.collectAsStateWithLifecycle()
    val leftAmp by viewModel.leftAmp.collectAsStateWithLifecycle()
    val rightAmp by viewModel.rightAmp.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Permission launcher for Android 12+ (API 31+) & Audio Record
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val btConnectGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            true
        }
        viewModel.updatePermissionsGranted(btConnectGranted)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate50,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PolishBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "App Icon",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dual Audio Splitter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (connectedDevice != null) CallEmerald else Slate400)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (connectedDevice != null) {
                                        "ACTIVE • ${connectedDevice?.name?.take(18)}"
                                    } else if (isBtEnabled) {
                                        "BLUETOOTH READY"
                                    } else {
                                        "BLUETOOTH OFFLINE"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (connectedDevice != null) CallEmerald else Slate400
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshDevices() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                            .testTag("top_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Slate800,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                            .testTag("top_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Bluetooth Settings",
                            tint = Slate800,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Bluetooth Status & Warning Card (if Bluetooth is turned off)
            if (!isBtEnabled) {
                item {
                    BluetoothEnableBanner(
                        onOpenSettings = {
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        }
                    )
                }
            }

            // 2. PRIMARY USER REQUIREMENT: 1-Click Audio Mode Toggle (Split vs Unified)
            item {
                ModeSwitchSection(
                    currentMode = audioMode,
                    onToggleMode = { viewModel.toggleAudioMode() },
                    onSelectMode = { viewModel.setAudioMode(it) },
                    modifier = Modifier.testTag("mode_switch_section")
                )
            }

            // 3. Live Real-Time Stereo Channel Waveform Visualizer
            item {
                AudioChannelVisualizerSection(
                    leftAmp = leftAmp,
                    rightAmp = rightAmp,
                    audioMode = audioMode,
                    isMediaActive = isMediaPlaying,
                    isCallActive = isCallPlaying || isMicMonitoring,
                    modifier = Modifier.testTag("visualizer_section")
                )
            }

            // 4. Paired Bluetooth Devices Section (Tap to connect)
            item {
                BluetoothDeviceSection(
                    devices = pairedDevices,
                    connectedDevice = connectedDevice,
                    isScanning = isScanning,
                    onDeviceClick = { viewModel.connectDevice(it) },
                    onDisconnectClick = { viewModel.disconnectDevice(it) },
                    onRefreshClick = { viewModel.refreshDevices() },
                    onOpenSettingsClick = {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    },
                    modifier = Modifier.testTag("bluetooth_devices_section")
                )
            }

            // 5. Left Speaker - Media Audio Player Controls
            item {
                MediaPlayerSection(
                    isPlaying = isMediaPlaying,
                    currentTrack = currentTrack,
                    volume = mediaVolume,
                    onTogglePlay = { viewModel.toggleMediaPlayback() },
                    onTrackSelect = { viewModel.selectTrack(it) },
                    onVolumeChange = { viewModel.setMediaVolume(it) },
                    modifier = Modifier.testTag("media_player_section")
                )
            }

            // 6. Right Speaker - Call Phone Audio Controls (Speech Simulator / Live Mic)
            item {
                CallAudioSection(
                    isPlaying = isCallPlaying,
                    isMicMonitoring = isMicMonitoring,
                    currentCallType = currentCallType,
                    volume = callVolume,
                    onTogglePlay = { viewModel.toggleCallAudio() },
                    onToggleMic = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                        }
                        viewModel.toggleMicMonitoring()
                    },
                    onCallTypeSelect = { viewModel.selectCallAudioType(it) },
                    onVolumeChange = { viewModel.setCallVolume(it) },
                    modifier = Modifier.testTag("call_audio_section")
                )
            }

            // 7. Speaker Isolation Test & Balance
            item {
                SpeakerBalanceSection(
                    panBalance = panBalance,
                    onBalanceChange = { viewModel.setPanBalance(it) },
                    onTestSpeaker = { viewModel.testSpeaker(it) },
                    modifier = Modifier.testTag("speaker_balance_section")
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun BluetoothEnableBanner(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFEF2F2),
        border = BorderStroke(1.dp, Color(0xFFFECACA))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BluetoothDisabled,
                        contentDescription = "Bluetooth Off",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Bluetooth Offline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF991B1B)
                    )
                    Text(
                        text = "Enable Bluetooth to stream to headphones.",
                        fontSize = 11.sp,
                        color = Color(0xFFB91C1C)
                    )
                }
            }

            Button(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("turn_on_bt_button")
            ) {
                Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

