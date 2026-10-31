package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.NetworkDeviceEntity
import com.example.data.ProtocolShare
import com.example.data.TrafficSample
import com.example.data.TrustStatus
import com.example.data.WifiNetworkSnapshot
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
        // 1. Live Throughput KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThroughputKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "DOWNLOAD LIVE",
                    value = "${"%.1f".format(currentDownMbps)} Mbps",
                    subtitle = "${(latest?.downloadKbps ?: 0f).toInt()} KB/s • Vârf ${"%.1f".format(peakDownMbps)} Mbps",
                    accentColor = ElectricCyan,
                    icon = Icons.Default.ArrowDownward
                )
                ThroughputKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "UPLOAD LIVE",
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
                        Column {
                            Text(
                                text = "FLUX TRAFIC SUBREȚEA ÎN TIMP REAL",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                            Text(
                                text = "Monitorizare continuă pachete RX/TX (TrafficStats + Sonde LAN)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Total: ${"%.2f".format(totalSessionGb)} GB",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberEmerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendIndicator(color = ElectricCyan, label = "Download (RX KB/s)")
                        LegendIndicator(color = CyberEmerald, label = "Upload (TX KB/s)")
                    }

                    // Custom Canvas Area Chart
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
                modifier = Modifier.fillMaxWidth(),
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
                            text = "DISTRIBUȚIE CONSUM PE PROTOCOALE & SERVICII",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
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
                            modifier = Modifier.size(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 22.dp.toPx()
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sesiune",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            protocolShares.forEachIndexed { idx, share ->
                                val c = sliceColors[idx % sliceColors.size]
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(c, CircleShape)
                                            )
                                            Text(
                                                text = share.protocolName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "${(share.percentage * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = c,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "${"%.0f".format(share.megabytesTotal)} MB • ${share.descriptionRo}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
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
                modifier = Modifier.fillMaxWidth(),
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
                                text = "ANALIZĂ SPECTRU RADIO & CONGESTIE CANAL WI-FI",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyberEmerald
                            )
                            Text(
                                text = "${wifiSnapshot.ssid} pe Canalul ${wifiSnapshot.channelNumber} (${wifiSnapshot.frequencyMHz} MHz) • ${wifiSnapshot.rssiDbm} dBm",
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
                    text = "CLASAMENT CONSUM TRAFIC PE DISPOZITIVE",
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
                    .clickable { onDeviceClick(device) },
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
                        Column(modifier = Modifier.weight(1f)) {
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${"%.1f".format(device.sessionTrafficMb)} MB",
                            style = MaterialTheme.typography.titleMedium,
                            color = barColor,
                            fontWeight = FontWeight.Bold
                        )
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
            accentColor.copy(alpha = 0.35f),
            RoundedCornerShape(16.dp)
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LegendIndicator(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

        // Horizontal reference grid lines
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
        // Neighbouring channels (low interference)
        drawChannelBell(0.22f, 0.16f, 0.42f, AmberAlert)
        drawChannelBell(0.78f, 0.15f, 0.36f, VioletSpectrum)
        // Primary active Wi-Fi 6 channel
        drawChannelBell(0.52f, 0.22f, activeHeight, CyberEmerald)
    }
}
