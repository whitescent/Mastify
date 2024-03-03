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

import com.github.whitescent.mastify.ui.component.buildPlainText
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HtmlTextTest {

  @Test
  fun `test normal parse`() {
    val text = "<p>想用 Vista 了<br>毛玻璃真好看</p>"
    assertEquals("想用 Vista 了\n毛玻璃真好看", buildPlainText(text, false))
  }

  @Test
  fun `test normal parse 2`() {
    val text = "<p><span class=\"h-card\" translate=\"no\"><a href=\"https://bgme.me/@bgme\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@<span>bgme</span></a></span></p><p>blackblaze b2 可能稍微便宜一点点 <a href=\"https://www.backblaze.com/cloud-storage/pricing\" rel=\"nofollow noopener noreferrer\" translate=\"no\" target=\"_blank\"><span class=\"invisible\">https://www.</span><span class=\"ellipsis\">backblaze.com/cloud-storage/pr</span><span class=\"invisible\">icing</span></a> 但是也实在找不出比这更便宜的 managed s3 储存了</p><p>要再便宜的话可能就得哪里找个裸机然后自己搭个 minio 之类的上去</p>"
    assertEquals("blackblaze b2 可能稍微便宜一点点 https://www.backblaze.com/cloud-storage/pricing 但是也实在找不出比这更便宜的 managed s3 储存了\n\n要再便宜的话可能就得哪里找个裸机然后自己搭个 minio 之类的上去", buildPlainText(text, true))
  }

  @Test
  fun `test p label parse`() {
    val text = "<p>Tailscale finally offers a solution to avoid choosing the sub-optimal DERP regions (kind of), by specifying <code>derpmap</code> in ACLs.  :ablobhungry:</p><p><a href=\"https://github.com/tailscale/tailscale/issues/6187#issuecomment-1928098737\" rel=\"nofollow noopener noreferrer\" translate=\"no\" target=\"_blank\"><span class=\"invisible\">https://</span><span class=\"ellipsis\">github.com/tailscale/tailscale</span><span class=\"invisible\">/issues/6187#issuecomment-1928098737</span></a></p>"
    val expected = "Tailscale finally offers a solution to avoid choosing the sub-optimal DERP regions (kind of), by specifying derpmap in ACLs.  :ablobhungry:\n\nhttps://github.com/tailscale/tailscale/issues/6187#issuecomment-1928098737"
    assertEquals(expected, buildPlainText(text, false))
  }

  @Test
  fun `test del label parse`() {
    val text = "<p><span>还是觉得某些群体在「非受众」的游戏内曲解角色和要求「增加与我们群体有关的内容」十分过分了。<br><br>玩家应该是去寻找适合自己群体的游戏，而不是对其它游戏说三道四吧。<br><br></span><del>没有？没有的话就自己做啊。</del></p>"
    val expected = "还是觉得某些群体在「非受众」的游戏内曲解角色和要求「增加与我们群体有关的内容」十分过分了。\n\n玩家应该是去寻找适合自己群体的游戏，而不是对其它游戏说三道四吧。\n\n没有？没有的话就自己做啊。"
    assertEquals(expected, buildPlainText(text, false))
  }

  @Test
  fun `test reply text`() {
    val text = "<p><a href=\"https://mastodon.ktachibana.party/@kt\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@kt@mastodon.ktachibana.party</a><span> 想用 KTT 了 他真可爱！</span></p>"
    val expected = "想用 KTT 了 他真可爱！"
    // without @username
    assertEquals(expected, buildPlainText(text, true))
  }

  @Test
  fun `test reply text with br and blank`() {
    val text = "<p><a href=\"https://mastodon.ktachibana.party/@kt\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@kt@mastodon.ktachibana.party</a><span> <br>Mikudayo~Mikudayo~</span></p>"
    val expected = "Mikudayo~Mikudayo~"
    // without @username
    assertEquals(expected, buildPlainText(text, true))
    // with @username
    assertEquals(
      "@kt@mastodon.ktachibana.party \nMikudayo~Mikudayo~",
      buildPlainText(text, false)
    )
  }

  @Test
  fun `test reply text in different p label`() {
    val text = "<p><span class=\"h-card\" translate=\"no\"><a href=\"https://infosec.exchange/@0xabad1dea\" class=\"u-url mention\">@<span>0xabad1dea</span></a></span> </p><p>A float makes the bits not sink in water.</p>"
    val expected = "A float makes the bits not sink in water."
    // without @username
    assertEquals(expected, buildPlainText(text, true))
    // with @username
    assertEquals(
      "@0xabad1dea \n\nA float makes the bits not sink in water.",
      buildPlainText(text, false)
    )
  }

  @Test
  fun `test p label`() {
    val text = "<p><span class=\"h-card\" translate=\"no\"><a href=\"https://androiddev.social/@andy\" class=\"u-url mention\">@<span>andy</span></a></span> yess 100% compose</p><p><a href=\"https://github.com/whitescent/Mastify\" target=\"_blank\" rel=\"nofollow noopener noreferrer\" translate=\"no\"><span class=\"invisible\">https://</span><span class=\"\">github.com/whitescent/Mastify</span><span class=\"invisible\"></span></a></p>"
    val expected = "yess 100% compose\n\nhttps://github.com/whitescent/Mastify"
    assertEquals(expected, buildPlainText(text, true))
  }

  @Test
  fun `test br label`() {
    val text = "<p><span class=\"h-card\" translate=\"no\"><a href=\"https://o3o.ca/@lazzzis\" class=\"u-url mention\">@<span>lazzzis</span></a></span> 混音算编曲的一部分，不过混音部分也能单独拿出来做（一般是整首都做得差不多的时候），一般编曲环节也要做混音。<br />后者的话，一般就是指的混音吧</p>"
    val expected = "混音算编曲的一部分，不过混音部分也能单独拿出来做（一般是整首都做得差不多的时候），一般编曲环节也要做混音。\n后者的话，一般就是指的混音吧"
    assertEquals(expected, buildPlainText(text, true))
  }

  @Test
  fun `test br label 2`() {
    val text = "<p><span class=\"h-card\" translate=\"no\"><a href=\"https://mastodon.ktachibana.party/@kt\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@<span>kt</span></a></span> <br>网的确好2333</p>"
    val expected = "网的确好2333"
    assertEquals(expected, buildPlainText(text, true))
  }

  @Test
  fun `test br label 3`() {
    val text = "<p><span class=\"h-card\" translate=\"no\"><a href=\"https://mastodon.ktachibana.party/@kt\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@<span>kt</span></a></span> <br>传说中的roadstreet么</p><p>-某群呆多了天天想回加-</p>"
    val expected = "传说中的roadstreet么\n\n-某群呆多了天天想回加-"
    assertEquals(expected, buildPlainText(text, true))
  }

  @Test
  fun `test strong text` () {
    val text = "<p><b>伟大的粗体 sex</b></p>"
    val expected = "伟大的粗体 sex"
    assertEquals(expected, buildPlainText(text, true))
  }

  @Test
  fun `test strong text2` () {
    val text = "<p><strong>伟大的粗体 sex</strong></p>"
    val expected = "伟大的粗体 sex"
    assertEquals(expected, buildPlainText(text, true))
  }

  @Test
  fun `test complex text`() {
    val text = "<p>Hi <a href=\"https://androiddev.social/tags/AndroidDev\" class=\"mention hashtag\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">#<span>AndroidDev</span></a></p><p>I'm working on a Mastodon client that's written entirely in Jetpack Compose and is open source <a href=\"https://github.com/whitescent/Mastify\" rel=\"nofollow noopener noreferrer\" translate=\"no\" target=\"_blank\"><span class=\"invisible\">https://</span><span class=\"\">github.com/whitescent/Mastify</span><span class=\"invisible\"></span></a><br>and now available in Google Play Early Access <a href=\"https://play.google.com/store/apps/details?id=com.github.whitescent.mastify\" rel=\"nofollow noopener noreferrer\" translate=\"no\" target=\"_blank\"><span class=\"invisible\">https://</span><span class=\"ellipsis\">play.google.com/store/apps/det</span><span class=\"invisible\">ails?id=com.github.whitescent.mastify</span></a></p><p>Note: There are many features that have not been implemented yet</p><p>If you can provide some suggestions about App, or contribute to this project, I will be very grateful! :androidPetPet:</p><p>If you want to follow Mastify's development progress, you can follow this account <span class=\"h-card\" translate=\"no\"><a href=\"https://mastodon.social/@mastify\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@<span>mastify</span></a></span></p>"
    val expected = "Hi #AndroidDev\n" +
      "\n" +
      "I'm working on a Mastodon client that's written entirely in Jetpack Compose and is open source https://github.com/whitescent/Mastify\n" +
      "and now available in Google Play Early Access https://play.google.com/store/apps/details?id=com.github.whitescent.mastify\n" +
      "\n" +
      "Note: There are many features that have not been implemented yet\n" +
      "\n" +
      "If you can provide some suggestions about App, or contribute to this project, I will be very grateful! :androidPetPet:\n" +
      "\n" +
      "If you want to follow Mastify's development progress, you can follow this account @mastify"
    assertEquals(expected, buildPlainText(text, false))
  }
}
