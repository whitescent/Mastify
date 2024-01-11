package com.github.whitescent.mastify.screen.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.whitescent.R
import com.github.whitescent.mastify.data.repository.UploadEvent
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.viewModel.MediaModel
import com.microsoft.fluentui.theme.token.Icon

@Composable
fun PostImage(
  mediaModel: MediaModel,
  onCancelImage: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.clip(AppTheme.shape.mediumAvatar).heightIn(max = 300.dp)
  ) {
    Box(
      modifier = Modifier.let {
        if (mediaModel.uploadEvent !is UploadEvent.FinishedEvent) {
          it.drawWithContent {
            drawContent()
            when (mediaModel.uploadEvent) {
              is UploadEvent.ProgressEvent -> {
                drawRect(Color.Black.copy(
                  alpha = (1 - mediaModel.uploadEvent.percentage / 100f).coerceIn(0f, 0.5f))
                )
              }
              is UploadEvent.ErrorEvent -> drawRect(Color.Black.copy(0.5f))
              else -> Unit
            }
          }
        } else it
      }
    ) {
      AsyncImage(
        model = mediaModel.uri,
        contentDescription = null,
        modifier = Modifier.fillMaxHeight(),
        contentScale = ContentScale.Crop
      )
    }
    Box(
      modifier = Modifier.align(Alignment.Center)
    ) {
      when (mediaModel.uploadEvent) {
        is UploadEvent.ProgressEvent -> {
          CircularProgressIndicator(
            progress = { mediaModel.uploadEvent.percentage / 100f },
            color = AppTheme.colors.accent,
            modifier = Modifier.align(Alignment.Center)
          )
        }
        is UploadEvent.ErrorEvent -> {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              painter = painterResource(id = R.drawable.cloud_warning),
              contentDescription = null,
              modifier = Modifier.size(32.dp),
              tint = Color.Red
            )
            Text(
              text = mediaModel.uploadEvent.error.localizedMessage ?: "Error uploading image",
              color = Color.White
            )
          }
        }
        else -> Unit
      }
    }
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(12.dp)
        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
    ) {
      Icon(
        painter = painterResource(id = R.drawable.close),
        contentDescription = null,
        modifier = Modifier.padding(8.dp)
          .size(24.dp)
          .clickable { onCancelImage() },
        tint = Color.White
      )
    }
  }
}
