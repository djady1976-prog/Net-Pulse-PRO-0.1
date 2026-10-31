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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.CompositionLocalProvider
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
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.toAppStrings
import com.example.ui.screens.AboutDeveloperCard
import com.example.ui.screens.DeviceInspectorSheet
import com.example.ui.screens.ProToolsScreen
import com.example.ui.screens.RadarScannerScreen
import com.example.ui.screens.SecuredHistoryScreen
import com.example.ui.screens.SecurityAlertsScreen
import com.example.ui.screens.TrafficChartsScreen
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.CrimsonIntrusion
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.NetPulseProTheme

enum class NetPulseTab(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTagId: String
) {
    RADAR(Icons.Filled.Radar, Icons.Outlined.Radar, "nav_tab_radar"),
    TRAFFIC(Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_tab_traffic"),
    TOOLS(Icons.Filled.Build, Icons.Outlined.Build, "nav_tab_tools"),
    ALERTS(Icons.Filled.Shield, Icons.Outlined.Shield, "nav_tab_alerts"),
    HISTORY(Icons.Filled.EnhancedEncryption, Icons.Outlined.EnhancedEncryption, "nav_tab_history")
}

class MainActivity : ComponentActivity() {

    private var isFullScreenActive: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

        val savedLangCode = prefs.getString("selected_language", AppLanguage.RO.code)
        val initialLang = AppLanguage.entries.firstOrNull { it.code == savedLangCode }
            ?: AppLanguage.RO

        setContent {
            var currentThemePreset by rememberSaveable { mutableStateOf(initialPreset) }
            var currentLanguage by rememberSaveable { mutableStateOf(initialLang) }
            var fullScreenState by rememberSaveable { mutableStateOf(true) }

            LaunchedEffect(fullScreenState) {
                isFullScreenActive = fullScreenState
                applyImmersiveFullScreen(fullScreenState)
            }

            CompositionLocalProvider(
                LocalAppStrings provides currentLanguage.toAppStrings(),
                LocalAppLanguage provides currentLanguage
            ) {
                NetPulseProTheme(themePreset = currentThemePreset) {
                    NetPulseApp(
                        currentThemePreset = currentThemePreset,
                        currentLanguage = currentLanguage,
                        isFullScreen = fullScreenState,
                        onSelectThemePreset = { newPreset ->
                            currentThemePreset = newPreset
                            prefs.edit().putString("selected_theme", newPreset.id).apply()
                        },
                        onSelectLanguage = { newLang ->
                            currentLanguage = newLang
                            prefs.edit().putString("selected_language", newLang.code).apply()
                        },
                        onToggleFullScreen = { enabled ->
                            fullScreenState = enabled
                            applyImmersiveFullScreen(enabled)
                        }
                    )
                }
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
    currentLanguage: AppLanguage = AppLanguage.RO,
    isFullScreen: Boolean = true,
    onSelectThemePreset: (AppThemePreset) -> Unit = {},
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onToggleFullScreen: (Boolean) -> Unit = {},
    viewModel: NetPulseViewModel = viewModel()
) {
    val strings = LocalAppStrings.current
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

    val dualSpeedTestResult by viewModel.dualSpeedTestResult.collectAsStateWithLifecycle()
    val signalHeatmapPoints by viewModel.signalHeatmapPoints.collectAsStateWithLifecycle()
    val tracerouteTarget by viewModel.tracerouteTarget.collectAsStateWithLifecycle()
    val tracerouteHops by viewModel.tracerouteHops.collectAsStateWithLifecycle()
    val isTracerouteRunning by viewModel.isTracerouteRunning.collectAsStateWithLifecycle()
    val dnsLookupDomain by viewModel.dnsLookupDomain.collectAsStateWithLifecycle()
    val dnsBenchmarkResults by viewModel.dnsBenchmarkResults.collectAsStateWithLifecycle()
    val isDnsBenchmarkRunning by viewModel.isDnsBenchmarkRunning.collectAsStateWithLifecycle()
    val subnetCalcResult by viewModel.subnetCalcResult.collectAsStateWithLifecycle()

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

    BackHandler(enabled = currentTab != NetPulseTab.RADAR) {
        currentTab = NetPulseTab.RADAR
    }

    val unknownCount = allDevices.count { it.trustEnum != TrustStatus.TRUSTED }

    fun tabTitle(tab: NetPulseTab): String = when (tab) {
        NetPulseTab.RADAR -> strings.navRadar
        NetPulseTab.TRAFFIC -> strings.navTraffic
        NetPulseTab.TOOLS -> strings.navTools
        NetPulseTab.ALERTS -> strings.navAlerts
        NetPulseTab.HISTORY -> strings.navHistory
    }

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
                                text = "${allDevices.size} ${strings.nodesCountLabel} • ${wifiSnapshot.subnetCidr}",
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
                                text = "$unknownCount ${strings.alertsBadgeSuffix}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonIntrusion,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                    // Quick 1-Tap Language Cycle / Selector Badge (RO / EN / IT / DE)
                    Surface(
                        onClick = {
                            val nextIdx = (AppLanguage.entries.indexOf(currentLanguage) + 1) % AppLanguage.entries.size
                            onSelectLanguage(AppLanguage.entries[nextIdx])
                        },
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .testTag("top_language_cycle_button")
                    ) {
                        Text(
                            text = currentLanguage.badgeCode,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.triggerNetworkScan() },
                        modifier = Modifier.testTag("top_rescan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = strings.scanNetworkBtn,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onToggleFullScreen(!isFullScreen) },
                        modifier = Modifier.testTag("toggle_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Full Screen",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(
                        onClick = { showThemePickerSheet = true },
                        modifier = Modifier.testTag("open_theme_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = strings.themeAndDisplaySheetTitle,
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
                    val labelText = tabTitle(tab)
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
                                        contentDescription = labelText
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = labelText
                                )
                            }
                        },
                        label = {
                            Text(
                                text = labelText,
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
                            "Authorized by network administrator."
                        )
                    },
                    onFlagDeviceSuspicious = { dev ->
                        viewModel.updateDeviceTrust(
                            dev,
                            TrustStatus.SUSPICIOUS_FLAGGED,
                            "Isolated in quarantine as suspicious device."
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

            if (showThemePickerSheet) {
                ThemeAndDisplayPickerSheet(
                    currentPreset = currentThemePreset,
                    currentLanguage = currentLanguage,
                    isFullScreen = isFullScreen,
                    onSelectPreset = onSelectThemePreset,
                    onSelectLanguage = onSelectLanguage,
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
    currentLanguage: AppLanguage,
    isFullScreen: Boolean,
    onSelectPreset: (AppThemePreset) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleFullScreen: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
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
                .verticalScroll(rememberScrollState())
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
                        text = strings.themeAndDisplaySheetTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.themeAndDisplaySheetSub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. Language Selector Section (Română, English, Italiano, Deutsch)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = strings.languageSelectorHeader,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.entries.forEach { lang ->
                    val selected = lang == currentLanguage
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectLanguage(lang) },
                        label = {
                            Text(
                                text = "${lang.badgeCode} • ${lang.nativeName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("lang_chip_${lang.code}")
                    )
                }
            }

            // 2. Immersive Full-Screen Mode Card
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
                                text = strings.immersiveFullScreenTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.immersiveFullScreenSub,
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
                text = strings.chooseThemeHeader,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            AppThemePreset.entries.forEach { preset ->
                val isSelected = preset == currentPreset
                val (presetTitle, presetSub) = strings.themePresetTitles(preset)
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
                                    text = presetTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = presetSub,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active Theme",
                                tint = preset.previewPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            AboutDeveloperCard()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
