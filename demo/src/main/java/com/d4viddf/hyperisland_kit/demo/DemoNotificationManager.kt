package com.d4viddf.hyperisland_kit.demo

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.d4viddf.hyperisland_kit.HyperAction
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoRight
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo
import io.github.d4viddf.hyperisland_kit.models.TimerInfo
import java.util.concurrent.TimeUnit

// --- Keys ---
private const val ACTION_KEY_TAKEN = "action.taken"
private const val ACTION_KEY_APP_OPEN = "action.app.open"
private const val ACTION_KEY_STOP_PROGRESS = "action.stop"
private const val ACTION_KEY_CLOSE_NOTIFICATION = "action.close"
private const val ACTION_KEY_HINT = "action.hint"
private const val ACTION_KEY_TEST_1 = "action.test.1"
private const val ACTION_KEY_TEST_2 = "action.test.2"

private const val PIC_KEY_MEDICATION = "medication"
private const val PIC_KEY_DEMO_ICON = "pic.demo.icon"
private const val PIC_KEY_PROGRESS = "pic.progress"
private const val PIC_KEY_COUNTUP = "pic.countup"
private const val PIC_KEY_SIMPLE = "pic.simple"
private const val PIC_KEY_APP_OPEN = "pic.app.open"
private const val PIC_KEY_STOP_ICON = "pic.stop"
private const val PIC_KEY_CLOSE_ICON = "pic.close"
private const val PIC_KEY_RIGHT_SIDE = "pic.right.side"
private const val PIC_KEY_CAR = "pic.car"
private const val PIC_KEY_DOT_SEL = "pic.dot.sel"
private const val PIC_KEY_DOT_UNSEL = "pic.dot.unsel"
private const val PIC_KEY_FLAG_SEL = "pic.flag.sel"
private const val PIC_KEY_FLAG_UNSEL = "pic.flag.unsel"

private const val TAG = "DemoNotifManager"

object DemoNotificationManager {

