package com.d4viddf.hyperisland_kit.demo

import android.app.Notification
import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

class HyperIslandListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val extras = sbn.notification.extras

        // Filter: Only HyperIsland notifications
        val jsonParam = extras.getString("miui.focus.param") ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "No Title"
        val packageName = sbn.packageName

        val detailsMap = mutableMapOf<String, String>()
        val assetsMap = mutableMapOf<String, String>() // Store paths to saved images

        // 1. Basic Info
        detailsMap["[BASIC]"] = "--- NOTIFICATION SOURCE ---"
        detailsMap["Package"] = packageName
        detailsMap["ID"] = sbn.id.toString()

        // 2. Deep Dump & Asset Extraction
        detailsMap["[EXTRAS]"] = "--- BUNDLE HIERARCHY ---"
        try {
            // Pass assetsMap to collect images
            deepDump(extras, "extras", detailsMap, assetsMap)
        } catch (e: Exception) {
            detailsMap["Error"] = "Failed to dump: ${e.message}"
        }

        Log.d("HyperInspector", "Captured: $title with ${assetsMap.size} assets")

        NotificationLogRepository.addLog(
            NotificationLog(
                id = sbn.key,
                timestamp = System.currentTimeMillis(),
                packageName = packageName,
                title = title,
                jsonParam = jsonParam,
                extrasDetails = detailsMap,
                assets = assetsMap // Save the map of file paths
            )
        )
    }

    private fun deepDump(
        obj: Any?,
        path: String,
        map: MutableMap<String, String>,
        assets: MutableMap<String, String>
    ) {
        if (obj == null) {
            map[path] = "null"
            return
        }

        when (obj) {
            is Bundle -> {
                if (obj.isEmpty) {
                    map[path] = "Bundle{empty}"
                } else {
                    obj.keySet().forEach { key ->
                        if (key != "miui.focus.param") {
                            deepDump(obj.get(key), "$path -> $key", map, assets)
                        }
                    }
                }
            }
            is Array<*> -> {
                map[path] = "Array[${obj.size}]"
                obj.forEachIndexed { index, item ->
                    deepDump(item, "$path[$index]", map, assets)
                }
            }
            is List<*> -> {
                map[path] = "List[${obj.size}]"
                obj.forEachIndexed { index, item ->
                    deepDump(item, "$path[$index]", map, assets)
                }
            }

            // --- ASSET EXTRACTION ---

            is Icon -> {
                val iconInfo = StringBuilder()
                val typeStr = getIconTypeName(obj.type)
                iconInfo.append("Icon ($typeStr)")

                if (obj.type == Icon.TYPE_RESOURCE) {
                    iconInfo.append(" | ${obj.resPackage}:${obj.resId}")
                }

                // Try to save it
                val savedPath = saveIconToStorage(obj, path)
                if (savedPath != null) {
                    assets[path] = savedPath
                    iconInfo.append(" [SAVED]")
                } else {
                    iconInfo.append(" [SAVE FAILED]")
                }

                map[path] = iconInfo.toString()
            }

            is Bitmap -> {
                val savedPath = saveBitmapToStorage(obj, path)
                if (savedPath != null) {
                    assets[path] = savedPath
                    map[path] = "Bitmap (${obj.width}x${obj.height}) [SAVED]"
                } else {
                    map[path] = "Bitmap (${obj.width}x${obj.height}) [SAVE FAILED]"
                }
            }

            is PendingIntent -> map[path] = "PendingIntent (Creator: ${obj.creatorPackage})"
            is Notification.Action -> {
                map["$path.title"] = obj.title.toString()
                deepDump(obj.extras, "$path.extras", map, assets)
            }
            is CharSequence -> map[path] = "\"$obj\""
            is Number, is Boolean -> map[path] = obj.toString()
            else -> map[path] = "${obj.javaClass.simpleName}: $obj"
        }
    }

    private fun saveIconToStorage(icon: Icon, keyName: String): String? {
        return try {
            // Load the drawable (Context is this service)
            val drawable = icon.loadDrawable(this) ?: return null

            val bitmap = if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                // Handle VectorDrawables, etc.
                if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) return null
                val bmp = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }

            saveBitmapToStorage(bitmap, keyName)
        } catch (e: Exception) {
            Log.e("HyperInspector", "Failed to save icon $keyName: ${e.message}")
            null
        }
    }

    private fun saveBitmapToStorage(bitmap: Bitmap, keyName: String): String? {
        return try {
            // Clean up the key name to be a valid filename
            val safeName = keyName.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val fileName = "asset_${System.currentTimeMillis()}_$safeName.png"
            val file = File(cacheDir, fileName) // Save to cache dir

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("HyperInspector", "Failed to save bitmap $keyName: ${e.message}")
            null
        }
    }

    private fun getIconTypeName(type: Int): String {
        return when (type) {
            Icon.TYPE_BITMAP -> "BITMAP"
            Icon.TYPE_RESOURCE -> "RESOURCE"
            Icon.TYPE_DATA -> "DATA"
            Icon.TYPE_URI -> "URI"
            Icon.TYPE_ADAPTIVE_BITMAP -> "ADAPTIVE"
            Icon.TYPE_URI_ADAPTIVE_BITMAP -> "URI_ADAPTIVE"
            else -> "TYPE_$type"
        }
    }

}
