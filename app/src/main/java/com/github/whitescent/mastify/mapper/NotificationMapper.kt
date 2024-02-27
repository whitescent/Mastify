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

package com.github.whitescent.mastify.mapper

import com.github.whitescent.mastify.data.model.ui.NotificationUiData
import com.github.whitescent.mastify.network.model.notification.Notification

fun Notification.toUiData(lastNotificationId: String?) = NotificationUiData(
  type = Notification.byString(this.type),
  id = id,
  account = account,
  createdAt = createdAt,
  status = status?.toUiData(),
  report = report,
  unread = lastNotificationId?.let { this.id > it } ?: false
)

fun List<Notification>.toUiData(lastNotificationId: String?) = map { it.toUiData(lastNotificationId) }
