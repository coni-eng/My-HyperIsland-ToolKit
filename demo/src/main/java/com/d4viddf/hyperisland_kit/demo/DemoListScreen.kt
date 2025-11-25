package com.d4viddf.hyperisland_kit.demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DemoListScreen(
    navController: NavController
) {
    val context = LocalContext.current

    CheckPermissionLost(navController = navController)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // --- 1. Simple Demos ---
        item {
            DemoCard(
                title = "App Open Demo",
                description = "A basic notification. Tap the notification or drag the island to open the app.",
                onClick = {
                    DemoNotificationManager.showAppOpenNotification(context)
                }
            )
        }

        item {
            DemoCard(
                title = "Chat Info (Text Action)",
                description = "Shows a 'chat' style panel with a text-only 'Open App' action button.",
                onClick = {
                    DemoNotificationManager.showChatNotification(context)
                }
            )
        }

        item {
            DemoCard(
                title = "Chat Info (Icon Action)",
                description = "Uses 'chatInfo' to show a simple panel with an 'Open App' icon button.",
                onClick = {
                    DemoNotificationManager.showSimpleSmallIslandNotification(context)
                }
            )
        }

        // --- NEW DEMO ---
        item {
            DemoCard(
                title = "Right Image Demo",
                description = "Shows content with an image on the right side of the Expanded Island.",
                onClick = {
                    DemoNotificationManager.showRightImageNotification(context)
                }
            )
        }

        item {
            DemoCard(
                title = "Split Info (Left & Right)",
                description = "Shows content on both the left (Icon+Text) and right (Text only) sides of the Expanded Island.",
                onClick = {
                    DemoNotificationManager.showSplitIslandNotification(context)
                }
            )
        }

        // --- 2. Progress Bars ---
        item {
            DemoCard(
                title = "Linear Progress Bar",
                description = "Shows a 'chat' style (expanded) with a linear progress bar below it.",
                onClick = {
                    DemoNotificationManager.showProgressBarNotification(context)
                }
            )
        }

        item {
            DemoCard(
                title = "Circular Progress Demo",
                description = "Shows circular progress on both the big island (expanded) and small island (summary).",
                onClick = {
                    DemoNotificationManager.showCircularProgressNotification(context)
                }
            )
        }

        // --- 3. Timers ---
        item {
            DemoCard(
                title = "Countdown Notification",
                description = "Shows a 15-minute countdown in chat, big island (expanded), and summary views.",
                onClick = {
                    DemoNotificationManager.showCountdownNotification(context)
                }
            )
        }

        item {
            DemoCard(
                title = "Count-Up Timer",
                description = "Shows a timer counting *up* in the expanded view and a simple icon in summary.",
                onClick = {
                    DemoNotificationManager.showCountUpNotification(context)
                }
            )
        }

        // --- 4. Multi-Action ---
        item {
            DemoCard(
                title = "Multi-Action Demo",
                description = "Shows a progress button and a standard 'Close' button. Tap 'Close' to dismiss or 'Stop' to see a toast.",
                onClick = {
                    DemoNotificationManager.showMultiActionNotification(context)
                }
            )
        }
    }
}

@Composable
fun DemoCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}