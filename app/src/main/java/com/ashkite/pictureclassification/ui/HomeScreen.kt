package com.ashkite.pictureclassification.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.ashkite.pictureclassification.R
import com.ashkite.pictureclassification.data.model.PlaceCount
import com.ashkite.pictureclassification.worker.MediaScanScheduler
import kotlinx.coroutines.delay
import java.text.NumberFormat
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
    val locale = context.resources.configuration.locales[0]
    val numberFormat = remember(locale) { NumberFormat.getInstance(locale) }
    val dateTimeFormat = stringResource(R.string.date_time_format)
    val dateTimeFormatter = remember(dateTimeFormat) { DateTimeFormatter.ofPattern(dateTimeFormat) }
    val unknownValue = stringResource(R.string.value_unknown)

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
    val scanStateLabel = stringResource(id = scanStateLabelRes(scanState))
    val scanStateColor = when (scanState) {
        WorkInfo.State.RUNNING -> MaterialTheme.colorScheme.primary
        WorkInfo.State.ENQUEUED -> MaterialTheme.colorScheme.tertiary
        WorkInfo.State.SUCCEEDED -> MaterialTheme.colorScheme.secondary
        WorkInfo.State.FAILED -> MaterialTheme.colorScheme.error
        WorkInfo.State.CANCELLED -> MaterialTheme.colorScheme.outline
        WorkInfo.State.BLOCKED -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
            title = formatPlaceName(place, locale),
            countLabel = numberFormat.format(place.count),
            onClick = { onPlaceClick(place.cityId) }
        )
    }
    val dateRows = uiState.dateCounts.map { date ->
        SectionRow(
            title = date.localDate,
            countLabel = numberFormat.format(date.count),
            onClick = { onDateClick(date.localDate) }
        )
    }
    val peopleRows = uiState.peopleCounts.map { tag ->
        SectionRow(
            title = displayTagName(tag.type, tag.name),
            countLabel = numberFormat.format(tag.count),
            onClick = { onTagClick(tag.type, tag.tagId) }
        )
    }
    val eventRows = uiState.eventCounts.map { tag ->
        SectionRow(
            title = displayTagName(tag.type, tag.name),
            countLabel = numberFormat.format(tag.count),
            onClick = { onTagClick(tag.type, tag.tagId) }
        )
    }
    val unknownRows = if (uiState.unknownTotal == 0 && uiState.unknownDateCounts.isEmpty()) {
        emptyList()
    } else {
        buildList {
            add(
                SectionRow(
                    title = stringResource(R.string.home_unknown_all),
                    countLabel = numberFormat.format(uiState.unknownTotal),
                    onClick = onUnknownClick
                )
            )
            uiState.unknownDateCounts.forEach { date ->
                add(
                    SectionRow(
                        title = date.localDate,
                        countLabel = numberFormat.format(date.count),
                        onClick = { onUnknownDateClick(date.localDate) }
                    )
                )
            }
        }
    }

    val lastScan = formatEpoch(uiState.lastScanEpoch, dateTimeFormatter, unknownValue)
    val lastSuccess = formatEpoch(uiState.lastSuccessEpoch, dateTimeFormatter, unknownValue)
    val mediaCountText = numberFormat.format(uiState.mediaCount)
    val errorCountText = numberFormat.format(uiState.errorCount)

    AppScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Reveal(delayMs = 0) {
                    HeroPanel(mediaCount = mediaCountText, lastSuccess = lastSuccess)
                }
            }
            item {
                Reveal(delayMs = 120) {
                    ScanControlCard(
                        scanStateLabel = scanStateLabel,
                        scanStateColor = scanStateColor,
                        isScanning = isScanning,
                        mediaCount = mediaCountText,
                        lastScan = lastScan,
                        lastSuccess = lastSuccess,
                        errorCount = errorCountText,
                        hasReadPermission = hasReadPermission,
                        hasLocationPermission = hasLocationPermission,
                        onRequestPermissions = {
                            permissionLauncher.launch(
                                (requiredPermissions.read + requiredPermissions.location).toTypedArray()
                            )
                        },
                        onScan = { MediaScanScheduler.enqueueOneTime(context, force = true) },
                        onRefresh = { viewModel.refresh() }
                    )
                }
            }
            item {
                Reveal(delayMs = 220) {
                    LanguageSelector(currentLanguage = locale.language)
                }
            }
            item {
                Reveal(delayMs = 320) {
                    SectionCard(
                        title = stringResource(R.string.home_section_places),
                        icon = Icons.Outlined.LocationOn,
                        accent = MaterialTheme.colorScheme.tertiary,
                        rows = placeRows,
                        emptyText = stringResource(R.string.home_empty_places)
                    )
                }
            }
            item {
                Reveal(delayMs = 380) {
                    SectionCard(
                        title = stringResource(R.string.home_section_dates),
                        icon = Icons.Outlined.CalendarToday,
                        accent = MaterialTheme.colorScheme.primary,
                        rows = dateRows,
                        emptyText = stringResource(R.string.home_empty_dates)
                    )
                }
            }
            item {
                Reveal(delayMs = 440) {
                    SectionCard(
                        title = stringResource(R.string.home_section_people),
                        icon = Icons.Outlined.People,
                        accent = MaterialTheme.colorScheme.secondary,
                        rows = peopleRows,
                        emptyText = stringResource(R.string.home_empty_people)
                    )
                }
            }
            item {
                Reveal(delayMs = 500) {
                    SectionCard(
                        title = stringResource(R.string.home_section_events),
                        icon = Icons.Outlined.Celebration,
                        accent = MaterialTheme.colorScheme.tertiary,
                        rows = eventRows,
                        emptyText = stringResource(R.string.home_empty_events)
                    )
                }
            }
            item {
                Reveal(delayMs = 560) {
                    SectionCard(
                        title = stringResource(R.string.home_section_unknown),
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        accent = MaterialTheme.colorScheme.outline,
                        rows = unknownRows,
                        emptyText = stringResource(R.string.home_empty_unknown)
                    )
                }
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
    val countLabel: String,
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

private fun formatEpoch(epoch: Long?, formatter: DateTimeFormatter, placeholder: String): String {
    if (epoch == null || epoch == 0L) return placeholder
    return Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(formatter)
}

private fun scanStateLabelRes(state: WorkInfo.State?): Int {
    return when (state) {
        WorkInfo.State.RUNNING -> R.string.scan_status_scanning
        WorkInfo.State.ENQUEUED -> R.string.scan_status_queued
        WorkInfo.State.SUCCEEDED -> R.string.scan_status_completed
        WorkInfo.State.FAILED -> R.string.scan_status_failed
        WorkInfo.State.CANCELLED -> R.string.scan_status_cancelled
        WorkInfo.State.BLOCKED -> R.string.scan_status_blocked
        else -> R.string.scan_status_idle
    }
}

@Composable
private fun Reveal(delayMs: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
            slideInVertically(animationSpec = tween(durationMillis = 500)) { it / 6 }
    ) {
        content()
    }
}

