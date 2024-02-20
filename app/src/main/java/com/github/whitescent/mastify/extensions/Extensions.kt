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

package com.github.whitescent.mastify.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Continue
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Start
import com.github.whitescent.mastify.network.model.status.Status
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

// get all items size from 0 to index
fun <A, B> Map<A, List<B>>.getSizeOfIndex(index: Int): Int {
  if (index == 0) return 0
  var count = 0
  this.onEachIndexed { currentIndex, entry ->
    count += entry.value.size
    if (currentIndex == index - 1) return count + index
  }
  return -1
}

fun String.insertString(insert: String, index: Int): String {
  val start = this.substring(0, index)
  val end = this.substring(index)
  return start + insert + end
}

/**
 * Updates the status in a status list with the latest data from the status detail screen.
 */
fun List<StatusUiData>.updateStatusActionData(newStatus: StatusBackResult): List<StatusUiData> {
  return if (this.any { it.actionableId == newStatus.id }) {
    val result = this.toMutableList()
    val index = result.indexOfFirst { it.actionableId == newStatus.id }
    if (index != -1) {
      result[index] = result[index].copy(
        favorited = newStatus.favorited,
        favouritesCount = newStatus.favouritesCount,
        reblogged = newStatus.reblogged,
        reblogsCount = newStatus.reblogsCount,
        repliesCount = newStatus.repliesCount,
        bookmarked = newStatus.bookmarked,
        poll = newStatus.poll,
        actionable = result[index].actionable.copy(
          favorited = newStatus.favorited,
          favouritesCount = newStatus.favouritesCount,
          reblogged = newStatus.reblogged,
          reblogsCount = newStatus.reblogsCount,
          repliesCount = newStatus.repliesCount,
          bookmarked = newStatus.bookmarked,
          poll = newStatus.poll,
        )
      )
      result
    } else this
  } else this
}

@JvmName("updateStatusActionDataStatus")
fun List<Status>.updateStatusActionData(newStatus: StatusBackResult): List<Status> {
  return if (this.any { it.actionableId == newStatus.id }) {
    val result = this.toMutableList()
    forEachIndexed { index, status ->
      if (status.actionableId == newStatus.id) {
        result[index] = result[index].copy(
          favorited = newStatus.favorited,
          favouritesCount = newStatus.favouritesCount,
          reblogged = newStatus.reblogged,
          reblogsCount = newStatus.reblogsCount,
          repliesCount = newStatus.repliesCount,
          bookmarked = newStatus.bookmarked,
          poll = newStatus.poll,
          reblog = if (status.reblog != null) {
            status.reblog.copy(
              favorited = newStatus.favorited,
              favouritesCount = newStatus.favouritesCount,
              reblogged = newStatus.reblogged,
              reblogsCount = newStatus.reblogsCount,
              repliesCount = newStatus.repliesCount,
              bookmarked = newStatus.bookmarked,
              poll = newStatus.poll
            )
          } else null
        )
      }
    }
    result
  } else this
}

fun List<StatusUiData>.hasUnloadedParent(index: Int): Boolean {
  val current = get(index)
  val currentType = getReplyChainType(index)
  if (currentType == Null || !current.isInReplyTo) return false
  return when (val prev = getOrNull(index - 1)) {
    null -> false
    else -> current.inReplyToId != prev.id
  }
}

inline fun <S, B> S.emptyOr(block: (S) -> B): B? where S : Collection<*> {
  return if (this.isEmpty()) null else block(this)
}

fun List<StatusUiData>.getReplyChainType(index: Int): StatusUiData.ReplyChainType {
  val prev = getOrNull(index - 1)
  val current = get(index)
  val next = getOrNull(index + 1)

  return when {
    prev != null && next != null -> {
      when {
        (current.isInReplyTo &&
          current.inReplyToId == prev.id && next.inReplyToId == current.id) -> Continue
        next.inReplyToId == current.id -> Start
        current.inReplyToId == prev.id -> End
        else -> Null
      }
    }
    prev == null && next != null -> {
      when (next.inReplyToId) {
        current.id -> Start
        else -> Null
      }
    }
    prev != null && next == null -> {
      when {
        current.isInReplyTo && current.inReplyToId == prev.id -> End
        else -> Null
      }
    }
    else -> Null
  }
}

fun String.buildTextWithLimit(
  maxLength: Int,
  textColor: Color,
  warningBackgroundColor: Color,
  fontSize: TextUnit = 18.sp
): AnnotatedString {
  val text = this
  return buildAnnotatedString {
    withStyle(
      style = SpanStyle(fontSize = fontSize, color = textColor)
    ) {
      append(
        text = text.substring(
          startIndex = 0,
          endIndex = if (text.length <= maxLength) text.length else maxLength
        )
      )
    }
    if (text.length > maxLength) {
      withStyle(
        style = SpanStyle(
          color = textColor,
          background = warningBackgroundColor,
          fontSize = fontSize
        )
      ) {
        append(text.substring(startIndex = maxLength, endIndex = text.length))
      }
    }
  }
}

fun buildHtmlText(
  document: Document,
  filterMentionText: Boolean = false
): AnnotatedString {
  if (filterMentionText) {
    val brElements = document.select("br")
    for (br in brElements) {
      val prev = br.previousSibling()
      val hasTextBefore = prev is TextNode && prev.text().trim().isNotEmpty()
      if (!hasTextBefore) document.select("br").remove()
    }
  }
  return buildAnnotatedString {
    document.body().childNodes().forEach {
      renderNode(it, filterMentionText)
    }
  }
}

private fun AnnotatedString.Builder.renderNode(
  node: Node,
  filterMentionText: Boolean = false
) {
  when (node) {
    is Element -> renderElement(node, filterMentionText)
    is TextNode -> renderText(if (filterMentionText) node.wholeText.trim() else node.wholeText)
  }
}

private fun AnnotatedString.Builder.renderElement(
  element: Element,
  filterMentionText: Boolean = false
) {
  if (skipElement(element = element)) return
  when (val normalName = element.normalName()) {
    "a" -> {
      if (element.hasClass("u-url mention") && filterMentionText) return
      val href = element.attr("href")
      pushStringAnnotation(tag = "fake", annotation = href)
      append(element.text())
      pop()
    }

    "br" -> renderText("\n")

    "code", "pre", "strong" -> renderText(element.text())

    "span", "p", "i", "em" -> {
      val prevNode = element.previousSibling()
      val isParagraph = normalName == "p" && prevNode?.normalName() == "p"
      if (!filterMentionText) {
        if (isParagraph) append("\n\n")
      } else {
        if (isParagraph && prevNode?.hasTextNode() == true) append("\n\n")
      }
      element.childNodes().forEach {
        renderNode(
          node = it,
          filterMentionText = filterMentionText
        )
      }
    }
  }
}

private fun Node.hasTextNode(): Boolean {
  if (this is Element && hasClass("u-url mention")) return false
  if (this is TextNode && !this.isBlank) return true
  for (child in this.childNodes()) {
    if (child.hasTextNode()) return true
  }
  return false
}

private fun AnnotatedString.Builder.renderText(text: String) = append(text)

private fun skipElement(element: Element): Boolean = element.hasClass("invisible")
