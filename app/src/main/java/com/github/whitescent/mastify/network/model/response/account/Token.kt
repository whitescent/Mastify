package com.github.whitescent.mastify.network.model.response.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Token(
  @SerialName("access_token") val accessToken: String
)
