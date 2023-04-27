package com.github.whitescent.mastify.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InstanceModel(
  val name: String,
  val clientId: String,
  val clientSecret: String
) : Parcelable
