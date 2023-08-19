package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.gestures.GestureCancellationException
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private const val INLINE_CONTENT_TAG = "androidx.compose.foundation.text.inlineContent"

/*
* ClickableText with material3 theme support
* */

@Composable
fun ClickableText(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  color: Color = Color.Unspecified,
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
  inlineContent: Map<String, InlineTextContent> = mapOf(),
  onTextLayout: (TextLayoutResult) -> Unit = {},
  style: TextStyle = LocalTextStyle.current,
  onClick: (annotations: AnnotatedString.Range<String>) -> Unit = {},
  pointerInput: (suspend PointerInputScope.() -> Unit)? = null,
) {
  var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
  var pressedTextBounds by remember { mutableStateOf((listOf<Rect>())) }
  val pressOutlineDraw = rememberAnnotationPressOutline(pressedTextBounds.toImmutableList())
  val pressIndicator = Modifier
    .ifNotNull(pointerInput) {
      pointerInput(pointerInput, it)
    }
    .pointerInput(onClick) {
      detectTapAndPress(
        onPress = { offset ->
          textLayoutResult?.also { layoutResult ->
            val clickedAnnotation = getAnnotationForOffsetOrNull(layoutResult, offset, text)
            clickedAnnotation?.let {
              pressedTextBounds = layoutResult.getBoundingBoxes(
                it.start,
                it.end,
              )
            }
            tryAwaitRelease()
            pressedTextBounds = listOf()
          }
        },
        shouldConsumePress = { offset ->
          val clickedAnnotation = textLayoutResult?.let {
            getAnnotationForOffsetOrNull(it, offset, text)
          }
          clickedAnnotation != null
        },
        onTap = { offset ->
          val clickedAnnotation = textLayoutResult?.let {
            getAnnotationForOffsetOrNull(it, offset, text)
          }

          if (clickedAnnotation != null) {
            onClick(clickedAnnotation)
            true
          } else {
            false
          }
        },
      )
    }

  val textColor = color.takeOrElse {
    style.color.takeOrElse {
      LocalContentColor.current
    }
  }
  // NOTE(text-perf-review): It might be worthwhile writing a bespoke merge implementation that
  // will avoid reallocating if all of the options here are the defaults
  val mergedStyle = style.merge(
    TextStyle(
      color = textColor,
      fontSize = fontSize,
      fontWeight = fontWeight,
      textAlign = textAlign,
      lineHeight = lineHeight,
      fontFamily = fontFamily,
      textDecoration = textDecoration,
      fontStyle = fontStyle,
      letterSpacing = letterSpacing,
    ),
  )
  BasicText(
    text = text,
    modifier = modifier
      .then(pressIndicator)
      .drawBehind {
        pressOutlineDraw()
      },
    style = mergedStyle,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    inlineContent = inlineContent,
    onTextLayout = {
      textLayoutResult = it
      onTextLayout(it)
    },
  )
}

private fun getAnnotationForOffsetOrNull(
  layoutResult: TextLayoutResult,
  clickedOffset: Offset,
  text: AnnotatedString,
): AnnotatedString.Range<String>? {
  val textOffset = layoutResult.getOffsetForPosition(clickedOffset)
  return text.getStringAnnotations(textOffset, textOffset).firstOrNull()
    ?.takeUnless { it.tag == INLINE_CONTENT_TAG }
}

@Composable
private fun rememberAnnotationPressOutline(
  textBounds: ImmutableList<Rect>,
  layoutDirection: LayoutDirection = LocalLayoutDirection.current,
  color: Color = MaterialTheme.colorScheme.primary,
): DrawScope.() -> Unit = remember(textBounds) {
  {
    textBounds.fastForEachIndexed { index, rect ->
      val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
      val leftCornersRadius = when {
        layoutDirection == LayoutDirection.Ltr && index == 0 -> cornerRadius
        layoutDirection == LayoutDirection.Rtl && index == textBounds.size - 1 -> cornerRadius
        else -> CornerRadius.Zero
      }
      val rightCornersRadius = when {
        layoutDirection == LayoutDirection.Rtl && index == 0 -> cornerRadius
        layoutDirection == LayoutDirection.Ltr && index == textBounds.size - 1 -> cornerRadius
        else -> CornerRadius.Zero
      }
      val path = Path().apply {
        addRoundRect(
          RoundRect(
            rect = Rect(
              offset = rect.topLeft,
              size = rect.size,
            ),
            topLeft = leftCornersRadius,
            topRight = rightCornersRadius,
            bottomLeft = leftCornersRadius,
            bottomRight = rightCornersRadius,
          ),
        )
      }
      drawPath(path, color = color.copy(alpha = 0.1f), style = Fill)
      drawPath(
        path,
        color = color.copy(alpha = 0.6f),
        style = Stroke(width = 1.sp.toPx()),
      )
    }
  }
}

