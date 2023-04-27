package com.github.whitescent.mastify.network.model.response.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientInfo(
  @SerialName("client_id")
  val clientId: String,
  @SerialName("client_secret")
  val clientSecret: String,
  @SerialName("vapid_key")
  val vapidKey: String
)
