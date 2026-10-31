package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DeviceCategory
import com.example.data.NetworkDeviceEntity
import com.example.data.TrustStatus
import com.example.data.WifiNetworkSnapshot
import com.example.ui.DeviceSortOption
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CrimsonIntrusion
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.ElectricCyan
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RadarScannerScreen(
    wifiSnapshot: WifiNetworkSnapshot,
    allDevices: List<NetworkDeviceEntity>,
    filteredDevices: List<NetworkDeviceEntity>,
    isScanning: Boolean,
    scanProgress: Float,
    scanStatusText: String,
    searchQuery: String,
    selectedTrustFilter: TrustStatus?,
    selectedSortOption: DeviceSortOption,
    hasLocationPermission: Boolean,
    onRequestPermissions: () -> Unit,
    onTriggerScan: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectTrustFilter: (TrustStatus?) -> Unit,
    onSelectSortOption: (DeviceSortOption) -> Unit,
    onDeviceClick: (NetworkDeviceEntity) -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val unknownDevicesCount = allDevices.count { it.trustEnum != TrustStatus.TRUSTED }
    val suspiciousCount = allDevices.count { it.trustEnum == TrustStatus.SUSPICIOUS_FLAGGED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("radar_scanner_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Radar & Live Wi-Fi Telemetry Card
        item {
            WifiRadarHeroCard(
                wifiSnapshot = wifiSnapshot,
                devices = allDevices,
                isScanning = isScanning,
                scanProgress = scanProgress,
                scanStatusText = scanStatusText,
                hasLocationPermission = hasLocationPermission,
                onRequestPermissions = onRequestPermissions,
                onTriggerScan = onTriggerScan
            )
        }

        // 2. Real-time Unknown / Intruding Device Alert Banner
        if (unknownDevicesCount > 0) {
            item {
                IntrusionAlertBanner(
                    unknownCount = unknownDevicesCount,
                    suspiciousCount = suspiciousCount,
                    onReviewAlerts = onNavigateToAlerts
                )
            }
        }

        // 3. Search Bar + Trust Filter Chips + Sort Options
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("device_search_input"),
                    placeholder = {
                        Text("Caută după IP, MAC, producător OUI, OS sau port (ex. 554, Apple)…")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Caută dispozitiv",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Șterge căutarea"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Filter by Trust Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedTrustFilter == null,
                        onClick = { onSelectTrustFilter(null) },
                        label = { Text("Toate (${allDevices.size})") },
                        modifier = Modifier.testTag("filter_all_chip")
                    )
                    FilterChip(
                        selected = selectedTrustFilter == TrustStatus.SUSPICIOUS_FLAGGED,
                        onClick = { onSelectTrustFilter(TrustStatus.SUSPICIOUS_FLAGGED) },
                        label = {
                            Text("Suspecte (${allDevices.count { it.trustEnum == TrustStatus.SUSPICIOUS_FLAGGED }})")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = CrimsonIntrusion,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_suspicious_chip")
                    )
                    FilterChip(
                        selected = selectedTrustFilter == TrustStatus.UNKNOWN_NEW,
                        onClick = { onSelectTrustFilter(TrustStatus.UNKNOWN_NEW) },
                        label = {
                            Text("Necunoscute (${allDevices.count { it.trustEnum == TrustStatus.UNKNOWN_NEW }})")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AmberAlert,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_unknown_chip")
                    )
                    FilterChip(
                        selected = selectedTrustFilter == TrustStatus.TRUSTED,
                        onClick = { onSelectTrustFilter(TrustStatus.TRUSTED) },
                        label = {
                            Text("De Încredere (${allDevices.count { it.trustEnum == TrustStatus.TRUSTED }})")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_trusted_chip")
                    )
                }

                // Sort row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Sortare",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Ordine:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DeviceSortOption.entries.forEach { option ->
                        FilterChip(
                            selected = selectedSortOption == option,
                            onClick = { onSelectSortOption(option) },
                            label = { Text(option.labelRo, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // 4. Discovered LAN Devices Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APARATE CONECTATE ÎN TIMP REAL (${filteredDevices.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Apasă pentru inspecție tehnică",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 5. Device Cards
        items(filteredDevices, key = { it.macAddress }) { device ->
            NetworkDeviceTechnicalCard(
                device = device,
                onClick = { onDeviceClick(device) }
            )
        }

        // 6. About / Author Card
        item {
            AboutDeveloperCard()
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AboutDeveloperCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("about_developer_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "About NetPulse Pro",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ABOUT NETPULSE PRO • v1.0 ENTERPRISE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "CREATED BY: ANDREI ADRIAN BUTA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Scaner Profesional Wi-Fi & LAN, Audit Securitate și Telemetrie în Timp Real",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun WifiRadarHeroCard(
    wifiSnapshot: WifiNetworkSnapshot,
    devices: List<NetworkDeviceEntity>,
    isScanning: Boolean,
    scanProgress: Float,
    scanStatusText: String,
    hasLocationPermission: Boolean,
    onRequestPermissions: () -> Unit,
    onTriggerScan: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isScanning) 1800 else 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.ic_network_radar_hero),
                contentDescription = "Ilustrație topologie radar rețea",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.35f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Wi-Fi Activ",
                                tint = CyberEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = wifiSnapshot.ssid,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "BSSID: ${wifiSnapshot.bssid} • ${wifiSnapshot.bandLabel} (CH ${wifiSnapshot.channelNumber})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Animated Live Topology Radar Mini-Canvas
                    Box(
                        modifier = Modifier.size(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRadius = size.minDimension / 2f - 4.dp.toPx()

                            // Concentric radar rings
                            drawCircle(
                                color = ElectricCyan.copy(alpha = 0.22f),
                                radius = maxRadius,
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            drawCircle(
                                color = ElectricCyan.copy(alpha = 0.30f),
                                radius = maxRadius * 0.62f,
                                center = center,
                                style = Stroke(width = 1.dp.toPx())
                            )
                            drawCircle(
                                color = ElectricCyan.copy(alpha = 0.40f),
                                radius = maxRadius * 0.28f,
                                center = center,
                                style = Stroke(width = 1.dp.toPx())
                            )

                            // Sweeping Radar Beam
                            val rad = Math.toRadians(radarAngle.toDouble())
                            val beamEnd = Offset(
                                x = center.x + (maxRadius * cos(rad)).toFloat(),
                                y = center.y + (maxRadius * sin(rad)).toFloat()
                            )
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(ElectricCyan, ElectricCyan.copy(alpha = 0.05f)),
                                    start = center,
                                    end = beamEnd
                                ),
                                start = center,
                                end = beamEnd,
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Plot discovered devices around the center gateway
                            devices.forEachIndexed { idx, dev ->
                                if (dev.isGateway) {
                                    drawCircle(
                                        color = ElectricCyan,
                                        radius = 4.5.dp.toPx(),
                                        center = center
                                    )
                                } else {
                                    val nodeAngle = Math.toRadians((idx * 51.0 + 25.0))
                                    val distFactor = ((dev.latencyMs.coerceIn(1, 30)) / 34f).coerceIn(0.32f, 0.88f)
                                    val pt = Offset(
                                        x = center.x + (maxRadius * distFactor * cos(nodeAngle)).toFloat(),
                                        y = center.y + (maxRadius * distFactor * sin(nodeAngle)).toFloat()
                                    )
                                    val dotColor = when (dev.trustEnum) {
                                        TrustStatus.SUSPICIOUS_FLAGGED -> CrimsonIntrusion
                                        TrustStatus.UNKNOWN_NEW -> AmberAlert
                                        TrustStatus.TRUSTED -> CyberEmerald
                                    }
                                    drawCircle(
                                        color = dotColor,
                                        radius = 3.8.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }
                }

                // Technical Subnet Specs Grid
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TechMetricPill(label = "IP LOCAL", value = wifiSnapshot.localIpv4)
                            TechMetricPill(label = "GATEWAY", value = wifiSnapshot.gatewayIp)
                            TechMetricPill(label = "SUBREȚEA", value = wifiSnapshot.subnetCidr)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TechMetricPill(
                                label = "SEMNAL RSSI",
                                value = "${wifiSnapshot.rssiDbm} dBm (${wifiSnapshot.signalQualityPercent}%)"
                            )
                            TechMetricPill(
                                label = "VITEZĂ LINK",
                                value = "${wifiSnapshot.linkSpeedMbps} Mbps"
                            )
                            TechMetricPill(
                                label = "INTERFAȚĂ",
                                value = "${wifiSnapshot.interfaceName} • MTU ${wifiSnapshot.mtuSize}"
                            )
                        }
                        Text(
                            text = "DNS Activ: ${wifiSnapshot.dnsServers} • Criptare: ${wifiSnapshot.securityProtocol}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(visible = isScanning) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ElectricCyan,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Text(
                            text = scanStatusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onTriggerScan,
                        enabled = !isScanning,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("scan_network_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.Radar else Icons.Default.Refresh,
                            contentDescription = "Scanează Rețeaua",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScanning) "Scanare în curs…" else "Scanează Subrețeaua LAN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!hasLocationPermission) {
                        OutlinedButton(
                            onClick = onRequestPermissions,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("grant_wifi_permission_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Permisiune SSID",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SSID Exact", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechMetricPill(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun IntrusionAlertBanner(
    unknownCount: Int,
    suspiciousCount: Int,
    onReviewAlerts: () -> Unit
) {
    val bannerColor = if (suspiciousCount > 0) CrimsonIntrusion else AmberAlert
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReviewAlerts() }
            .testTag("intrusion_alert_banner"),
        color = bannerColor.copy(alpha = 0.14f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, bannerColor.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(bannerColor.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Alertă Securitate",
                    tint = bannerColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alertă: $unknownCount Dispozitive Necunoscute pe Wi-Fi",
                    style = MaterialTheme.typography.titleMedium,
                    color = bannerColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (suspiciousCount > 0) {
                        "$suspiciousCount aparat prezintă porturi vulnerabile deschise (RTSP/Telnet). Apasă pentru izolare și audit."
                    } else {
                        "S-au detectat aparate neautorizate conectate la subrețea. Verifică amprentele MAC și OUI."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NetworkDeviceTechnicalCard(
    device: NetworkDeviceEntity,
    onClick: () -> Unit
) {
    val trustColor = when (device.trustEnum) {
        TrustStatus.TRUSTED -> CyberEmerald
        TrustStatus.UNKNOWN_NEW -> AmberAlert
        TrustStatus.SUSPICIOUS_FLAGGED -> CrimsonIntrusion
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (device.trustEnum != TrustStatus.TRUSTED) 1.2.dp else 0.8.dp,
                color = trustColor.copy(alpha = if (device.trustEnum != TrustStatus.TRUSTED) 0.65f else 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("device_card_${device.ipAddress}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Category Icon, Name + Hostname, Trust Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(trustColor.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon(device.categoryEnum),
                            contentDescription = device.categoryEnum.labelRo,
                            tint = trustColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = device.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (device.isGateway) {
                                Surface(
                                    color = ElectricCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "GATEWAY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (device.isLocalDevice) {
                                Surface(
                                    color = CyberEmerald.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "TU",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberEmerald,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${device.vendorOui} • ${device.roomLocation}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = trustColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, trustColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = device.trustEnum.labelRo,
                        style = MaterialTheme.typography.labelSmall,
                        color = trustColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Middle Row: Monospace IP, MAC, OS, Ping, Signal, Live Speed
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "IPv4: ${device.ipAddress}",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MAC: ${device.macAddress}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "OS: ${device.osFingerprint}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${device.latencyMs} ms • ${device.signalRssiDbm} dBm",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (device.latencyMs <= 12) CyberEmerald else AmberAlert
                        )
                    }
                }
            }

            // Bottom Row: Open Ports Badges + Current Traffic Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    device.parsedPorts.take(4).forEach { portInfo ->
                        val portBadgeColor = when (portInfo.riskLevel) {
                            "CRITIC" -> CrimsonIntrusion
                            "MEDIU" -> AmberAlert
                            else -> ElectricCyan
                        }
                        Surface(
                            color = portBadgeColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${portInfo.port}/${portInfo.serviceName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = portBadgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Trafic curent",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "↓${device.currentDownloadKbps.toInt()} ↑${device.currentUploadKbps.toInt()} KB/s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

fun categoryIcon(category: DeviceCategory): ImageVector = when (category) {
    DeviceCategory.ROUTER_GATEWAY -> Icons.Default.Router
    DeviceCategory.SMARTPHONE -> Icons.Default.Smartphone
    DeviceCategory.WORKSTATION_PC -> Icons.Default.Computer
    DeviceCategory.SMART_TV_MEDIA -> Icons.Default.Tv
    DeviceCategory.IOT_SENSOR -> Icons.Default.Memory
    DeviceCategory.NAS_SERVER -> Icons.Default.Dns
    DeviceCategory.PRINTER -> Icons.Default.Print
    DeviceCategory.SECURITY_CAMERA -> Icons.Default.CameraAlt
    DeviceCategory.UNKNOWN -> Icons.Default.Warning
}