    // --- Helpers ---
    private fun hasNotificationPermission(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Notification permission not granted", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun getUniqueNotificationId() = System.currentTimeMillis().toInt()
    private fun showSupportToast(context: Context) {
        if (!HyperIslandNotification.isSupported(context)) {
            Toast.makeText(context, "HyperIsland not supported on this device", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun createSimpleAZone(picKey: String, text: String): ImageTextInfoLeft {
        return ImageTextInfoLeft(
            picInfo = PicInfo(type = 1, pic = picKey),
            textInfo = TextInfo(title = text)
        )
    }

    private fun createAppOpenIntent(context: Context, requestCode: Int = 0): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createBroadcastIntent(
        context: Context,
        notificationId: Int,
        action: String,
        toastMessage: String?,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            if (toastMessage != null) {
                putExtra(NotificationActionReceiver.EXTRA_TOAST_MESSAGE, toastMessage)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Creates a bitmap-based Icon with built-in padding (margin) and a specific tint.
     */
    private fun createCustomIcon(
        context: Context,
        drawableResId: Int,
        color: Int,
        paddingFactor: Float = 0.25f
    ): Icon {
        val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate()
            ?: return Icon.createWithResource(context, drawableResId)

        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val padding = (size * paddingFactor).toInt()
        drawable.setBounds(padding, padding, size - padding, size - padding)
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        drawable.draw(canvas)
        return Icon.createWithBitmap(bitmap)
    }

    // ============================================================================================
    // STANDARD DEMOS (Using Builder)
    // ============================================================================================

    fun showAppOpenNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val title = "App Open Demo"
        val text = "Tap or drag to open the app."
        val appOpenPicture =
            HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.rounded_arrow_outward_24)
        val openAppIntent = createAppOpenIntent(context, 0)
        val bigIslandInfo = ImageTextInfoLeft(
            picInfo = PicInfo(type = 1, pic = PIC_KEY_APP_OPEN),
            textInfo = TextInfo(title = "Open Demo", content = "Tap or drag")
        )
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setSmallWindowTarget("${context.packageName}.MainActivity").setBaseInfo(
            title = "App Open Demo",
            content = "Tap or drag to open the app",
            pictureKey = PIC_KEY_APP_OPEN
        ).setBigIslandInfo(bigIslandInfo).setSmallIslandIcon(PIC_KEY_APP_OPEN)
            .addPicture(appOpenPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showChatNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Chat Notification"
        val text = "This demonstrates the 'ChatInfo' template."
        val openAppIntent = createAppOpenIntent(context, 0)
        val takenPendingIntent = createAppOpenIntent(context, 1)
        val takenAction = HyperAction(
            key = ACTION_KEY_TAKEN,
            title = "Open App",
            pendingIntent = takenPendingIntent,
            actionIntentType = 1,
            actionBgColor = "#007AFF"
        )
        val medPicture = HyperPicture(PIC_KEY_MEDICATION, context, R.drawable.rounded_medication_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setSmallWindowTarget("${context.packageName}.MainActivity").setChatInfo(
            title = "Ibuprofen",
            content = "Next dose: 30 minutes",
            pictureKey = "miui.focus.pic_$PIC_KEY_MEDICATION",
            actionKeys = listOf(ACTION_KEY_TAKEN)
        ).setBigIslandInfo(createSimpleAZone(PIC_KEY_MEDICATION, "Ibuprofen"))
            .setSmallIslandIcon(PIC_KEY_MEDICATION).addAction(takenAction).addPicture(medPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showSimpleSmallIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Simple Small Island"
        val text = "Icon on left (small), icon+text (big)."
        val openAppIntentContent = createAppOpenIntent(context, 0)
        val openAppIntentAction = createAppOpenIntent(context, 1)
        val simplePicture =
            HyperPicture(PIC_KEY_SIMPLE, context, R.drawable.rounded_arrow_outward_24)
        val appOpenAction = HyperAction(
            key = ACTION_KEY_APP_OPEN,
            title = "Open App",
            context = context,
            drawableRes = R.drawable.rounded_arrow_outward_24,
            pendingIntent = openAppIntentAction,
            actionIntentType = 1,
            isProgressButton = false
        )
        val bigIslandInfo = ImageTextInfoLeft(
            picInfo = PicInfo(type = 1, pic = PIC_KEY_SIMPLE),
            textInfo = TextInfo(title = "Simple Info", content = "This is the expanded view")
        )
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Simple Info",
                content = "This is the expanded view",
                pictureKey = PIC_KEY_SIMPLE,
                actionKeys = listOf(ACTION_KEY_APP_OPEN)
            ).setBigIslandInfo(bigIslandInfo).setSmallIslandIcon(PIC_KEY_SIMPLE)
            .addPicture(simplePicture).addAction(appOpenAction)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntentContent).addExtras(resourceBundle)
            .build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showRightImageNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Right Image Demo"
        val text = "Expanded island has an image on the right."
        val openAppIntent = createAppOpenIntent(context, 0)
        val leftPic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val rightPic = HyperPicture(PIC_KEY_RIGHT_SIDE, context, R.drawable.rounded_medication_24)
        val leftInfo = ImageTextInfoLeft(
            picInfo = PicInfo(type = 1, pic = PIC_KEY_DEMO_ICON),
            textInfo = TextInfo(title = "Left Info")
        )
        val rightInfo = ImageTextInfoRight(
            picInfo = PicInfo(type = 1, pic = PIC_KEY_RIGHT_SIDE),
            textInfo = TextInfo(title = "Right", content = "With Image")
        )
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Right Image Demo",
                content = "Check the expanded island",
                pictureKey = PIC_KEY_DEMO_ICON
            ).setBigIslandInfo(left = leftInfo, right = rightInfo)
            .setSmallIsland(aZone = leftInfo, bZone = rightInfo).addPicture(leftPic)
            .addPicture(rightPic)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showSplitIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Split Island"
        val text = "Left & Right content on Big Island."
        val openAppIntent = createAppOpenIntent(context, 0)
        val demoPicture =
            HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val leftInfo = ImageTextInfoLeft(
            picInfo = PicInfo(type = 1, pic = PIC_KEY_DEMO_ICON),
            textInfo = TextInfo(title = "Left")
        )
        val rightInfo =
            ImageTextInfoRight(textInfo = TextInfo(title = "Right", content = "Content"))
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Split Info",
                content = "Left & Right Content",
                pictureKey = PIC_KEY_DEMO_ICON
            ).setBigIslandInfo(left = leftInfo, right = rightInfo)
            .setSmallIsland(aZone = leftInfo, bZone = rightInfo).addPicture(demoPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showHintInfoNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Hint Info Demo"
        val text = "Shows a hint above the island."
        val openAppIntent = createAppOpenIntent(context, 0)
        val hintIntent = createAppOpenIntent(context, 1)
        val hintAction = HyperAction(
            key = ACTION_KEY_HINT,
            title = "View",
            pendingIntent = hintIntent,
            actionIntentType = 1
        )
        val demoPicture =
            HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Main Content",
                content = "Look above!",
                pictureKey = PIC_KEY_DEMO_ICON
            ).setBigIslandInfo(createSimpleAZone(PIC_KEY_DEMO_ICON, "Main"))
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON)
            .setHintInfo(title = "2 New Messages", actionKey = ACTION_KEY_HINT)
            .addAction(hintAction).addPicture(demoPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showMultiNodeProgressNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Node Progress"
        val text = "Segmented progress on notification panel."
        val openAppIntent = createAppOpenIntent(context, 0)
        val demoPicture =
            HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setBaseInfo(
                title = "Processing Order",
                content = "Your order is being prepared.",
                pictureKey = PIC_KEY_DEMO_ICON,
                type = 2
            ).setBigIslandInfo(createSimpleAZone(PIC_KEY_DEMO_ICON, "Processing"))
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON)
            .setMultiProgress(title = "Step 2 of 4", progress = 50, color = "#34C759", points = 3)
            .addPicture(demoPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        Log.d(TAG, "showMultiNodeProgressNotification: Posting notification")
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showColoredBaseNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Colored Title"
        val text = "Custom title color in Base Info."
        val openAppIntent = createAppOpenIntent(context, 0)
        val demoPicture =
            HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setBaseInfo(
                title = "Urgent Alert",
                content = "This title is red.",
                pictureKey = PIC_KEY_DEMO_ICON,
                titleColor = "#FF3B30"
            ).setBigIslandInfo(createSimpleAZone(PIC_KEY_DEMO_ICON, "Alert"))
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON).addPicture(demoPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showProgressBarNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Icon Progress"
        val text = "Progress bar with icons."
        val openAppIntent = createAppOpenIntent(context, 0)
        val mainPic = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_upload_24)
        val picCar = HyperPicture(PIC_KEY_CAR, context, R.drawable.rounded_arrow_outward_24)
        val picDotSel = HyperPicture(PIC_KEY_DOT_SEL, context, R.drawable.round_smart_button_24)
        val picDotUnsel =
            HyperPicture(PIC_KEY_DOT_UNSEL, context, R.drawable.ic_launcher_foreground)
        val picFlagSel = HyperPicture(PIC_KEY_FLAG_SEL, context, R.drawable.ic_launcher_foreground)
        val picFlagUnsel =
            HyperPicture(PIC_KEY_FLAG_UNSEL, context, R.drawable.ic_launcher_foreground)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Delivery Status",
                content = "Arriving in 5 mins...",
                pictureKey = PIC_KEY_PROGRESS
            )
            .setBigIslandInfo(createSimpleAZone(PIC_KEY_PROGRESS, "Arriving"))
            .setSmallIslandIcon(PIC_KEY_PROGRESS)
            .setProgressBar(
                progress = 70,
                color = "#FF8514",
                colorEnd = "#FF0000",
                picForwardKey = PIC_KEY_CAR,
                picMiddleKey = PIC_KEY_DOT_SEL,
                picMiddleUnselectedKey = PIC_KEY_DOT_UNSEL,
                picEndKey = PIC_KEY_FLAG_SEL,
                picEndUnselectedKey = PIC_KEY_FLAG_UNSEL
            )
            .addPicture(mainPic).addPicture(picCar).addPicture(picDotSel).addPicture(picDotUnsel)
            .addPicture(picFlagSel).addPicture(picFlagUnsel)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.rounded_cloud_upload_24).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        Log.d(TAG, "showProgressBarNotification: Posting notification")
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showCircularProgressNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val title = "Circular Progress Demo"
        val text = "Showing circular progress on island"
        val progress = 75
        val progressColor = "#34C759"
        val progressPicture =
            HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_download_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Downloading...",
                content = "75% complete",
                pictureKey = PIC_KEY_PROGRESS
            ).setBigIslandProgressCircle(
            PIC_KEY_PROGRESS,
            "Downloading",
            progress,
            progressColor,
            isCCW = true
        ).setSmallIslandCircularProgress(PIC_KEY_PROGRESS, progress, progressColor, isCCW = true)
            .addPicture(progressPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showCountdownNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val title = "Countdown Notification"
        val text = "This demonstrates a countdown timer."
        val countdownTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)
        val countdownTimer =
            TimerInfo(-1, countdownTime, System.currentTimeMillis(), System.currentTimeMillis())
        val demoPicture =
            HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.rounded_timer_arrow_down_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Pizza in oven",
                timer = countdownTimer,
                pictureKey = PIC_KEY_DEMO_ICON
            ).setBigIslandCountdown(countdownTime, PIC_KEY_DEMO_ICON)
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON).addPicture(demoPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.rounded_timer_arrow_down_24).setContentTitle(title)
            .setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showCountUpNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val title = "Count-Up Timer"
        val text = "This demonstrates a count-up timer."
        val startTime = System.currentTimeMillis()
        val countUpTimer = TimerInfo(1, startTime, startTime, System.currentTimeMillis())
        val countUpPicture =
            HyperPicture(PIC_KEY_COUNTUP, context, R.drawable.rounded_timer_arrow_up_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(title = "Recording...", timer = countUpTimer, pictureKey = PIC_KEY_COUNTUP)
            .setBigIslandCountUp(startTime, PIC_KEY_COUNTUP).setSmallIslandIcon(PIC_KEY_COUNTUP)
            .addPicture(countUpPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showMultiActionNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Multi-Action Demo"
        val text = "This demonstrates multiple buttons."
        val stopPendingIntent = createBroadcastIntent(
            context,
            notificationId,
            NotificationActionReceiver.ACTION_SHOW_TOAST_ONLY,
            "Stop Pressed",
            notificationId + 1
        )
        val closePendingIntent = createBroadcastIntent(
            context,
            notificationId,
            NotificationActionReceiver.ACTION_CLICK_AND_CANCEL,
            "Notification Closed",
            notificationId + 2
        )
        val stopAction = HyperAction(
            key = ACTION_KEY_STOP_PROGRESS,
            title = null,
            context = context,
            drawableRes = R.drawable.rounded_pause_24,
            pendingIntent = stopPendingIntent,
            actionIntentType = 2,
            isProgressButton = true,
            progress = 10,
            colorReach = "#D9E0FA",
            isCCW = false
        )
        val closeAction = HyperAction(
            key = ACTION_KEY_CLOSE_NOTIFICATION,
            title = "Close",
            pendingIntent = closePendingIntent,
            actionIntentType = 2,
            actionBgColor = "#FF3B30"
        )
        val appPicture = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.round_smart_button_24)
        val stopPicture =
            HyperPicture(PIC_KEY_STOP_ICON, context, R.drawable.ic_launcher_foreground)
        val closePicture =
            HyperPicture(PIC_KEY_CLOSE_ICON, context, R.drawable.ic_launcher_foreground)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Multi-Action",
                content = "Stop or Close",
                pictureKey = PIC_KEY_APP_OPEN,
                actionKeys = listOf(ACTION_KEY_STOP_PROGRESS, ACTION_KEY_CLOSE_NOTIFICATION)
            ).setBigIslandInfo(createSimpleAZone(PIC_KEY_APP_OPEN, "Actions"))
            .setSmallIslandIcon(PIC_KEY_APP_OPEN).addAction(stopAction).addAction(closeAction)
            .addPicture(appPicture).addPicture(stopPicture).addPicture(closePicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    fun showConfigurableNotification(
        context: Context,
        timeout: Long,
        enableFloat: Boolean,
        isShowNotification: Boolean
    ) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val title = "Configurable Demo"
        val text = "Timeout: ${timeout}ms, Float: $enableFloat"
        val openAppIntent = createAppOpenIntent(context, 0)
        val demoPicture =
            HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .setChatInfo(title = title, content = text, pictureKey = PIC_KEY_DEMO_ICON)
            .setBigIslandInfo(createSimpleAZone(PIC_KEY_DEMO_ICON, "Configured"))
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON).addPicture(demoPicture)
            .setEnableFloat(enableFloat).setShowNotification(isShowNotification)
        if (timeout > 0) hyperIslandBuilder.setTimeout(timeout)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // ============================================================================================
    // RAW JSON TESTS
    // ============================================================================================

    // 1. Raw BaseInfo (Unchanged)
    fun showRawBaseInfoFull(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)

        val builder = HyperIslandNotification.Builder(context, "demo", "Raw Base").addPicture(pic)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_base_test","updatable":true,"ticker":"Raw Base","isShownNotification":true,"baseInfo":{"type":2,"title":"Major Alert","subTitle":"Subtitle","extraTitle":"Extra","specialTitle":"Special","content":"Content Line 1","subContent":"Content Line 2","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON","colorTitle":"#FF3B30","colorSubTitle":"#007AFF","colorExtraTitle":"#34C759","colorSpecialTitle":"#FFFFFF","colorSpecialBg":"#FF3B30","showDivider":true,"showContentDivider":true},"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Raw BaseInfo")
            .setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 2. Raw HighlightInfo (Unchanged)
    fun showRawHighlightInfo(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "demo", "Highlight").addPicture(pic)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_highlight","updatable":true,"ticker":"Highlight","isShownNotification":true,"highlightInfo":{"title":"45 Mins","content":"Recording...","subContent":"Meeting","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON","colorTitle":"#FF8514","colorContent":"#999999"},"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Raw HighlightInfo")
            .setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 3. Raw HighlightInfoV3 (Unchanged)
    fun showRawHighlightInfoV3(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val btnIcon = HyperPicture("pic_cart", context, R.drawable.rounded_cloud_download_24)
        val testAction = HyperAction(
            key = ACTION_KEY_TEST_1,
            title = "Buy Now",
            pendingIntent = createAppOpenIntent(context, 1),
            actionIntentType = 1,
            actionBgColor = "#007AFF"
        )
        val builder = HyperIslandNotification.Builder(context, "demo", "V3").addPicture(pic)
            .addPicture(btnIcon).addAction(testAction)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_v3","updatable":true,"ticker":"V3","isShownNotification":true,"baseInfo":{"type":1,"title":"Promo Alert","content":"Flash sale ending soon!","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"highlightInfoV3":{"primaryText":"$999","secondaryText":"$1200","highLightText":"SALE","primaryColor":"#FF3B30","secondaryColor":"#999999","highLightTextColor":"#FFFFFF","highLightbgColor":"#FF3B30","actionInfo":{"actionTitle":"Buy Now","actionIcon":"miui.focus.pic_pic_cart","actionBgColor":"#007AFF","actionTitleColor":"#FFFFFF","actionIntentType":1,"actionIntent":"miui.focus.action_$ACTION_KEY_TEST_1"}},"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Raw HighlightInfoV3")
            .setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 4. Raw TextButton (Unchanged)
    fun showRawTextButton(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val btnIcon = HyperPicture("pic_btn_icon", context, R.drawable.rounded_arrow_outward_24)
        val action1 = HyperAction(ACTION_KEY_TEST_1, "A", createAppOpenIntent(context, 1), 1)
        val action2 = HyperAction(ACTION_KEY_TEST_2, "B", createAppOpenIntent(context, 2), 1)
        val builder = HyperIslandNotification.Builder(context, "demo", "TextBtn").addPicture(pic)
            .addPicture(btnIcon).addAction(action1).addAction(action2)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_text_btn","updatable":true,"ticker":"Text Buttons","isShownNotification":true,"baseInfo":{"type":2,"title":"Text Button + Icon","content":"Custom colors & icons","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"textButton":[{"actionTitle":"Accept","actionIcon":"miui.focus.pic_pic_btn_icon","actionIconDark":"miui.focus.pic_pic_btn_icon","actionBgColor":"#34C759","actionBgColorDark":"#34C759","actionTitleColor":"#FFFFFF","actionTitleColorDark":"#FFFFFF","actionIntentType":1,"actionIntent":"miui.focus.action_$ACTION_KEY_TEST_1","type":0},{"actionTitle":"Reject","actionIcon":"miui.focus.pic_pic_btn_icon","actionIconDark":"miui.focus.pic_pic_btn_icon","actionBgColor":"#FF3B30","actionBgColorDark":"#FF3B30","actionTitleColor":"#FFFFFF","actionTitleColorDark":"#FFFFFF","actionIntentType":1,"actionIntent":"miui.focus.action_$ACTION_KEY_TEST_2","type":0}],"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Raw TextButton")
            .setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 5. Raw Colored Actions (HintInfo) (Unchanged)
    fun showRawColoredActions(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val iconBtn = HyperPicture("pic_btn_icon", context, R.drawable.rounded_arrow_outward_24)
        val actionGreen =
            HyperAction(ACTION_KEY_TEST_1, "Ignored", createAppOpenIntent(context, 1), 1)
        val builder = HyperIslandNotification.Builder(context, "demo", "Colors").addPicture(pic)
            .addPicture(iconBtn).addAction(actionGreen)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_colored_actions","updatable":true,"ticker":"Colors","isShownNotification":true,"baseInfo":{"type":1,"title":"Colored Action","content":"Using HintInfo Template","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"hintInfo":{"type":1,"title":"Action Required","content":"Tap the green button","actionInfo":{"action":"miui.focus.action_$ACTION_KEY_TEST_1","actionTitle":"Green Button","actionIcon":"miui.focus.pic_pic_btn_icon","actionIconDark":"miui.focus.pic_pic_btn_icon","actionBgColor":"#34C759","actionBgColorDark":"#34C759","actionTitleColor":"#FFFFFF","actionTitleColorDark":"#FFFFFF","actionIntentType":1,"actionIntent":"miui.focus.action_$ACTION_KEY_TEST_1"}},"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Raw Colored Actions")
            .setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 6. Raw Colored Text Buttons (TextButton) (Unchanged)
    fun showRawColoredTextButtons(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val btnIcon = HyperPicture("pic_btn_icon", context, R.drawable.rounded_arrow_outward_24)
        val action1 = HyperAction(ACTION_KEY_TEST_1, "A", createAppOpenIntent(context, 1), 1)
        val action2 = HyperAction(ACTION_KEY_TEST_2, "B", createAppOpenIntent(context, 2), 1)
        val builder = HyperIslandNotification.Builder(context, "demo", "TextBtn").addPicture(pic)
            .addPicture(btnIcon).addAction(action1).addAction(action2)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_text_btn","updatable":true,"ticker":"Text Buttons","isShownNotification":true,"baseInfo":{"type":2,"title":"Text Button + Icon","content":"Custom colors & icons","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"textButton":[{"actionTitle":"Accept","actionIcon":"miui.focus.pic_pic_btn_icon","actionIconDark":"miui.focus.pic_pic_btn_icon","actionBgColor":"#34C759","actionBgColorDark":"#34C759","actionTitleColor":"#FFFFFF","actionTitleColorDark":"#FFFFFF","actionIntentType":1,"actionIntent":"miui.focus.action_$ACTION_KEY_TEST_1","type":0},{"actionTitle":"Reject","actionIcon":"miui.focus.pic_pic_btn_icon","actionIconDark":"miui.focus.pic_pic_btn_icon","actionBgColor":"#FF3B30","actionBgColorDark":"#FF3B30","actionTitleColor":"#FFFFFF","actionTitleColorDark":"#FFFFFF","actionIntentType":1,"actionIntent":"miui.focus.action_$ACTION_KEY_TEST_2","type":0}],"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Raw Colored Text Buttons").setContentIntent(openAppIntent)
            .addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 7. Raw Call Notification (Unchanged)
    fun showRawCallNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val avatarPic = HyperPicture("pic_avatar", context, R.drawable.ic_launcher_foreground)
        val iconDecline = HyperPicture("pic_decline", context, R.drawable.rounded_pause_24)
        val iconAnswer = HyperPicture("pic_answer", context, R.drawable.round_smart_button_24)
        val intentDecline = createAppOpenIntent(context, 1)
        val intentAnswer = createAppOpenIntent(context, 2)
        val actionDecline = HyperAction(ACTION_KEY_TEST_1, "Decline", intentDecline, 1)
        val actionAnswer = HyperAction(ACTION_KEY_TEST_2, "Answer", intentAnswer, 1)
        val builder = HyperIslandNotification.Builder(context, "demo", "Call").addPicture(avatarPic)
            .addPicture(iconDecline).addPicture(iconAnswer).addAction(actionDecline)
            .addAction(actionAnswer).setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_call","updatable":true,"ticker":"Incoming Call","isShownNotification":true,"baseInfo":{"type":2,"title":"Incoming Call","content":"John Doe","picFunction":"miui.focus.pic_pic_avatar"},"textButton":[{"actionTitle":"Decline","actionIcon":"miui.focus.pic_pic_decline","actionIconDark":"miui.focus.pic_pic_decline","actionBgColor":"#FF3B30","actionBgColorDark":"#FF3B30","actionTitleColor":"#FFFFFF","actionTitleColorDark":"#FFFFFF","actionIntent":"miui.focus.action_$ACTION_KEY_TEST_1","actionIntentType":1,"type":0},{"actionTitle":"Answer","actionIcon":"miui.focus.pic_pic_answer","actionIconDark":"miui.focus.pic_pic_answer","actionBgColor":"#34C759","actionBgColorDark":"#34C759","actionTitleColor":"#FFFFFF","actionTitleColorDark":"#FFFFFF","actionIntent":"miui.focus.action_$ACTION_KEY_TEST_2","actionIntentType":1,"type":0}],"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_pic_avatar"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Raw Call Notification").setContentIntent(openAppIntent)
            .addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 8. Raw Icon Buttons (Unchanged)
    fun showRawIconButtons(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val iconPrev = HyperPicture("pic_prev", context, R.drawable.rounded_timer_arrow_down_24)
        val iconNext = HyperPicture("pic_next", context, R.drawable.rounded_timer_arrow_up_24)
        val intentPrev = createAppOpenIntent(context, 1)
        val intentNext = createAppOpenIntent(context, 2)
        val actionPrev = HyperAction(ACTION_KEY_TEST_1, "Prev", intentPrev, 1)
        val actionNext = HyperAction(ACTION_KEY_TEST_2, "Next", intentNext, 1)
        val builder = HyperIslandNotification.Builder(context, "demo", "Icons").addPicture(pic)
            .addPicture(iconPrev).addPicture(iconNext).addAction(actionPrev).addAction(actionNext)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_icon_btns","updatable":true,"ticker":"Icon Buttons","isShownNotification":true,"baseInfo":{"type":2,"title":"Music Control","content":"Icon Only Buttons","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"actions":[{"actionIcon":"miui.focus.pic_pic_prev","actionIconDark":"miui.focus.pic_pic_prev","actionBgColor":"#E0E0E0","actionBgColorDark":"#333333","actionIntent":"miui.focus.action_$ACTION_KEY_TEST_1","actionIntentType":1},{"actionIcon":"miui.focus.pic_pic_next","actionIconDark":"miui.focus.pic_pic_next","actionBgColor":"#E0E0E0","actionBgColorDark":"#333333","actionIntent":"miui.focus.action_$ACTION_KEY_TEST_2","actionIntentType":1}],"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Raw Icon Buttons")
            .setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 9. Raw Progress + Color Button (Unchanged)
    fun showRawProgressAndColorButton(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val progressColor = Color.parseColor("#34C759")
        val greenStopIcon =
            createCustomIcon(context, R.drawable.rounded_pause_24, progressColor, 0.20f)
        val closeDrawable = android.R.drawable.ic_menu_close_clear_cancel
        val whiteCloseIcon = createCustomIcon(context, closeDrawable, Color.WHITE, 0.20f)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.rounded_timer_arrow_down_24)
        val iconStop = HyperPicture("pic_stop", greenStopIcon)
        val iconClose = HyperPicture("pic_close", whiteCloseIcon)
        val intentProgress = createAppOpenIntent(context, 1)
        val intentClose = createAppOpenIntent(context, 2)
        val actionProgress = HyperAction(ACTION_KEY_TEST_1, "Progress", intentProgress, 1)
        val actionClose = HyperAction(ACTION_KEY_TEST_2, "Close", intentClose, 1)
        val builder = HyperIslandNotification.Builder(context, "demo", "Mix").addPicture(pic)
            .addPicture(iconStop).addPicture(iconClose).addAction(actionProgress)
            .addAction(actionClose).setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_mix_btns","updatable":true,"ticker":"Mixed Buttons","isShownNotification":true,"baseInfo":{"type":2,"title":"Downloading...","content":"Progress + Close","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"actions":[{"type":1,"actionIntent":"miui.focus.action_$ACTION_KEY_TEST_1","actionIntentType":1,"actionIcon":"miui.focus.pic_pic_stop","actionIconDark":"miui.focus.pic_pic_stop","progressInfo":{"progress":65,"colorProgress":"#34C759","colorProgressDark":"#34C759","colorProgressEnd":"#E0E0E0","colorProgressEndDark":"#333333","isCCW":true}},{"action":"miui.focus.action_$ACTION_KEY_TEST_2","type":0,"actionTitle":"Close","actionIcon":"miui.focus.pic_pic_close","actionIconDark":"miui.focus.pic_pic_close","actionBgColor":"#FF6700","actionBgColorDark":"#FF6700","actionTitleColor":"#FFFFFF","actionTitleColorDark":"#FFFFFF","actionIntent":"miui.focus.action_$ACTION_KEY_TEST_2","actionIntentType":1}],"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Raw Progress + Color Button").setContentIntent(openAppIntent)
            .addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 10. Raw BgInfo (Unchanged)
    fun showRawBgInfo(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "demo", "BgInfo").addPicture(pic)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_bg_info","updatable":true,"ticker":"BgInfo Demo","isShownNotification":true,"baseInfo":{"type":2,"title":"Custom Background","content":"This notification has a colored bg.","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"bgInfo":{"type":0,"colorBg":"#E6F0FF","picBg":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"param_island":{"islandProperty":1,"highlightColor":"#FF3B30","smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}},"bigIslandArea":{"imageTextInfoLeft":{"type":1,"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"textInfo":{"title":"Background Info","showHighlightColor":true}}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Raw BgInfo")
            .setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    // 11. Raw Icon+Bg Actions (UPDATED with new showRawActionIconsWithBg)
    fun showRawActionIconsWithBg(context: Context) {
        if (!hasNotificationPermission(context)) return
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val iconPrevRaw = android.R.drawable.ic_media_rew
        val iconNextRaw = android.R.drawable.ic_media_ff
        val iconPrevBitmap = createCustomIcon(context, iconPrevRaw, Color.WHITE, 0.25f)
        val iconNextBitmap = createCustomIcon(context, iconNextRaw, Color.WHITE, 0.25f)
        val picPrev = HyperPicture("pic_prev_w", iconPrevBitmap)
        val picNext = HyperPicture("pic_next_w", iconNextBitmap)
        val actionPrev = HyperAction(ACTION_KEY_TEST_1, "Prev", createAppOpenIntent(context, 1), 1)
        val actionNext = HyperAction(ACTION_KEY_TEST_2, "Next", createAppOpenIntent(context, 2), 1)
        val builder = HyperIslandNotification.Builder(context, "demo", "IconBg").addPicture(pic)
            .addPicture(picPrev).addPicture(picNext).addAction(actionPrev).addAction(actionNext)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
        val jsonParam =
            """{"param_v2":{"business":"raw_icon_bg","updatable":true,"ticker":"Icon+Bg","isShownNotification":true,"baseInfo":{"type":2,"title":"Colored Icon Buttons","content":"Actions with BG Color","picFunction":"miui.focus.pic_$PIC_KEY_DEMO_ICON"},"actions":[{"type":0,"actionIntent":"miui.focus.action_$ACTION_KEY_TEST_1","actionIntentType":1,"actionIcon":"miui.focus.pic_pic_prev_w","actionIconDark":"miui.focus.pic_pic_prev_w","actionBgColor":"#FF9500","actionBgColorDark":"#FF9500"},{"type":0,"actionIntent":"miui.focus.action_$ACTION_KEY_TEST_2","actionIntentType":1,"actionIcon":"miui.focus.pic_pic_next_w","actionIconDark":"miui.focus.pic_pic_next_w","actionBgColor":"#007AFF","actionBgColorDark":"#007AFF"}],"param_island":{"islandProperty":1,"smallIslandArea":{"picInfo":{"type":1,"pic":"miui.focus.pic_$PIC_KEY_DEMO_ICON"}}}}}""".trimIndent()
        val resourceBundle = builder.buildResourceBundle()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Raw Action Icon+Bg")
            .setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }


    // ============================================================================================
// 12. SHOW RAW ANIM (DotLottie Version)
// ============================================================================================
    fun showRawAnimTextInfo(context: Context) {
        val notificationId = getUniqueNotificationId()
        val openAppIntent = createAppOpenIntent(context, 0)


        // 2. PREPARE PICTURES
        val picLight = HyperPicture("pic_anim_light", context, R.drawable.ic_launcher_foreground)

        // 3. BUILDER
        val builder = HyperIslandNotification.Builder(context, "demo", "Anim")
            .addPicture(picLight)
            .setSmallWindowTarget("${context.packageName}.MainActivity")

        // 4. JSON PAYLOAD
        // [FIX] We use the KEY ("miui.focus.pic_anim_lottie_key") instead of the URI string.
        // The system looks up the key in the bundle -> finds the Icon -> extracts the URI -> plays Lottie.
        val jsonParam = """
    {
      "param_v2": {
        "business": "raw_anim",
        "updatable": true,
        "ticker": "AnimText",
        "isShownNotification": true,
        "scene": "recorder",
        "animTextInfo": {
           "title": "Playing .lottie",
           "content": "Rendered via Icon.createWithContentUri",
           "colorTitle": "#FF8514",
           "animIconInfo": {
             "type": 2, 
             "src": "voiceWaveBig",
             "loop": true,
             "autoplay": true
           }
        },
        "param_island": { 
            "islandProperty": 1, 
            "smallIslandArea": { 
                "picInfo": { "type": 1, "pic": "miui.focus.pic_pic_anim_light" } 
            },
            "bigIslandArea": {
                "imageTextInfoLeft": {
                    "type": 1,
                    "picInfo": { "type": 1, "pic": "miui.focus.pic_pic_anim_light" },
                    "textInfo": { "title": "Lottie Test" }
                },
                "picInfo": {
                    "type": 2,
                    "pic": "voiceWaveSmall",
                    "loop": true,
                    "autoplay": true
                }
            }
        }
      }
    }
    """.trimIndent()

        val resourceBundle = builder.buildResourceBundle()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Raw AnimTextInfo (.lottie)")
            .setContentIntent(openAppIntent)
            .addExtras(resourceBundle)
            .build()

        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

}