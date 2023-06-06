package com.github.whitescent.mastify.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimelineModel(
  val firstVisibleItemIndex: Int,
  val offset: Int
) : Parcelable
