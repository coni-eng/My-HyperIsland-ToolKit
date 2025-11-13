package io.github.d4viddf.hyperisland_kit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseInfo(
    val type: Int = 1,
    val title: String,
    val subTitle: String? = null,
    val content: String,
    @SerialName("picFunction")
    val picFunction: String? = null,
    val actions: List<HyperActionRef>? = null
)