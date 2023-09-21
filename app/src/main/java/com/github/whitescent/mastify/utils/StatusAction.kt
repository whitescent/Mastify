package com.github.whitescent.mastify.utils

import androidx.compose.runtime.Stable

@Stable
sealed interface StatusAction {
  data class CopyText(val text: String) : StatusAction
  data class CopyLink(val link: String) : StatusAction
  data object Mute : StatusAction // TODO
  data object Block : StatusAction // TODO
  data object Report : StatusAction // TODO
  data class Favorite(val id: String, val favorite: Boolean) : StatusAction
  data class Bookmark(val id: String, val bookmark: Boolean) : StatusAction
  data class Reblog(val id: String, val reblog: Boolean) : StatusAction
}
