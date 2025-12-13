package com.d4viddf.hyperisland_kit.demo

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
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
import java.util.concurrent.TimeUnit
import androidx.core.graphics.createBitmap

// --- Resource Keys ---
private const val PIC_KEY_ICON = "icon_main"
private const val PIC_KEY_SUB = "icon_sub"
private const val PIC_KEY_COVER = "cover_image"
private const val PIC_KEY_AVATAR = "avatar"
private const val PIC_KEY_APP_OPEN = "app_open_pic"
private const val PIC_KEY_MEDICATION = "medication"
private const val PIC_KEY_SIMPLE = "simple"
private const val PIC_KEY_RIGHT_SIDE = "right_side"
private const val PIC_KEY_DEMO_ICON = "demo_icon"
private const val PIC_KEY_PROGRESS = "progress"
private const val PIC_KEY_COUNTUP = "countup"
private const val PIC_KEY_CAR = "car"
private const val PIC_KEY_DOT_SEL = "dot_sel"
private const val PIC_KEY_DOT_UNSEL = "dot_unsel"
private const val PIC_KEY_FLAG_SEL = "flag_sel"
private const val PIC_KEY_FLAG_UNSEL = "flag_unsel"

private const val ACTION_KEY_TAKEN = "taken"
private const val ACTION_KEY_APP_OPEN = "app_open"
private const val ACTION_KEY_HINT = "hint"
private const val ACTION_KEY_STOP = "stop"
private const val ACTION_KEY_CLOSE = "close"
private const val ACTION_KEY_TEST_1 = "test_1"
private const val ACTION_KEY_TEST_2 = "test_2"


object DemoNotificationManager {

    // --- Helpers ---
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

