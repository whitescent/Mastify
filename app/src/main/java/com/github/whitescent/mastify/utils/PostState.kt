package com.github.whitescent.mastify.utils

sealed class PostState {
  data object Idle : PostState()
  data object Posting : PostState()
  data object Success : PostState()
  data object Failure : PostState()
}
