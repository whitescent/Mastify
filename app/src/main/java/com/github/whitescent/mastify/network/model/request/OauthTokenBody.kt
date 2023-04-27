package com.github.whitescent.mastify.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OauthTokenBody(
  @SerialName("client_id")
  val clientId: String,

  @SerialName("client_secret")
  val clientSecret: String,

  @SerialName("redirect_uri")
  val redirectUri: String,

  @SerialName("grant_type")
  val grantType: String,

  val code: String,
  val scope: String,
)
