package com.ashkite.pictureclassification.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ashkite.pictureclassification.worker.MediaScanScheduler

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sections = listOf(
        "Places",
        "Dates",
        "People",
        "Events",
        "Location Unknown"
    )

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
                Button(
                    onClick = { MediaScanScheduler.enqueueOneTime(context, force = true) },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Start scan")
                }
            }

            items(sections) { label ->
                Card {
                    Text(
                        text = label,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}
