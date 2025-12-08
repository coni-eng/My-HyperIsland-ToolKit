package com.d4viddf.hyperisland_kit.demo

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationLogScreen() {
    val context = LocalContext.current
    val logs = NotificationLogRepository.logs
    var isSettingsOpen by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Notification Inspector", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { isSettingsOpen = !isSettingsOpen }) {
                val rotation by animateFloatAsState(if (isSettingsOpen) 180f else 0f, label = "rotation")
                Icon(Icons.Default.KeyboardArrowDown, "Toggle", modifier = Modifier.rotate(rotation))
            }
        }

        AnimatedVisibility(visible = isSettingsOpen) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Grant 'Notification Access' to spy on system apps.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }, modifier = Modifier.weight(1f)) { Text("Grant Permission") }
                        Button(onClick = { NotificationLogRepository.clear() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Clear") }
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs) { log -> LogItemCard(log) }
        }
    }
}

@Composable
fun LogItemCard(log: NotificationLog) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "arrow")
    val formattedJson = remember(log.jsonParam) { formatJson(log.jsonParam) }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(log.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(log.title, style = MaterialTheme.typography.bodyMedium, maxLines = if (isExpanded) Int.MAX_VALUE else 1)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Icon(Icons.Default.KeyboardArrowDown, "Expand", modifier = Modifier.padding(top = 4.dp).rotate(arrowRotation))
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // --- 1. JSON SECTION ---
                    Text("JSON Payload:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    SelectionContainer {
                        Text(
                            text = formattedJson,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(top = 4.dp).background(Color(0xFF263238), RoundedCornerShape(6.dp)).padding(12.dp).fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- 2. EXTRAS SECTION (Reformatted for Readability) ---
                    Text("Decoded Extras (Pictures & Actions):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            val entries = log.extrasDetails.entries.toList()

                            entries.forEachIndexed { index, (key, value) ->
                                if (key.startsWith("[SECTION")) {
                                    // Section Header (Yellow, bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = value,
                                        color = Color(0xFFFFEB3B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(bottom = 4.dp))
                                } else {
                                    // Regular Entry (Vertical Layout)
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        // Key (Purple)
                                        Text(
                                            text = key,
                                            color = Color(0xFFBB86FC),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        // Value (White, indented)
                                        Text(
                                            text = value,
                                            color = Color(0xFFEEEEEE),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                                        )
                                    }
                                    // Subtle divider between items
                                    if (index < entries.size - 1 && !entries[index + 1].key.startsWith("[SECTION")) {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = Color(0xFF333333)
                                        )
                                    }
                                }
                            }

                            if (log.extrasDetails.isEmpty()) {
                                Text("No relevant extras found.", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatJson(jsonStr: String?): String {
    if (jsonStr.isNullOrEmpty()) return "null"
    return try { JSONObject(jsonStr).toString(4) } catch (e: Exception) { try { org.json.JSONArray(jsonStr).toString(4) } catch (e2: Exception) { jsonStr } }
}