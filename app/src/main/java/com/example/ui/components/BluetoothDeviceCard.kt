package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BluetoothDeviceItem
import com.example.model.ConnectionStatus
import com.example.model.DeviceType
import com.example.ui.theme.PolishBlue
import com.example.ui.theme.PolishBlueContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.SuccessEmeraldBg

@Composable
fun BluetoothDeviceSection(
    devices: List<BluetoothDeviceItem>,
    connectedDevice: BluetoothDeviceItem?,
    isScanning: Boolean,
    onDeviceClick: (BluetoothDeviceItem) -> Unit,
    onDisconnectClick: (BluetoothDeviceItem) -> Unit,
    onRefreshClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header (Matching HTML design: text-xs font-bold uppercase tracking-wider text-slate-400)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PAIRED DEVICES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Slate400
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Scanning / Ready Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isScanning) PolishBlueContainer else SuccessEmeraldBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isScanning) "SCANNING..." else "READY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = if (isScanning) PolishBlue else Color(0xFF15803D)
                    )
                }

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("refresh_devices_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Devices",
                        tint = Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettingsClick,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("open_bt_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Bluetooth Settings",
                        tint = Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (devices.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Slate200)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No paired devices found. Pair a Bluetooth headset/speaker in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                devices.forEach { device ->
                    BluetoothDeviceItemRow(
                        device = device,
                        isConnected = device.address == connectedDevice?.address || device.status == ConnectionStatus.CONNECTED,
                        onConnect = { onDeviceClick(device) },
                        onDisconnect = { onDisconnectClick(device) }
                    )
                }
            }
        }
    }
}

@Composable
fun BluetoothDeviceItemRow(
    device: BluetoothDeviceItem,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnecting = device.status == ConnectionStatus.CONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // White Card with Rounded-3xl (24.dp) and subtle shadow & border
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isConnected) 3.dp else 1.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isConnected) PolishBlue.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.04f)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                if (isConnected) onDisconnect() else onConnect()
            }
            .testTag("device_item_${device.address}"),
        shape = RoundedCornerShape(24.dp),
        color = if (isConnected) Color.White else Color.White.copy(alpha = 0.85f),
        border = BorderStroke(
            1.dp,
            if (isConnected) PolishBlue.copy(alpha = 0.4f) else Slate200.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Squircle Device Icon (w-12 h-12 bg-slate-100 rounded-2xl)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isConnected) PolishBlueContainer else Slate100),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (device.deviceType) {
                        DeviceType.EARBUDS -> Icons.Default.Headphones
                        DeviceType.HEADPHONES -> Icons.Default.Headphones
                        DeviceType.SPEAKER -> Icons.Default.Speaker
                        DeviceType.CAR_AUDIO -> Icons.Default.DirectionsCar
                        DeviceType.HEADSET -> Icons.Default.Headset
                        DeviceType.PHONE -> Icons.Default.PhoneAndroid
                        DeviceType.GENERIC -> Icons.Default.Bluetooth
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = device.deviceType.name,
                        tint = if (isConnected) PolishBlue else Slate800,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        if (device.batteryPercent != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.BatteryFull,
                                    contentDescription = "Battery",
                                    tint = SuccessEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${device.batteryPercent}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SuccessEmerald
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isConnected) "Connected" else "Previously paired",
                            fontSize = 12.sp,
                            fontWeight = if (isConnected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isConnected) PolishBlue else Slate500
                        )

                        if (isConnected) {
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                            Text(
                                text = "A2DP Active",
                                fontSize = 11.sp,
                                color = SuccessEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Connection Indicator or Action Button
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = PolishBlue
                )
            } else if (isConnected) {
                // Pulsing dot indicator matching HTML design
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(PolishBlue.copy(alpha = alphaAnim))
                )
            } else {
                OutlinedButton(
                    onClick = onConnect,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate200),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Slate800
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("connect_btn_${device.address}")
                ) {
                    Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

