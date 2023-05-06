package com.github.whitescent.mastify.ui.component

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans


@Composable
fun MyHtmlText(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = Color.Black,
  linkTextColor: Color = Color(0xFF0079D3),
  fontSize: TextUnit = TextUnit.Unspecified,
  fontStyle: FontStyle? = null,
  fontWeight: FontWeight? = null,
  fontFamily: FontFamily? = null,
  letterSpacing: TextUnit = TextUnit.Unspecified,
  textDecoration: TextDecoration? = null,
  textAlign: TextAlign? = null,
  lineHeight: TextUnit = TextUnit.Unspecified,
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
  minLines: Int = 1,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  style: TextStyle = LocalTextStyle.current,
  onClickLink: ((String) -> Unit)? = null
) {
  AndroidView(
    modifier = modifier,
    factory = {
      TextView(it).apply {
        textSize = fontSize.value
        movementMethod = LinkMovementMethod.getInstance()
        textAlignment = when (textAlign) {
          TextAlign.Center -> View.TEXT_ALIGNMENT_CENTER
          TextAlign.Start -> View.TEXT_ALIGNMENT_TEXT_START
          TextAlign.End -> View.TEXT_ALIGNMENT_TEXT_END
          else -> View.TEXT_ALIGNMENT_INHERIT
        }
        ellipsize = when(overflow) {
          TextOverflow.Ellipsis -> TextUtils.TruncateAt.END
          else -> null
        }
        typeface = when (fontWeight) {
          FontWeight.Bold -> Typeface.DEFAULT_BOLD
          else -> Typeface.DEFAULT
        }
      }
    } ,
    update = {
      it.setLinkTextColor(linkTextColor.toArgb())
      it.setTextColor(color.toArgb())
      it.maxLines = maxLines
      it.minLines = minLines
      it.text =
        SpannableStringBuilder.valueOf(HtmlCompat.fromHtml(text, 0).trimEnd()).apply {
          getSpans<URLSpan>().forEach { span ->
            setSpan(
              object : ClickableSpan() {
                override fun onClick(widget: View) {
                  onClickLink?.invoke(span.url)
                }
              },
              getSpanStart(span),
              getSpanEnd(span),
              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            removeSpan(span)
          }
        }
    }
  )
}
