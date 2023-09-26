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

import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.database.model.AccountEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

// This test verifies whether our custom interceptor is effective
class InstanceSwitchAuthInterceptorTest {

  private val mockWebServer = MockWebServer()

  @Before
  fun setup() {
    mockWebServer.start()
  }

  @After
  fun teardown() {
    mockWebServer.shutdown()
  }

  @Test
  fun `should make regular request when requested`() {
    mockWebServer.enqueue(MockResponse())

    val accountManager: AccountRepository = mock {
      on { activeAccount } doAnswer { null }
    }

    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(InstanceSwitchAuthInterceptor(accountManager))
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

    val accountManager: AccountRepository = mock {
      on { activeAccount } doAnswer { null }
    }

    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(InstanceSwitchAuthInterceptor(accountManager))
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
  fun `should use user's instance name as request hostname when user is logged in`() {
    mockWebServer.enqueue(MockResponse())

    val accountManager: AccountRepository = mock {
      on { activeAccount } doAnswer {
        AccountEntity(
          id = 1,
          domain = mockWebServer.hostName,
          accessToken = "fakeToken",
          clientId = "fakeId",
          clientSecret = "fakeSecret",
          isActive = true,
          accountId = "fakeId",
          emojis = listOf(),
          fields = listOf(),
          followingCount = 0,
          followersCount = 0,
          statusesCount = 0,
          firstVisibleItemIndex = 0,
          offset = 0
        )
      }
    }

    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(InstanceSwitchAuthInterceptor(accountManager))
      .build()

    val request = Request.Builder()
      .get()
      .url("http://${MastodonApi.PLACEHOLDER_DOMAIN}:${mockWebServer.port}/test")
      .build()

    val response = okHttpClient.newCall(request).execute()

    Assert.assertEquals(200, response.code)

    // request hostname should be the user's instance name
    Assert.assertEquals(
      "http://${accountManager.activeAccount!!.domain}:${mockWebServer.port}/test",
      mockWebServer.takeRequest().requestUrl.toString()
    )
  }
}
