package io.github.d4viddf.hyperisland_kit

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import io.github.d4viddf.hyperisland_kit.models.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import androidx.core.net.toUri

private const val ACTION_PREFIX = "miui.focus.action_"
private const val PIC_PREFIX = "miui.focus.pic_"

// ==========================================
//  DATA WRAPPERS
// ==========================================

data class HyperAction(
    val key: String,
    val title: CharSequence?,
    val icon: Icon?, // Null for text-only buttons
    val pendingIntent: PendingIntent,
    val actionIntentType: Int, // 1=Activity, 2=Broadcast, 3=Service

    // Customization Fields
    val actionBgColor: String? = null,
    val actionBgColorDark: String? = null,
    val titleColor: String? = null,
    val titleColorDark: String? = null,

    // Progress Button Fields
    val isProgressButton: Boolean = false,
    val progress: Int = 0,
    val progressColor: String? = null,
    val colorReach: String? = null,
    val isCCW: Boolean = false
) {
    // Constructor 1 (Standard Resource Icon)
    constructor(
        key: String, title: CharSequence?, context: Context, drawableRes: Int, pendingIntent: PendingIntent, actionIntentType: Int,
        isProgressButton: Boolean = false, progress: Int = 0, progressColor: String? = null, actionBgColor: String? = null, isCCW: Boolean = false, colorReach: String? = null
    ) : this(key, title, Icon.createWithResource(context, drawableRes), pendingIntent, actionIntentType, actionBgColor = actionBgColor, isProgressButton = isProgressButton, progress = progress, progressColor = progressColor, isCCW = isCCW, colorReach = colorReach)

    // Constructor 2 (Bitmap Icon)
    constructor(
        key: String, title: CharSequence?, bitmap: Bitmap, pendingIntent: PendingIntent, actionIntentType: Int,
        isProgressButton: Boolean = false, progress: Int = 0, progressColor: String? = null, actionBgColor: String? = null, isCCW: Boolean = false, colorReach: String? = null
    ) : this(key, title, Icon.createWithBitmap(bitmap), pendingIntent, actionIntentType, actionBgColor = actionBgColor, isProgressButton = isProgressButton, progress = progress, progressColor = progressColor, isCCW = isCCW, colorReach = colorReach)

    // Constructor 3 (Text Only / TextButton)
    constructor(
        key: String, title: CharSequence, pendingIntent: PendingIntent, actionIntentType: Int,
        bgColor: String? = null, bgColorDark: String? = null, titleColor: String? = null, titleColorDark: String? = null
    ) : this(key, title, null, pendingIntent, actionIntentType, actionBgColor = bgColor, actionBgColorDark = bgColorDark, titleColor = titleColor, titleColorDark = titleColorDark, isProgressButton = false)
}

data class HyperPicture(
    val key: String,
    val icon: Icon
) {
    constructor(key: String, context: Context, drawableRes: Int) : this(key, Icon.createWithResource(context, drawableRes))
    constructor(key: String, bitmap: Bitmap) : this(key, Icon.createWithBitmap(bitmap))
}

private fun createTransparentIcon(context: Context): Icon {
    return Icon.createWithResource(context, android.R.drawable.screen_background_light_transparent)
}

// ==========================================
//  BUILDER CLASS
// ==========================================

