/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.core.ui.shape

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

fun SmoothCornerShape(corner: CornerSize) = SmoothCornerShape(corner, corner, corner, corner)

fun SmoothCornerShape(size: Dp) = SmoothCornerShape(CornerSize(size))

fun SmoothCornerShape(size: Float) = SmoothCornerShape(CornerSize(size))

fun SmoothCornerShape(percent: Int) = SmoothCornerShape(CornerSize(percent))

fun SmoothCornerShape(
  topStart: Dp = 0.dp,
  topEnd: Dp = 0.dp,
  bottomEnd: Dp = 0.dp,
  bottomStart: Dp = 0.dp
) = SmoothCornerShape(
  topStart = CornerSize(topStart),
  topEnd = CornerSize(topEnd),
  bottomEnd = CornerSize(bottomEnd),
  bottomStart = CornerSize(bottomStart)
)

fun SmoothCornerShape(
  topStart: Float = 0.0f,
  topEnd: Float = 0.0f,
  bottomEnd: Float = 0.0f,
  bottomStart: Float = 0.0f
) = SmoothCornerShape(
  topStart = CornerSize(topStart),
  topEnd = CornerSize(topEnd),
  bottomEnd = CornerSize(bottomEnd),
  bottomStart = CornerSize(bottomStart)
)

