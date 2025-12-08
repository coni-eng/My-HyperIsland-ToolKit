package com.d4viddf.hyperisland_kit.demo

import androidx.compose.runtime.mutableStateListOf

data class NotificationLog(
    val id: String,
    val timestamp: Long,
    val packageName: String,
    val title: String,
    val jsonParam: String?,
    // Stores decoded details about pictures and actions
    val extrasDetails: Map<String, String>
)

object NotificationLogRepository {
    val logs = mutableStateListOf<NotificationLog>()

    fun addLog(log: NotificationLog) {
        logs.add(0, log)
        if (logs.size > 50) {
            logs.removeRange(50, logs.size)
        }
    }

    fun clear() {
        logs.clear()
    }
}