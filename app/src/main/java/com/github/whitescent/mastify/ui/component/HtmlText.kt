package com.github.whitescent.mastify.ui.component

import android.graphics.Typeface
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.util.fastForEach
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpanned
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.ui.theme.AppTheme
import java.io.File

private const val URL_TAG = "url_tag"
private const val PATH_SYSTEM_FONTS_FILE = "/system/etc/fonts.xml"
private const val PATH_SYSTEM_FONTS_DIR = "/system/fonts/"

@Composable
fun HtmlText(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = AppTheme.colors.primaryContent,
  style: TextStyle = TextStyle.Default.copy(color = color),
  softWrap: Boolean = true,
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  onClickLink: ((String) -> Unit)? = null,
  onClick: (() -> Unit)? = null,
  fontSize: TextUnit = 14.sp,
  fontWeight: FontWeight? = null,
  flags: Int = HtmlCompat.FROM_HTML_MODE_COMPACT,
  urlSpanStyle: SpanStyle = SpanStyle(
    color = Color(0xFF0079D3),
    textDecoration = TextDecoration.None
  ),
  inlineContent: Map<String, InlineTextContent> = mapOf()
) {
  val content = text.asHTML(fontSize, flags, inlineContent, urlSpanStyle)
  if (onClickLink != null) {
    ClickableText(
      modifier = modifier,
      text = content,
      style = style,
      fontWeight = fontWeight,
      softWrap = softWrap,
      overflow = overflow,
      maxLines = maxLines,
      onTextLayout = onTextLayout,
      onClick = {
        content
          .getStringAnnotations(URL_TAG, it.start, it.end)
          .firstOrNull()
          ?.let { stringAnnotation -> onClickLink(stringAnnotation.item) }
          ?: run { onClick?.invoke() }
      },
      inlineContent = inlineContent
    )
  } else {
    Text(
      modifier = modifier,
      text = content,
      style = style,
      fontSize = fontSize,
      fontWeight = fontWeight,
      softWrap = softWrap,
      overflow = overflow,
      maxLines = maxLines,
      onTextLayout = onTextLayout,
      inlineContent = inlineContent
    )
  }
}

@Composable
private fun String.asHTML(
  fontSize: TextUnit,
  flags: Int,
  inlineContent: Map<String, InlineTextContent>,
  urlSpanStyle: SpanStyle
) = buildAnnotatedString {
  // remove bottom padding
  val spanned = HtmlCompat.fromHtml(this@asHTML, flags).trimEnd().toSpanned()
  val spans = spanned.getSpans(0, spanned.length, Any::class.java)

  annotateInlineEmojis(spanned.toString(), inlineContent.keys.toList(), this)

  spans
    .filter { it !is BulletSpan }
    .forEach { span ->
      val start = spanned.getSpanStart(span)
      val end = spanned.getSpanEnd(span)
      when (span) {
        is RelativeSizeSpan -> span.spanStyle(fontSize)
        is StyleSpan -> span.spanStyle()
        is UnderlineSpan -> span.spanStyle()
        is ForegroundColorSpan -> span.spanStyle()
        is TypefaceSpan -> span.spanStyle()
        is StrikethroughSpan -> span.spanStyle()
        is SuperscriptSpan -> span.spanStyle()
        is SubscriptSpan -> span.spanStyle()
        is URLSpan -> {
          addStringAnnotation(
            tag = URL_TAG,
            annotation = span.url,
            start = start,
            end = end
          )
          urlSpanStyle
        }
        else -> {
          null
        }
      }?.let { spanStyle ->
        addStyle(spanStyle, start, end)
      }
    }
}

fun annotateInlineEmojis(
  text: String,
  shortcodes: List<String>,
  to: AnnotatedString.Builder,
) {
  val emojiPositions = emojiRegex.findAll(text).filter { shortcodes.contains(it.value) }
  text.forEachIndexed { index: Int, c: Char ->
    val emojiPosition =
      emojiPositions.find {
        it.range.any { rangeElement -> rangeElement in index - 1..index + 1 }
      }
    // Account for custom emoji ':' parenthesis
    if (emojiPosition?.range?.first == index) {
      emojiPosition.also {
        to.appendInlineContent(emojiPosition.value)
      }
    }
    if (emojiPosition == null) {
      to.append(c)
    }
  }
}

private val emojiRegex = "(?<=:)(.*?)(?=:)".toRegex() // Everything between ':' and ':' non inclusive

@Composable
private fun linkTextColor() = Color(
  TextView(LocalContext.current).linkTextColors.defaultColor
)

internal fun UnderlineSpan.spanStyle(): SpanStyle =
  SpanStyle(textDecoration = TextDecoration.Underline)

internal fun ForegroundColorSpan.spanStyle(): SpanStyle =
  SpanStyle(color = Color(foregroundColor))

internal fun StrikethroughSpan.spanStyle(): SpanStyle =
  SpanStyle(textDecoration = TextDecoration.LineThrough)

internal fun RelativeSizeSpan.spanStyle(fontSize: TextUnit): SpanStyle =
  SpanStyle(fontSize = (fontSize.value * sizeChange).sp)

internal fun StyleSpan.spanStyle(): SpanStyle? = when (style) {
  Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
  Typeface.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
  Typeface.BOLD_ITALIC -> SpanStyle(
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Italic,
  )
  else -> null
}

internal fun SubscriptSpan.spanStyle(): SpanStyle =
  SpanStyle(baselineShift = BaselineShift.Subscript)

internal fun SuperscriptSpan.spanStyle(): SpanStyle =
  SpanStyle(baselineShift = BaselineShift.Superscript)

internal fun TypefaceSpan.spanStyle(): SpanStyle? {
  val xmlContent = File(PATH_SYSTEM_FONTS_FILE).readText()
  return if (xmlContent.contains("""<family name="$family""")) {
    val familyChunkXml = xmlContent.substringAfter("""<family name="$family""")
      .substringBefore("""</family>""")
    val fontName = familyChunkXml.substringAfter("""<font weight="400" style="normal">""")
      .substringBefore("</font>")
    SpanStyle(fontFamily = FontFamily(Typeface.createFromFile("$PATH_SYSTEM_FONTS_DIR$fontName")))
  } else {
    null
  }
}

@Composable
fun inlineTextContentWithEmoji(
  emojis: List<Emoji>,
  size: TextUnit = LocalTextStyle.current.fontSize,
): Map<String, InlineTextContent> {
  return buildMap {
    emojis.fastForEach { emoji ->
      put(
        emoji.shortcode,
        InlineTextContent(
          placeholder = Placeholder(
            width = size.takeOrElse { 14.sp },
            height = size.takeOrElse { 14.sp },
            PlaceholderVerticalAlign.TextCenter,
          ),
          children = {
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current)
                .data(emoji.url)
                .crossfade(true)
                .build(),
              contentDescription = emoji.shortcode,
              modifier = Modifier.fillMaxSize(),
            )
          },
        ),
      )
    }
  }
}