class SmoothCornerShape(
  topStart: CornerSize = ZeroCornerSize,
  topEnd: CornerSize = ZeroCornerSize,
  bottomEnd: CornerSize = ZeroCornerSize,
  bottomStart: CornerSize = ZeroCornerSize,
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {

  override fun copy(
    topStart: CornerSize,
    topEnd: CornerSize,
    bottomEnd: CornerSize,
    bottomStart: CornerSize
  ): CornerBasedShape = SmoothCornerShape(topStart, topEnd, bottomEnd, bottomStart)

  override fun createOutline(
    size: Size,
    topStart: Float,
    topEnd: Float,
    bottomEnd: Float,
    bottomStart: Float,
    layoutDirection: LayoutDirection
  ): Outline = when {
    size.minDimension / 2 == 100f -> {
      // 百分百圆度的角在平滑圆角模式情况下会出问题，所以这里我们切换回正常情况的全圆角（效果完全相同）
      CircleShape.createOutline(size, topStart, topEnd, bottomEnd, bottomStart, layoutDirection)
    }
    topStart + topEnd + bottomStart + bottomEnd == 0f -> {
      RoundedCornerShape(0.dp).createOutline(size, topStart, topEnd, bottomEnd, bottomStart, layoutDirection)
    }
    else -> Outline.Generic(Path().apply {
      val width = size.width
      val height = size.height
      val centerX = width * 1.0f / 2
      val centerY = height * 1.0f / 2
      val posX: Float = centerX - width / 2
      val posY: Float = centerY - height / 2

      val trVertexRatio: Float = if (topEnd / (width / 2).coerceAtMost(height / 2) > 0.5) {
        val percentage = (topEnd / (width / 2).coerceAtMost(height / 2) - 0.5f) / 0.4f
        val clampedPer = 1f.coerceAtMost(percentage)
        1f - (1 - 1.104f / 1.2819f) * clampedPer
      } else {
        1f
      }
      val brVertexRatio: Float = if (bottomEnd / (width / 2).coerceAtMost(height / 2) > 0.5) {
        val percentage = (bottomEnd / (width / 2).coerceAtMost(height / 2) - 0.5f) / 0.4f
        val clampedPer = 1f.coerceAtMost(percentage)
        1f - (1 - 1.104f / 1.2819f) * clampedPer
      } else {
        1f
      }
      val tlVertexRatio: Float = if (topStart / (width / 2).coerceAtMost(height / 2) > 0.5) {
        val percentage = (topStart / (width / 2).coerceAtMost(height / 2) - 0.5f) / 0.4f
        val clampedPer = 1f.coerceAtMost(percentage)
        1f - (1 - 1.104f / 1.2819f) * clampedPer
      } else {
        1f
      }
      val blVertexRatio: Float = if (bottomStart / (width / 2).coerceAtMost(height / 2) > 0.5) {
        val percentage = (bottomStart / (width / 2).coerceAtMost(height / 2) - 0.5f) / 0.4f
        val clampedPer = 1f.coerceAtMost(percentage)
        1f - (1 - 1.104f / 1.2819f) * clampedPer
      } else {
        1f
      }

      val tlControlRatio: Float = if (topStart / (width / 2).coerceAtMost(height / 2) > 0.6) {
        val percentage = (topStart / (width / 2).coerceAtMost(height / 2) - 0.6f) / 0.3f
        val clampedPer = 1f.coerceAtMost(percentage)
        1 + (0.8717f / 0.8362f - 1) * clampedPer
      } else {
        1f
      }
      val trControlRatio: Float = if (topEnd / (width / 2).coerceAtMost(height / 2) > 0.6) {
        val percentage = (topEnd / (width / 2).coerceAtMost(height / 2) - 0.6f) / 0.3f
        val clampedPer = 1f.coerceAtMost(percentage)
        1 + (0.8717f / 0.8362f - 1) * clampedPer
      } else {
        1f
      }
      val blControlRatio: Float = if (bottomStart / (width / 2).coerceAtMost(height / 2) > 0.6) {
        val percentage = (bottomStart / (width / 2).coerceAtMost(height / 2) - 0.6f) / 0.3f
        val clampedPer = 1f.coerceAtMost(percentage)
        1 + (0.8717f / 0.8362f - 1) * clampedPer
      } else {
        1f
      }
      val brControlRatio: Float = if (bottomEnd / (width / 2).coerceAtMost(height / 2) > 0.6) {
        val percentage = (bottomEnd / (width / 2).coerceAtMost(height / 2) - 0.6f) / 0.3f
        val clampedPer = 1f.coerceAtMost(percentage)
        1 + (0.8717f / 0.8362f - 1) * clampedPer
      } else {
        1f
      }

      moveTo(posX + width / 2, posY)

      lineTo(
        posX + (width / 2).coerceAtLeast(width - topEnd / 100.0f * 128.19f * trVertexRatio),
        posY
      )
      cubicTo(
        posX + width - topEnd / 100f * 83.62f * trControlRatio,
        posY,
        posX + width - topEnd / 100f * 67.45f,
        posY + topEnd / 100f * 4.64f,
        posX + width - topEnd / 100f * 51.16f,
        posY + topEnd / 100f * 13.36f
      )
      cubicTo(
        posX + width - topEnd / 100f * 34.86f,
        posY + topEnd / 100f * 22.07f,
        posX + width - topEnd / 100f * 22.07f,
        posY + topEnd / 100f * 34.86f,
        posX + width - topEnd / 100f * 13.36f,
        posY + topEnd / 100f * 51.16f
      )
      cubicTo(
        posX + width - topEnd / 100f * 4.64f,
        posY + topEnd / 100f * 67.45f,
        posX + width,
        posY + topEnd / 100f * 83.62f * trControlRatio,
        posX + width,
        posY + (height / 2).coerceAtMost(topEnd / 100f * 128.19f * trVertexRatio)
      )

      lineTo(
        posX + width,
        posY + (height / 2).coerceAtLeast(height - bottomEnd / 100f * 128.19f * brVertexRatio)
      )
      cubicTo(
        posX + width,
        posY + height - bottomEnd / 100f * 83.62f * brControlRatio,
        posX + width - bottomEnd / 100f * 4.64f,
        posY + height - bottomEnd / 100f * 67.45f,
        posX + width - bottomEnd / 100f * 13.36f,
        posY + height - bottomEnd / 100f * 51.16f
      )
      cubicTo(
        posX + width - bottomEnd / 100f * 22.07f,
        posY + height - bottomEnd / 100f * 34.86f,
        posX + width - bottomEnd / 100f * 34.86f,
        posY + height - bottomEnd / 100f * 22.07f,
        posX + width - bottomEnd / 100f * 51.16f,
        posY + height - bottomEnd / 100f * 13.36f
      )
      cubicTo(
        posX + width - bottomEnd / 100f * 67.45f,
        posY + height - bottomEnd / 100f * 4.64f,
        posX + width - bottomEnd / 100f * 83.62f * brControlRatio,
        posY + height,
        posX + (width / 2).coerceAtLeast(width - bottomEnd / 100f * 128.19f * brVertexRatio),
        posY + height
      )

      lineTo(
        posX + (width / 2).coerceAtMost(bottomStart / 100f * 128.19f * blControlRatio),
        posY + height
      )
      cubicTo(
        posX + bottomStart / 100f * 83.62f * blControlRatio,
        posY + height,
        posX + bottomStart / 100f * 67.45f,
        posY + height - bottomStart / 100f * 4.64f,
        posX + bottomStart / 100f * 51.16f,
        posY + height - bottomStart / 100f * 13.36f
      )
      cubicTo(
        posX + bottomStart / 100f * 34.86f,
        posY + height - bottomStart / 100f * 22.07f,
        posX + bottomStart / 100f * 22.07f,
        posY + height - bottomStart / 100f * 34.86f,
        posX + bottomStart / 100f * 13.36f,
        posY + height - bottomStart / 100f * 51.16f
      )
      cubicTo(
        posX + bottomStart / 100f * 4.64f,
        posY + height - bottomStart / 100f * 67.45f,
        posX,
        posY + height - bottomStart / 100f * 83.62f * blControlRatio,
        posX,
        posY + (height / 2).coerceAtLeast(height - bottomStart / 100f * 128.19f * blVertexRatio)
      )

      lineTo(posX, posY + (height / 2).coerceAtMost(topStart / 100f * 128.19f * tlVertexRatio))
      cubicTo(
        posX,
        posY + topStart / 100f * 83.62f * tlControlRatio,
        posX + topStart / 100f * 4.64f,
        posY + topStart / 100f * 67.45f,
        posX + topStart / 100f * 13.36f,
        posY + topStart / 100f * 51.16f
      )
      cubicTo(
        posX + topStart / 100f * 22.07f,
        posY + topStart / 100f * 34.86f,
        posX + topStart / 100f * 34.86f,
        posY + topStart / 100f * 22.07f,
        posX + topStart / 100f * 51.16f,
        posY + topStart / 100f * 13.36f
      )
      cubicTo(
        posX + topStart / 100f * 67.45f,
        posY + topStart / 100f * 4.64f,
        posX + topStart / 100f * 83.62f * tlControlRatio,
        posY,
        posX + (width / 2).coerceAtMost(topStart / 100f * 128.19f * tlVertexRatio),
        posY
      )
      close()
    })
  }
}
