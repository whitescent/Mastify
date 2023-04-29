package com.github.whitescent.mastify.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountModel(
  val username: String,
  val instanceName: String,
  val note: String,
  val accessToken: String,
  val avatar: String,
  val header: String,
  val id: String,
  val followersCount: Long,
  val followingCount: Long,
  val statusesCount: Long,
  val isLoggedIn: Boolean
) : Parcelable