    private fun createAppOpenIntent(context: Context, requestCode: Int = 0): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createRoundedBackgroundIcon(
        context: Context,
        drawableResId: Int,
        iconColor: Int,
        backgroundColor: Int,
        paddingFactor: Float = 0.25f
    ): Icon {
        val size = 128
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = backgroundColor; style = Paint.Style.FILL }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate()
        drawable?.let {
            val padding = (size * paddingFactor).toInt()
            it.setBounds(padding, padding, size - padding, size - padding)
            it.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
            it.draw(canvas)
        }
        return Icon.createWithBitmap(bitmap)
    }

    private fun createCustomIcon(
        context: Context,
        drawableResId: Int,
        color: Int,
        paddingFactor: Float = 0.25f
    ): Icon {
        val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate()
            ?: return Icon.createWithResource(context, drawableResId)

        val size = 96
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val padding = (size * paddingFactor).toInt()
        drawable.setBounds(padding, padding, size - padding, size - padding)
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        drawable.draw(canvas)
        return Icon.createWithBitmap(bitmap)
    }

    // ============================================================================================
    // CONFIGURABLE DEMO (Restored)
    // ============================================================================================

    fun showConfigurableNotification(context: Context, timeout: Long, enableFloat: Boolean, isShowNotification: Boolean) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)

        val builder = HyperIslandNotification.Builder(context, "config", "Config")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(pic)
            .setChatInfo("Configurable", "Timeout: ${timeout}ms", PIC_KEY_ICON)
            .setSmallIslandIcon(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Configured")))
            .setEnableFloat(enableFloat).setShowNotification(isShowNotification)

        if (timeout > 0) builder.setTimeout(timeout)

        notify(context, "Configurable Demo", builder)
    }

    // ============================================================================================
    // OFFICIAL TEMPLATES (1-22)
    // ============================================================================================

    // 1. Weather (BaseInfo Type 1)
    fun showTemplate1_Weather(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "weather", "Weather")
            .addPicture(pic)
            .setBaseInfo(
                type = 1,
                title = "Heavy Snow",
                subTitle = "Red Alert",
                content = "Chaoyang District",
                subContent = "Tonight to Tomorrow",
                pictureKey = PIC_KEY_ICON,
                colorTitle = "#FF0000"
            )
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 1: Weather", builder)
    }

    // 2. Taxi (BaseInfo Type 2)
    fun showTemplate2_Taxi(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.rounded_arrow_outward_24)
        val builder = HyperIslandNotification.Builder(context, "taxi", "Taxi")
            .addPicture(pic)
            .setBaseInfo(
                type = 2,
                title = "Calling...",
                content = "Queue Position: 5",
                subContent = "Est. 7 mins",
                pictureKey = PIC_KEY_ICON
            )
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 2: Taxi", builder)
    }

    // 3. IM/Chat (ChatInfo)
    fun showTemplate3_Chat(context: Context) {
        if (!hasNotificationPermission(context)) return
        val avatar = HyperPicture(PIC_KEY_AVATAR, context, R.drawable.rounded_medication_24)
        val builder = HyperIslandNotification.Builder(context, "chat", "Message")
            .addPicture(avatar)
            .setChatInfo(
                title = "John Doe",
                content = "Invited you to a video call",
                pictureKey = PIC_KEY_AVATAR
            )
            .setSmallIslandIcon(PIC_KEY_AVATAR)
        notify(context, "Template 3: Chat", builder)
    }

    // 4. Taxi Queue (BaseInfo 2 + Progress)
    fun showTemplate4_TaxiQueue(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "taxi", "Queue")
            .addPicture(pic)
            .setBaseInfo(
                type = 2,
                title = "Queueing",
                content = "Pos 5",
                subContent = "Est 7m",
                pictureKey = PIC_KEY_ICON
            )
            .setMultiProgress(title = "Finding Driver...", progress = 1, points = 0, color = "#007AFF")
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 4: Taxi Queue", builder)
    }

    // 5. Dining Queue (BaseInfo 1 + Progress)
    fun showTemplate5_DiningQueue(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "dining", "Queue")
            .addPicture(pic)
            .setBaseInfo(
                type = 1,
                title = "A24",
                subTitle = "6 Tables",
                content = "Haidilao",
                subContent = "Wait 20m",
                pictureKey = PIC_KEY_ICON
            )
            .setProgressBar(progress = 30, color = "#FF8514")
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 5: Dining Queue", builder)
    }

    // 6. Parking (BaseInfo 2 + Progress)
    fun showTemplate6_Parking(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "parking", "Parking")
            .addPicture(pic)
            .setBaseInfo(
                type = 2,
                title = "Entered",
                content = "Charge starts 16:00",
                subContent = "Parked 10m",
                pictureKey = PIC_KEY_ICON
            )
            .setProgressBar(progress = 15, color = "#34C759")
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 6: Parking", builder)
    }

    // 7. Upload (ChatInfo + Progress)
    fun showTemplate7_Upload(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "upload", "Upload")
            .addPicture(pic)
            .setChatInfo("Uploading...", "201MB / 233MB", PIC_KEY_ICON)
            .setProgressBar(progress = 86, color = "#34C759")
            .setSmallIslandCircularProgress(PIC_KEY_ICON, 86, "#34C759")
            .setBigIslandInfo(left = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = "miui.focus.pic_$PIC_KEY_ICON"), textInfo = TextInfo("Uploading")))
        notify(context, "Template 7: Upload", builder)
    }

    // 8. Coupon (ChatInfo + Button Component 3/HintInfo)
    fun showTemplate8_Coupon(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val action = HyperAction("view", "View", null, createAppOpenIntent(context), 1)

        val builder = HyperIslandNotification.Builder(context, "coupon", "Coupon")
            .addPicture(pic) // NO addAction() to avoid duplication in bottom row
            .setChatInfo("Coffee Shop", "Buy 1 Get 1 Free", PIC_KEY_ICON)
            .setHintAction("Coupon Available", action = action) // Top Button
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 8: Coupon", builder)
    }

    // 9. Movie Ticket (BaseInfo 2 + Button Component 2/HintTimer)
    fun showTemplate9_Movie(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground) // Use poster if available
        val poster = HyperPicture("poster", context, R.drawable.poster)
        val action = HyperAction("code", "Code", null, createAppOpenIntent(context), 1)

        val builder = HyperIslandNotification.Builder(context, "movie", "Ticket")
            .addPicture(pic)
            .addPicture(poster)
            .setBaseInfo(
                type = 2,
                title = "Oppenheimer",
                subTitle = "IMAX",
                content = "19:20",
                subContent = "Row 4 Seat 6",
                pictureKey = PIC_KEY_ICON
            )
            .setBackground("poster","#6b504c", type = 2)
            .setHintTimer(frontText1 = "Start", mainText1 = "19:20", action = action)
            .setSmallIslandIcon("poster")
        notify(context, "Template 9: Movie Ticket", builder)
    }

    // 10. Pickup (BaseInfo 2 + Button Component 3/HintInfo)
    fun showTemplate10_Pickup(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val action = HyperAction("code", "Code", null, createAppOpenIntent(context), 1)

        val builder = HyperIslandNotification.Builder(context, "pickup", "Package")
            .addPicture(pic)
            .setBaseInfo(
                type = 2,
                title = "Ready for Pickup",
                content = "Cainiao Station",
                subContent = "2 Packages",
                pictureKey = PIC_KEY_ICON
            )
            .setHintAction("Pickup Code", action = action)
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 10: Pickup", builder)
    }

    // 11. Sports/Timer (HighlightInfo + Button Component 2)
    fun showTemplate11_Sports(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val action = HyperAction("pause", "Pause", null, createAppOpenIntent(context), 1)

        val builder = HyperIslandNotification.Builder(context, "run", "Run")
            .addPicture(pic)
            .setHighlightInfo(title = "6.12 km", content = "00:30:59", subContent = "Pace 5'30\"", picKey = PIC_KEY_ICON)
            .setHintTimer(frontText1 = "Time", mainText1 = "00:30:59", action = action)
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 11: Sports", builder)
    }

    // 12. Call (ChatInfo + Button Component 1/Standard Actions)
    fun showTemplate12_Call(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val actDecline = HyperAction("decline", "Decline", null, createAppOpenIntent(context), 1, actionBgColor = "#FF3B30", titleColor = "#FFFFFF")
        val actAnswer = HyperAction("answer", "Answer", null, createAppOpenIntent(context), 1, actionBgColor = "#34C759", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "call", "Call")
            .addPicture(pic).addAction(actDecline).addAction(actAnswer)
            .setChatInfo("John Doe", "Incoming Video Call", PIC_KEY_ICON)
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 12: Call", builder)
    }

    // 13. Recording (HighlightInfo + Button Component 1)
    fun showTemplate13_Recording(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val actStop = HyperAction("stop", null, context, R.drawable.rounded_pause_24, createAppOpenIntent(context), 1, actionBgColor = "#E0E0E0")

        val builder = HyperIslandNotification.Builder(context, "record", "Rec")
            .addPicture(pic).addAction(actStop)
            .setHighlightInfo("03:58", "Recording...", picKey = PIC_KEY_ICON)
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 13: Recording", builder)
    }

    // 14. Navigation (IconTextInfo)
    fun showTemplate14_Navigation(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "nav", "Nav")
            .addPicture(pic)
            .setIconTextInfo(PIC_KEY_ICON, "Turn Right", "88 meters", "Navigating")
            .setBigIslandFixedWidthDigit(digit = 88, content = "Meters", showHighlight = true)
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 14: Navigation", builder)
    }

    // 15. Recorder (AnimTextInfo + Button Component 1)
    fun showTemplate15_Recorder(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val actStop = HyperAction("stop", null, context, R.drawable.rounded_pause_24, createAppOpenIntent(context), 1)

        val builder = HyperIslandNotification.Builder(context, "recorder", "Rec")
            .setScene("recorder") // Essential for animation
            .addPicture(pic).addAction(actStop)
            .setAnimTextInfo(PIC_KEY_ICON, "Recording", "00:05", isAnimation = true)
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 15: Recorder", builder)
    }

    // 16. Code (IconTextInfo + Button Component 1)
    fun showTemplate16_Code(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val actCopy = HyperAction("copy", "Copy", null, createAppOpenIntent(context), 1, actionBgColor = "#E0E0E0")

        val builder = HyperIslandNotification.Builder(context, "code", "Code")
            .addPicture(pic).addAction(actCopy)
            .setIconTextInfo(PIC_KEY_ICON, "C23JH1", "Verification Code")
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 16: Code", builder)
    }

    // 17. Promo (HighlightInfoV3)
    fun showTemplate17_Promo(context: Context) {
        if (!hasNotificationPermission(context)) return
        val action = HyperAction("buy", "Buy", null, createAppOpenIntent(context), 1, actionBgColor = "#FF3B30", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "promo", "Sale")
            // No addAction here, it's inside V3
            .setHighlightInfoV3(primaryText = "¥4999", secondaryText = "¥5999", label = "Sale", action = action, primaryColor = "#FF0000")
            .setBaseInfo("Flash Sale", "Xiaomi 15", type = 2) // Fallback base
            .setSmallIslandIcon(PIC_KEY_ICON)
            .addPicture(HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)) // Ensure icon exists
        notify(context, "Template 17: Promo", builder)
    }

    // 18. File Request (IconTextInfo + TextButton)
    fun showTemplate18_FileRequest(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val actAccept = HyperAction("accept", "Accept", null, createAppOpenIntent(context), 1, actionBgColor = "#007AFF", titleColor = "#FFFFFF")
        val actDecline = HyperAction("decline", "Decline", null, createAppOpenIntent(context), 1, actionBgColor = "#333333", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "file", "File")
            .addPicture(pic)
            .setIconTextInfo(PIC_KEY_ICON, "Receive Photo", "20MB | From Xiaomi 14")
            .setTextButtons(actDecline, actAccept)
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 18: File Request", builder)
    }

    // 19. Cover Info (CoverInfo + Button Component 5)
    fun showTemplate19_Cover(context: Context) {
        if (!hasNotificationPermission(context)) return
        val cover = HyperPicture(PIC_KEY_COVER, context, R.drawable.starry_pplaceholder) // Tall image
        val action = HyperAction("book", "Book", null, createAppOpenIntent(context), 1, actionBgColor = "#007AFF", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "cover", "Concert")
            .addPicture(cover).addAction(action)
            .setBaseInfo("Concert", "Jay Chou", pictureKey = PIC_KEY_COVER) // Fallback
            .setCoverInfo(PIC_KEY_COVER, "Jay Chou", "Chengdu", "Feb 24")
            .setHintAction("26:00 Remaining", action = action) // Reuse HintAction for Comp 5
            .setSmallIslandIcon(PIC_KEY_COVER)
        notify(context, "Template 19: Cover Info", builder)
    }

    // 20. Data Usage (IconTextInfo + Progress)
    fun showTemplate20_Data(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "data", "Data")
            .addPicture(pic)
            .setIconTextInfo(PIC_KEY_ICON, "Thailand 1GB", "Left: 2 Days", "199MB Left")
            .setProgressBar(progress = 20, color = "#007AFF")
            .setSmallIslandCircularProgress(PIC_KEY_ICON, 20, "#007AFF")
        notify(context, "Template 20: Data Usage", builder)
    }

    // 21. Game Download (ChatInfo + Progress)
    fun showTemplate21_Game(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "game", "Download")
            .addPicture(pic)
            .setChatInfo("Game Launch", "Downloading assets...", PIC_KEY_ICON)
            .setProgressBar(progress = 45, color = "#FF6700")
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 21: Game Download", builder)
    }

    // 22. IoT (IconTextInfo + Progress)
    fun showTemplate22_IoT(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "iot", "IoT")
            .addPicture(pic)
            .setIconTextInfo(PIC_KEY_ICON, "Washing", "30 mins left", "Balcony Washer")
            .setProgressBar(progress = 60, color = "#007AFF")
            .setSmallIslandIcon(PIC_KEY_ICON)
        notify(context, "Template 22: IoT", builder)
    }

    // ============================================================================================
    // ADVANCED CUSTOMIZATION DEMOS (Restored)
    // ============================================================================================

    fun showRawColoredTextButtons(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val action1 = HyperAction(ACTION_KEY_TEST_1, "Accept", null, createAppOpenIntent(context), 1, actionBgColor = "#34C759", titleColor = "#FFFFFF")
        val action2 = HyperAction(ACTION_KEY_TEST_2, "Reject", null, createAppOpenIntent(context), 1, actionBgColor = "#FF3B30", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "textbtn", "Buttons")
            .addPicture(pic).addAction(action1).addAction(action2)
            .setBaseInfo("Text Buttons", "Colored actions", pictureKey = PIC_KEY_ICON, type = 2)
            .setTextButtons(action1, action2)
            .setSmallIslandIcon(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Buttons")))

        notify(context, "Text Buttons", builder)
    }

    fun showRawIconButtons(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)

        // Use custom icon creation if needed, or resources
        val action1 = HyperAction(ACTION_KEY_TEST_1, "Prev", context, android.R.drawable.ic_media_rew, createAppOpenIntent(context), 1, actionBgColor = "#E0E0E0")
        val action2 = HyperAction(ACTION_KEY_TEST_2, "Next", context, android.R.drawable.ic_media_ff, createAppOpenIntent(context), 1, actionBgColor = "#E0E0E0")

        val builder = HyperIslandNotification.Builder(context, "icons", "Icons")
            .addPicture(pic).addAction(action1).addAction(action2)
            .setBaseInfo("Music Control", "Icon buttons", pictureKey = PIC_KEY_ICON, type = 2)
            .setSmallIslandIcon(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Music")))

        notify(context, "Icon Buttons", builder)
    }

    fun showRawProgressAndColorButton(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val action1 = HyperAction(ACTION_KEY_TEST_1, "Progress", context, R.drawable.rounded_pause_24, createAppOpenIntent(context), 1, isProgressButton = true, progress = 65, progressColor = "#34C759")
        val action2 = HyperAction(ACTION_KEY_TEST_2, "Close", null, createAppOpenIntent(context), 1, actionBgColor = "#FF6700", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "mix", "Mix")
            .addPicture(pic).addAction(action1).addAction(action2)
            .setBaseInfo("Download", "Mix Buttons", pictureKey = PIC_KEY_ICON, type = 2)
            .setSmallIslandIcon(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Download")))

        notify(context, "Mix Buttons", builder)
    }

    fun showRawBgInfo(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "bg", "Bg")
            .addPicture(pic)
            .setBaseInfo("Custom BG", "Colored background", pictureKey = PIC_KEY_ICON, type = 2)
            .setBackground(color = "#E6F0FF")
            .setSmallIslandIcon(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Custom BG")))

        notify(context, "Bg Info", builder)
    }

    // ============================================================================================
    // STANDARD DEMOS (Restored for Compatibility)
    // ============================================================================================

    fun showAppOpenNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.rounded_arrow_outward_24)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "App Open")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(pic)
            .setBaseInfo(title = "App Open Demo", content = "Tap or drag to open", pictureKey = PIC_KEY_APP_OPEN)
            .setBigIslandInfo(left = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = "miui.focus.pic_$PIC_KEY_APP_OPEN"), textInfo = TextInfo(title = "Open Demo")))
            .setSmallIslandIcon(PIC_KEY_APP_OPEN)
        notify(context, "App Open Demo", builder)
    }

    fun showChatNotification(context: Context) {
        showTemplate3_Chat(context) // Alias to template 3
    }

    fun showSimpleSmallIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val simplePicture = HyperPicture(PIC_KEY_SIMPLE, context, R.drawable.rounded_arrow_outward_24)
        val appOpenAction = HyperAction(ACTION_KEY_APP_OPEN, "Open", context, R.drawable.rounded_arrow_outward_24, createAppOpenIntent(context), 1)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Simple")
            .setChatInfo(title = "Simple Info", content = "Expanded view", pictureKey = PIC_KEY_SIMPLE, actionKeys = listOf(ACTION_KEY_APP_OPEN))
            .setSmallIslandIcon(PIC_KEY_SIMPLE)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_SIMPLE"), textInfo=TextInfo(title="Simple Info")))
            .addPicture(simplePicture).addAction(appOpenAction)
        notify(context, "Simple Island", builder)
    }

    fun showRightImageNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val leftPic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val rightPic = HyperPicture(PIC_KEY_RIGHT_SIDE, context, R.drawable.rounded_medication_24)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Right Img")
            .setChatInfo("Right Image", "Check island", PIC_KEY_DEMO_ICON)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON"), textInfo=TextInfo(title="Left")),
                right = ImageTextInfoRight(type=2, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_RIGHT_SIDE"), textInfo=TextInfo(title="Right"))
            )
            .setSmallIsland(aZone = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON")), bZone = ImageTextInfoRight(type=2, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_RIGHT_SIDE")))
            .addPicture(leftPic).addPicture(rightPic)
        notify(context, "Right Image", builder)
    }

    fun showSplitIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Split")
            .setChatInfo("Split Info", "Left & Right", PIC_KEY_DEMO_ICON)
            .addPicture(pic)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON"), textInfo=TextInfo(title="Left")),
                right = ImageTextInfoRight(type=2, textInfo=TextInfo(title="Right"))
            )
            .setSmallIsland(aZone = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON")), bZone = ImageTextInfoRight(type=2, textInfo=TextInfo(title="Right")))
        notify(context, "Split Island", builder)
    }

    fun showHintInfoNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val action = HyperAction(ACTION_KEY_HINT, "View", null, createAppOpenIntent(context), 1)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Hint")
            .setChatInfo("Main Content", "Look up", PIC_KEY_DEMO_ICON)
            .setHintInfo("2 New Messages", ACTION_KEY_HINT)
            .addPicture(pic).addAction(action)
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON"), textInfo=TextInfo(title="Main Content")))
        notify(context, "Hint Info", builder)
    }

    fun showMultiNodeProgressNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Nodes")
            .setBaseInfo("Processing", "Order is being prepared", pictureKey = PIC_KEY_DEMO_ICON, type = 2)
            .setMultiProgress("Step 2 of 4", 2, "#34C759", 3)
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON"), textInfo=TextInfo(title="Processing")))
            .addPicture(pic)
        notify(context, "Multi Node", builder)
    }

    fun showProgressBarNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val picMain = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_upload_24)
        val picCar = HyperPicture(PIC_KEY_CAR, context, R.drawable.rounded_arrow_outward_24)
        val picDot = HyperPicture(PIC_KEY_DOT_SEL, context, R.drawable.round_smart_button_24)
        val picDotUn = HyperPicture(PIC_KEY_DOT_UNSEL, context, R.drawable.ic_launcher_foreground)
        val picFlag = HyperPicture(PIC_KEY_FLAG_SEL, context, R.drawable.ic_launcher_foreground)
        val picFlagUn = HyperPicture(PIC_KEY_FLAG_UNSEL, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "progress", "Progress")
            .addPicture(picMain).addPicture(picCar).addPicture(picDot).addPicture(picDotUn).addPicture(picFlag).addPicture(picFlagUn)
            .setChatInfo("Delivery", "Arriving...", PIC_KEY_PROGRESS)
            .setProgressBar(70, "#FF8514", null, PIC_KEY_CAR, PIC_KEY_DOT_SEL, PIC_KEY_DOT_UNSEL, PIC_KEY_FLAG_SEL, PIC_KEY_FLAG_UNSEL)
            .setSmallIslandIcon(PIC_KEY_PROGRESS)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_PROGRESS"), textInfo=TextInfo(title="Arriving")))
        notify(context, "Icon Progress", builder)
    }

    fun showCircularProgressNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_download_24)
        val builder = HyperIslandNotification.Builder(context, "circle", "Circle")
            .addPicture(pic)
            .setChatInfo("Downloading", "75%", PIC_KEY_PROGRESS)
            .setSmallIslandCircularProgress(PIC_KEY_PROGRESS, 75, "#34C759")
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(1, "miui.focus.pic_$PIC_KEY_PROGRESS"), textInfo=TextInfo("Downloading", "75%")))
        notify(context, "Circular Progress", builder)
    }

    fun showCountdownNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val countdownTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.rounded_timer_arrow_down_24)
        val builder = HyperIslandNotification.Builder(context, "countdown", "Timer")
            .addPicture(pic)
            .setChatInfo("Timer", "Countdown", PIC_KEY_DEMO_ICON)
            .setBigIslandCountdown(countdownTime, PIC_KEY_DEMO_ICON)
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON)
        notify(context, "Countdown", builder)
    }

    fun showCountUpNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_COUNTUP, context, R.drawable.rounded_timer_arrow_up_24)
        val startTime = System.currentTimeMillis()
        val builder = HyperIslandNotification.Builder(context, "timer", "Timer")
            .addPicture(pic)
            .setChatInfo("Timer", "Count Up", PIC_KEY_COUNTUP)
            .setSmallIslandIcon(PIC_KEY_COUNTUP)
            .setBigIslandCountUp(startTime, PIC_KEY_COUNTUP)
        notify(context, "CountUp", builder)
    }

    fun showMultiActionNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val stopAction = HyperAction(ACTION_KEY_STOP, null, null, createAppOpenIntent(context), 2, isProgressButton = true, progress = 10, colorReach = "#D9E0FA")
        val closeAction = HyperAction(ACTION_KEY_CLOSE, "Close", null, createAppOpenIntent(context), 2, actionBgColor = "#FF3B30")
        val pic = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.round_smart_button_24)
        val builder = HyperIslandNotification.Builder(context, "multi", "Actions")
            .addPicture(pic).addAction(stopAction).addAction(closeAction)
            .setChatInfo("Actions", "Stop or Close", PIC_KEY_APP_OPEN, actionKeys = listOf(ACTION_KEY_STOP, ACTION_KEY_CLOSE))
            .setSmallIslandIcon(PIC_KEY_APP_OPEN)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_APP_OPEN"), textInfo=TextInfo(title="Actions")))
        notify(context, "Multi Action", builder)
    }

    // --- Helper ---
    private fun notify(context: Context, title: String, builder: HyperIslandNotification) {
        val notificationId = getUniqueNotificationId()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentIntent(createAppOpenIntent(context)).addExtras(builder.buildResourceBundle()).build()
        notification.extras.putString("miui.focus.param", builder.buildJsonParam())
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }
}