class HyperIslandNotification private constructor(
    private val context: Context,
    private val businessName: String,
    private val ticker: String
) {
    private var targetPage: String? = null
    private var scene: String? = null

    // Templates
    private var baseInfo: BaseInfo? = null
    private var chatInfo: ChatInfo? = null
    private var highlightInfo: HighlightInfo? = null
    private var highlightInfoV3: HighlightInfoV3? = null
    private var coverInfo: CoverInfo? = null
    private var animTextInfo: AnimTextInfo? = null
    private var iconTextInfo: IconTextInfo? = null

    // Components
    private var paramIsland: ParamIsland? = null
    private var progressBar: ProgressInfo? = null
    private var multiProgressInfo: MultiProgressInfo? = null
    private var bgInfo: BgInfo? = null
    private var hintInfo: HintInfo? = null
    private var stepInfo: StepInfo? = null
    private var textButton: List<TextButtonInfo>? = null

    // Island Configs
    private var islandPriority: Int = 2
    private var islandTimeout: Int? = null
    private var dismissIsland: Boolean = false
    private var maxSize: Boolean = false
    private var needCloseAnimation: Boolean = true
    private var expandedTime: Int? = null
    private var highlightColor: String? = null
    private var shareData: ShareData? = null

    private var timeout: Long? = null
    private var enableFloat: Boolean = true
    private var isShowNotification: Boolean = true
    private var logEnabled: Boolean = true

    private val actions = mutableListOf<HyperAction>()
    private val pictures = mutableListOf<HyperPicture>()

    @OptIn(ExperimentalSerializationApi::class)
    private val jsonSerializer = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    private fun HyperAction.getType(): Int {
        return when {
            this.isProgressButton -> 1
            this.icon == null && this.title != null -> 2
            else -> 0
        }
    }

    // --- General Methods ---

    fun setLogEnabled(enabled: Boolean) = apply { this.logEnabled = enabled }
    fun setTimeout(durationMs: Long) = apply { this.timeout = durationMs }
    fun setEnableFloat(enable: Boolean) = apply { this.enableFloat = enable }
    fun setShowNotification(show: Boolean) = apply { this.isShowNotification = show }
    fun setSmallWindowTarget(activityName: String) = apply { this.targetPage = activityName }
    fun setScene(sceneName: String) = apply { this.scene = sceneName }

    fun addAction(action: HyperAction) = apply { this.actions.add(action) }
    fun addPicture(picture: HyperPicture) = apply { this.pictures.add(picture) }

    fun setIslandConfig(
        priority: Int = 2,
        timeout: Int? = null,
        dismissible: Boolean = false,
        maxSize: Boolean = false,
        highlightColor: String? = null,
        expandedTimeMs: Int? = null,
        needCloseAnimation: Boolean = true
    ) = apply {
        this.islandPriority = priority
        this.islandTimeout = timeout
        this.dismissIsland = dismissible
        this.maxSize = maxSize
        this.highlightColor = highlightColor
        this.expandedTime = expandedTimeMs
        this.needCloseAnimation = needCloseAnimation
    }

    fun setShareData(title: String, content: String, picKey: String, shareContent: String, sharePicKey: String? = null) = apply {
        this.shareData = ShareData(title = title, content = content, pic = PIC_PREFIX + picKey, shareContent = shareContent, sharePic = sharePicKey?.let { PIC_PREFIX + it })
    }

    // ==========================================
    //  TEMPLATE SETTERS
    // ==========================================

    fun setChatInfo(
        title: String,
        content: String? = null,
        pictureKey: String? = null,
        pictureKeyDark: String? = null,
        appPkg: String? = null,
        timer: TimerInfo? = null,
        actionKeys: List<String>? = null,
        titleColor: String? = null,
        titleColorDark: String? = null,
        contentColor: String? = null,
        contentColorDark: String? = null
    ) = apply {
        clearOtherTemplates()
        this.chatInfo = ChatInfo(
            title = title,
            content = if (timer != null) null else content,
            picFunction = pictureKey?.let { PIC_PREFIX + it },
            picFunctionDark = pictureKeyDark?.let { PIC_PREFIX + it },
            appIconPkg = appPkg,
            timerInfo = timer,
            colorTitle = titleColor,
            colorTitleDark = titleColorDark,
            colorContent = contentColor,
            colorContentDark = contentColorDark,
            actions = resolveActionKeys(actionKeys)
        )
    }

    fun setBaseInfo(
        title: String,
        content: String,
        subTitle: String? = null,
        extraTitle: String? = null,
        specialTitle: String? = null,
        subContent: String? = null,
        pictureKey: String? = null,
        type: Int = 1,
        // Colors
        colorTitle: String? = null,
        colorSpecialBg: String? = null,
        actionKeys: List<String>? = null
    ) = apply {
        clearOtherTemplates()
        this.baseInfo = BaseInfo(
            type = type,
            title = title,
            subTitle = subTitle,
            extraTitle = extraTitle,
            specialTitle = specialTitle,
            content = content,
            subContent = subContent,
            picFunction = pictureKey?.let { PIC_PREFIX + it },
            colorTitle = colorTitle,
            colorSpecialBg = colorSpecialBg,
            actions = resolveActionKeys(actionKeys)
        )
    }

    fun setHighlightInfo(title: String, content: String? = null, subContent: String? = null, picKey: String? = null, timer: TimerInfo? = null) = apply {
        clearOtherTemplates()
        this.highlightInfo = HighlightInfo(
            title = title,
            content = content,
            subContent = subContent,
            picFunction = picKey?.let { PIC_PREFIX + it },
            timerInfo = timer
        )
    }

    fun setHighlightInfoV3(primaryText: String, secondaryText: String? = null, label: String? = null, action: HyperAction? = null, primaryColor: String? = null) = apply {
        clearOtherTemplates()
        this.highlightInfoV3 = HighlightInfoV3(
            primaryText = primaryText,
            secondaryText = secondaryText,
            highLightText = label,
            actionInfo = action?.toActionRef(true),
            primaryColor = primaryColor
        )
    }

    fun setCoverInfo(picKey: String, title: String, content: String? = null, subContent: String? = null) = apply {
        clearOtherTemplates()
        this.coverInfo = CoverInfo(
            picCover = PIC_PREFIX + picKey,
            title = title,
            content = content,
            subContent = subContent
        )
    }

    fun setAnimTextInfo(picKey: String, title: String, content: String? = null, isAnimation: Boolean = false) = apply {
        clearOtherTemplates()
        this.animTextInfo = AnimTextInfo(
            animIconInfo = AnimIconInfo(src = resolvePicSource(picKey), type = if (isAnimation) 3 else 0),
            title = title,
            content = content
        )
    }

    fun setIconTextInfo(picKey: String, title: String, content: String? = null, subContent: String? = null) = apply {
        clearOtherTemplates()
        this.iconTextInfo = IconTextInfo(
            animIconInfo = AnimIconInfo(src = resolvePicSource(picKey)),
            title = title,
            content = content,
            subContent = subContent
        )
    }

    // ==========================================
    //  COMPONENT SETTERS
    // ==========================================

    fun setTextButtons(vararg actions: HyperAction) = apply {
        // [IMPORTANT] Do NOT call clearOtherTemplates() here, as buttons are added TO a template
        this.textButton = actions.map { it.toTextButtonInfo() }
    }

    fun setHintInfo(title: String, actionKey: String? = null) = apply {
        // [IMPORTANT] Do NOT call clearOtherTemplates()
        val actionRef = actionKey?.let { key ->
            actions.firstOrNull { it.key == key }?.toActionRef(true)
        }
        this.hintInfo = HintInfo(
            type = 1,
            title = title,
            actionInfo = actionRef
        )
    }

    // Button Component 3 (HintInfo Type 1): Title + Action
    fun setHintAction(title: String, content: String? = null, action: HyperAction) = apply {
        // [IMPORTANT] Do NOT call clearOtherTemplates()
        this.hintInfo = HintInfo(
            type = 1,
            title = title,
            content = content,
            actionInfo = action.toActionRef(true)
        )
    }

    // Button Component 2 (HintInfo Type 2): Timer/Text + Action
    fun setHintTimer(
        frontText1: String, frontText2: String? = null,
        mainText1: String? = null, mainText2: String? = null,
        timer: TimerInfo? = null,
        action: HyperAction
    ) = apply {
        // [IMPORTANT] Do NOT call clearOtherTemplates()
        this.hintInfo = HintInfo(
            type = 2,
            content = frontText1,
            subContent = frontText2,
            title = mainText1,
            subTitle = mainText2,
            timerInfo = timer,
            actionInfo = action.toActionRef(true)
        )
    }

    fun setBackground(picKey: String? = null, color: String? = null, type: Int = 1) = apply {
        this.bgInfo = BgInfo(
            type = type,
            picBg = picKey?.let { PIC_PREFIX + it },
            colorBg = color
        )
    }

    fun setMultiProgress(title: String, progress: Int, color: String? = null, points: Int = 0) = apply {
        this.multiProgressInfo = MultiProgressInfo(
            title = title,
            progress = progress.coerceIn(0, 4),
            points = points.coerceIn(0, 4),
            color = color
        )
    }

    fun setStepProgress(currentStep: Int, totalStep: Int, activeColor: String? = null) = apply {
        this.stepInfo = StepInfo(
            currentStep = currentStep,
            totalStep = totalStep,
            activeColor = activeColor
        )
    }

    fun setProgressBar(progress: Int, color: String? = null, colorEnd: String? = null, picForwardKey: String? = null, picMiddleKey: String? = null, picMiddleUnselectedKey: String? = null, picEndKey: String? = null, picEndUnselectedKey: String? = null) = apply {
        this.progressBar = ProgressInfo(
            progress = progress,
            colorProgress = color,
            colorProgressEnd = colorEnd,
            picForward = picForwardKey?.let { PIC_PREFIX + it },
            picMiddle = picMiddleKey?.let { PIC_PREFIX + it },
            picMiddleUnselected = picMiddleUnselectedKey?.let { PIC_PREFIX + it },
            picEnd = picEndKey?.let { PIC_PREFIX + it },
            picEndUnselected = picEndUnselectedKey?.let { PIC_PREFIX + it }
        )
    }

    // ==========================================
    //  ISLAND SETTERS
    // ==========================================

    fun setSmallIsland(aZone: ImageTextInfoLeft, bZone: ImageTextInfoRight?) = apply {
        ensureParamIsland()
        val fixedA = aZone.copy(picInfo = prefixPicInfo(aZone.picInfo))
        val fixedB = bZone?.copy(picInfo = prefixPicInfo(bZone.picInfo))
        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            smallIslandArea = SmallIslandArea(imageTextInfoLeft = fixedA, imageTextInfoRight = fixedB)
        )
    }

    fun setSmallIslandIcon(picKey: String) = apply {
        ensureParamIsland()
        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            smallIslandArea = SmallIslandArea(picInfo = PicInfo(type = 1, pic = PIC_PREFIX + picKey))
        )
    }

    fun setSmallIslandCircularProgress(pictureKey: String, progress: Int, color: String? = null, colorUnReach: String? = null, isCCW: Boolean = false) = apply {
        ensureParamIsland()
        val combine = CombinePicInfo(
            picInfo = PicInfo(type = 1, pic = PIC_PREFIX + pictureKey),
            progressInfo = CircularProgressInfo(progress = progress, colorReach = color, colorUnReach = colorUnReach, isCCW = isCCW)
        )
        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            smallIslandArea = SmallIslandArea(combinePicInfo = combine)
        )
    }

    fun setBigIslandInfo(left: ImageTextInfoLeft? = null, right: ImageTextInfoRight? = null, actionKeys: List<String>? = null) = apply {
        ensureParamIsland()
        val actionRefs = actionKeys?.map { SimpleActionRef(ACTION_PREFIX + it) }?.ifEmpty { null }
        val fixedLeft = left?.copy(picInfo = prefixPicInfo(left.picInfo))
        val fixedRight = right?.copy(picInfo = prefixPicInfo(right.picInfo))
        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = fixedLeft,
                imageTextInfoRight = fixedRight,
                actions = actionRefs
            )
        )
    }

    fun setBigIslandFixedWidthDigit(digit: Int, content: String? = null, showHighlight: Boolean = false) = apply {
        ensureParamIsland()
        this.paramIsland = this.paramIsland?.copy(
            bigIslandArea = BigIslandArea(
                fixedWidthDigitInfo = FixedWidthDigitInfo(digit = digit, content = content, showHighlightColor = showHighlight)
            )
        )
    }

    fun setBigIslandCountdown(countdownTime: Long, pictureKey: String, actionKeys: List<String>? = null) = apply {
        ensureParamIsland()
        val timer = TimerInfo(-1, countdownTime, System.currentTimeMillis(), System.currentTimeMillis())
        val left = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = PIC_PREFIX + pictureKey), textInfo = null)
        val actions = actionKeys?.map { SimpleActionRef(ACTION_PREFIX + it) }?.ifEmpty { null }

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = left,
                sameWidthDigitInfo = SameWidthDigitInfo(timerInfo = timer),
                actions = actions
            )
        )
    }

    fun setBigIslandCountUp(startTime: Long, pictureKey: String, actionKeys: List<String>? = null) = apply {
        ensureParamIsland()
        val timer = TimerInfo(1, startTime, startTime, System.currentTimeMillis())
        val left = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = PIC_PREFIX + pictureKey), textInfo = null)
        val actions = actionKeys?.map { SimpleActionRef(ACTION_PREFIX + it) }?.ifEmpty { null }

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = left,
                sameWidthDigitInfo = SameWidthDigitInfo(timerInfo = timer),
                actions = actions
            )
        )
    }

    fun setBigIslandAnim(animSrc: String, isLoop: Boolean = true, effectColor: String? = null) = apply {
        ensureParamIsland()
        this.paramIsland = this.paramIsland?.copy(
            bigIslandArea = BigIslandArea(
                picInfo = PicInfo(type = 2, pic = resolvePicSource(animSrc), loop = isLoop, effectColor = effectColor)
            )
        )
    }

    // ==========================================
    //  BUILD & HELPERS
    // ==========================================

    private fun resolveActionKeys(keys: List<String>?): List<HyperActionRef>? {
        return keys?.mapNotNull { key -> actions.firstOrNull { it.key == key }?.toActionRef(false) }?.ifEmpty { null }
    }

    private fun resolvePicSource(src: String): String {
        return if (pictures.any { PIC_PREFIX + it.key == src || it.key == src }) {
            if (src.startsWith(PIC_PREFIX)) src else PIC_PREFIX + src
        } else {
            src
        }
    }

    private fun ensureParamIsland() { if (this.paramIsland == null) this.paramIsland = ParamIsland() }
    private fun prefixPicInfo(picInfo: PicInfo?): PicInfo? { return picInfo?.copy(pic = PIC_PREFIX + picInfo.pic) }

    private fun clearOtherTemplates() {
        this.baseInfo = null
        this.chatInfo = null
        this.highlightInfo = null
        this.highlightInfoV3 = null
        this.coverInfo = null
        this.animTextInfo = null
        this.iconTextInfo = null
    }

    fun buildResourceBundle(): Bundle {
        if (!isSupported(context)) return Bundle()
        val bundle = Bundle()
        val actionsBundle = Bundle()
        actions.forEach {
            val actionIcon = it.icon ?: createTransparentIcon(context)
            actionsBundle.putParcelable(ACTION_PREFIX + it.key, Notification.Action.Builder(actionIcon, it.title, it.pendingIntent).build())
        }
        bundle.putBundle("miui.focus.actions", actionsBundle)
        val picsBundle = Bundle()
        pictures.forEach { picsBundle.putParcelable(PIC_PREFIX + it.key, it.icon) }
        bundle.putBundle("miui.focus.pics", picsBundle)
        return bundle
    }

    fun buildJsonParam(): String {
        val finalIsland = (this.paramIsland ?: ParamIsland()).copy(
            islandPriority = this.islandPriority, islandTimeout = this.islandTimeout, dismissIsland = this.dismissIsland,
            maxSize = this.maxSize, needCloseAnimation = this.needCloseAnimation, expandedTime = this.expandedTime,
            highlightColor = this.highlightColor, shareData = this.shareData
        )

        val paramV2 = ParamV2(
            business = businessName, ticker = ticker, smallWindowInfo = targetPage?.let { SmallWindowInfo(it) },
            // Templates
            chatInfo = chatInfo, baseInfo = baseInfo, highlightInfo = highlightInfo, highlightInfoV3 = highlightInfoV3,
            coverInfo = coverInfo, animTextInfo = animTextInfo, iconTextInfo = iconTextInfo,
            // Components
            paramIsland = finalIsland, actions = actions.map { it.toActionRef(true) }.ifEmpty { null },
            textButton = textButton, progressInfo = progressBar, multiProgressInfo = multiProgressInfo, bgInfo = bgInfo,
            hintInfo = hintInfo, stepInfo = stepInfo, timeout = timeout, enableFloat = enableFloat, isShowNotification = isShowNotification,
            islandFirstFloat = enableFloat
        )
        val payload = HyperIslandPayload(paramV2, scene = scene)
        val jsonString = jsonSerializer.encodeToString(payload)
        if (logEnabled) Log.d("HyperIsland", "Payload JSON: $jsonString")
        return jsonString
    }

    private fun HyperAction.toActionRef(isFullDefinition: Boolean): HyperActionRef {
        val pKey = ACTION_PREFIX + this.key
        return HyperActionRef(
            type = this.getType(),
            action = if (isFullDefinition) pKey else null,
            actionIntent = if (!isFullDefinition) pKey else null,
            actionIntentType = this.actionIntentType,
            progressInfo = if (this.isProgressButton) ProgressInfo(this.progress, this.colorReach ?: this.progressColor) else null,
            actionTitle = this.title?.toString(),
            actionBgColor = this.actionBgColor
        )
    }

    private fun HyperAction.toTextButtonInfo(): TextButtonInfo {
        val iconKey = if (this.icon != null) PIC_PREFIX + this.key else null
        return TextButtonInfo(
            type = 0,
            actionTitle = this.title?.toString() ?: "",
            actionIcon = iconKey, actionIconDark = iconKey,
            actionBgColor = this.actionBgColor, actionBgColorDark = this.actionBgColorDark ?: this.actionBgColor,
            actionTitleColor = this.titleColor, actionTitleColorDark = this.titleColorDark ?: this.titleColor,
            actionIntentType = this.actionIntentType, actionIntent = ACTION_PREFIX + this.key
        )
    }

    companion object {
        fun Builder(context: Context, businessName: String, ticker: String): HyperIslandNotification = HyperIslandNotification(context, businessName, ticker)
        fun isSupported(context: Context): Boolean = isXiaomiDevice() && hasFocusPermission(context) && isSupportIsland()
        private fun isXiaomiDevice() = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
        private fun hasFocusPermission(context: Context): Boolean {
            return try {
                val bundle = context.contentResolver.call("content://miui.statusbar.notification.public".toUri(), "canShowFocus", null, Bundle().apply { putString("package", context.packageName) })
                bundle?.getBoolean("canShowFocus", false) ?: false
            } catch (e: Exception) { false }
        }
        @SuppressLint("PrivateApi")
        private fun isSupportIsland(): Boolean {
            return try {
                val method = Class.forName("android.os.SystemProperties").getDeclaredMethod("getBoolean", String::class.java, Boolean::class.java)
                method.invoke(null, "persist.sys.feature.island", false) as Boolean
            } catch (e: Exception) { false }
        }
    }
}