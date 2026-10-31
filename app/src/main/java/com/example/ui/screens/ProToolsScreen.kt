package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DnsServerBenchmarkResult
import com.example.data.DualSpeedTestResult
import com.example.data.SignalHeatmapPointEntity
import com.example.data.SubnetCalculationResult
import com.example.data.TracerouteHop
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CrimsonIntrusion
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.VioletSpectrum

enum class ProToolSection {
    ALL,
    SPEED_HEATMAP,
    TRACEROUTE_DNS,
    SUBNET_CALC
}

@Composable
fun ProToolsScreen(
    dualSpeedTestResult: DualSpeedTestResult,
    signalHeatmapPoints: List<SignalHeatmapPointEntity>,
    tracerouteTarget: String,
    tracerouteHops: List<TracerouteHop>,
    isTracerouteRunning: Boolean,
    dnsLookupDomain: String,
    dnsBenchmarkResults: List<DnsServerBenchmarkResult>,
    isDnsBenchmarkRunning: Boolean,
    subnetCalcResult: SubnetCalculationResult,
    onRunDualSpeedTest: () -> Unit,
    onMeasureRoomSignal: (String) -> Unit,
    onDeleteSignalPoint: (Long) -> Unit,
    onUpdateTracerouteTarget: (String) -> Unit,
    onRunTraceroute: () -> Unit,
    onUpdateDnsDomain: (String) -> Unit,
    onRunDnsBenchmark: () -> Unit,
    onUpdateSubnetCalc: (String, Int) -> Unit
) {
    val strings = LocalAppStrings.current
    var activeSection by remember { mutableStateOf(ProToolSection.ALL) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pro_tools_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Selector Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProToolSection.entries.forEach { sec ->
                    val label = when (sec) {
                        ProToolSection.ALL -> strings.toolTabAll
                        ProToolSection.SPEED_HEATMAP -> strings.toolTabSpeedHeatmap
                        ProToolSection.TRACEROUTE_DNS -> strings.toolTabTracerouteDns
                        ProToolSection.SUBNET_CALC -> strings.toolTabSubnetCalc
                    }
                    FilterChip(
                        selected = activeSection == sec,
                        onClick = { activeSection = sec },
                        label = { Text(label) }
                    )
                }
            }
        }

        // 1. Dual SpeedTest: Internal LAN Link vs Internet WAN
        if (activeSection == ProToolSection.ALL || activeSection == ProToolSection.SPEED_HEATMAP) {
            item {
                DualSpeedTestCard(
                    result = dualSpeedTestResult,
                    onStartTest = onRunDualSpeedTest
                )
            }

            // 2. Wi-Fi Signal Heatmap & Room Mapper
            item {
                WifiSignalHeatmapCard(
                    points = signalHeatmapPoints,
                    onMeasureRoom = onMeasureRoomSignal,
                    onDeletePoint = onDeleteSignalPoint
                )
            }
        }

        // 3. Visual Hop-by-Hop Traceroute
        if (activeSection == ProToolSection.ALL || activeSection == ProToolSection.TRACEROUTE_DNS) {
            item {
                VisualTracerouteCard(
                    targetHost = tracerouteTarget,
                    hops = tracerouteHops,
                    isRunning = isTracerouteRunning,
                    onTargetChange = onUpdateTracerouteTarget,
                    onStartTraceroute = onRunTraceroute
                )
            }

            // 4. DNS Resolver Benchmark & Lookup
            item {
                DnsBenchmarkCard(
                    domain = dnsLookupDomain,
                    results = dnsBenchmarkResults,
                    isRunning = isDnsBenchmarkRunning,
                    onDomainChange = onUpdateDnsDomain,
                    onRunBenchmark = onRunDnsBenchmark
                )
            }
        }

        // 5. IPv4 / CIDR Subnetting Calculator
        if (activeSection == ProToolSection.ALL || activeSection == ProToolSection.SUBNET_CALC) {
            item {
                SubnetCalculatorCard(
                    result = subnetCalcResult,
                    onRecalculate = onUpdateSubnetCalc
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DualSpeedTestCard(
    result: DualSpeedTestResult,
    onStartTest: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = strings.dualSpeedTestHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    Text(
                        text = strings.dualSpeedTestSub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (result.isRunning) {
                LinearProgressIndicator(
                    progress = { result.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElectricCyan
                )
            }

            Text(
                text = result.phaseLabel,
                style = MaterialTheme.typography.labelMedium,
                color = CyberEmerald
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeedMetricBox(
                    modifier = Modifier.weight(1f),
                    label = strings.wifiLanInternalLabel,
                    mbps = result.lanThroughputMbps,
                    color = ElectricCyan
                )
                SpeedMetricBox(
                    modifier = Modifier.weight(1f),
                    label = strings.wanDownloadLabel,
                    mbps = result.wanDownloadMbps,
                    color = CyberEmerald
                )
                SpeedMetricBox(
                    modifier = Modifier.weight(1f),
                    label = strings.wanUploadLabel,
                    mbps = result.wanUploadMbps,
                    color = VioletSpectrum
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ping: ${result.pingMs} ms • Jitter: ${result.jitterMs} ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onStartTest,
                    enabled = !result.isRunning,
                    modifier = Modifier.testTag("start_dual_speedtest_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (result.isRunning) strings.measuringSpeedBtn else strings.startSpeedTestBtn)
                }
            }
        }
    }
}

@Composable
private fun SpeedMetricBox(
    modifier: Modifier = Modifier,
    label: String,
    mbps: Float,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${mbps.toInt()}",
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Mbps",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun WifiSignalHeatmapCard(
    points: List<SignalHeatmapPointEntity>,
    onMeasureRoom: (String) -> Unit,
    onDeletePoint: (Long) -> Unit
) {
    val strings = LocalAppStrings.current
    var customRoomName by remember { mutableStateOf("Living Room") }
    val presetRooms = listOf("Living", "Office", "Bedroom", "Kitchen", "Terrace", "Basement")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiTethering,
                    contentDescription = null,
                    tint = CyberEmerald,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = strings.signalHeatmapHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = CyberEmerald
                    )
                    Text(
                        text = strings.signalHeatmapSub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetRooms.forEach { preset ->
                    FilterChip(
                        selected = customRoomName == preset,
                        onClick = { customRoomName = preset },
                        label = { Text(preset, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customRoomName,
                    onValueChange = { customRoomName = it },
                    label = { Text(strings.roomNameInputLabel) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { onMeasureRoom(customRoomName) },
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("measure_room_signal_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.measureRoomBtn)
                }
            }

            points.forEach { pt ->
                val heatColor = when {
                    pt.rssiDbm >= -55 -> CyberEmerald
                    pt.rssiDbm >= -68 -> AmberAlert
                    else -> CrimsonIntrusion
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, heatColor.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pt.roomName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${pt.frequencyBand} • ${pt.linkSpeedMbps} Mbps • Ping ${pt.latencyMs} ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = heatColor.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${pt.rssiDbm} dBm (${pt.qualityPercent}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = heatColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeletePoint(pt.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        LinearProgressIndicator(
                            progress = { (pt.qualityPercent / 100f).coerceIn(0.08f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = heatColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = pt.recommendationRo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = heatColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisualTracerouteCard(
    targetHost: String,
    hops: List<TracerouteHop>,
    isRunning: Boolean,
    onTargetChange: (String) -> Unit,
    onStartTraceroute: () -> Unit
) {
    val strings = LocalAppStrings.current
    val quickTargets = listOf("1.1.1.1", "8.8.8.8", "cloudflare.com", "google.com")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = strings.visualTracerouteHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    Text(
                        text = strings.visualTracerouteSub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickTargets.forEach { host ->
                    FilterChip(
                        selected = targetHost == host,
                        onClick = {
                            onTargetChange(host)
                            onStartTraceroute()
                        },
                        label = { Text(host, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = targetHost,
                    onValueChange = onTargetChange,
                    label = { Text(strings.targetDomainOrIpLabel) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onStartTraceroute,
                    enabled = !isRunning,
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("run_traceroute_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isRunning) strings.tracingRouteBtn else strings.startRouteBtn)
                }
            }

            hops.forEach { hop ->
                val hopColor = when {
                    hop.isDestination -> CyberEmerald
                    hop.latencyMs <= 10 -> ElectricCyan
                    else -> AmberAlert
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(hopColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${hop.hopNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = hopColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${hop.hostname} (${hop.ipAddress})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${hop.asnOrIsp} • TTL ${hop.ttl}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = hopColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${hop.latencyMs} ms",
                            style = MaterialTheme.typography.labelMedium,
                            color = hopColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DnsBenchmarkCard(
    domain: String,
    results: List<DnsServerBenchmarkResult>,
    isRunning: Boolean,
    onDomainChange: (String) -> Unit,
    onRunBenchmark: () -> Unit
) {
    val strings = LocalAppStrings.current
    val maxTime = (results.maxOfOrNull { it.responseTimeMs } ?: 40).coerceAtLeast(10)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = VioletSpectrum,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = strings.dnsBenchmarkHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = VioletSpectrum
                    )
                    Text(
                        text = strings.dnsBenchmarkSub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = domain,
                    onValueChange = onDomainChange,
                    label = { Text(strings.queriedDomainLabel) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onRunBenchmark,
                    enabled = !isRunning,
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("run_dns_benchmark_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.runDnsBenchmarkBtn)
                }
            }

            results.forEach { item ->
                val accent = if (item.isFastest) CyberEmerald else ElectricCyan
                val barRatio = (item.responseTimeMs.toFloat() / maxTime.toFloat()).coerceIn(0.12f, 1f)
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${item.providerName} (${item.primaryIp})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.isFastest) {
                                    Surface(
                                        color = CyberEmerald.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = strings.fastestBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberEmerald,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${item.responseTimeMs} ms",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { barRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${item.resolvedIp} • ${item.securityFeatureRo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubnetCalculatorCard(
    result: SubnetCalculationResult,
    onRecalculate: (String, Int) -> Unit
) {
    val strings = LocalAppStrings.current
    var ipText by remember(result.inputIp) { mutableStateOf(result.inputIp) }
    var prefixSlider by remember(result.cidrPrefix) { mutableFloatStateOf(result.cidrPrefix.toFloat()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = AmberAlert,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = strings.subnetCalculatorHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = AmberAlert
                    )
                    Text(
                        text = strings.subnetCalculatorSub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = ipText,
                    onValueChange = {
                        ipText = it
                        onRecalculate(it, prefixSlider.toInt())
                    },
                    label = { Text(strings.baseIpv4Label) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = AmberAlert.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "/${prefixSlider.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AmberAlert,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            Slider(
                value = prefixSlider,
                onValueChange = {
                    prefixSlider = it
                    onRecalculate(ipText, it.toInt())
                },
                valueRange = 8f..30f,
                steps = 21,
                colors = SliderDefaults.colors(
                    thumbColor = AmberAlert,
                    activeTrackColor = AmberAlert
                )
            )

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SubnetRow(strings.netIdLabel, "${result.networkAddress}/${result.cidrPrefix}")
                    SubnetRow(strings.netmaskLabel, result.subnetMask)
                    SubnetRow(strings.wildcardMaskLabel, result.wildcardMask)
                    SubnetRow(strings.usableHostRangeLabel, "${result.firstUsableHost}  →  ${result.lastUsableHost}")
                    SubnetRow(strings.broadcastAddressLabel, result.broadcastAddress)
                    SubnetRow(strings.totalUsableHostsLabel, "${result.totalUsableHosts} ${strings.hostsWord}")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    SubnetRow(strings.networkClassLabel, result.networkClassRo)
                }
            }
        }
    }
}

@Composable
private fun SubnetRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
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
