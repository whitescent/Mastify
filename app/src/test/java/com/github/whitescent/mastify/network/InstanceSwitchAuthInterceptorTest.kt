/*
 * Copyright 2023 WhiteScent
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

package com.github.whitescent.mastify.network

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.database.dao.AccountDao
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.database.util.Converters
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// This test verifies whether our custom interceptor is effective
@RunWith(AndroidJUnit4::class)
class InstanceSwitchAuthInterceptorTest {

  private val mockWebServer = MockWebServer()
  private lateinit var db: AppDatabase
  private lateinit var accountDao: AccountDao

  @Before
  fun setup() {
    mockWebServer.start()
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .addTypeConverter(Converters(json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
      }))
      .build()
    accountDao = db.accountDao()
  }

  @After
  fun teardown() {
    mockWebServer.shutdown()
  }

  @Test
  fun `should make regular request when requested`() {
    mockWebServer.enqueue(MockResponse())

    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(InstanceSwitchAuthInterceptor(db))
      .build()

    val request = Request.Builder()
      .get()
      .url(mockWebServer.url("/test"))
      .build()

    val response = okHttpClient.newCall(request).execute()

    Assert.assertEquals(200, response.code)
  }

  @Test
  fun `should changed request hostname when request url with domain header`() {
    mockWebServer.enqueue(MockResponse())

    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(InstanceSwitchAuthInterceptor(db))
      .build()

    val request = Request.Builder()
      .get()
      .url("http://${MastodonApi.PLACEHOLDER_DOMAIN}:${mockWebServer.port}/test")
      .header(MastodonApi.DOMAIN_HEADER, mockWebServer.hostName)
      .build()

    val response = okHttpClient.newCall(request).execute()

    Assert.assertEquals(200, response.code)

    // request hostname should be the header's hostname
    Assert.assertEquals(
      "http://${mockWebServer.hostName}:${mockWebServer.port}/test",
      mockWebServer.takeRequest().requestUrl.toString()
    )
  }

  @Test
  fun `should use user's instance name as request hostname when user is logged in`() = runTest {
    mockWebServer.enqueue(MockResponse())
    val fakeAccount = AccountEntity(
      accountId = "fake",
      username = "username",
      displayName = "displayName",
      note = "note",
      domain = mockWebServer.hostName,
      profilePictureUrl = "avatar",
      header = "header",
      followersCount = 0,
      followingCount = 0,
      statusesCount = 0,
      createdAt = "createdAt",
      emojis = emptyList(),
      fields = emptyList(),
      accessToken = "accessToken",
      clientId = "clientId",
      clientSecret = "clientSecret",
      isActive = true,
      id = 0,
      firstVisibleItemIndex = 0,
      offset = 0,
      lastNotificationId = "114514"
    )
    accountDao.insert(fakeAccount)

    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(InstanceSwitchAuthInterceptor(db))
      .build()

    val request = Request.Builder()
      .get()
      .url("http://${MastodonApi.PLACEHOLDER_DOMAIN}:${mockWebServer.port}/test")
      .build()

    val response = okHttpClient.newCall(request).execute()

    Assert.assertEquals(200, response.code)

    // request hostname should be the user's instance name
    Assert.assertEquals(
      "http://${fakeAccount.domain}:${mockWebServer.port}/test",
      mockWebServer.takeRequest().requestUrl.toString()
    )
  }
}
