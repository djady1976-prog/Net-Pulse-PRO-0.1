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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.NetworkDeviceEntity
import com.example.data.RouterArpSecurityAudit
import com.example.data.TrustStatus
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CrimsonIntrusion
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.ElectricCyan

@Composable
fun SecurityAlertsScreen(
    devices: List<NetworkDeviceEntity>,
    routerArpAudit: RouterArpSecurityAudit,
    alertOnUnknownDevices: Boolean,
    alertOnVulnerablePorts: Boolean,
    continuousProtectionEnabled: Boolean,
    onToggleAlertUnknown: (Boolean) -> Unit,
    onToggleAlertPorts: (Boolean) -> Unit,
    onToggleContinuousProtection: (Boolean) -> Unit,
    onAuthorizeDevice: (NetworkDeviceEntity) -> Unit,
    onFlagDeviceSuspicious: (NetworkDeviceEntity) -> Unit,
    onToggleWatchdog: (NetworkDeviceEntity, Boolean) -> Unit,
    onTestPushNotification: () -> Unit,
    onInspectDevice: (NetworkDeviceEntity) -> Unit
) {
    val unknownDevices = devices.filter { it.trustEnum != TrustStatus.TRUSTED }
    val suspiciousCount = devices.count { it.trustEnum == TrustStatus.SUSPICIOUS_FLAGGED }
    val devicesWithRiskyPorts = devices.filter { it.hasHighRiskPorts }

    val rawScore = 100 -
        (suspiciousCount * 22) -
        ((unknownDevices.size - suspiciousCount).coerceAtLeast(0) * 10) -
        (devicesWithRiskyPorts.size * 6) -
        (if (routerArpAudit.arpSpoofingDetected) 25 else 0)
    val securityScore = rawScore.coerceIn(18, 100)
    val scoreColor = when {
        securityScore >= 85 -> CyberEmerald
        securityScore >= 60 -> AmberAlert
        else -> CrimsonIntrusion
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("security_alerts_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cyber Security Score & Posture Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, scoreColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val trackColor = MaterialTheme.colorScheme.surface
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 10.dp.toPx()
                                val diameter = size.minDimension - stroke
                                val topLeft = Offset(stroke / 2f, stroke / 2f)
                                drawArc(
                                    color = trackColor,
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(diameter, diameter),
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = scoreColor,
                                    startAngle = 135f,
                                    sweepAngle = 270f * (securityScore / 100f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(diameter, diameter),
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$securityScore",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = scoreColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "/ 100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = when {
                                    securityScore >= 85 -> "Rețea Bine Securizată"
                                    securityScore >= 60 -> "Atenție: Aparate Neautorizate"
                                    else -> "Risc Ridicat de Intruziune LAN"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = scoreColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${unknownDevices.size} dispozitive necunoscute/suspecte și ${devicesWithRiskyPorts.size} aparate cu porturi vulnerabile expuse pe Wi-Fi.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onTestPushNotification,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_push_notification_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testează Alerta Push în Bara de Stare Android")
                    }
                }
            }
        }

        // 2. Active Router, DNS Hijacking & ARP Spoofing Detector Card
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
                            imageVector = Icons.Default.Router,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "DETECTOR ARP SPOOFING (MitM) & AUDIT ROUTER",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                    }

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
                            AuditStatusRow(
                                label = "Protecție ARP Spoofing",
                                value = routerArpAudit.duplicateMacDetails,
                                isOk = !routerArpAudit.arpSpoofingDetected
                            )
                            AuditStatusRow(
                                label = "Integritate DNS Hijacking",
                                value = routerArpAudit.dnsIntegrityStatusRo,
                                isOk = true
                            )
                            AuditStatusRow(
                                label = "Porturi Admin Router Expuse",
                                value = if (routerArpAudit.routerAdminExposedPorts.isEmpty()) "Niciunul"
                                else routerArpAudit.routerAdminExposedPorts.joinToString(", ") { "TCP:$it" },
                                isOk = !routerArpAudit.upnpExposed
                            )
                        }
                    }

                    Text(
                        text = routerArpAudit.overallRouterAdviceRo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (routerArpAudit.arpSpoofingDetected) CrimsonIntrusion else AmberAlert
                    )
                }
            }
        }

        // 3. Dedicated Hidden Camera / SpyCam Finder (RTSP & ONVIF)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (routerArpAudit.spyCamerasDetected.any { it.trustEnum != TrustStatus.TRUSTED })
                            CrimsonIntrusion.copy(alpha = 0.6f)
                        else CyberEmerald.copy(alpha = 0.4f),
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
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = CrimsonIntrusion,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "DETECTOR CAMERE ASCUNSE (SPYCAM & RTSP/ONVIF)",
                                style = MaterialTheme.typography.labelLarge,
                                color = CrimsonIntrusion
                            )
                            Text(
                                text = "${routerArpAudit.spyCamerasDetected.size} dispozitiv(e) video identificat(e) pe subrețeaua Wi-Fi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    routerArpAudit.spyCamerasDetected.forEach { cam ->
                        val isAuthorized = cam.trustEnum == TrustStatus.TRUSTED
                        val badgeCol = if (isAuthorized) CyberEmerald else CrimsonIntrusion
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInspectDevice(cam) },
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${cam.displayName} (${cam.ipAddress})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "MAC: ${cam.macAddress} • ${cam.vendorOui}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Flux: ${cam.mdnsServices} • Upload: ${cam.currentUploadKbps.toInt()} KB/s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeCol
                                    )
                                }
                                Surface(
                                    color = badgeCol.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isAuthorized) "VERIFICATĂ" else "SUSPECTĂ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeCol,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Quarantine & Unknown Devices Alert Queue
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (unknownDevices.isEmpty()) Icons.Default.GppGood else Icons.Default.GppBad,
                    contentDescription = null,
                    tint = if (unknownDevices.isEmpty()) CyberEmerald else CrimsonIntrusion,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "COADĂ CARANTINĂ DISPOZITIVE NECUNOSCUTE (${unknownDevices.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (unknownDevices.isEmpty()) CyberEmerald else CrimsonIntrusion
                )
            }
        }

        if (unknownDevices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "Toate dispozitivele conectate sunt autorizate",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberEmerald,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Niciun aparat necunoscut nu este activ în acest moment pe subrețeaua Wi-Fi.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(unknownDevices, key = { "alert_${it.macAddress}" }) { device ->
                val isSuspicious = device.trustEnum == TrustStatus.SUSPICIOUS_FLAGGED
                val accent = if (isSuspicious) CrimsonIntrusion else AmberAlert

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                        .clickable { onInspectDevice(device) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(accent.copy(alpha = 0.18f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSuspicious) Icons.Default.Error else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "${device.displayName} (${device.ipAddress})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "MAC: ${device.macAddress} • ${device.vendorOui}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent
                                )
                            }
                        }

                        Text(
                            text = device.securityNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onAuthorizeDevice(device) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("authorize_${device.ipAddress}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberEmerald,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Marchează De Încredere", style = MaterialTheme.typography.labelMedium)
                            }

                            if (!isSuspicious) {
                                OutlinedButton(
                                    onClick = { onFlagDeviceSuspicious(device) },
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Marchează Suspect", style = MaterialTheme.typography.labelMedium, color = CrimsonIntrusion)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Watchdog 24/7 Uptime Monitor for Critical Hosts
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
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "WATCHDOG 24/7 & MONITORIZARE UPTIME SERVERE",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyberEmerald
                            )
                            Text(
                                text = "Alertează automat dacă un echipament critic (Router, NAS, Cameră) devine offline",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    devices.take(5).forEach { dev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${dev.displayName} (${dev.ipAddress})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Uptime: ${"%.2f".format(dev.uptimePercent)}% • Ping ${dev.latencyMs} ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (dev.isWatchdogEnabled) CyberEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = dev.isWatchdogEnabled,
                                onCheckedChange = { onToggleWatchdog(dev, it) }
                            )
                        }
                    }
                }
            }
        }

        // 6. Exposed Ports & Vulnerability Advisories
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = AmberAlert,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "AUDIT PORTURI DESCHISE ȘI VULNERABILITĂȚI LAN",
                    style = MaterialTheme.typography.labelLarge,
                    color = AmberAlert
                )
            }
        }

        items(devicesWithRiskyPorts, key = { "vuln_${it.macAddress}" }) { dev ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInspectDevice(dev) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${dev.displayName} (${dev.ipAddress})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Inspectează →",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan
                        )
                    }
                    dev.parsedPorts.filter { it.riskLevel != "SCĂZUT" }.forEach { port ->
                        val riskCol = if (port.riskLevel == "CRITIC") CrimsonIntrusion else AmberAlert
                        Surface(
                            color = riskCol.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Port ${port.port}/${port.serviceName}: ${port.banner}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = port.riskLevel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = riskCol,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Real-Time Alert Configuration Toggles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = ElectricCyan
                        )
                        Text(
                            text = "POLITICI DE ALERTARE AUTOMATĂ",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    AlertPolicyToggleRow(
                        title = "Alertă la Dispozitive Noi (MAC Necunoscut)",
                        subtitle = "Notifică instant când o adresă MAC neînregistrată primește IP prin DHCP",
                        checked = alertOnUnknownDevices,
                        onCheckedChange = onToggleAlertUnknown
                    )
                    AlertPolicyToggleRow(
                        title = "Detecție Porturi Critice (Telnet, RTSP, SMB)",
                        subtitle = "Avertizează dacă un dispozitiv expune servicii necriptate în subrețea",
                        checked = alertOnVulnerablePorts,
                        onCheckedChange = onToggleAlertPorts
                    )
                    AlertPolicyToggleRow(
                        title = "Gardian ARP & Monitorizare Continuă",
                        subtitle = "Verifică periodic integritatea adresei MAC a gateway-ului împotriva ARP Spoofing",
                        checked = continuousProtectionEnabled,
                        onCheckedChange = onToggleContinuousProtection
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
private fun AuditStatusRow(
    label: String,
    value: String,
    isOk: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = if (isOk) CyberEmerald else AmberAlert,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun AlertPolicyToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
