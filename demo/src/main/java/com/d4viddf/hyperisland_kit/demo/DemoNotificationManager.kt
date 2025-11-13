package com.d4viddf.hyperisland_kit.demo

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.d4viddf.hyperisland_kit.HyperAction
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo
import io.github.d4viddf.hyperisland_kit.models.TimerInfo
import java.util.concurrent.TimeUnit

// --- (Keys are unchanged) ---
private const val ACTION_KEY_TAKEN = "action.taken"
private const val ACTION_KEY_APP_OPEN = "action.app.open"
private const val ACTION_KEY_STOP_PROGRESS = "action.stop"
private const val ACTION_KEY_CLOSE_NOTIFICATION = "action.close"
private const val PIC_KEY_MEDICATION = "pic.medication"
private const val PIC_KEY_DEMO_ICON = "pic.demo.icon"
private const val PIC_KEY_PROGRESS = "pic.progress"
private const val PIC_KEY_COUNTUP = "pic.countup"
private const val PIC_KEY_SIMPLE = "pic.simple"
private const val PIC_KEY_APP_OPEN = "pic.app.open"
private const val PIC_KEY_STOP_ICON = "pic.stop"
private const val PIC_KEY_CLOSE_ICON = "pic.close"

private const val TAG = "DemoNotifManager" // Tag for logging

object DemoNotificationManager {

    private fun hasNotificationPermission(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Notification permission not granted", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun getUniqueNotificationId() = System.currentTimeMillis().toInt()
    private fun showSupportToast(context: Context) {
        if (!HyperIslandNotification.isSupported(context)) {
            Toast.makeText(context, "HyperIsland not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }
    private fun createSimpleAZone(picKey: String, text: String): ImageTextInfoLeft {
        return ImageTextInfoLeft(
            picInfo = PicInfo(type = 1, pic = picKey),
            textInfo = TextInfo(title = text)
        )
    }
    private fun createAppOpenIntent(context: Context, requestCode: Int = 0): PendingIntent {
        Log.d(TAG, "Creating Activity PendingIntent with request code: $requestCode")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun createBroadcastIntent(
        context: Context,
        notificationId: Int,
        action: String,
        toastMessage: String?,
        requestCode: Int
    ): PendingIntent {
        Log.d(TAG, "Creating Broadcast PendingIntent with action: $action, request code: $requestCode")
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            if (toastMessage != null) {
                putExtra(NotificationActionReceiver.EXTRA_TOAST_MESSAGE, toastMessage)
            }
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }


    fun showChatNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val notificationId = getUniqueNotificationId()
        val title = "Chat Notification"
        val text = "This demonstrates the 'ChatInfo' template."
        val openAppIntent = createAppOpenIntent(context, 0)

        // This is an Activity, so type = 1
        val takenPendingIntent = createAppOpenIntent(context, 1)

        val takenAction = HyperAction(
            key = ACTION_KEY_TAKEN,
            title = "Open App",
            pendingIntent = takenPendingIntent,
            actionIntentType = 1, // 1 for Activity
            actionBgColor = "#007AFF"
        )

        val medPicture = HyperPicture(PIC_KEY_MEDICATION, context, R.drawable.rounded_medication_24)

        // 1. Create the builder and add actions/pics
        val hyperIslandBuilder = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .setChatInfo(
                title = "Ibuprofen",
                content = "Next dose: 30 minutes",
                pictureKey = PIC_KEY_MEDICATION,
                actionKeys = listOf(ACTION_KEY_TAKEN) // Pass key to chatInfo
            )
            .setBigIslandInfo(
                createSimpleAZone(PIC_KEY_MEDICATION, "Ibuprofen")
            )
            .setSmallIslandIcon(PIC_KEY_MEDICATION)
            .addAction(takenAction)
            .addPicture(medPicture)

        // 2. Build the two separate parts
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()

        // 3. Create the notification
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppIntent)
            .addExtras(resourceBundle) // <-- Add the resource bundle here
            .build()

        // 4. Add the JSON param string AFTER build
        notification.extras.putString("miui.focus.param", jsonParam)

        Log.d(TAG, "showChatNotification: Posting notification $notificationId")
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showCountdownNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "Countdown Notification"
        val text = "This demonstrates a countdown timer."
        val countdownTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)
        val countdownTimer = TimerInfo(-1, countdownTime, System.currentTimeMillis(), System.currentTimeMillis())
        val demoPicture = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.rounded_timer_arrow_down_24)

        val hyperIslandBuilder = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo(title = "Pizza in oven", timer = countdownTimer, pictureKey = PIC_KEY_DEMO_ICON)
            .setBigIslandCountdown(countdownTime, PIC_KEY_DEMO_ICON) // No actions
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON)
            .addPicture(demoPicture)

        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.rounded_timer_arrow_down_24)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(resourceBundle)
            .build()
        notification.extras.putString("miui.focus.param", jsonParam)

