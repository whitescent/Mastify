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

package com.github.whitescent.mastify.utils

import android.content.Context
import android.net.Uri
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Mention

fun statusLinkHandler(
  mentions: List<Mention>,
  context: Context,
  link: String?,
  navigateToProfile: (Account) -> Unit,
  navigateToTagInfo: (String) -> Unit
) {
  val regex = """.*/tags/(.+)""".toRegex()
  val mention = mentions.firstOrNull { it.url == link }
  val hashtag = link?.contains("tags")?.let {
    regex.matchEntire(link)?.groupValues?.getOrNull(1)
  }
  when {
    mention != null -> navigateToProfile(mention.toAccount())
    hashtag != null -> navigateToTagInfo(hashtag)
    else -> {
      launchCustomChromeTab(
        context = context,
        uri = Uri.parse(link)
      )
    }
  }
}
