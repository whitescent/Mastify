package com.github.whitescent.mastify.network.model.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessToken(
  @SerialName("access_token") val accessToken: String
)
