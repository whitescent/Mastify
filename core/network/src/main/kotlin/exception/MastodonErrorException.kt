package com.github.whitescent.mastify.core.network.exception

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MastodonErrorException(
  @SerialName("error") val error: String
  // Normally the json returned by the mastodon backend only has the error field,
  // so if other instances have other error messages, we'll add them here.
)