@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.wifiguard.app.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wifiguard.app.data.AppContainer
import com.wifiguard.app.model.AlertEntity
import com.wifiguard.app.model.ConnectionEventType
import com.wifiguard.app.model.DeviceEntity
import com.wifiguard.app.model.DeviceStatus
import com.wifiguard.app.model.HistoryEntity
import com.wifiguard.app.ui.theme.GuardNeon
import com.wifiguard.app.ui.theme.GuardOrange
import com.wifiguard.app.ui.theme.GuardPanel
import com.wifiguard.app.ui.theme.GuardRed
import com.wifiguard.app.ui.viewmodel.AlertsViewModel
import com.wifiguard.app.ui.viewmodel.DashboardViewModel
import com.wifiguard.app.ui.viewmodel.DeviceDetailsViewModel
import com.wifiguard.app.ui.viewmodel.DevicesViewModel
import com.wifiguard.app.ui.viewmodel.HistoryViewModel
import com.wifiguard.app.ui.viewmodel.SettingsViewModel
import com.wifiguard.app.ui.viewmodel.StatisticsViewModel
import com.wifiguard.app.ui.viewmodel.WiFiGuardViewModelFactory
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("dashboard", "Dashboard", Icons.Default.Security),
    Tab("devices", "Devices", Icons.Default.Devices),
    Tab("history", "History", Icons.Default.History),
    Tab("alerts", "Alerts", Icons.Default.Notifications),
    Tab("stats", "Stats", Icons.Default.Assessment),
    Tab("settings", "Settings", Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiGuardApp(container: AppContainer) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: "dashboard"

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = GuardPanel) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = current.startsWith(tab.route),
                        onClick = { navController.navigate(tab.route) { launchSingleTop = true } },
                        icon = { Icon(tab.icon, tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            composable("dashboard") {
                val vm: DashboardViewModel = viewModel(factory = WiFiGuardViewModelFactory(container))
                DashboardScreen(vm)
            }
            composable("devices") {
                val vm: DevicesViewModel = viewModel(factory = WiFiGuardViewModelFactory(container))
                DevicesScreen(vm) { mac ->
                    navController.navigate("details/${mac.encodeRoute()}")
                }
            }
            composable("history") {
                val vm: HistoryViewModel = viewModel(factory = WiFiGuardViewModelFactory(container))
                HistoryScreen(vm)
            }
            composable(
                "details/{mac}",
                arguments = listOf(navArgument("mac") { type = NavType.StringType })
            ) { entry ->
                val mac = entry.arguments?.getString("mac").orEmpty().decodeRoute()
                val vm: DeviceDetailsViewModel = viewModel(factory = WiFiGuardViewModelFactory(container, mac))
                DeviceDetailsScreen(vm)
            }
            composable("alerts") {
                val vm: AlertsViewModel = viewModel(factory = WiFiGuardViewModelFactory(container))
                AlertsScreen(vm)
            }
            composable("stats") {
                val vm: StatisticsViewModel = viewModel(factory = WiFiGuardViewModelFactory(container))
                StatisticsScreen(vm)
            }
            composable("settings") {
                val vm: SettingsViewModel = viewModel(factory = WiFiGuardViewModelFactory(container))
                SettingsScreen(vm)
            }
        }
    }
}

