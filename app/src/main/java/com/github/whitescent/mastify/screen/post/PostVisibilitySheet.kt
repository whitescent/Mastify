package com.github.whitescent.mastify.screen.post

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostVisibilitySheet(
  sheetState: SheetState,
  currentVisibility: Visibility,
  onVisibilityUpdated: (Visibility) -> Unit,
  onDismissRequest: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    windowInsets = WindowInsets(0, WindowInsets.statusBars.getTop(LocalDensity.current), 0, 0),
    containerColor = AppTheme.colors.bottomSheetBackground
  ) {
    Column(Modifier.padding(vertical = 10.dp)) {
      Text(
        text = "更改嘟文可见性",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.align(Alignment.CenterHorizontally),
        color = AppTheme.colors.primaryContent
      )
      HeightSpacer(value = 6.dp)
      Text(
        text = "更改嘟文的可见性设置，确保只有您想要的人群可以看到您的内容。",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 48.dp),
        color = Color.Gray
      )
      HeightSpacer(value = 6.dp)
      Column(Modifier.padding(horizontal = 12.dp)) {
        Visibility.values().forEach {
          PostVisibilityItem(
            selected = it == currentVisibility,
            visibility = it,
            onClick = { onVisibilityUpdated(it) },
          )
          if (it != Visibility.values().last()) HeightSpacer(value = 6.dp)
        }
      }
    }
  }
}

@Composable
private fun PostVisibilityItem(
  selected: Boolean,
  visibility: Visibility,
  onClick: () -> Unit,
) {
  if (visibility == Visibility.Unknown) return
  Surface(
    shape = RoundedCornerShape(14.dp),
    border = if (selected) BorderStroke((2.5).dp, AppTheme.colors.bottomSheetSelectedBorder) else null,
    color = when (selected) {
      true -> AppTheme.colors.bottomSheetItemSelectedBackground
      else -> AppTheme.colors.bottomSheetItemBackground
    },
    onClick = onClick,
    modifier = Modifier.fillMaxWidth()
  ) {
    CenterRow(Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
      CenterRow(Modifier.weight(1f)) {
        Icon(
          painter = when (visibility) {
            Visibility.Public -> painterResource(R.drawable.globe)
            Visibility.Unlisted -> painterResource(R.drawable.lock_open)
            Visibility.Private -> painterResource(R.drawable.lock)
            Visibility.Direct -> painterResource(R.drawable.at)
            else -> throw IllegalArgumentException("Invalid visibility: $visibility")
          },
          contentDescription = null,
          tint = AppTheme.colors.primaryContent,
          modifier = Modifier.size(24.dp)
        )
        WidthSpacer(value = 4.dp)
        Text(
          text = when (visibility) {
            Visibility.Public -> "对所有人公开"
            Visibility.Unlisted -> "在公共时间轴中隐藏"
            Visibility.Private -> "仅关注者可见"
            Visibility.Direct -> "仅提及的人可见"
            else -> throw IllegalArgumentException("Invalid visibility: $visibility")
          },
          color = AppTheme.colors.primaryContent,
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp
        )
      }
      if (selected) {
        Icon(
          painter = painterResource(id = R.drawable.check_border_0_5),
          contentDescription = null,
          modifier = Modifier.size(20.dp),
          tint = AppTheme.colors.bottomSheetItemSelectedIcon
        )
      }
    }
  }
}
