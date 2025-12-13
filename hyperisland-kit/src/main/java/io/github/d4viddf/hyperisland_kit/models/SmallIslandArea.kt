package io.github.d4viddf.hyperisland_kit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmallIslandArea(
    @SerialName("imageTextInfoLeft") val imageTextInfoLeft: ImageTextInfoLeft? = null,
    @SerialName("imageTextInfoRight") val imageTextInfoRight: ImageTextInfoRight? = null,
    @SerialName("combinePicInfo") val combinePicInfo: CombinePicInfo? = null,
    val picInfo: PicInfo? = null
)