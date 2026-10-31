package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.NetworkDeviceEntity
import com.example.data.PingDiagnosticResult
import com.example.data.TrustStatus
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CrimsonIntrusion
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.ElectricCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeviceInspectorSheet(
    device: NetworkDeviceEntity,
    pingResult: PingDiagnosticResult?,
    isPortAuditRunning: Boolean,
    onDismiss: () -> Unit,
    onUpdateTrust: (TrustStatus) -> Unit,
    onToggleWatchdog: (Boolean) -> Unit,
    onSaveIdentity: (String, String, String) -> Unit,
    onRunPortAudit: () -> Unit,
    onStartPing: () -> Unit,
    onSendWakeOnLan: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isEditingIdentity by remember(device.macAddress) { mutableStateOf(false) }
    var aliasInput by remember(device.macAddress, device.customAlias) { mutableStateOf(device.customAlias) }
    var roomInput by remember(device.macAddress, device.roomLocation) { mutableStateOf(device.roomLocation) }
    var notesInput by remember(device.macAddress, device.securityNotes) { mutableStateOf(device.securityNotes) }

    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("device_inspector_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = categoryIcon(device.categoryEnum),
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = device.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${device.categoryEnum.labelRo} • ${device.vendorOui}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { isEditingIdentity = !isEditingIdentity }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editează eticheta dispozitivului",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Închide inspectorul"
                    )
                }
            }

            // Optional Identity Editor
            if (isEditingIdentity) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Personalizare Identitate & Locație",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricCyan
                        )
                        OutlinedTextField(
                            value = aliasInput,
                            onValueChange = { aliasInput = it },
                            label = { Text("Denumire Dispozitiv (Alias)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = roomInput,
                            onValueChange = { roomInput = it },
                            label = { Text("Cameră / Zonă Fizică") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Note Tehnice / Observații Audit") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onSaveIdentity(aliasInput, roomInput, notesInput)
                                isEditingIdentity = false
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvează")
                        }
                    }
                }
            }

            // Trust Status Selector + Watchdog Toggle
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "STATUT DE SECURITATE ȘI AUTORIZARE REȚEA",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrustStatus.entries.forEach { status ->
                        val selected = device.trustEnum == status
                        val chipColor = when (status) {
                            TrustStatus.TRUSTED -> CyberEmerald
                            TrustStatus.UNKNOWN_NEW -> AmberAlert
                            TrustStatus.SUSPICIOUS_FLAGGED -> CrimsonIntrusion
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateTrust(status) },
                            label = { Text(status.labelRo, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (status) {
                                        TrustStatus.TRUSTED -> Icons.Default.CheckCircle
                                        TrustStatus.UNKNOWN_NEW -> Icons.Default.Warning
                                        TrustStatus.SUSPICIOUS_FLAGGED -> Icons.Default.Error
                                    },
                                    contentDescription = null,
                                    tint = chipColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorHeart,
                                contentDescription = null,
                                tint = CyberEmerald
                            )
                            Column {
                                Text(
                                    text = "Watchdog 24/7 Uptime (${"%.2f".format(device.uptimePercent)}%)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Alertă la deconectarea echipamentului din LAN",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = device.isWatchdogEnabled,
                            onCheckedChange = onToggleWatchdog
                        )
                    }
                }
            }

            // Full Technical Specification Matrix
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FIȘĂ TEHNICĂ COMPLETĂ HARDWARE & REȚEA",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SpecRow("Adresă IPv4", device.ipAddress)
                    SpecRow("Adresă IPv6 Locală", device.ipv6Address)
                    SpecRow("Adresă Fizică MAC", device.macAddress)
                    SpecRow("Producător OUI IEEE", device.vendorOui)
                    SpecRow("Hostname Rețea (rDNS)", device.hostname)
                    SpecRow("Amprentă Sistem Operare", device.osFingerprint)
                    SpecRow("Alocare IP / DHCP", device.dhcpType)
                    SpecRow("Banda & Viteză Link", "${device.frequencyBand} • ${device.linkSpeedMbps} Mbps")
                    SpecRow("Putere Semnal & Ping", "${device.signalRssiDbm} dBm • Latență ${device.latencyMs} ms (TTL ${device.ttl})")
                    SpecRow("Servicii mDNS / Bonjour", device.mdnsServices)
                    SpecRow(
                        "Trafic Curent & Total",
                        "↓ ${device.currentDownloadKbps.toInt()} KB/s | ↑ ${device.currentUploadKbps.toInt()} KB/s • Total: ${"%.1f".format(device.sessionTrafficMb)} MB"
                    )
                    SpecRow("Prima Detectare", sdf.format(Date(device.firstSeenEpochMs)))
                    SpecRow("Ultima Activitate", sdf.format(Date(device.lastSeenEpochMs)))
                }
            }

            // Open Ports & Security Audit Section
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PORTURI TCP/UDP DESCHISE (${device.parsedPorts.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                            Text(
                                text = "Servicii expuse și bannere de protocol detectate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onRunPortAudit,
                            enabled = !isPortAuditRunning,
                            modifier = Modifier.testTag("deep_port_scan_button")
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPortAuditRunning) "Audit…" else "Re-scanează")
                        }
                    }

                    if (isPortAuditRunning) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = ElectricCyan
                        )
                    }

                    device.parsedPorts.forEach { portInfo ->
                        val riskColor = when (portInfo.riskLevel) {
                            "CRITIC" -> CrimsonIntrusion
                            "MEDIU" -> AmberAlert
                            else -> CyberEmerald
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Port ${portInfo.port}/${portInfo.protocol} • ${portInfo.serviceName}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Banner: ${portInfo.banner}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = riskColor.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "RISC ${portInfo.riskLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = riskColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Live Diagnostic Tools: Ping & Wake-on-LAN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartPing,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("ping_device_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.NetworkPing, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Ping & Jitter", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = onSendWakeOnLan,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("wol_device_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Wake-on-LAN", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Live Ping Results Panel if active
            if (pingResult != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "TELEMETRIE ICMP PING (${pingResult.targetIp})",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyberEmerald
                            )
                        }
                        Text(
                            text = pingResult.statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("MIN: ${pingResult.minMs} ms", style = MaterialTheme.typography.labelMedium, color = CyberEmerald)
                            Text("MEDIE: ${pingResult.avgMs} ms", style = MaterialTheme.typography.labelMedium, color = ElectricCyan)
                            Text("MAX: ${pingResult.maxMs} ms", style = MaterialTheme.typography.labelMedium, color = AmberAlert)
                            Text("JITTER: ${pingResult.jitterMs} ms", style = MaterialTheme.typography.labelMedium)
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            pingResult.samplesMs.forEachIndexed { i, ms ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "#${i + 1}: ${ms}ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
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
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.58f)
        )
    }
}
