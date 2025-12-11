package io.github.d4viddf.hyperisland_kit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatInfo(
    val type: Int = 1,
    val title: String,
    val content: String? = null,
    @SerialName("picProfile")
    val picFunction: String? = null,
    val actions: List<HyperActionRef>? = null,
    val timerInfo: TimerInfo? = null
)