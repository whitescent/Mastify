package com.github.whitescent.mastify.core.model.session

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginSession(
  val domain: String,
  val clientId: String,
  val clientSecret: String
) : Parcelable
