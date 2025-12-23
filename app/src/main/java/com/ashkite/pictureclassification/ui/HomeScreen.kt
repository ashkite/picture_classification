package com.ashkite.pictureclassification.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ashkite.pictureclassification.data.model.PlaceCount
import com.ashkite.pictureclassification.worker.MediaScanScheduler
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onPlaceClick: (Long) -> Unit,
    onDateClick: (String) -> Unit,
    onTagClick: (String, Long) -> Unit,
    onUnknownClick: () -> Unit,
    onUnknownDateClick: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    val requiredPermissions = remember { buildPermissions() }
    var hasReadPermission by remember {
        mutableStateOf(hasRequiredPermissions(context, requiredPermissions.read))
    }
    var hasLocationPermission by remember {
        mutableStateOf(hasRequiredPermissions(context, requiredPermissions.location))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasReadPermission = hasRequiredPermissions(context, requiredPermissions.read)
        hasLocationPermission = hasRequiredPermissions(context, requiredPermissions.location)
    }

    val workInfos by WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow(MediaScanScheduler.UNIQUE_ONE_TIME)
        .collectAsState(initial = emptyList())
    val scanState = workInfos.firstOrNull()?.state
    val isScanning = scanState == WorkInfo.State.RUNNING || scanState == WorkInfo.State.ENQUEUED
    val scanStateLabel = when (scanState) {
        WorkInfo.State.RUNNING -> "Scanning"
        WorkInfo.State.ENQUEUED -> "Queued"
        WorkInfo.State.SUCCEEDED -> "Completed"
        WorkInfo.State.FAILED -> "Failed"
        WorkInfo.State.CANCELLED -> "Cancelled"
        WorkInfo.State.BLOCKED -> "Blocked"
        else -> "Idle"
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    LaunchedEffect(scanState) {
        if (scanState == WorkInfo.State.SUCCEEDED || scanState == WorkInfo.State.FAILED) {
            viewModel.refresh()
        }
    }

    val placeRows = uiState.placeCounts.map { place ->
        SectionRow(
            title = formatPlaceName(place),
            count = place.count,
            onClick = { onPlaceClick(place.cityId) }
        )
    }
    val dateRows = uiState.dateCounts.map { date ->
        SectionRow(
            title = date.localDate,
            count = date.count,
            onClick = { onDateClick(date.localDate) }
        )
    }
    val peopleRows = uiState.peopleCounts.map { tag ->
        SectionRow(
            title = tag.name,
            count = tag.count,
            onClick = { onTagClick(tag.type, tag.tagId) }
        )
    }
    val eventRows = uiState.eventCounts.map { tag ->
        SectionRow(
            title = tag.name,
            count = tag.count,
            onClick = { onTagClick(tag.type, tag.tagId) }
        )
    }
    val unknownRows = if (uiState.unknownTotal == 0 && uiState.unknownDateCounts.isEmpty()) {
        emptyList()
    } else {
        buildList {
            add(
                SectionRow(
                    title = "All unknown",
                    count = uiState.unknownTotal,
                    onClick = onUnknownClick
                )
            )
            uiState.unknownDateCounts.forEach { date ->
                add(
                    SectionRow(
                        title = date.localDate,
                        count = date.count,
                        onClick = { onUnknownDateClick(date.localDate) }
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Picture Classification") }) }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "MVP scaffolding is ready.",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Next: media scan, offline geocode, and TFLite tagging.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card {
                    val lastScan = formatEpoch(uiState.lastScanEpoch)
                    val lastSuccess = formatEpoch(uiState.lastSuccessEpoch)
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Scan status: $scanStateLabel",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Media indexed: ${uiState.mediaCount}")
                        Text("Last scan: $lastScan")
                        Text("Last success: $lastSuccess")
                        Text("Errors: ${uiState.errorCount}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (hasReadPermission) {
                                "Media access: granted"
                            } else {
                                "Media access: required"
                            }
                        )
                        Text(
                            text = if (hasLocationPermission) {
                                "Location metadata: enabled"
                            } else {
                                "Location metadata: disabled"
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val needsPermission = !hasReadPermission || !hasLocationPermission
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        (requiredPermissions.read + requiredPermissions.location).toTypedArray()
                                    )
                                },
                                enabled = needsPermission
                            ) {
                                Text(if (needsPermission) "Grant permissions" else "Permissions ok")
                            }
                            Button(
                                onClick = {
                                    MediaScanScheduler.enqueueOneTime(context, force = true)
                                },
                                enabled = hasReadPermission && !isScanning
                            ) {
                                Text(if (isScanning) "Scanning..." else "Start scan")
                            }
                            Button(
                                onClick = { viewModel.refresh() }
                            ) {
                                Text("Refresh")
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "Places",
                    rows = placeRows,
                    emptyText = "No location data yet."
                )
            }
            item {
                SectionCard(
                    title = "Dates",
                    rows = dateRows,
                    emptyText = "No date groups yet."
                )
            }
            item {
                SectionCard(
                    title = "People",
                    rows = peopleRows,
                    emptyText = "No people tags yet."
                )
            }
            item {
                SectionCard(
                    title = "Events",
                    rows = eventRows,
                    emptyText = "No event tags yet."
                )
            }
            item {
                SectionCard(
                    title = "Location Unknown",
                    rows = unknownRows,
                    emptyText = "No unknown-location items."
                )
            }
        }
    }
}

private data class PermissionGroup(
    val read: List<String>,
    val location: List<String>
)

private data class SectionRow(
    val title: String,
    val count: Int,
    val onClick: (() -> Unit)?
)

private fun buildPermissions(): PermissionGroup {
    val read = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(Manifest.permission.ACCESS_MEDIA_LOCATION)
    } else {
        emptyList()
    }
    return PermissionGroup(read = read, location = location)
}

private fun hasRequiredPermissions(context: android.content.Context, permissions: List<String>): Boolean {
    if (permissions.isEmpty()) return true
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

private fun formatEpoch(epoch: Long?): String {
    if (epoch == null || epoch == 0L) return "-"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(formatter)
}

@Composable
private fun SectionCard(title: String, rows: List<SectionRow>, emptyText: String) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (rows.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                rows.forEachIndexed { index, row ->
                    SectionRowItem(row = row)
                    if (index != rows.lastIndex) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionRowItem(row: SectionRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = row.onClick != null) { row.onClick?.invoke() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = row.title)
        Text(text = row.count.toString())
    }
}

private fun formatPlaceName(place: PlaceCount): String {
    val name = if (place.nameKo.isNotBlank()) place.nameKo else place.nameEn
    return "$name (${place.countryCode})"
}