@Composable
private fun DashboardScreen(vm: DashboardViewModel) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Screen("WiFi Guard", "Live network defense") {
        PulseStatus(state.wifi.isConnected, if (state.isScanning) "Scanning" else "Real-time monitor")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Network", state.wifi.ssid, Icons.Default.Wifi, Modifier.weight(1f))
            MetricCard("Router IP", state.wifi.routerIp, Icons.Default.Router, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Connected", state.stats.totalDevices.toString(), Icons.Default.Devices, Modifier.weight(1f))
            MetricCard("Unknown", state.stats.unknownDevices.toString(), Icons.Default.Shield, Modifier.weight(1f), GuardOrange)
        }
        state.stats.lastDevice?.let {
            SectionCard {
                Text("Last connected device", style = MaterialTheme.typography.labelLarge, color = GuardNeon)
                Text(it.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${it.ipAddress}  ${it.macAddress}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Button(onClick = { vm.scan() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(8.dp)) {
            Text(if (state.isScanning) "Scanning Devices..." else "Scan Devices", fontWeight = FontWeight.Bold)
        }
        AnimatedVisibility(state.remoteEnabled) {
            Button(onClick = { vm.refreshRemote() }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text(if (state.isRemoteRefreshing) "Refreshing Remote..." else "Refresh Remote over 4G", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DevicesScreen(vm: DevicesViewModel, onOpen: (String) -> Unit) {
    val devices by vm.devices.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<DeviceStatus?>(null) }
    val filtered = devices.filter {
        (filter == null || it.status == filter) &&
            (it.name.contains(search, true) || it.ipAddress.contains(search) || it.macAddress.contains(search, true))
    }
    Screen("Connected Devices", "Known, unknown and blocked clients") {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text("Search device, IP or MAC") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All") })
            DeviceStatus.entries.forEach { status ->
                FilterChip(selected = filter == status, onClick = { filter = status }, label = { Text(status.label()) })
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(520.dp)) {
            items(filtered, key = { it.macAddress }) { device ->
                DeviceRow(device, onOpen)
            }
        }
    }
}

@Composable
private fun HistoryScreen(vm: HistoryViewModel) {
    val context = LocalContext.current
    val history by vm.history.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf("Last 7 Days") }
    val filtered = history.filterByDate(filter)
    Screen("Connection History", "Every connect and disconnect event") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Today", "Yesterday", "Last 7 Days", "Last 30 Days").forEach {
                FilterChip(selected = filter == it, onClick = { filter = it }, label = { Text(it) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { Toast.makeText(context, "PDF: ${vm.exportPdf(filtered).absolutePath}", Toast.LENGTH_LONG).show() }) {
                Icon(Icons.Default.FileDownload, null)
                Spacer(Modifier.width(8.dp))
                Text("PDF")
            }
            Button(onClick = { Toast.makeText(context, "Excel: ${vm.exportExcel(filtered).absolutePath}", Toast.LENGTH_LONG).show() }) {
                Icon(Icons.Default.FileDownload, null)
                Spacer(Modifier.width(8.dp))
                Text("Excel")
            }
        }
        Timeline(filtered)
    }
}

@Composable
private fun DeviceDetailsScreen(vm: DeviceDetailsViewModel) {
    val device by vm.device.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    var rename by remember { mutableStateOf("") }
    Screen("Device Details", "Identity, trust and history controls") {
        device?.let {
            SectionCard {
                Text(it.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                StatusBadge(it.status)
                Detail("Manufacturer", it.manufacturer)
                Detail("IP", it.ipAddress)
                Detail("MAC", it.macAddress)
                Detail("First connection", it.firstSeen.asDateTime())
                Detail("Last connection", it.lastSeen.asDateTime())
                Detail("Number of connections", it.connectionCount.toString())
            }
            OutlinedTextField(rename, { rename = it }, placeholder = { Text("Rename Device") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.trust() }) { Text("Mark as Trusted") }
                Button(onClick = { if (rename.isNotBlank()) vm.rename(rename) }) { Text("Rename") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.block() }) { Text("Block Device") }
                TextButton(onClick = { vm.deleteHistory() }) {
                    Icon(Icons.Default.Delete, null)
                    Text("Delete History")
                }
            }
            Timeline(history)
        } ?: Text("Device not found")
    }
}

@Composable
private fun AlertsScreen(vm: AlertsViewModel) {
    val alerts by vm.alerts.collectAsStateWithLifecycle()
    Screen("Notifications Center", "Security alerts and network events") {
        TextButton(onClick = { vm.markRead() }) { Text("Mark all as read") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(560.dp)) {
            items(alerts, key = { it.id }) { AlertRow(it) }
        }
    }
}

@Composable
private fun StatisticsScreen(vm: StatisticsViewModel) {
    val devices by vm.devices.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val trusted = devices.count { it.status == DeviceStatus.TRUSTED }
    val newDevices = devices.count { System.currentTimeMillis() - it.firstSeen < 7 * 24 * 60 * 60 * 1000L }
    Screen("Statistics", "Weekly and monthly network visibility") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Connections", history.size.toString(), Icons.Default.History, Modifier.weight(1f))
            MetricCard("New Devices", newDevices.toString(), Icons.Default.Devices, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Trusted", trusted.toString(), Icons.Default.Shield, Modifier.weight(1f), GuardNeon)
            MetricCard("Monthly", history.filterByDate("Last 30 Days").size.toString(), Icons.Default.Assessment, Modifier.weight(1f))
        }
        SectionCard {
            Text("Connection chart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            MiniChart(history.groupByDay())
        }
        SectionCard {
            Text("Most connected devices", color = GuardNeon)
            devices.sortedByDescending { it.connectionCount }.take(5).forEach {
                Detail(it.name, "${it.connectionCount} connections")
            }
        }
    }
}

@Composable
private fun SettingsScreen(vm: SettingsViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Screen("Settings", "Scanning, notifications and backups") {
        SectionCard {
            Text("Scan interval", color = GuardNeon, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 5, 15, 30).forEach { minutes ->
                    FilterChip(
                        selected = settings.scanIntervalMinutes == minutes,
                        onClick = { vm.setInterval(minutes) },
                        label = { Text("$minutes min") }
                    )
                }
            }
        }
        SettingSwitch("Enable notifications", settings.notificationsEnabled) { vm.setNotifications(it) }
        SettingSwitch("Auto scan on startup", settings.autoScanOnStartup) { vm.setAutoScan(it) }
        SettingSwitch("Dark mode", settings.darkMode) { vm.setDarkMode(it) }
        SettingSwitch("Remote monitoring over 4G", settings.remoteEnabled) { vm.setRemoteEnabled(it) }
        SectionCard {
            Text("Remote cloud link", color = GuardNeon, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = settings.remoteEndpoint,
                onValueChange = { vm.setRemoteEndpoint(it) },
                label = { Text("HTTPS endpoint") },
                placeholder = { Text("https://your-server.com/api") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = settings.remoteSiteId,
                onValueChange = { vm.setRemoteSiteId(it) },
                label = { Text("Site ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = settings.remoteToken,
                onValueChange = { vm.setRemoteToken(it) },
                label = { Text("Access token") },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Use one Android phone inside the WiFi as the scanner. Your 4G phone can refresh this site through the secure endpoint.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        SectionCard {
            Text("Backup and restore database", color = GuardNeon, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    Toast.makeText(context, "Backup: ${vm.backupDatabase().absolutePath}", Toast.LENGTH_LONG).show()
                }) { Text("Backup") }
                Button(onClick = {
                    val restored = vm.restoreDatabase()
                    Toast.makeText(context, restored?.let { "Restored. Restart app." } ?: "No backup found", Toast.LENGTH_LONG).show()
                }) { Text("Restore") }
            }
        }
    }
}

@Composable
private fun Screen(title: String, subtitle: String, content: @Composable Column.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(subtitle, color = MaterialTheme.colorScheme.secondary)
        }
        item { Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content) }
    }
}

@Composable
private fun SectionCard(content: @Composable Column.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GuardPanel),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun MetricCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, tint: Color = GuardNeon) {
    SectionCardWrapper(modifier) {
        Icon(icon, null, tint = tint)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionCardWrapper(modifier: Modifier, content: @Composable Column.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = GuardPanel), shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}

@Composable
private fun PulseStatus(active: Boolean, text: String) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val radius by transition.animateFloat(4f, 10f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "radius")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(Modifier.size(24.dp)) {
            drawCircle(if (active) GuardNeon else GuardRed, radius = radius, style = Stroke(width = 3f))
            drawCircle(if (active) GuardNeon else GuardRed, radius = 5f)
        }
        Text(text, color = if (active) GuardNeon else GuardRed, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DeviceRow(device: DeviceEntity, onOpen: (String) -> Unit) {
    SectionCardWrapper(Modifier.fillMaxWidth().clickable { onOpen(device.macAddress) }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Devices, null, tint = GuardNeon)
            }
            Column(Modifier.weight(1f)) {
                Text(device.name, fontWeight = FontWeight.Bold)
                Text(device.ipAddress, color = MaterialTheme.colorScheme.secondary)
                Text(device.macAddress, style = MaterialTheme.typography.bodySmall)
                Text("Last seen ${device.lastSeen.asDateTime()}", style = MaterialTheme.typography.bodySmall)
            }
            StatusBadge(device.status)
        }
    }
}

@Composable
private fun StatusBadge(status: DeviceStatus) {
    val color = when (status) {
        DeviceStatus.TRUSTED -> GuardNeon
        DeviceStatus.UNKNOWN -> GuardOrange
        DeviceStatus.BLOCKED -> GuardRed
    }
    Badge(containerColor = color, contentColor = Color.Black) {
        Text(status.label(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

@Composable
private fun Timeline(history: List<HistoryEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(360.dp)) {
        items(history, key = { it.id }) { event ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(if (event.eventType == ConnectionEventType.CONNECTED) GuardNeon else GuardRed))
                    Box(Modifier.width(2.dp).height(54.dp).background(MaterialTheme.colorScheme.outline))
                }
                SectionCardWrapper(Modifier.weight(1f)) {
                    Text(event.eventType.name, color = if (event.eventType == ConnectionEventType.CONNECTED) GuardNeon else GuardRed, fontWeight = FontWeight.Bold)
                    Text(event.deviceName)
                    Text("${event.ipAddress}  ${event.macAddress}", style = MaterialTheme.typography.bodySmall)
                    Text(event.timestamp.asDateTime(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun AlertRow(alert: AlertEntity) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Notifications, null, tint = GuardNeon)
            Text(alert.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            AnimatedVisibility(!alert.isRead) { AssistChip(onClick = {}, label = { Text("New") }) }
        }
        Text(alert.message)
        Text(alert.timestamp.asDateTime(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun Detail(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.secondary)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun MiniChart(values: List<Int>) {
    val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        values.takeLast(7).forEach { value ->
            Box(
                Modifier.weight(1f)
                    .height((24 + (value * 90 / max)).dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(GuardNeon)
            )
        }
    }
}

private fun DeviceStatus.label() = name.lowercase().replaceFirstChar { it.titlecase() }

private fun List<HistoryEntity>.filterByDate(filter: String): List<HistoryEntity> {
    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L
    return when (filter) {
        "Today" -> filter { now - it.timestamp < day }
        "Yesterday" -> filter { now - it.timestamp in day until 2 * day }
        "Last 7 Days" -> filter { now - it.timestamp < 7 * day }
        else -> filter { now - it.timestamp < 30 * day }
    }
}

private fun List<HistoryEntity>.groupByDay(): List<Int> {
    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L
    return (6 downTo 0).map { offset ->
        count { now - it.timestamp in (offset * day) until ((offset + 1) * day) }
    }
}

private fun String.encodeRoute(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
private fun String.decodeRoute(): String = URLDecoder.decode(this, StandardCharsets.UTF_8.toString())
