package com.github.whitescent.mastify.core.model.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppCredentials(
  @SerialName("client_id") val clientId: String,
  @SerialName("client_secret") val clientSecret: String
)
