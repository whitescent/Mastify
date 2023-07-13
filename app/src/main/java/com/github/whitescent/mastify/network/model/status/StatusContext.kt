package com.github.whitescent.mastify.network.model.status

data class StatusContext(
  val ancestors: List<Status>,
  val descendants: List<Status>
)