@Composable
private fun HeroPanel(mediaCount: String, lastSuccess: String) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.home_hero_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.home_hero_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatPill(
                        title = stringResource(R.string.home_stat_media_count),
                        value = mediaCount,
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        title = stringResource(R.string.home_stat_last_success),
                        value = lastSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScanControlCard(
    scanStateLabel: String,
    scanStateColor: Color,
    isScanning: Boolean,
    mediaCount: String,
    lastScan: String,
    lastSuccess: String,
    errorCount: String,
    hasReadPermission: Boolean,
    hasLocationPermission: Boolean,
    onRequestPermissions: () -> Unit,
    onScan: () -> Unit,
    onRefresh: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_status_title),
                    style = MaterialTheme.typography.titleMedium
                )
                StatusPill(label = scanStateLabel, color = scanStateColor)
            }
            if (isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            StatusRow(label = stringResource(R.string.home_status_media_indexed), value = mediaCount)
            StatusRow(label = stringResource(R.string.home_status_last_scan), value = lastScan)
            StatusRow(label = stringResource(R.string.home_status_last_success), value = lastSuccess)
            StatusRow(label = stringResource(R.string.home_status_errors), value = errorCount)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PermissionChip(
                    enabled = hasReadPermission,
                    label = if (hasReadPermission) {
                        stringResource(R.string.home_permission_media_granted)
                    } else {
                        stringResource(R.string.home_permission_media_required)
                    }
                )
                PermissionChip(
                    enabled = hasLocationPermission,
                    label = if (hasLocationPermission) {
                        stringResource(R.string.home_permission_location_enabled)
                    } else {
                        stringResource(R.string.home_permission_location_disabled)
                    }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val needsPermission = !hasReadPermission || !hasLocationPermission
                Button(
                    onClick = onRequestPermissions,
                    enabled = needsPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (needsPermission) {
                            stringResource(R.string.home_action_grant_permissions)
                        } else {
                            stringResource(R.string.home_action_permissions_ok)
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onScan,
                        enabled = !isScanning && hasReadPermission,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isScanning) {
                                stringResource(R.string.home_action_scan_running)
                            } else {
                                stringResource(R.string.home_action_scan_start)
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = onRefresh,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.home_action_refresh))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PermissionChip(enabled: Boolean, label: String) {
    val icon = if (enabled) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline
    val color = if (enabled) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.error
    }
    AssistChip(
        onClick = {},
        label = { Text(text = label) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, tint = color)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun LanguageSelector(currentLanguage: String) {
    val options = listOf(
        "ko" to stringResource(R.string.language_korean),
        "en" to stringResource(R.string.language_english),
        "ja" to stringResource(R.string.language_japanese)
    )
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.home_language_title),
                style = MaterialTheme.typography.titleSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (language, label) ->
                    val selected = currentLanguage == language
                    FilterChip(
                        selected = selected,
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(language)
                            )
                        },
                        label = { Text(text = label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    rows: List<SectionRow>,
    emptyText: String
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = accent.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (rows.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                rows.forEachIndexed { index, row ->
                    SectionRowItem(row = row)
                    if (index != rows.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
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
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = row.countLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

private fun formatPlaceName(place: PlaceCount, locale: java.util.Locale): String {
    val prefersKorean = locale.language == "ko"
    val name = if (prefersKorean) {
        place.nameKo.ifBlank { place.nameEn }
    } else {
        place.nameEn.ifBlank { place.nameKo }
    }
    return if (place.countryCode.isBlank()) {
        name
    } else {
        "$name (${place.countryCode})"
    }
}
