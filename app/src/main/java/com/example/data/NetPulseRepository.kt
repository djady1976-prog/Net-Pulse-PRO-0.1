package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class NetPulseRepository(
    private val dao: NetPulseDao,
    private val scannerEngine: NetworkScannerEngine
) {
    val allDevices: Flow<List<NetworkDeviceEntity>> = dao.observeAllDevices()
    val connectionEvents: Flow<List<ConnectionEventEntity>> = dao.observeConnectionEvents()
    val signalHeatmapPoints: Flow<List<SignalHeatmapPointEntity>> = dao.observeSignalHeatmapPoints()

    fun getWifiSnapshot(): WifiNetworkSnapshot = scannerEngine.inspectActiveWifiSnapshot()

    fun sampleTraffic(devices: List<NetworkDeviceEntity>): TrafficSample =
        scannerEngine.sampleLiveTraffic(devices)

    suspend fun seedInitialSignalHeatmapIfNeeded() {
        if (dao.getSignalHeatmapCount() == 0) {
            val now = System.currentTimeMillis()
            val initialPoints = listOf(
                SignalHeatmapPointEntity(
                    roomName = "Living Room (Lângă Router)",
                    rssiDbm = -39,
                    qualityPercent = 98,
                    linkSpeedMbps = 1201,
                    frequencyBand = "5 GHz (802.11ax HE80)",
                    latencyMs = 2,
                    recommendationRo = "Semnal Excelent • Ideal pentru streaming 4K/8K și gaming.",
                    timestampEpochMs = now - 7200_000L
                ),
                SignalHeatmapPointEntity(
                    roomName = "Birou Principal",
                    rssiDbm = -51,
                    qualityPercent = 88,
                    linkSpeedMbps = 866,
                    frequencyBand = "5 GHz (802.11ax)",
                    latencyMs = 4,
                    recommendationRo = "Acoperire Foarte Bună • Latență stabilă pentru apeluri video și NAS.",
                    timestampEpochMs = now - 5400_000L
                ),
                SignalHeatmapPointEntity(
                    roomName = "Dormitor Matrimonial",
                    rssiDbm = -64,
                    qualityPercent = 72,
                    linkSpeedMbps = 433,
                    frequencyBand = "5 GHz (802.11ac)",
                    latencyMs = 9,
                    recommendationRo = "Acoperire Bună • Tranziție recomandată pe 5 GHz Band Steering.",
                    timestampEpochMs = now - 3600_000L
                ),
                SignalHeatmapPointEntity(
                    roomName = "Terasă / Curte Exterioară",
                    rssiDbm = -78,
                    qualityPercent = 44,
                    linkSpeedMbps = 72,
                    frequencyBand = "2.4 GHz (802.11n)",
                    latencyMs = 24,
                    recommendationRo = "Zonă cu Atenuare Ridicată • Se recomandă adăugarea unui nod Wi-Fi Mesh!",
                    timestampEpochMs = now - 1800_000L
                )
            )
            dao.insertSignalHeatmapPoints(initialPoints)
        }
    }

    suspend fun recordRoomSignalMeasurement(roomName: String): SignalHeatmapPointEntity {
        val snap = getWifiSnapshot()
        val jitterRssi = (snap.rssiDbm + Random.nextInt(-6, 5)).coerceIn(-88, -32)
        val quality = ((jitterRssi + 100) * 2).coerceIn(18, 100)
        val recommendation = when {
            jitterRssi >= -52 -> "Semnal Excelent • Nu necesită amplificare."
            jitterRssi >= -67 -> "Semnal Stabil • Performanță bună pentru majoritatea aplicațiilor."
            else -> "Zonă Slabă (< -68 dBm) • Recomandat nod Wi-Fi Mesh sau Access Point!"
        }
        val entity = SignalHeatmapPointEntity(
            roomName = roomName.trim().ifBlank { "Cameră Măsurată" },
            rssiDbm = jitterRssi,
            qualityPercent = quality,
            linkSpeedMbps = snap.linkSpeedMbps,
            frequencyBand = snap.bandLabel,
            latencyMs = Random.nextInt(2, 14),
            recommendationRo = recommendation,
            timestampEpochMs = System.currentTimeMillis()
        )
        dao.insertSignalHeatmapPoint(entity)
        return entity
    }

    suspend fun deleteSignalMeasurement(id: Long) {
        dao.deleteSignalHeatmapPoint(id)
    }

    suspend fun performNetworkScan(
        onProgress: (Float, String) -> Unit
    ): List<NetworkDeviceEntity> {
        val existing = dao.getAllDevicesSnapshot()
        val existingMacs = existing.map { it.macAddress }.toSet()
        val scanned = scannerEngine.scanLanSubnet(existing, onProgress)
        dao.upsertDevices(scanned)

        val now = System.currentTimeMillis()
        scanned.forEach { device ->
            val isNew = device.macAddress !in existingMacs
            if (isNew && device.trustEnum != TrustStatus.TRUSTED) {
                logSecuredEvent(
                    timestamp = now,
                    deviceMac = device.macAddress,
                    deviceIp = device.ipAddress,
                    deviceName = device.displayName,
                    vendor = device.vendorOui,
                    eventType = if (device.trustEnum == TrustStatus.SUSPICIOUS_FLAGGED) "INTRUSION_ALERT" else "DEVICE_CONNECTED",
                    severity = if (device.trustEnum == TrustStatus.SUSPICIOUS_FLAGGED) "CRITIC" else "AVERTISMENT",
                    summary = if (device.trustEnum == TrustStatus.SUSPICIOUS_FLAGGED) {
                        "Dispozitiv suspect detectat pe ${device.ipAddress} (${device.vendorOui}) cu porturi critice expuse: ${device.parsedPorts.joinToString { "${it.serviceName}:${it.port}" }}."
                    } else {
                        "Dispozitiv necunoscut nou identificat pe Wi-Fi la ${device.ipAddress} [${device.macAddress}] • OS: ${device.osFingerprint}."
                    }
                )
            }
        }

        val unknownCount = scanned.count { it.trustEnum != TrustStatus.TRUSTED }
        logSecuredEvent(
            timestamp = now + 10L,
            deviceMac = getWifiSnapshot().bssid,
            deviceIp = getWifiSnapshot().subnetCidr,
            deviceName = getWifiSnapshot().ssid,
            vendor = "NetPulse Audit Engine",
            eventType = "SCAN_AUDIT",
            severity = if (unknownCount > 0) "AVERTISMENT" else "INFO",
            summary = "Scanare subrețea completă: ${scanned.size} aparate active, $unknownCount dispozitive neautorizate/necunoscute."
        )

        return scanned
    }

    suspend fun updateTrustStatus(
        device: NetworkDeviceEntity,
        newTrust: TrustStatus,
        notes: String
    ) {
        dao.updateDeviceTrust(device.macAddress, newTrust.name, notes)
        logSecuredEvent(
            timestamp = System.currentTimeMillis(),
            deviceMac = device.macAddress,
            deviceIp = device.ipAddress,
            deviceName = device.displayName,
            vendor = device.vendorOui,
            eventType = "TRUST_UPDATED",
            severity = if (newTrust == TrustStatus.SUSPICIOUS_FLAGGED) "CRITIC" else "INFO",
            summary = "Statut de securitate actualizat în '${newTrust.labelRo}' pentru ${device.displayName} (${device.ipAddress})."
        )
    }

    suspend fun toggleDeviceWatchdog(device: NetworkDeviceEntity, enabled: Boolean) {
        dao.updateDeviceWatchdog(device.macAddress, enabled)
        logSecuredEvent(
            timestamp = System.currentTimeMillis(),
            deviceMac = device.macAddress,
            deviceIp = device.ipAddress,
            deviceName = device.displayName,
            vendor = device.vendorOui,
            eventType = "SCAN_AUDIT",
            severity = "INFO",
            summary = if (enabled) {
                "Watchdog Uptime activat pentru ${device.displayName} (${device.ipAddress}) • Monitorizare disponibilitate 24/7."
            } else {
                "Watchdog Uptime dezactivat pentru ${device.displayName}."
            }
        )
    }

    suspend fun updateDeviceIdentity(
        device: NetworkDeviceEntity,
        alias: String,
        room: String,
        notes: String
    ) {
        dao.updateDeviceIdentity(device.macAddress, alias.trim(), room.trim(), notes.trim())
    }

    suspend fun executeDeepPortAudit(device: NetworkDeviceEntity): List<ServicePortInfo> {
        val ports = scannerEngine.runDeepPortScan(device)
        val raw = ports.joinToString("|") {
            "${it.port}:${it.protocol}:${it.serviceName}:${it.banner}:${it.riskLevel}"
        }
        val updated = device.copy(openPortsRaw = raw, lastSeenEpochMs = System.currentTimeMillis())
        dao.upsertDevice(updated)

        val criticalCount = ports.count { it.riskLevel == "CRITIC" }
        logSecuredEvent(
            timestamp = System.currentTimeMillis(),
            deviceMac = device.macAddress,
            deviceIp = device.ipAddress,
            deviceName = device.displayName,
            vendor = device.vendorOui,
            eventType = "PORT_VULNERABILITY",
            severity = if (criticalCount > 0) "CRITIC" else "INFO",
            summary = "Audit TCP/UDP pe ${device.ipAddress}: ${ports.size} porturi deschise (${ports.joinToString { "${it.port}/${it.serviceName}" }}), $criticalCount cu risc critic."
        )
        return ports
    }

    suspend fun runPingDiagnostic(
        device: NetworkDeviceEntity,
        onStep: (PingDiagnosticResult) -> Unit
    ) {
        scannerEngine.runLivePingTest(
            ipAddress = device.ipAddress,
            deviceName = device.displayName,
            baseLatencyMs = device.latencyMs,
            onStep = onStep
        )
    }

    suspend fun sendWakeOnLan(device: NetworkDeviceEntity, subnetCidr: String): Result<String> {
        val res = scannerEngine.sendWakeOnLanMagicPacket(device.macAddress, subnetCidr)
        res.onSuccess { msg ->
            logSecuredEvent(
                timestamp = System.currentTimeMillis(),
                deviceMac = device.macAddress,
                deviceIp = device.ipAddress,
                deviceName = device.displayName,
                vendor = device.vendorOui,
                eventType = "DEVICE_CONNECTED",
                severity = "INFO",
                summary = msg
            )
        }
        return res
    }

    suspend fun runTraceroute(
        targetHost: String,
        onHopProgress: (List<TracerouteHop>, Boolean) -> Unit
    ) {
        scannerEngine.runVisualTraceroute(targetHost, onHopProgress)
    }

    suspend fun runDnsBenchmark(targetDomain: String): List<DnsServerBenchmarkResult> =
        scannerEngine.runDnsBenchmarkAndLookup(targetDomain)

    fun calculateSubnet(ip: String, cidrPrefix: Int): SubnetCalculationResult =
        scannerEngine.calculateIpv4Subnet(ip, cidrPrefix)

    suspend fun runDualSpeedTest(onStep: (DualSpeedTestResult) -> Unit) {
        scannerEngine.runDualSpeedTest(getWifiSnapshot(), onStep)
    }

    fun getRouterArpAudit(devices: List<NetworkDeviceEntity>): RouterArpSecurityAudit =
        scannerEngine.auditRouterAndArpSecurity(devices, getWifiSnapshot())

    suspend fun clearEvents() {
        dao.clearAllConnectionEvents()
    }

    private suspend fun logSecuredEvent(
        timestamp: Long,
        deviceMac: String,
        deviceIp: String,
        deviceName: String,
        vendor: String,
        eventType: String,
        severity: String,
        summary: String
    ) {
        val signaturePayload = "$timestamp|$deviceMac|$deviceIp|$eventType|$summary"
        val signature = NetPulseDatabase.computeSha256Signature(signaturePayload)
        dao.insertConnectionEvent(
            ConnectionEventEntity(
                timestampEpochMs = timestamp,
                deviceMac = deviceMac,
                deviceIp = deviceIp,
                deviceName = deviceName,
                vendorOui = vendor,
                eventType = eventType,
                severity = severity,
                technicalSummary = summary,
                sha256Signature = signature
            )
        )
    }
}
