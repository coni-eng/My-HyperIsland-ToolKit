package com.d4viddf.hyperisland_kit

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.d4viddf.hyperisland_kit.models.* // Import all our models
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Public data class for the developer to define an action
data class HyperAction(
    val key: String, // The unique key linking JSON to the bundle
    val title: CharSequence,
    val icon: Icon,
    val pendingIntent: PendingIntent,
    val isProgressButton: Boolean = false,
    val progress: Int = 0, // 0-100
    val progressColor: String? = null // e.g., "#FF8514"
)

// Public data class for the developer to define a picture
data class HyperPicture(
    val key: String, // The unique key linking JSON to the bundle
    val icon: Icon
) {
    constructor(key: String, context: Context, drawableRes: Int) : this(
        key = key,
        icon = Icon.createWithBitmap(
            getBitmapFromVectorDrawable(
                context,
                drawableRes
            )
        )
    )

    companion object {
        private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? { // [cite: 236]
            return ContextCompat.getDrawable(context, drawableId)?.let { drawable ->
                val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }
    }
}

class HyperIslandNotification private constructor(
    private val context: Context,
    private val businessName: String,
    private val ticker: String
) {
    private var targetPage: String? = null
    private var chatInfo: ChatInfo? = null
    private var paramIsland: ParamIsland? = null
    // ... Add properties for other templates (baseInfo, etc.)

    private val actions = mutableListOf<HyperAction>()
    private val pictures = mutableListOf<HyperPicture>()

    // --- Public Builder Methods ---

    /**
     * Sets the deep-link target page for the small window.
     * @param fullyQualifiedActivityName E.g., "com.domain.example.MainActivity"
     */
    fun setSmallWindowTarget(fullyQualifiedActivityName: String) = apply {
        this.targetPage = fullyQualifiedActivityName
    }

    /**
     * Adds a "Chat" style notification. [cite: 3604]
     */
    fun setChatInfo(
        title: String,
        content: String? = null,
        pictureKey: String? = null,
        timer: TimerInfo? = null,
        actionKey: String? = null
    ) = apply {
        val actionRef = actionKey?.let { key ->
            actions.firstOrNull { it.key == key }?.toActionRef()
        }

        this.chatInfo = ChatInfo(
            title = title,
            content = if (timer != null) null else content, // Timer overrides content [cite: 2448]
            picFunction = pictureKey,
            timerInfo = timer,
            actions = if (actionRef != null) listOf(actionRef) else null
        )
    }

    /**
     * Sets the summary (small island) state.
     * @param aZone The left-side component (optional). [cite: 4180]
     * @param bZone The right-side component (optional). [cite: 4183]
     */
    fun setSmallIsland(aZone: ImageTextInfoLeft?, bZone: ImageTextInfoRight?) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        this.paramIsland = this.paramIsland?.copy(
            smallIslandArea = SmallIslandArea(imageTextInfoLeft = aZone, imageTextInfoRight = bZone)
        )
    }

    /**
     * Sets the expanded (big island) state.
     * @param countdownTime The target time (in millis) for a countdown timer.
     */
    fun setBigIslandCountdown(countdownTime: Long) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val timerInfo = TimerInfo(
            timerType = -1, // -1 for countdown
            timerWhen = countdownTime,
            timerTotal = System.currentTimeMillis(),
            timerSystemCurrent = System.currentTimeMillis()
        )
        this.paramIsland = this.paramIsland?.copy(
            bigIslandArea = BigIslandArea(
                sameWidthDigitInfo = SameWidthDigitInfo(timerInfo)
            )
        )
    }

    fun addAction(action: HyperAction) = apply {
        this.actions.add(action)
    }

    fun addPicture(picture: HyperPicture) = apply {
        this.pictures.add(picture)
    }

    /**
     * Builds the final Bundle to be attached to the notification.
     */
    fun buildExtras(): Bundle {
        val bundle = Bundle()
        if (!isSupported(context)) return bundle

        // 1. Build the JSON payload
        val paramV2 = ParamV2(
            business = businessName,
            ticker = ticker,
            smallWindowInfo = targetPage?.let { SmallWindowInfo(it) },
            chatInfo = this.chatInfo,
            paramIsland = this.paramIsland,
            actions = this.actions.map { it.toActionRef() }.ifEmpty { null }
            // ... map other components here
        )
        val payload = HyperIslandPayload(paramV2)

        // Use kotlinx.serialization to create the JSON string
        val jsonString = Json.encodeToString(payload)
        bundle.putString("miui.focus.param", jsonString)
        Log.d("HyperIsland", "JSON: $jsonString") // For debugging

        // 2. Build the Actions Bundle
        val actionsBundle = Bundle()
        actions.forEach {
            val notificationAction = Notification.Action.Builder(it.icon, it.title, it.pendingIntent).build()
            actionsBundle.putParcelable(it.key, notificationAction)
        }
        bundle.putBundle("miui.focus.actions", actionsBundle)

        // 3. Build the Pics Bundle
        val picsBundle = Bundle()
        pictures.forEach {
            picsBundle.putParcelable(it.key, it.icon)
        }
        bundle.putBundle("miui.focus.pics", picsBundle)

        return bundle
    }

    // --- Private Helpers ---

    private fun HyperAction.toActionRef(): HyperActionRef {
        return HyperActionRef(
            type = if (isProgressButton) 2 else 1,
            action = this.key,
            actionIntent = this.key, // Cover both fields
            progressInfo = if (isProgressButton) ProgressInfo(progress, progressColor) else null,
            actionTitle = this.title.toString()
        )
    }

    companion object {
        fun Builder(context: Context, businessName: String, ticker: String): HyperIslandNotification {
            return HyperIslandNotification(context, businessName, ticker)
        }
        fun isSupported(context: Context): Boolean {
            return isXiaomiDevice() && hasFocusPermission(context) && isSupportIsland()
        }

        private fun isXiaomiDevice(): Boolean { //
            return Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
        }

        private fun hasFocusPermission(context: Context): Boolean { //
            return try {
                val uri = Uri.parse("content://miui.statusbar.notification.public")
                val extras = Bundle()
                extras.putString("package", context.packageName)
                val bundle = context.contentResolver.call(uri, "canShowFocus", null, extras)
                bundle?.getBoolean("canShowFocus", false) ?: false
            } catch (e: Exception) {
                false
            }
        }

        private fun isSupportIsland(): Boolean { // [cite: 58]
            return try {
                val clazz = Class.forName("android.os.SystemProperties")
                val method = clazz.getDeclaredMethod("getBoolean", String::class.java, Boolean::class.java)
                method.invoke(null, "persist.sys.feature.island", false) as Boolean
            } catch (e: Exception) {
                false
            }
        }

    }
}