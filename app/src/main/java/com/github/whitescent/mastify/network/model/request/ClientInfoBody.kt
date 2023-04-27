package com.github.whitescent.mastify.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientInfoBody(
  @SerialName("client_name")
  val clientName: String,
  @SerialName("redirect_uris")
  val redirectUris: String,
  val scopes: String
)
