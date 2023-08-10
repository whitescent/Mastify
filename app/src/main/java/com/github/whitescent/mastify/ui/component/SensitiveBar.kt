package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R

@Composable
fun SensitiveBar(
  spoilerText: String,
  onClick: () -> Unit,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .height(90.dp),
    color = Color(0xFF7E7E7E),
    shape = RoundedCornerShape(10.dp),
    onClick = onClick
  ) {
    Row(Modifier.padding(24.dp)) {
      Icon(
        painter = painterResource(id = R.drawable.eye_hide),
        contentDescription = null,
        tint = sensitiveContentColor,
        modifier = Modifier.size(24.dp)
      )
      WidthSpacer(value = 8.dp)
      Column {
        Text(
          text = spoilerText.ifEmpty { stringResource(id = R.string.sensitive_content) },
          color = sensitiveContentColor,
          fontSize = 18.sp
        )
        Text(
          text = "单击以显示内容",
          color = sensitiveContentColor,
          fontSize = 12.sp
        )
      }
    }
  }
}

private val sensitiveContentColor = Color(0xFFCACACA)
