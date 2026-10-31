package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiChannel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.NetworkDeviceEntity
import com.example.data.ProtocolShare
import com.example.data.TrafficSample
import com.example.data.TrustStatus
import com.example.data.WifiNetworkSnapshot
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CrimsonIntrusion
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.VioletSpectrum

@Composable
fun TrafficChartsScreen(
    trafficHistory: List<TrafficSample>,
    devices: List<NetworkDeviceEntity>,
    protocolShares: List<ProtocolShare>,
    wifiSnapshot: WifiNetworkSnapshot,
    onDeviceClick: (NetworkDeviceEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val latest = trafficHistory.lastOrNull()
    val currentDownMbps = (latest?.downloadKbps ?: 1450f) / 1000f * 8f
    val currentUpMbps = (latest?.uploadKbps ?: 420f) / 1000f * 8f
    val peakDownMbps = ((trafficHistory.maxOfOrNull { it.downloadKbps } ?: 2400f) / 1000f * 8f)
    val totalSessionGb = (devices.sumOf { it.sessionTrafficMb.toDouble() } / 1024.0).toFloat()

    val sortedByTraffic = devices.sortedByDescending { it.sessionTrafficMb }
    val maxDeviceTrafficMb = (sortedByTraffic.firstOrNull()?.sessionTrafficMb ?: 1000f).coerceAtLeast(1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("traffic_charts_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Symmetrical Equal-Height Live Throughput KPI Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThroughputKpiCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    title = strings.liveDownloadTitle,
                    value = "${"%.1f".format(currentDownMbps)} Mbps",
                    subtitle = "${(latest?.downloadKbps ?: 0f).toInt()} KB/s • ${strings.peakPrefix} ${"%.1f".format(peakDownMbps)} Mbps",
                    accentColor = ElectricCyan,
                    icon = Icons.Default.ArrowDownward
                )
                ThroughputKpiCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    title = strings.liveUploadTitle,
                    value = "${"%.1f".format(currentUpMbps)} Mbps",
                    subtitle = "${(latest?.uploadKbps ?: 0f).toInt()} KB/s • ${latest?.packetsPerSec ?: 420} pkt/s",
                    accentColor = CyberEmerald,
                    icon = Icons.Default.ArrowUpward
                )
            }
        }

        // 2. Real-Time Bandwidth Dual-Line Area Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                        RoundedCornerShape(18.dp)
                    ),
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = strings.realtimeSubnetTrafficHeader,
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.realtimeSubnetTrafficSub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.8.dp,
                                CyberEmerald.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = "${strings.sessionTotalPrefix}: ${"%.2f".format(totalSessionGb)} GB",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Symmetrical 2-Column Legend Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendIndicator(
                            modifier = Modifier.weight(1f),
                            color = ElectricCyan,
                            label = strings.downloadLegend
                        )
                        LegendIndicator(
                            modifier = Modifier.weight(1f),
                            color = CyberEmerald,
                            label = strings.uploadLegend
                        )
                    }

                    LiveTrafficAreaCanvas(
                        samples = trafficHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    )
                }
            }
        }

        // 3. Protocol & Service Traffic Distribution Donut Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        0.8.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataUsage,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = strings.protocolDistributionHeader,
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val sliceColors = listOf(
                        ElectricCyan,
                        CyberEmerald,
                        VioletSpectrum,
                        AmberAlert,
                        CrimsonIntrusion
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(126.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 20.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                                var startAngle = -90f
                                protocolShares.forEachIndexed { idx, share ->
                                    val sweep = share.percentage * 360f
                                    drawArc(
                                        color = sliceColors[idx % sliceColors.size],
                                        startAngle = startAngle,
                                        sweepAngle = (sweep - 3f).coerceAtLeast(2f),
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = Size(diameter, diameter),
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                    )
                                    startAngle += sweep
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${"%.1f".format(totalSessionGb)} GB",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = strings.sessionLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            protocolShares.forEachIndexed { idx, share ->
                                val c = sliceColors[idx % sliceColors.size]
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(9.dp)
                                                        .background(c, CircleShape)
                                                )
                                                Text(
                                                    text = share.protocolName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = "${(share.percentage * 100).toInt()}% • ${"%.0f".format(share.megabytesTotal)} MB",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = c,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { share.percentage.coerceIn(0.05f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = c,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Wi-Fi Channel Spectrum & SNR Curve
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        0.8.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiChannel,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = strings.wifiSpectrumHeader,
                                style = MaterialTheme.typography.labelLarge,
                                color = CyberEmerald
                            )
                            Text(
                                text = "${wifiSnapshot.ssid} • ${strings.channelWord} ${wifiSnapshot.channelNumber} (${wifiSnapshot.frequencyMHz} MHz) • ${wifiSnapshot.rssiDbm} dBm",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    WifiSpectrumCurveCanvas(
                        activeRssi = wifiSnapshot.rssiDbm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }
        }

        // 5. Per-Device Bandwidth Ranking
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = strings.deviceBandwidthRankingHeader,
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
            }
        }

        items(sortedByTraffic, key = { it.macAddress }) { device ->
            val ratio = (device.sessionTrafficMb / maxDeviceTrafficMb).coerceIn(0.06f, 1f)
            val barColor = when (device.trustEnum) {
                TrustStatus.SUSPICIOUS_FLAGGED -> CrimsonIntrusion
                TrustStatus.UNKNOWN_NEW -> AmberAlert
                TrustStatus.TRUSTED -> ElectricCyan
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceClick(device) }
                    .border(
                        0.8.dp,
                        barColor.copy(alpha = 0.3f),
                        RoundedCornerShape(14.dp)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        ) {
                            Text(
                                text = device.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${device.ipAddress} • ↓ ${device.currentDownloadKbps.toInt()} KB/s | ↑ ${device.currentUploadKbps.toInt()} KB/s",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            color = barColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${"%.1f".format(device.sessionTrafficMb)} MB",
                                style = MaterialTheme.typography.labelLarge,
                                color = barColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThroughputKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier.border(
            1.dp,
            accentColor.copy(alpha = 0.38f),
            RoundedCornerShape(16.dp)
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LegendIndicator(
    modifier: Modifier = Modifier,
    color: Color,
    label: String
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LiveTrafficAreaCanvas(
    samples: List<TrafficSample>,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padBottom = 12.dp.toPx()
        val chartHeight = h - padBottom

        for (i in 0..3) {
            val y = chartHeight * (i / 3f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (samples.size < 2) return@Canvas

        val maxRate = (samples.maxOfOrNull { maxOf(it.downloadKbps, it.uploadKbps) } ?: 3000f)
            .coerceAtLeast(500f) * 1.15f

        fun buildSeriesPaths(selector: (TrafficSample) -> Float): Pair<Path, Path> {
            val strokePath = Path()
            val fillPath = Path()
            val stepX = w / (samples.size - 1).coerceAtLeast(1)

            samples.forEachIndexed { idx, sample ->
                val x = idx * stepX
                val norm = (selector(sample) / maxRate).coerceIn(0.02f, 0.98f)
                val y = chartHeight * (1f - norm)
                if (idx == 0) {
                    strokePath.moveTo(x, y)
                    fillPath.moveTo(x, chartHeight)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = (idx - 1) * stepX
                    val prevNorm = (selector(samples[idx - 1]) / maxRate).coerceIn(0.02f, 0.98f)
                    val prevY = chartHeight * (1f - prevNorm)
                    val ctrlX = (prevX + x) / 2f
                    strokePath.cubicTo(ctrlX, prevY, ctrlX, y, x, y)
                    fillPath.cubicTo(ctrlX, prevY, ctrlX, y, x, y)
                }
                if (idx == samples.lastIndex) {
                    fillPath.lineTo(x, chartHeight)
                    fillPath.close()
                }
            }
            return strokePath to fillPath
        }

        val (downStroke, downFill) = buildSeriesPaths { it.downloadKbps }
        val (upStroke, upFill) = buildSeriesPaths { it.uploadKbps }

        drawPath(
            path = downFill,
            brush = Brush.verticalGradient(
                colors = listOf(ElectricCyan.copy(alpha = 0.36f), ElectricCyan.copy(alpha = 0.02f)),
                startY = 0f,
                endY = chartHeight
            )
        )
        drawPath(
            path = downStroke,
            color = ElectricCyan,
            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
        )

        drawPath(
            path = upFill,
            brush = Brush.verticalGradient(
                colors = listOf(CyberEmerald.copy(alpha = 0.28f), CyberEmerald.copy(alpha = 0.01f)),
                startY = 0f,
                endY = chartHeight
            )
        )
        drawPath(
            path = upStroke,
            color = CyberEmerald,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun WifiSpectrumCurveCanvas(
    activeRssi: Int,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawLine(
            color = gridColor,
            start = Offset(0f, h - 4.dp.toPx()),
            end = Offset(w, h - 4.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )

        fun drawChannelBell(centerFrac: Float, widthFrac: Float, heightFrac: Float, color: Color) {
            val cx = w * centerFrac
            val halfW = w * widthFrac
            val peakY = h * (1f - heightFrac)
            val baseY = h - 4.dp.toPx()

            val path = Path().apply {
                moveTo(cx - halfW, baseY)
                cubicTo(cx - halfW * 0.45f, baseY, cx - halfW * 0.35f, peakY, cx, peakY)
                cubicTo(cx + halfW * 0.35f, peakY, cx + halfW * 0.45f, baseY, cx + halfW, baseY)
                close()
            }
            drawPath(
                path = path,
                color = color.copy(alpha = 0.22f)
            )
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        val activeHeight = ((activeRssi + 100) / 70f).coerceIn(0.45f, 0.92f)
        drawChannelBell(0.22f, 0.16f, 0.42f, AmberAlert)
        drawChannelBell(0.78f, 0.15f, 0.36f, VioletSpectrum)
        drawChannelBell(0.52f, 0.22f, activeHeight, CyberEmerald)
    }
}
