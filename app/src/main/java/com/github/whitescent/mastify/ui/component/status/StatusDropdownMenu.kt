package com.github.whitescent.mastify.ui.component.status

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.viewModel.StatusMenuAction

@Composable
fun StatusDropdownMenu(
  expanded: Boolean,
  fullname: String,
  enableCopyText: Boolean,
  modifier: Modifier = Modifier,
  offset: IntOffset = IntOffset.Zero,
  onDismissRequest: () -> Unit,
  actionHandler: (StatusMenuAction) -> Unit
) {
  if (expanded) {
    val actions = mutableListOf<MenuAction>().apply {
      if (enableCopyText)
        add(MenuAction("复制文本内容", R.drawable.copy, StatusMenuAction.CopyText))
      add(MenuAction("复制链接", R.drawable.link_simple, StatusMenuAction.CopyLink))
      add(MenuAction("添加到书签", R.drawable.bookmark_simple, StatusMenuAction.Bookmark))
      add(MenuAction("隐藏 $fullname", R.drawable.eye_hide, StatusMenuAction.Mute))
      add(MenuAction("屏蔽 $fullname", R.drawable.block, StatusMenuAction.Block))
      add(MenuAction("举报", R.drawable.report, StatusMenuAction.Report))
    }
    Popup(
      onDismissRequest = onDismissRequest,
      properties = PopupProperties(true),
      offset = offset,
      alignment = Alignment.TopEnd,
    ) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.background,
        shadowElevation = 6.dp,
        modifier = modifier.widthIn(max = 250.dp)
      ) {
        Column {
          actions.forEach {
            StatusMenuListItem(it) { actionHandler(it.action) }
            if (it != actions.last()) AppHorizontalDivider()
          }
        }
      }
    }
  }
}

@Composable
private fun StatusMenuListItem(
  action: MenuAction,
  onClick: () -> Unit
) {
  CenterRow(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        onClick = onClick,
        indication = rememberRipple(
          bounded = true,
          radius = 250.dp,
        ),
        interactionSource = remember { MutableInteractionSource() },
      )
      .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {
    Icon(
      painter = painterResource(id = action.icon),
      contentDescription = null,
      tint = if (action.icon != R.drawable.report) AppTheme.colors.primaryContent else Color(0xFFE75656),
      modifier = Modifier.size(22.dp)
    )
    WidthSpacer(value = 8.dp)
    Text(
      text = action.text,
      color = if (action.icon != R.drawable.report) AppTheme.colors.primaryContent else Color(0xFFE75656),
      fontSize = 16.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

data class MenuAction(
  val text: String, // TODO switch to @StringRes
  @DrawableRes val icon: Int,
  val action: StatusMenuAction
)
