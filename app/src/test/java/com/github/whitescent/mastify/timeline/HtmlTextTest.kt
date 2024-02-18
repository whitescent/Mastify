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

package com.github.whitescent.mastify.timeline

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HtmlTextTest {

  @Test
  fun `test normal parse`() {
    val text = "<p>想用 Vista 了<br>毛玻璃真好看</p>"
    val document = Jsoup.parse(text)
    assertEquals("想用 Vista 了\n毛玻璃真好看", buildContentAnnotatedString(document).text)
  }

  @Test
  fun `test p label parse`() {
    val text = "<p>Tailscale finally offers a solution to avoid choosing the sub-optimal DERP regions (kind of), by specifying <code>derpmap</code> in ACLs.  :ablobhungry:</p><p><a href=\"https://github.com/tailscale/tailscale/issues/6187#issuecomment-1928098737\" rel=\"nofollow noopener noreferrer\" translate=\"no\" target=\"_blank\"><span class=\"invisible\">https://</span><span class=\"ellipsis\">github.com/tailscale/tailscale</span><span class=\"invisible\">/issues/6187#issuecomment-1928098737</span></a></p>"
    val document = Jsoup.parse(text)
    val expected = "Tailscale finally offers a solution to avoid choosing the sub-optimal DERP regions (kind of), by specifying derpmap in ACLs.  :ablobhungry:\n\nhttps://github.com/tailscale/tailscale/issues/6187#issuecomment-1928098737"
    assertEquals(expected, buildContentAnnotatedString(document).text)
  }

  @Test
  fun `test reply text`() {
    val text = "<p><a href=\"https://mastodon.ktachibana.party/@kt\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@kt@mastodon.ktachibana.party</a><span> 想用 KTT 了 他真可爱！</span></p>"
    val document = Jsoup.parse(text)
    val expected = "想用 KTT 了 他真可爱！"
    // without @username
    assertEquals(expected, buildContentAnnotatedString(document, true).text)
  }

  @Test
  fun `test reply text with br and blank`() {
    val text = "<p><a href=\"https://mastodon.ktachibana.party/@kt\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@kt@mastodon.ktachibana.party</a><span> <br>Mikudayo~Mikudayo~</span></p>"
    val document = Jsoup.parse(text)
    val expected = "Mikudayo~Mikudayo~"
    // without @username
    assertEquals(expected, buildContentAnnotatedString(document, true).text)
    // with @username
    assertEquals(
      "@kt@mastodon.ktachibana.party Mikudayo~Mikudayo~",
      buildContentAnnotatedString(document, false).text
    )
  }

  @Test
  fun `test reply text in different p label`() {
    val text = "<p><span class=\"h-card\" translate=\"no\"><a href=\"https://infosec.exchange/@0xabad1dea\" class=\"u-url mention\">@<span>0xabad1dea</span></a></span> </p><p>A float makes the bits not sink in water.</p>"
    val document = Jsoup.parse(text)
    val expected = "A float makes the bits not sink in water."
    // without @username
    assertEquals(expected, buildContentAnnotatedString(document, true).text)
    // with @username
    assertEquals(
      "@0xabad1dea \n\nA float makes the bits not sink in water.",
      buildContentAnnotatedString(document, false).text
    )
  }

  @Test
  fun `test p label`() {
    val text = "<p><span class=\"h-card\" translate=\"no\"><a href=\"https://androiddev.social/@andy\" class=\"u-url mention\">@<span>andy</span></a></span> yess 100% compose</p><p><a href=\"https://github.com/whitescent/Mastify\" target=\"_blank\" rel=\"nofollow noopener noreferrer\" translate=\"no\"><span class=\"invisible\">https://</span><span class=\"\">github.com/whitescent/Mastify</span><span class=\"invisible\"></span></a></p>"
    val document = Jsoup.parse(text)
    val expected = "yess 100% compose\n\nhttps://github.com/whitescent/Mastify"
    assertEquals(expected, buildContentAnnotatedString(document, true).text)
  }

  @Test
  fun `test br label`() {
    val text = "<p><span class=\"h-card\" translate=\"no\"><a href=\"https://o3o.ca/@lazzzis\" class=\"u-url mention\">@<span>lazzzis</span></a></span> 混音算编曲的一部分，不过混音部分也能单独拿出来做（一般是整首都做得差不多的时候），一般编曲环节也要做混音。<br />后者的话，一般就是指的混音吧</p>"
    val document = Jsoup.parse(text)
    val expected = "混音算编曲的一部分，不过混音部分也能单独拿出来做（一般是整首都做得差不多的时候），一般编曲环节也要做混音。\n后者的话，一般就是指的混音吧"
    assertEquals(expected, buildContentAnnotatedString(document, true).text)
  }
}

private fun buildContentAnnotatedString(
  document: Document,
  filterMentionText: Boolean = false
): AnnotatedString {
  if (filterMentionText && document.select("p").size == 1) {
    val br = document.select("br")
    if (!br.prev().hasText()) document.select("br").remove()
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
