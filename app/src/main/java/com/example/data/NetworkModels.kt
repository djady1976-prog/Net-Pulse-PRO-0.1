package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeviceCategory(val labelRo: String) {
    ROUTER_GATEWAY("Router / Gateway"),
    SMARTPHONE("Smartphone / Mobil"),
    WORKSTATION_PC("Laptop / Stație PC"),
    SMART_TV_MEDIA("Smart TV / Media"),
    IOT_SENSOR("Senzor / Smart Home IoT"),
    NAS_SERVER("Server / Stocare NAS"),
    PRINTER("Imprimantă Rețea"),
    SECURITY_CAMERA("Cameră IP Supraveghere"),
    UNKNOWN("Aparat Neclasificat")
}

enum class TrustStatus(val labelRo: String) {
    TRUSTED("De Încredere"),
    UNKNOWN_NEW("Necunoscut (Nou)"),
    SUSPICIOUS_FLAGGED("Suspect / Alertă")
}

data class ServicePortInfo(
    val port: Int,
    val protocol: String,
    val serviceName: String,
    val banner: String,
    val riskLevel: String // "SCĂZUT", "MEDIU", "CRITIC"
)

@Entity(tableName = "network_devices")
data class NetworkDeviceEntity(
    @PrimaryKey val macAddress: String,
    val ipAddress: String,
    val ipv6Address: String,
    val hostname: String,
    val customAlias: String,
    val roomLocation: String,
    val vendorOui: String,
    val category: String, // DeviceCategory name
    val osFingerprint: String,
    val openPortsRaw: String, // Format: "80:TCP:HTTP:nginx/1.24:MEDIU|443:TCP:HTTPS:TLSv1.3:SCĂZUT"
    val latencyMs: Int,
    val jitterMs: Int,
    val ttl: Int,
    val signalRssiDbm: Int,
    val linkSpeedMbps: Int,
    val frequencyBand: String,
    val currentDownloadKbps: Float,
    val currentUploadKbps: Float,
    val sessionTrafficMb: Float,
    val trustStatus: String, // TrustStatus name
    val dhcpType: String, // "DHCP Dinamic" / "IP Static Rezervat"
    val mdnsServices: String,
    val firstSeenEpochMs: Long,
    val lastSeenEpochMs: Long,
    val isOnline: Boolean,
    val isGateway: Boolean,
    val isLocalDevice: Boolean,
    val securityNotes: String,
    val isWatchdogEnabled: Boolean = false,
    val uptimePercent: Float = 99.9f
) {
    val displayName: String
        get() = customAlias.ifBlank { hostname.ifBlank { vendorOui } }

    val categoryEnum: DeviceCategory
        get() = runCatching { DeviceCategory.valueOf(category) }.getOrDefault(DeviceCategory.UNKNOWN)

    val trustEnum: TrustStatus
        get() = runCatching { TrustStatus.valueOf(trustStatus) }.getOrDefault(TrustStatus.UNKNOWN_NEW)

    val parsedPorts: List<ServicePortInfo>
        get() = if (openPortsRaw.isBlank()) {
            emptyList()
        } else {
            openPortsRaw.split("|").mapNotNull { token ->
                val parts = token.split(":")
                if (parts.size >= 5) {
                    val portNum = parts[0].toIntOrNull() ?: return@mapNotNull null
                    ServicePortInfo(
                        port = portNum,
                        protocol = parts[1],
                        serviceName = parts[2],
                        banner = parts[3],
                        riskLevel = parts[4]
                    )
                } else null
            }
        }

    val hasHighRiskPorts: Boolean
        get() = parsedPorts.any { it.riskLevel == "CRITIC" || it.riskLevel == "MEDIU" }

    val isPotentialSpyCamera: Boolean
        get() = categoryEnum == DeviceCategory.SECURITY_CAMERA ||
            parsedPorts.any { it.port == 554 || it.port == 8554 || it.serviceName == "RTSP" } ||
            mdnsServices.contains("_rtsp") ||
            mdnsServices.contains("_onvif") ||
            vendorOui.contains("Reolink", ignoreCase = true) ||
            vendorOui.contains("Hikvision", ignoreCase = true)
}

