package com.github.whitescent.mastify.network.model.status

import kotlinx.serialization.Serializable

@Serializable
data class StatusContext(
  val ancestors: List<Status>,
  val descendants: List<Status>
)
