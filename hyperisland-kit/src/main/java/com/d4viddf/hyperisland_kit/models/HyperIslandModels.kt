package com.d4viddf.hyperisland_kit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Top-level payload wrapper
@Serializable
data class HyperIslandPayload(
    @SerialName("param_v2")
    val paramV2: ParamV2
)

// The main "param_v2" object
@Serializable
data class ParamV2(
    val protocol: Int = 3,
    val business: String, // e.g., "company"
    val updatable: Boolean = true,
    val ticker: String,
    @SerialName("isShownNotification")
    val isShownNotification: Boolean = true,
    @SerialName("islandFirstFloat")
    val islandFirstFloat: Boolean = true,
    @SerialName("smallWindowInfo")
    val smallWindowInfo: SmallWindowInfo? = null,
    @SerialName("param_island")
    val paramIsland: ParamIsland? = null,
    val chatInfo: ChatInfo? = null, // From
    val actions: List<HyperActionRef>? = null
)

@Serializable
data class SmallWindowInfo(
    val targetPage: String // e.g., "com.example.projectname.MainActivity" [cite: 181]
)

// Corresponds to "IM图文组件:chatinfo" [cite: 3604]
@Serializable
data class ChatInfo(
    val type: Int = 1,
    val title: String,
    val content: String? = null, // Note: Set to null if using timerInfo [cite: 2448]
    @SerialName("picFunction")
    val picFunction: String? = null, // e.g., "miui.focus.pic_imageText"
    val actions: List<HyperActionRef>? = null,
    val timerInfo: TimerInfo? = null
)

// Corresponds to "timerInfo" sub-object [cite: 3609]
@Serializable
data class TimerInfo(
    @SerialName("timerType")
    val timerType: Int, // 1 for count-up, -1 for count-down
    @SerialName("timerWhen")
    val timerWhen: Long, // Target time for countdown, current time for count-up [cite: 114, 126]
    @SerialName("timerTotal")
    val timerTotal: Long, // Start time (System.currentTimeMillis())
    @SerialName("timerSystemCurrent")
    val timerSystemCurrent: Long // System.currentTimeMillis()
)

// Represents an ACTION reference in the JSON [cite: 3697, 3704]
// This just points to an action in the bundle by its key
@Serializable
data class HyperActionRef(
    val type: Int, // 1 = standard, 2 = progress [cite: 191, 198]
    val action: String, // The KEY, e.g., "miui.focus.action.[function_name]"
    val progressInfo: ProgressInfo? = null,
    val actionTitle: String? = null, // For standard buttons [cite: 204]
    @SerialName("actionIntent")
    val actionIntent: String? = null // Some templates use this for the KEY
)

// Corresponds to "进度组件2:progressInfo" [cite: 3677]
@Serializable
data class ProgressInfo(
    val progress: Int, // 0-100
    @SerialName("colorProgress")
    val colorProgress: String? = null // e.g., "#FF8514"
)

// --- Island States (Summary/Expanded) ---

@Serializable
data class ParamIsland(
    @SerialName("islandProperty")
    val islandProperty: Int = 1,
    @SerialName("bigIslandArea")
    val bigIslandArea: BigIslandArea? = null,
    @SerialName("smallIslandArea")
    val smallIslandArea: SmallIslandArea? = null // This defines the summary "A/B" state [cite: 3881]
)

@Serializable
data class BigIslandArea(
    @SerialName("sameWidthDigitInfo")
    val sameWidthDigitInfo: SameWidthDigitInfo? = null,
    @SerialName("imageTextInfoLeft")
    val imageTextInfoLeft: ImageTextInfoLeft? = null
    // Add other components from "焦点通知/岛(展开态)" [cite: 2537] as needed
)

// Corresponds to "等宽数字文本组件" (e.g., countdown) [cite: 4193]
@Serializable
data class SameWidthDigitInfo(
    val timerInfo: TimerInfo,
    @SerialName("showHighlightColor")
    val showHighlightColor: Boolean = true
)

// --- A/B Zone Components for Summary State ---

// "Small Island" or Summary state [cite: 4166]
@Serializable
data class SmallIslandArea(
    // A-Zone [cite: 4180]
    @SerialName("imageTextInfoLeft")
    val imageTextInfoLeft: ImageTextInfoLeft? = null,
    // B-Zone [cite: 4183]
    @SerialName("imageTextInfoRight")
    val imageTextInfoRight: ImageTextInfoRight? = null,
    // Used for simple icon-only small island [cite: 4198]
    val picInfo: PicInfo? = null
)

// A-Zone: "图文组件1:imageTextInfoLeft" [cite: 4180]
@Serializable
data class ImageTextInfoLeft(
    val type: Int = 1,
    val picInfo: PicInfo? = null,
    val textInfo: TextInfo? = null
)

// B-Zone: "图文组件2:imageTextInfoRight" [cite: 4183]
@Serializable
data class ImageTextInfoRight(
    val type: Int = 2,
    val picInfo: PicInfo? = null,
    val textInfo: TextInfo? = null
)

// Generic PicInfo [cite: 4198]
@Serializable
data class PicInfo(
    val type: Int = 1, // 1 = static icon
    val pic: String // The KEY, e.g., "miui.focus.pic_imageText"
)

// Generic TextInfo [cite: 4180]
@Serializable
data class TextInfo(
    val title: String,
    val content: String? = null,
    @SerialName("showHighlightColor")
    val showHighlightColor: Boolean = false,
    val narrowFont: Boolean? = null // For "窄字体" [cite: 3922]
)