private fun TextLayoutResult.getBoundingBoxes(
  startOffset: Int,
  endOffset: Int,
): ImmutableList<Rect> {
  if (startOffset == endOffset) {
    return persistentListOf()
  }

  val startLineNum = getLineForOffset(startOffset)
  val endLineNum = getLineForOffset(endOffset)

  // Compose UI does not offer any API for reading paragraph direction for an entire line.
  // So this code assumes that all paragraphs in the text will have the same direction.
  // It also assumes that this paragraph does not contain bi-directional text.
  val isLtr =
    multiParagraph.getParagraphDirection(offset = layoutInput.text.lastIndex) == ResolvedTextDirection.Ltr

  return fastMapRange(startLineNum, endLineNum) { lineNum ->
    Rect(
      top = getLineTop(lineNum),
      bottom = getLineBottom(lineNum),
      left = if (lineNum == startLineNum) {
        getHorizontalPosition(startOffset, usePrimaryDirection = isLtr)
      } else {
        getLineLeft(lineNum)
      },
      right = if (lineNum == endLineNum) {
        getHorizontalPosition(endOffset, usePrimaryDirection = isLtr)
      } else {
        getLineRight(lineNum)
      },
    )
  }.toImmutableList()
}

private val NoPressGesture: suspend PressGestureScope.(Offset) -> Unit = { }

private suspend fun PointerInputScope.detectTapAndPress(
  onPress: suspend PressGestureScope.(Offset) -> Unit = NoPressGesture,
  shouldConsumePress: (Offset) -> Boolean,
  onTap: ((Offset) -> Boolean)? = null,
) {
  val pressScope = PressGestureScopeImpl(this)
  coroutineScope {
    awaitEachGesture {
      launch {
        pressScope.reset()
      }

      val down = awaitFirstDown(requireUnconsumed = false).also {
        if (shouldConsumePress(it.position)) it.consume()
      }

      if (onPress !== NoPressGesture) {
        launch {
          pressScope.onPress(down.position)
        }
      }

      val up = waitForUpOrCancellation()
      if (up == null) {
        launch {
          pressScope.cancel() // tap-up was canceled
        }
      } else {
        val shouldConsume = onTap?.invoke(up.position)
        if (shouldConsume == true) {
          up.consume()
        }
        launch {
          pressScope.release()
        }
      }
    }
  }
}

/**
 * [detectTapGestures]'s implementation of [PressGestureScope].
 */
private class PressGestureScopeImpl(
  density: Density,
) : PressGestureScope, Density by density {
  private var isReleased = false
  private var isCanceled = false
  private val mutex = Mutex(locked = false)

  /**
   * Called when a gesture has been canceled.
   */
  fun cancel() {
    isCanceled = true
    mutex.unlock()
  }

  /**
   * Called when all pointers are up.
   */
  fun release() {
    isReleased = true
    mutex.unlock()
  }

  /**
   * Called when a new gesture has started.
   */
  fun reset() {
    mutex.tryLock() // If tryAwaitRelease wasn't called, this will be unlocked.
    isReleased = false
    isCanceled = false
  }

  override suspend fun awaitRelease() {
    if (!tryAwaitRelease()) {
      throw GestureCancellationException("The press gesture was canceled.")
    }
  }

  override suspend fun tryAwaitRelease(): Boolean {
    if (!isReleased && !isCanceled) {
      mutex.lock()
    }
    return isReleased
  }
}

@OptIn(ExperimentalContracts::class)
inline fun <R> fastMapRange(
  start: Int,
  end: Int,
  transform: (Int) -> R
): List<R> {
  contract { callsInPlace(transform) }
  val destination = ArrayList<R>(/* initialCapacity = */ end - start + 1)
  for (i in start..end) {
    destination.add(transform(i))
  }
  return destination
}

inline fun <T : Any> Modifier.ifNotNull(value: T?, builder: Modifier.(T) -> Modifier): Modifier =
  then(if (value != null) builder(value) else Modifier)
