package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ConnectionEventEntity
import com.example.data.DnsServerBenchmarkResult
import com.example.data.DualSpeedTestResult
import com.example.data.NetPulseDatabase
import com.example.data.NetPulseNotificationHelper
import com.example.data.NetPulseRepository
import com.example.data.NetworkDeviceEntity
import com.example.data.NetworkProfileInfo
import com.example.data.NetworkScannerEngine
import com.example.data.PingDiagnosticResult
import com.example.data.ProtocolShare
import com.example.data.RouterArpSecurityAudit
import com.example.data.SignalHeatmapPointEntity
import com.example.data.SubnetCalculationResult
import com.example.data.TracerouteHop
import com.example.data.TrafficSample
import com.example.data.TrustStatus
import com.example.data.WifiNetworkSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DeviceSortOption(val labelRo: String) {
    RISK_FIRST("Risc & Necunoscute"),
    BANDWIDTH_DESC("Consum Trafic"),
    LATENCY_ASC("Ping / Latență"),
    SIGNAL_DESC("Putere Semnal Wi-Fi"),
    IP_ASC("Adresă IPv4")
}

class NetPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NetPulseDatabase.getDatabase(application)
    private val scannerEngine = NetworkScannerEngine(application)
    private val repository = NetPulseRepository(database.netPulseDao(), scannerEngine)
    private val notificationHelper = NetPulseNotificationHelper(application)

    val allDevices: StateFlow<List<NetworkDeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connectionEvents: StateFlow<List<ConnectionEventEntity>> = repository.connectionEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val signalHeatmapPoints: StateFlow<List<SignalHeatmapPointEntity>> = repository.signalHeatmapPoints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _wifiSnapshot = MutableStateFlow(repository.getWifiSnapshot())
    val wifiSnapshot: StateFlow<WifiNetworkSnapshot> = _wifiSnapshot.asStateFlow()

    val routerArpAudit: StateFlow<RouterArpSecurityAudit> = combine(
        allDevices,
        _wifiSnapshot
    ) { devices, _ ->
        repository.getRouterArpAudit(devices)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.getRouterArpAudit(emptyList())
    )

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanStatusText = MutableStateFlow("Pregătit pentru scanarea subrețelei")
    val scanStatusText: StateFlow<String> = _scanStatusText.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _trustFilter = MutableStateFlow<TrustStatus?>(null)
    val trustFilter: StateFlow<TrustStatus?> = _trustFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(DeviceSortOption.RISK_FIRST)
    val sortOption: StateFlow<DeviceSortOption> = _sortOption.asStateFlow()

    private val _selectedDeviceMac = MutableStateFlow<String?>(null)
    val selectedDevice: StateFlow<NetworkDeviceEntity?> = combine(
        allDevices,
        _selectedDeviceMac
    ) { devices, mac ->
        if (mac == null) null else devices.firstOrNull { it.macAddress == mac }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val filteredDevices: StateFlow<List<NetworkDeviceEntity>> = combine(
        allDevices,
        _searchQuery,
        _trustFilter,
        _sortOption
    ) { devices, query, trust, sort ->
        val q = query.trim().lowercase()
        val matching = devices.filter { dev ->
            val matchesTrust = trust == null || dev.trustEnum == trust
            val matchesQuery = q.isEmpty() ||
                dev.displayName.lowercase().contains(q) ||
                dev.ipAddress.lowercase().contains(q) ||
                dev.macAddress.lowercase().contains(q) ||
                dev.vendorOui.lowercase().contains(q) ||
                dev.osFingerprint.lowercase().contains(q) ||
                dev.roomLocation.lowercase().contains(q) ||
                dev.openPortsRaw.lowercase().contains(q)
            matchesTrust && matchesQuery
        }

        when (sort) {
            DeviceSortOption.RISK_FIRST -> matching.sortedWith(
                compareBy<NetworkDeviceEntity> {
                    when (it.trustEnum) {
                        TrustStatus.SUSPICIOUS_FLAGGED -> 0
                        TrustStatus.UNKNOWN_NEW -> 1
                        TrustStatus.TRUSTED -> 2
                    }
                }.thenByDescending { it.hasHighRiskPorts }
                    .thenByDescending { it.currentDownloadKbps + it.currentUploadKbps }
            )
            DeviceSortOption.BANDWIDTH_DESC -> matching.sortedByDescending {
                it.currentDownloadKbps + it.currentUploadKbps
            }
            DeviceSortOption.LATENCY_ASC -> matching.sortedBy { it.latencyMs }
            DeviceSortOption.SIGNAL_DESC -> matching.sortedByDescending { it.signalRssiDbm }
            DeviceSortOption.IP_ASC -> matching.sortedBy {
                it.ipAddress.substringAfterLast(".").toIntOrNull() ?: 999
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _trafficHistory = MutableStateFlow<List<TrafficSample>>(emptyList())
    val trafficHistory: StateFlow<List<TrafficSample>> = _trafficHistory.asStateFlow()

    private val _activePingResult = MutableStateFlow<PingDiagnosticResult?>(null)
    val activePingResult: StateFlow<PingDiagnosticResult?> = _activePingResult.asStateFlow()

    private val _isPortAuditRunning = MutableStateFlow(false)
    val isPortAuditRunning: StateFlow<Boolean> = _isPortAuditRunning.asStateFlow()

    private val _actionBannerMessage = MutableStateFlow<String?>(null)
    val actionBannerMessage: StateFlow<String?> = _actionBannerMessage.asStateFlow()

    // Pro Tools States
    private val _tracerouteTarget = MutableStateFlow("1.1.1.1")
    val tracerouteTarget: StateFlow<String> = _tracerouteTarget.asStateFlow()

    private val _tracerouteHops = MutableStateFlow<List<TracerouteHop>>(emptyList())
    val tracerouteHops: StateFlow<List<TracerouteHop>> = _tracerouteHops.asStateFlow()

    private val _isTracerouteRunning = MutableStateFlow(false)
    val isTracerouteRunning: StateFlow<Boolean> = _isTracerouteRunning.asStateFlow()

    private val _dnsLookupDomain = MutableStateFlow("cloudflare.com")
    val dnsLookupDomain: StateFlow<String> = _dnsLookupDomain.asStateFlow()

    private val _dnsBenchmarkResults = MutableStateFlow<List<DnsServerBenchmarkResult>>(emptyList())
    val dnsBenchmarkResults: StateFlow<List<DnsServerBenchmarkResult>> = _dnsBenchmarkResults.asStateFlow()

    private val _isDnsBenchmarkRunning = MutableStateFlow(false)
    val isDnsBenchmarkRunning: StateFlow<Boolean> = _isDnsBenchmarkRunning.asStateFlow()

    private val _subnetCalcResult = MutableStateFlow(
        repository.calculateSubnet(_wifiSnapshot.value.localIpv4, 24)
    )
    val subnetCalcResult: StateFlow<SubnetCalculationResult> = _subnetCalcResult.asStateFlow()

    private val _dualSpeedTestResult = MutableStateFlow(
        DualSpeedTestResult(
            isRunning = false,
            phaseLabel = "Apasă 'Start SpeedTest Dual' pentru măsurarea vitezei LAN vs WAN",
            progress = 0f,
            lanThroughputMbps = 762f,
            wanDownloadMbps = 485f,
            wanUploadMbps = 230f,
            pingMs = 4,
            jitterMs = 1,
            ratingRo = "Pregătit pentru testare"
        )
    )
    val dualSpeedTestResult: StateFlow<DualSpeedTestResult> = _dualSpeedTestResult.asStateFlow()

    // Security Alert Toggles
    private val _alertOnUnknownDevices = MutableStateFlow(true)
    val alertOnUnknownDevices: StateFlow<Boolean> = _alertOnUnknownDevices.asStateFlow()

    private val _alertOnVulnerablePorts = MutableStateFlow(true)
    val alertOnVulnerablePorts: StateFlow<Boolean> = _alertOnVulnerablePorts.asStateFlow()

    private val _continuousProtectionEnabled = MutableStateFlow(true)
    val continuousProtectionEnabled: StateFlow<Boolean> = _continuousProtectionEnabled.asStateFlow()

    // Secured History Vault State (Default PIN: 2580 + Biometric Unlock)
    private var currentVaultPinHash: String = NetPulseDatabase.computeSha256Signature("PIN:2580")
    private val _currentPinHint = MutableStateFlow("2580")
    val currentPinHint: StateFlow<String> = _currentPinHint.asStateFlow()

    private val _isHistoryVaultUnlocked = MutableStateFlow(false)
    val isHistoryVaultUnlocked: StateFlow<Boolean> = _isHistoryVaultUnlocked.asStateFlow()

    private val _vaultAuthError = MutableStateFlow<String?>(null)
    val vaultAuthError: StateFlow<String?> = _vaultAuthError.asStateFlow()

    private val _eventTypeFilter = MutableStateFlow("ALL")
    val eventTypeFilter: StateFlow<String> = _eventTypeFilter.asStateFlow()

    val filteredEvents: StateFlow<List<ConnectionEventEntity>> = combine(
        connectionEvents,
        _eventTypeFilter
    ) { events, filter ->
        if (filter == "ALL") events else events.filter { it.eventType == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Multi-Network Saved Profiles
    private val _activeProfileId = MutableStateFlow("HOME")
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    val networkProfiles: StateFlow<List<NetworkProfileInfo>> = combine(
        allDevices,
        _wifiSnapshot
    ) { devices, snap ->
        val trustedCount = devices.count { it.trustEnum == TrustStatus.TRUSTED }
        listOf(
            NetworkProfileInfo(
                profileId = "HOME",
                nameRo = "Acasă • Rețea Principală",
                ssid = snap.ssid,
                subnetCidr = snap.subnetCidr,
                trustedCount = trustedCount,
                securityModeRo = "WPA3-SAE + Watchdog Activ"
            ),
            NetworkProfileInfo(
                profileId = "OFFICE",
                nameRo = "Birou • VLAN Enterprise",
                ssid = "CorpNet-5G-Enterprise",
                subnetCidr = "10.20.0.0/22",
                trustedCount = 14,
                securityModeRo = "802.1X EAP-TLS + Izolare Porturi"
            ),
            NetworkProfileInfo(
                profileId = "TRAVEL",
                nameRo = "Călătorie / Airbnb Audit",
                ssid = "Guest-Hotel-WiFi",
                subnetCidr = "172.16.10.0/24",
                trustedCount = 1,
                securityModeRo = "Mod Zero-Trust & Anti-SpyCam"
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialSignalHeatmapIfNeeded()
            runDnsBenchmark()
            runVisualTraceroute()
        }
        triggerNetworkScan()
        startLiveTrafficSampler()
    }

    fun triggerNetworkScan() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0.05f
            _wifiSnapshot.value = repository.getWifiSnapshot()
            runCatching {
                val scanned = repository.performNetworkScan { progress, status ->
                    _scanProgress.value = progress
                    _scanStatusText.value = status
                }
                val suspicious = scanned.count { it.trustEnum == TrustStatus.SUSPICIOUS_FLAGGED }
                val unknown = scanned.count { it.trustEnum != TrustStatus.TRUSTED }
                if (unknown > 0 && _alertOnUnknownDevices.value) {
                    notificationHelper.sendSecurityAlertNotification(
                        title = "NetPulse Pro: $unknown Dispozitive Necunoscute pe Wi-Fi",
                        message = if (suspicious > 0) {
                            "S-a detectat o cameră IP / dispozitiv suspect cu portul RTSP (554) și Telnet (23) deschis!"
                        } else {
                            "Au fost identificate $unknown aparate neautorizate conectate la ${_wifiSnapshot.value.ssid}."
                        }
                    )
                }
            }
            _wifiSnapshot.value = repository.getWifiSnapshot()
            _isScanning.value = false
        }
    }

    fun sendTestSecurityNotification() {
        val sent = notificationHelper.sendSecurityAlertNotification(
            title = "Alertă Securitate NetPulse Pro",
            message = "Test reușit: Sistemul nativ de alertare la intruziuni LAN și Camere IP ascunse este activ."
        )
        if (sent) {
            showBannerMessage("Notificare de securitate transmisă în bara de stare Android.")
        } else {
            showBannerMessage("Acordă permisiunea de notificări pentru alerte în bara de stare.")
        }
    }

    private fun startLiveTrafficSampler() {
        viewModelScope.launch {
            val initialSamples = mutableListOf<TrafficSample>()
            val now = System.currentTimeMillis()
            for (i in 16 downTo 1) {
                val t = now - (i * 1200L)
                val wave = kotlin.math.sin(i * 0.55).toFloat()
                val down = (2850f + wave * 920f).coerceAtLeast(320f)
                val up = (840f + wave * 280f).coerceAtLeast(110f)
                initialSamples.add(
                    TrafficSample(
                        timestampMs = t,
                        downloadKbps = down,
                        uploadKbps = up,
                        totalRxMb = 14800f + (16 - i) * 3.2f,
                        totalTxMb = 4200f + (16 - i) * 0.9f,
                        packetsPerSec = ((down + up) / 4.1f).toInt()
                    )
                )
            }
            _trafficHistory.value = initialSamples

            while (isActive) {
                delay(1200L)
                val sample = repository.sampleTraffic(allDevices.value)
                _trafficHistory.value = (_trafficHistory.value + sample).takeLast(28)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTrustFilter(trust: TrustStatus?) {
        _trustFilter.value = if (_trustFilter.value == trust) null else trust
    }

    fun setSortOption(option: DeviceSortOption) {
        _sortOption.value = option
    }

    fun selectDevice(device: NetworkDeviceEntity?) {
        _selectedDeviceMac.value = device?.macAddress
        _activePingResult.value = null
    }

    fun updateDeviceTrust(device: NetworkDeviceEntity, newTrust: TrustStatus, notes: String = device.securityNotes) {
        viewModelScope.launch {
            repository.updateTrustStatus(device, newTrust, notes)
            showBannerMessage("Statutul pentru ${device.displayName} a fost setat: ${newTrust.labelRo}")
        }
    }

    fun toggleWatchdogForDevice(device: NetworkDeviceEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleDeviceWatchdog(device, enabled)
            showBannerMessage(
                if (enabled) "Watchdog 24/7 activat pentru ${device.displayName}"
                else "Watchdog dezactivat pentru ${device.displayName}"
            )
        }
    }

    fun saveDeviceIdentity(device: NetworkDeviceEntity, alias: String, room: String, notes: String) {
        viewModelScope.launch {
            repository.updateDeviceIdentity(device, alias, room, notes)
            showBannerMessage("Detaliile dispozitivului ${alias.ifBlank { device.ipAddress }} au fost salvate.")
        }
    }

    fun runDeepPortAuditOnSelected(device: NetworkDeviceEntity) {
        if (_isPortAuditRunning.value) return
        viewModelScope.launch {
            _isPortAuditRunning.value = true
            val ports = repository.executeDeepPortAudit(device)
            _isPortAuditRunning.value = false
            showBannerMessage("Audit porturi finalizat: ${ports.size} servicii identificate pe ${device.ipAddress}.")
        }
    }

    fun startPingDiagnostic(device: NetworkDeviceEntity) {
        viewModelScope.launch {
            repository.runPingDiagnostic(device) { stepResult ->
                _activePingResult.value = stepResult
            }
        }
    }

    fun sendWakeOnLan(device: NetworkDeviceEntity) {
        viewModelScope.launch {
            val result = repository.sendWakeOnLan(device, _wifiSnapshot.value.subnetCidr)
            result.onSuccess { msg ->
                showBannerMessage(msg)
            }.onFailure { err ->
                showBannerMessage("Eroare transmisie WoL: ${err.localizedMessage}")
            }
        }
    }

    // Pro Tools Actions
    fun updateTracerouteTarget(target: String) {
        _tracerouteTarget.value = target
    }

    fun runVisualTraceroute() {
        if (_isTracerouteRunning.value) return
        viewModelScope.launch {
            _isTracerouteRunning.value = true
            repository.runTraceroute(_tracerouteTarget.value) { hops, stillRunning ->
                _tracerouteHops.value = hops
                _isTracerouteRunning.value = stillRunning
            }
            _isTracerouteRunning.value = false
        }
    }

    fun updateDnsLookupDomain(domain: String) {
        _dnsLookupDomain.value = domain
    }

    fun runDnsBenchmark() {
        if (_isDnsBenchmarkRunning.value) return
        viewModelScope.launch {
            _isDnsBenchmarkRunning.value = true
            _dnsBenchmarkResults.value = repository.runDnsBenchmark(_dnsLookupDomain.value)
            _isDnsBenchmarkRunning.value = false
        }
    }

    fun updateSubnetCalculation(ipInput: String, cidrPrefix: Int) {
        _subnetCalcResult.value = repository.calculateSubnet(ipInput, cidrPrefix)
    }

    fun runDualSpeedTest() {
        if (_dualSpeedTestResult.value.isRunning) return
        viewModelScope.launch {
            repository.runDualSpeedTest { step ->
                _dualSpeedTestResult.value = step
            }
        }
    }

    fun measureRoomSignal(roomName: String) {
        viewModelScope.launch {
            val point = repository.recordRoomSignalMeasurement(roomName)
            showBannerMessage("Măsurătoare salvată pentru '${point.roomName}': ${point.rssiDbm} dBm (${point.qualityPercent}%).")
        }
    }

    fun deleteSignalPoint(id: Long) {
        viewModelScope.launch {
            repository.deleteSignalMeasurement(id)
        }
    }

    fun toggleAlertOnUnknownDevices(enabled: Boolean) {
        _alertOnUnknownDevices.value = enabled
    }

    fun toggleAlertOnVulnerablePorts(enabled: Boolean) {
        _alertOnVulnerablePorts.value = enabled
    }

    fun toggleContinuousProtection(enabled: Boolean) {
        _continuousProtectionEnabled.value = enabled
    }

    fun attemptUnlockVault(pinInput: String): Boolean {
        val candidateHash = NetPulseDatabase.computeSha256Signature("PIN:${pinInput.trim()}")
        return if (candidateHash == currentVaultPinHash) {
            _isHistoryVaultUnlocked.value = true
            _vaultAuthError.value = null
            true
        } else {
            _vaultAuthError.value = "Cod PIN incorect. Codul implicit al seifului este ${_currentPinHint.value}."
            false
        }
    }

    fun unlockVaultViaBiometrics() {
        _isHistoryVaultUnlocked.value = true
        _vaultAuthError.value = null
        showBannerMessage("Autentificare Biometrică / Keyguard confirmată • Seif deblocat.")
    }

    fun lockHistoryVault() {
        _isHistoryVaultUnlocked.value = false
        _vaultAuthError.value = null
    }

    fun updateVaultPin(newPin: String) {
        val cleaned = newPin.trim()
        if (cleaned.length >= 4) {
            currentVaultPinHash = NetPulseDatabase.computeSha256Signature("PIN:$cleaned")
            _currentPinHint.value = cleaned
            showBannerMessage("Codul PIN pentru Istoricul Securizat a fost actualizat.")
        }
    }

    fun selectNetworkProfile(profileId: String) {
        _activeProfileId.value = profileId
        val profile = networkProfiles.value.firstOrNull { it.profileId == profileId }
        if (profile != null) {
            showBannerMessage("Profil activ comutat pe: ${profile.nameRo} (${profile.securityModeRo})")
        }
    }

    fun setEventTypeFilter(type: String) {
        _eventTypeFilter.value = type
    }

    fun clearSecuredHistory() {
        viewModelScope.launch {
            repository.clearEvents()
            showBannerMessage("Jurnalul de conectare a fost arhivat și curățat.")
        }
    }

    fun computeProtocolDistribution(): List<ProtocolShare> {
        val devices = allDevices.value
        val totalMb = devices.sumOf { it.sessionTrafficMb.toDouble() }.toFloat().coerceAtLeast(100f)
        return listOf(
            ProtocolShare(
                protocolName = "Streaming Media 4K / Cast (QUIC & UDP)",
                percentage = 0.42f,
                megabytesTotal = totalMb * 0.42f,
                descriptionRo = "Fluxuri video HDR, Google Cast și AirPlay"
            ),
            ProtocolShare(
                protocolName = "Transfer Local SMB3 / NAS Backup (TCP:445)",
                percentage = 0.27f,
                megabytesTotal = totalMb * 0.27f,
                descriptionRo = "Sincronizare Synology DSM și partajare fișiere LAN"
            ),
            ProtocolShare(
                protocolName = "Trafic Web Criptat HTTPS / TLS 1.3 (TCP:443)",
                percentage = 0.16f,
                megabytesTotal = totalMb * 0.16f,
                descriptionRo = "Navigare securizată, API-uri cloud și actualizări OS"
            ),
            ProtocolShare(
                protocolName = "Stream Video RTSP / Camere IP (TCP:554)",
                percentage = 0.11f,
                megabytesTotal = totalMb * 0.11f,
                descriptionRo = "Flux video continuu de la camere de supraveghere"
            ),
            ProtocolShare(
                protocolName = "Telemetrie IoT MQTT, DNS & mDNS (UDP:5353)",
                percentage = 0.04f,
                megabytesTotal = totalMb * 0.04f,
                descriptionRo = "Descoperire locală Bonjour, senzori ESP32 și interogări DNS"
            )
        )
    }

    fun buildFullAuditReportText(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val net = _wifiSnapshot.value
        val devices = allDevices.value
        val events = connectionEvents.value
        val heatmap = signalHeatmapPoints.value
        val arpAudit = routerArpAudit.value
        val unknownCount = devices.count { it.trustEnum != TrustStatus.TRUSTED }

        return buildString {
            appendLine("==================================================")
            appendLine("RAPORT TEHNIC DE AUDIT WI-FI & LAN - NETPULSE PRO")
            appendLine("Generat la: ${sdf.format(Date())}")
            appendLine("==================================================")
            appendLine("SSID Rețea: ${net.ssid} (${net.bssid})")
            appendLine("Subrețea: ${net.subnetCidr} | Gateway: ${net.gatewayIp}")
            appendLine("Frecvență: ${net.bandLabel} (Canal ${net.channelNumber}, ${net.frequencyMHz} MHz)")
            appendLine("Securitate: ${net.securityProtocol}")
            appendLine("Integritate ARP: ${arpAudit.duplicateMacDetails}")
            appendLine("Camere IP Detectate: ${arpAudit.spyCamerasDetected.size}")
            appendLine("Dispozitive Active: ${devices.size} | Necunoscute/Alerte: $unknownCount")
            appendLine()
            appendLine("--- INVENTAR TEHNIC DISPOZITIVE CONECTATE ---")
            devices.forEachIndexed { idx, d ->
                appendLine("${idx + 1}. ${d.displayName} [${d.trustEnum.labelRo}] (Watchdog: ${if (d.isWatchdogEnabled) "DA - ${d.uptimePercent}%" else "NU"})")
                appendLine("   IPv4: ${d.ipAddress} | MAC: ${d.macAddress} | Vendor: ${d.vendorOui}")
                appendLine("   OS: ${d.osFingerprint} | Semnal: ${d.signalRssiDbm} dBm | Ping: ${d.latencyMs} ms")
                appendLine("   Porturi Deschise: ${d.parsedPorts.joinToString { "${it.port}/${it.serviceName}(${it.riskLevel})" }.ifBlank { "Niciunul" }}")
                appendLine("   Consum Sesiune: ${"%.1f".format(d.sessionTrafficMb)} MB")
            }
            appendLine()
            appendLine("--- HARTA DE ACOPERIRE SEMNAL WI-FI PE CAMERE ---")
            heatmap.forEach { pt ->
                appendLine("• ${pt.roomName}: ${pt.rssiDbm} dBm (${pt.qualityPercent}%) | ${pt.linkSpeedMbps} Mbps | ${pt.recommendationRo}")
            }
            appendLine()
            appendLine("--- JURNAL EVENIMENTE CRIPTATE (SHA-256) ---")
            events.take(20).forEach { ev ->
                appendLine("[${sdf.format(Date(ev.timestampEpochMs))}] [${ev.severity}] ${ev.technicalSummary}")
                appendLine("   Semnătură Integritate SHA-256: ${ev.sha256Signature}")
            }
        }
    }

    fun dismissBannerMessage() {
        _actionBannerMessage.value = null
    }

    private fun showBannerMessage(msg: String) {
        _actionBannerMessage.value = msg
    }
}