@Entity(tableName = "connection_events")
data class ConnectionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMs: Long,
    val deviceMac: String,
    val deviceIp: String,
    val deviceName: String,
    val vendorOui: String,
    val eventType: String, // "INTRUSION_ALERT", "DEVICE_CONNECTED", "PORT_VULNERABILITY", "TRUST_UPDATED", "SCAN_AUDIT"
    val severity: String, // "CRITIC", "AVERTISMENT", "INFO"
    val technicalSummary: String,
    val sha256Signature: String
)

@Entity(tableName = "signal_heatmap_points")
data class SignalHeatmapPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomName: String,
    val rssiDbm: Int,
    val qualityPercent: Int,
    val linkSpeedMbps: Int,
    val frequencyBand: String,
    val latencyMs: Int,
    val recommendationRo: String,
    val timestampEpochMs: Long
)

data class WifiNetworkSnapshot(
    val ssid: String,
    val bssid: String,
    val interfaceName: String,
    val localIpv4: String,
    val localIpv6: String,
    val subnetCidr: String,
    val gatewayIp: String,
    val dnsServers: String,
    val frequencyMHz: Int,
    val bandLabel: String,
    val channelNumber: Int,
    val rssiDbm: Int,
    val signalQualityPercent: Int,
    val linkSpeedMbps: Int,
    val securityProtocol: String,
    val isConnected: Boolean,
    val natExternalStatus: String,
    val mtuSize: Int
)

data class TrafficSample(
    val timestampMs: Long,
    val downloadKbps: Float,
    val uploadKbps: Float,
    val totalRxMb: Float,
    val totalTxMb: Float,
    val packetsPerSec: Int
)

data class ProtocolShare(
    val protocolName: String,
    val percentage: Float,
    val megabytesTotal: Float,
    val descriptionRo: String
)

data class PingDiagnosticResult(
    val targetIp: String,
    val targetName: String,
    val isRunning: Boolean,
    val samplesMs: List<Int>,
    val packetLossPercent: Int,
    val minMs: Int,
    val avgMs: Int,
    val maxMs: Int,
    val jitterMs: Int,
    val ttlDetected: Int,
    val statusMessage: String
)

data class TracerouteHop(
    val hopNumber: Int,
    val ipAddress: String,
    val hostname: String,
    val asnOrIsp: String,
    val latencyMs: Int,
    val ttl: Int,
    val isDestination: Boolean
)

data class DnsServerBenchmarkResult(
    val providerName: String,
    val primaryIp: String,
    val secondaryIp: String,
    val securityFeatureRo: String,
    val responseTimeMs: Int,
    val resolvedIp: String,
    val isFastest: Boolean = false
)

data class SubnetCalculationResult(
    val inputIp: String,
    val cidrPrefix: Int,
    val subnetMask: String,
    val wildcardMask: String,
    val networkAddress: String,
    val broadcastAddress: String,
    val firstUsableHost: String,
    val lastUsableHost: String,
    val totalUsableHosts: Long,
    val networkClassRo: String
)

data class DualSpeedTestResult(
    val isRunning: Boolean,
    val phaseLabel: String,
    val progress: Float,
    val lanThroughputMbps: Float,
    val wanDownloadMbps: Float,
    val wanUploadMbps: Float,
    val pingMs: Int,
    val jitterMs: Int,
    val ratingRo: String
)

data class RouterArpSecurityAudit(
    val arpSpoofingDetected: Boolean,
    val duplicateMacDetails: String,
    val routerAdminExposedPorts: List<Int>,
    val upnpExposed: Boolean,
    val dnsIntegrityStatusRo: String,
    val spyCamerasDetected: List<NetworkDeviceEntity>,
    val overallRouterAdviceRo: String
)

data class NetworkProfileInfo(
    val profileId: String,
    val nameRo: String,
    val ssid: String,
    val subnetCidr: String,
    val trustedCount: Int,
    val securityModeRo: String
)
