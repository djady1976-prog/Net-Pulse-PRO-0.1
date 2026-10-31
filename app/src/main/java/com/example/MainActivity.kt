package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.EnhancedEncryption
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TrustStatus
import com.example.ui.NetPulseViewModel
import com.example.ui.screens.DeviceInspectorSheet
import com.example.ui.screens.ProToolsScreen
import com.example.ui.screens.RadarScannerScreen
import com.example.ui.screens.SecuredHistoryScreen
import com.example.ui.screens.SecurityAlertsScreen
import com.example.ui.screens.TrafficChartsScreen
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.CrimsonIntrusion
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NetPulseProTheme

enum class NetPulseTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTagId: String
) {
    RADAR(R.string.nav_radar, Icons.Filled.Radar, Icons.Outlined.Radar, "nav_tab_radar"),
    TRAFFIC(R.string.nav_traffic, Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_tab_traffic"),
    TOOLS(R.string.nav_tools, Icons.Filled.Build, Icons.Outlined.Build, "nav_tab_tools"),
    ALERTS(R.string.nav_alerts, Icons.Filled.Shield, Icons.Outlined.Shield, "nav_tab_alerts"),
    HISTORY(R.string.nav_history, Icons.Filled.EnhancedEncryption, Icons.Outlined.EnhancedEncryption, "nav_tab_history")
}

class MainActivity : ComponentActivity() {

    private var isFullScreenActive: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure display cutout & always start in full-screen immersive mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        applyImmersiveFullScreen(true)

        val prefs = getSharedPreferences("netpulse_display_prefs", Context.MODE_PRIVATE)
        val savedThemeId = prefs.getString("selected_theme", AppThemePreset.CYBER_DARK.id)
        val initialPreset = AppThemePreset.entries.firstOrNull { it.id == savedThemeId }
            ?: AppThemePreset.CYBER_DARK

        setContent {
            var currentThemePreset by rememberSaveable { mutableStateOf(initialPreset) }
            var fullScreenState by rememberSaveable { mutableStateOf(true) }

            LaunchedEffect(fullScreenState) {
                isFullScreenActive = fullScreenState
                applyImmersiveFullScreen(fullScreenState)
            }

            NetPulseProTheme(themePreset = currentThemePreset) {
                NetPulseApp(
                    currentThemePreset = currentThemePreset,
                    isFullScreen = fullScreenState,
                    onSelectThemePreset = { newPreset ->
                        currentThemePreset = newPreset
                        prefs.edit().putString("selected_theme", newPreset.id).apply()
                    },
                    onToggleFullScreen = { enabled ->
                        fullScreenState = enabled
                        applyImmersiveFullScreen(enabled)
                    }
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && isFullScreenActive) {
            applyImmersiveFullScreen(true)
        }
    }

    private fun applyImmersiveFullScreen(fullScreen: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (fullScreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetPulseApp(
    currentThemePreset: AppThemePreset = AppThemePreset.CYBER_DARK,
    isFullScreen: Boolean = true,
    onSelectThemePreset: (AppThemePreset) -> Unit = {},
    onToggleFullScreen: (Boolean) -> Unit = {},
    viewModel: NetPulseViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf(NetPulseTab.RADAR) }
    var showThemePickerSheet by rememberSaveable { mutableStateOf(false) }

    val wifiSnapshot by viewModel.wifiSnapshot.collectAsStateWithLifecycle()
    val allDevices by viewModel.allDevices.collectAsStateWithLifecycle()
    val filteredDevices by viewModel.filteredDevices.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val scanStatusText by viewModel.scanStatusText.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val trustFilter by viewModel.trustFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()
    val trafficHistory by viewModel.trafficHistory.collectAsStateWithLifecycle()
    val activePingResult by viewModel.activePingResult.collectAsStateWithLifecycle()
    val isPortAuditRunning by viewModel.isPortAuditRunning.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.actionBannerMessage.collectAsStateWithLifecycle()

    // Pro Tools states
    val dualSpeedTestResult by viewModel.dualSpeedTestResult.collectAsStateWithLifecycle()
    val signalHeatmapPoints by viewModel.signalHeatmapPoints.collectAsStateWithLifecycle()
    val tracerouteTarget by viewModel.tracerouteTarget.collectAsStateWithLifecycle()
    val tracerouteHops by viewModel.tracerouteHops.collectAsStateWithLifecycle()
    val isTracerouteRunning by viewModel.isTracerouteRunning.collectAsStateWithLifecycle()
    val dnsLookupDomain by viewModel.dnsLookupDomain.collectAsStateWithLifecycle()
    val dnsBenchmarkResults by viewModel.dnsBenchmarkResults.collectAsStateWithLifecycle()
    val isDnsBenchmarkRunning by viewModel.isDnsBenchmarkRunning.collectAsStateWithLifecycle()
    val subnetCalcResult by viewModel.subnetCalcResult.collectAsStateWithLifecycle()

    // Security & History states
    val routerArpAudit by viewModel.routerArpAudit.collectAsStateWithLifecycle()
    val alertOnUnknown by viewModel.alertOnUnknownDevices.collectAsStateWithLifecycle()
    val alertOnPorts by viewModel.alertOnVulnerablePorts.collectAsStateWithLifecycle()
    val continuousProtection by viewModel.continuousProtectionEnabled.collectAsStateWithLifecycle()

    val isVaultUnlocked by viewModel.isHistoryVaultUnlocked.collectAsStateWithLifecycle()
    val pinHint by viewModel.currentPinHint.collectAsStateWithLifecycle()
    val vaultAuthError by viewModel.vaultAuthError.collectAsStateWithLifecycle()
    val filteredEvents by viewModel.filteredEvents.collectAsStateWithLifecycle()
    val eventTypeFilter by viewModel.eventTypeFilter.collectAsStateWithLifecycle()
    val networkProfiles by viewModel.networkProfiles.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.triggerNetworkScan()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.sendTestSecurityNotification()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissBannerMessage()
        }
    }

    // Handle system Back press on secondary screens
    BackHandler(enabled = currentTab != NetPulseTab.RADAR) {
        currentTab = NetPulseTab.RADAR
    }

    val unknownCount = allDevices.count { it.trustEnum != TrustStatus.TRUSTED }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (isScanning) MaterialTheme.colorScheme.primary else CyberEmerald,
                                    CircleShape
                                )
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${allDevices.size} noduri • ${wifiSnapshot.subnetCidr}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (unknownCount > 0) {
                        Surface(
                            onClick = { currentTab = NetPulseTab.ALERTS },
                            color = CrimsonIntrusion.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            Text(
                                text = "$unknownCount ALERTE",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonIntrusion,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.triggerNetworkScan() },
                        modifier = Modifier.testTag("top_rescan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Re-scanează Rețeaua",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onToggleFullScreen(!isFullScreen) },
                        modifier = Modifier.testTag("toggle_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Comută Modul Full Screen",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(
                        onClick = { showThemePickerSheet = true },
                        modifier = Modifier.testTag("open_theme_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Selector Teme Vizuale",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NetPulseTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        modifier = Modifier.testTag(tab.testTagId),
                        icon = {
                            if (tab == NetPulseTab.ALERTS && unknownCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = CrimsonIntrusion) {
                                            Text("$unknownCount")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.titleRes)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.titleRes)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(tab.titleRes),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NetPulseTab.RADAR -> RadarScannerScreen(
                    wifiSnapshot = wifiSnapshot,
                    allDevices = allDevices,
                    filteredDevices = filteredDevices,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    scanStatusText = scanStatusText,
                    searchQuery = searchQuery,
                    selectedTrustFilter = trustFilter,
                    selectedSortOption = sortOption,
                    hasLocationPermission = hasLocationPermission,
                    onRequestPermissions = {
                        val perms = buildList {
                            add(Manifest.permission.ACCESS_FINE_LOCATION)
                            add(Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.NEARBY_WIFI_DEVICES)
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }.toTypedArray()
                        permissionLauncher.launch(perms)
                    },
                    onTriggerScan = { viewModel.triggerNetworkScan() },
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onSelectTrustFilter = { viewModel.setTrustFilter(it) },
                    onSelectSortOption = { viewModel.setSortOption(it) },
                    onDeviceClick = { viewModel.selectDevice(it) },
                    onNavigateToAlerts = { currentTab = NetPulseTab.ALERTS }
                )

                NetPulseTab.TRAFFIC -> TrafficChartsScreen(
                    trafficHistory = trafficHistory,
                    devices = allDevices,
                    protocolShares = viewModel.computeProtocolDistribution(),
                    wifiSnapshot = wifiSnapshot,
                    onDeviceClick = { viewModel.selectDevice(it) }
                )

                NetPulseTab.TOOLS -> ProToolsScreen(
                    dualSpeedTestResult = dualSpeedTestResult,
                    signalHeatmapPoints = signalHeatmapPoints,
                    tracerouteTarget = tracerouteTarget,
                    tracerouteHops = tracerouteHops,
                    isTracerouteRunning = isTracerouteRunning,
                    dnsLookupDomain = dnsLookupDomain,
                    dnsBenchmarkResults = dnsBenchmarkResults,
                    isDnsBenchmarkRunning = isDnsBenchmarkRunning,
                    subnetCalcResult = subnetCalcResult,
                    onRunDualSpeedTest = { viewModel.runDualSpeedTest() },
                    onMeasureRoomSignal = { viewModel.measureRoomSignal(it) },
                    onDeleteSignalPoint = { viewModel.deleteSignalPoint(it) },
                    onUpdateTracerouteTarget = { viewModel.updateTracerouteTarget(it) },
                    onRunTraceroute = { viewModel.runVisualTraceroute() },
                    onUpdateDnsDomain = { viewModel.updateDnsLookupDomain(it) },
                    onRunDnsBenchmark = { viewModel.runDnsBenchmark() },
                    onUpdateSubnetCalc = { ip, cidr -> viewModel.updateSubnetCalculation(ip, cidr) }
                )

                NetPulseTab.ALERTS -> SecurityAlertsScreen(
                    devices = allDevices,
                    routerArpAudit = routerArpAudit,
                    alertOnUnknownDevices = alertOnUnknown,
                    alertOnVulnerablePorts = alertOnPorts,
                    continuousProtectionEnabled = continuousProtection,
                    onToggleAlertUnknown = { viewModel.toggleAlertOnUnknownDevices(it) },
                    onToggleAlertPorts = { viewModel.toggleAlertOnVulnerablePorts(it) },
                    onToggleContinuousProtection = { viewModel.toggleContinuousProtection(it) },
                    onAuthorizeDevice = { dev ->
                        viewModel.updateDeviceTrust(
                            dev,
                            TrustStatus.TRUSTED,
                            "Autorizat manual de administratorul rețelei."
                        )
                    },
                    onFlagDeviceSuspicious = { dev ->
                        viewModel.updateDeviceTrust(
                            dev,
                            TrustStatus.SUSPICIOUS_FLAGGED,
                            "Izolat în carantină ca dispozitiv suspect."
                        )
                    },
                    onToggleWatchdog = { dev, enabled ->
                        viewModel.toggleWatchdogForDevice(dev, enabled)
                    },
                    onTestPushNotification = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.sendTestSecurityNotification()
                        }
                    },
                    onInspectDevice = { viewModel.selectDevice(it) }
                )

                NetPulseTab.HISTORY -> SecuredHistoryScreen(
                    isUnlocked = isVaultUnlocked,
                    pinHint = pinHint,
                    authError = vaultAuthError,
                    events = filteredEvents,
                    selectedEventTypeFilter = eventTypeFilter,
                    networkProfiles = networkProfiles,
                    activeProfileId = activeProfileId,
                    onUnlockAttempt = { viewModel.attemptUnlockVault(it) },
                    onBiometricUnlock = { viewModel.unlockVaultViaBiometrics() },
                    onLockVault = { viewModel.lockHistoryVault() },
                    onUpdatePin = { viewModel.updateVaultPin(it) },
                    onSelectNetworkProfile = { viewModel.selectNetworkProfile(it) },
                    onSelectEventTypeFilter = { viewModel.setEventTypeFilter(it) },
                    onClearHistory = { viewModel.clearSecuredHistory() },
                    onBuildAuditReport = { viewModel.buildFullAuditReportText() }
                )
            }

            // Full Technical Device Inspector BottomSheet
            selectedDevice?.let { activeDevice ->
                DeviceInspectorSheet(
                    device = activeDevice,
                    pingResult = activePingResult,
                    isPortAuditRunning = isPortAuditRunning,
                    onDismiss = { viewModel.selectDevice(null) },
                    onUpdateTrust = { newTrust ->
                        viewModel.updateDeviceTrust(activeDevice, newTrust)
                    },
                    onToggleWatchdog = { enabled ->
                        viewModel.toggleWatchdogForDevice(activeDevice, enabled)
                    },
                    onSaveIdentity = { alias, room, notes ->
                        viewModel.saveDeviceIdentity(activeDevice, alias, room, notes)
                    },
                    onRunPortAudit = { viewModel.runDeepPortAuditOnSelected(activeDevice) },
                    onStartPing = { viewModel.startPingDiagnostic(activeDevice) },
                    onSendWakeOnLan = { viewModel.sendWakeOnLan(activeDevice) }
                )
            }

            // Visual Theme & Full-Screen Selector Sheet
            if (showThemePickerSheet) {
                ThemeAndDisplayPickerSheet(
                    currentPreset = currentThemePreset,
                    isFullScreen = isFullScreen,
                    onSelectPreset = { preset ->
                        onSelectThemePreset(preset)
                    },
                    onToggleFullScreen = onToggleFullScreen,
                    onDismiss = { showThemePickerSheet = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeAndDisplayPickerSheet(
    currentPreset: AppThemePreset,
    isFullScreen: Boolean,
    onSelectPreset: (AppThemePreset) -> Unit,
    onToggleFullScreen: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("theme_picker_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Column {
                    Text(
                        text = "Teme Vizuale & Afișare Full Screen",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Personalizează paleta cromatică a consolei NetPulse Pro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Immersive Full-Screen Mode Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Mod Imersiv Full Screen la Pornire",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ascunde automat barele de sistem pentru spațiu maxim de telemetrie",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isFullScreen,
                        onCheckedChange = onToggleFullScreen
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Text(
                text = "ALEGE TEMA INTERFEȚEI (6 PALETE DISPONIBILE)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            AppThemePreset.entries.forEach { preset ->
                val isSelected = preset == currentPreset
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPreset(preset) }
                        .testTag("theme_option_${preset.id}"),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.6.dp else 0.8.dp,
                        color = if (isSelected) preset.previewPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Color Swatch Preview
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(preset.previewBg, RoundedCornerShape(10.dp))
                                    .border(1.dp, preset.previewPrimary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(preset.previewPrimary, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(preset.previewSecondary, CircleShape)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = preset.titleRo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = preset.subtitleRo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Temă Activă",
                                tint = preset.previewPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            com.example.ui.screens.AboutDeveloperCard()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