        Log.d(TAG, "showCountdownNotification: Posting notification")
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }

    fun showProgressBarNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "Progress Bar Demo"
        val text = "Showing linear progress bar"
        val progress = 60
        val progressColor = "#007AFF"
        val progressPicture = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_upload_24)

        val hyperIslandBuilder = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo(title = "Uploading file...", content = "60% complete", pictureKey = PIC_KEY_PROGRESS)
            .setProgressBar(progress, progressColor)
            .setBigIslandInfo(createSimpleAZone(PIC_KEY_PROGRESS, "Uploading..."))
            .setSmallIslandIcon(PIC_KEY_PROGRESS)
            .addPicture(progressPicture)

        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.rounded_cloud_upload_24)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(resourceBundle)
            .build()
        notification.extras.putString("miui.focus.param", jsonParam)

        Log.d(TAG, "showProgressBarNotification: Posting notification")
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }

    fun showCircularProgressNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "Circular Progress Demo"
        val text = "Showing circular progress on island"
        val progress = 75
        val progressColor = "#34C759"
        val progressPicture = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_download_24)

        val hyperIslandBuilder = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo(title = "Downloading...", content = "75% complete", pictureKey = PIC_KEY_PROGRESS)
            .setBigIslandProgressCircle(PIC_KEY_PROGRESS, "", progress, progressColor, isCCW = true)
            .setSmallIslandCircularProgress(PIC_KEY_PROGRESS, progress, progressColor, isCCW = true)
            .addPicture(progressPicture)

        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(resourceBundle)
            .build()
        notification.extras.putString("miui.focus.param", jsonParam)

        Log.d(TAG, "showCircularProgressNotification: Posting notification")
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }

    fun showCountUpNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "Count-Up Timer"
        val text = "This demonstrates a count-up timer."
        val startTime = System.currentTimeMillis()
        val countUpTimer = TimerInfo(1, startTime, startTime, System.currentTimeMillis())
        val countUpPicture = HyperPicture(PIC_KEY_COUNTUP, context, R.drawable.rounded_timer_arrow_up_24)

        val hyperIslandBuilder = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo(title = "Recording...", timer = countUpTimer, pictureKey = PIC_KEY_COUNTUP)
            .setBigIslandCountUp(startTime, PIC_KEY_COUNTUP)
            .setSmallIslandIcon(PIC_KEY_COUNTUP)
            .addPicture(countUpPicture)

        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(resourceBundle)
            .build()
        notification.extras.putString("miui.focus.param", jsonParam)

        Log.d(TAG, "showCountUpNotification: Posting notification")
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }


    fun showSimpleSmallIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val notificationId = getUniqueNotificationId()
        val title = "Simple Small Island"
        val text = "Icon on left (small), icon+text (big)."

        val openAppIntentContent = createAppOpenIntent(context, 0)
        val openAppIntentAction = createAppOpenIntent(context, 1) // Activity

        val simplePicture = HyperPicture(PIC_KEY_SIMPLE, context, R.drawable.rounded_arrow_outward_24)

        val appOpenAction = HyperAction(
            key = ACTION_KEY_APP_OPEN,
            title = "Open App",
            context = context,
            drawableRes = R.drawable.rounded_arrow_outward_24, // Not changing
            pendingIntent = openAppIntentAction,
            actionIntentType = 1, // 1 for Activity
            isProgressButton = false
        )

        val bigIslandInfo = ImageTextInfoLeft(
            picInfo = PicInfo(type = 1, pic = PIC_KEY_SIMPLE),
            textInfo = TextInfo(title = "Simple Info", content = "This is the expanded view")
        )

        val hyperIslandBuilder = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Simple Info",
                content = "This is the expanded view",
                pictureKey = PIC_KEY_SIMPLE,
                actionKeys = listOf(ACTION_KEY_APP_OPEN) // <-- Add key to chatInfo
            )
            .setBigIslandInfo(bigIslandInfo)
            .setSmallIslandIcon(PIC_KEY_SIMPLE)
            .addPicture(simplePicture)
            .addAction(appOpenAction) // Add action to builder

        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppIntentContent)
            .addExtras(resourceBundle)
            .build()
        notification.extras.putString("miui.focus.param", jsonParam)

        Log.d(TAG, "showSimpleSmallIslandNotification: Posting notification $notificationId")
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showAppOpenNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "App Open Demo"
        val text = "Tap or drag to open the app."
        val appOpenPicture = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.rounded_arrow_outward_24)
        val openAppIntent = createAppOpenIntent(context, 0)
        val bigIslandInfo = ImageTextInfoLeft(
            picInfo = PicInfo(type = 1, pic = PIC_KEY_APP_OPEN),
            textInfo = TextInfo(title = "Open Demo", content = "Tap or drag")
        )

        val hyperIslandBuilder = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .setBaseInfo(title = "App Open Demo", content = "Tap or drag to open the app", pictureKey = PIC_KEY_APP_OPEN)
            .setBigIslandInfo(bigIslandInfo)
            .setSmallIslandIcon(PIC_KEY_APP_OPEN)
            .addPicture(appOpenPicture)

        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppIntent)
            .addExtras(resourceBundle)
            .build()
        notification.extras.putString("miui.focus.param", jsonParam)

        Log.d(TAG, "showAppOpenNotification: Posting notification")
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }


    fun showMultiActionNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val notificationId = getUniqueNotificationId()
        val title = "Multi-Action Demo"
        val text = "This demonstrates multiple buttons."

        // 1. Create Intents (Correct)
        val stopPendingIntent = createBroadcastIntent(
            context = context,
            notificationId = notificationId,
            action = NotificationActionReceiver.ACTION_SHOW_TOAST_ONLY,
            toastMessage = "Stop Pressed",
            requestCode = notificationId + 1
        )
        val closePendingIntent = createBroadcastIntent(
            context = context,
            notificationId = notificationId,
            action = NotificationActionReceiver.ACTION_CLICK_AND_CANCEL,
            toastMessage = "Notification Closed",
            requestCode = notificationId + 2
        )

        // 2. Create HyperActions (Correct)
        val stopAction = HyperAction(
            key = ACTION_KEY_STOP_PROGRESS,
            title = null,
            context = context,
            drawableRes = R.drawable.rounded_pause_24,
            pendingIntent = stopPendingIntent,
            actionIntentType = 2, // 2 for Broadcast
            isProgressButton = true,
            progress = 10,
            colorProgress = "#D9E0FA",

        )
        val closeAction = HyperAction(
            key = ACTION_KEY_CLOSE_NOTIFICATION,
            title = null,
            context = context,
            drawableRes = R.drawable.rounded_close_24,
            pendingIntent = closePendingIntent,
            actionIntentType = 2, // 2 for Broadcast
            actionBgColor = "#FF3B30"
        )

        // 3. Create HyperPictures (Correct)
        val appPicture = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.round_smart_button_24)
        val stopPicture = HyperPicture(PIC_KEY_STOP_ICON, context, R.drawable.ic_launcher_foreground)
        val closePicture = HyperPicture(PIC_KEY_CLOSE_ICON, context, R.drawable.ic_launcher_foreground)

        // 4. Build Extras
        val hyperIslandBuilder = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Multi-Action",
                content = "Stop or Close",
                pictureKey = PIC_KEY_APP_OPEN,
                actionKeys = listOf(ACTION_KEY_STOP_PROGRESS, ACTION_KEY_CLOSE_NOTIFICATION)
            )
            .setBigIslandInfo(
                createSimpleAZone(PIC_KEY_APP_OPEN, "Actions")
            )
            .setSmallIslandIcon(PIC_KEY_APP_OPEN)
            .addAction(stopAction)
            .addAction(closeAction)
            .addPicture(appPicture)
            .addPicture(stopPicture)
            .addPicture(closePicture)

        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(resourceBundle)
            .build()
        notification.extras.putString("miui.focus.param", jsonParam)

        Log.d(TAG, "showMultiActionNotification: Posting notification $notificationId")
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }
}