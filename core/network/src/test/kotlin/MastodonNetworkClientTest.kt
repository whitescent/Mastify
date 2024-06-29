package com.github.whitescent.mastify.core.network.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.whitescent.mastify.core.common.cast
import com.github.whitescent.mastify.core.model.AppData
import com.github.whitescent.mastify.core.model.AppDataProvider
import com.github.whitescent.mastify.core.network.MastodonNetworkClient
import com.github.whitescent.mastify.core.network.NetworkModule
import com.github.whitescent.mastify.core.network.internal.jsonContentHeader
import com.github.whitescent.mastify.core.network.networkInterceptor
import com.github.whitescent.mastify.core.network.test.common.FakeBody
import com.github.whitescent.mastify.core.network.test.common.FakeResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.content.TextContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import javax.inject.Inject
import javax.inject.Singleton

@Config(
  manifest = Config.NONE,
  application = HiltTestApplication::class
)
@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class MastodonNetworkClientTest {
  @get:Rule
  var hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var json: Json

  @Inject
  lateinit var appDataProvider: AppDataProvider

  @Before
  fun setup() {
    ShadowLog.stream = System.out
    hiltRule.inject()
  }

  @Test
  fun `test get requests`() = runTest {
    val client = ApiClient(
      engine = MockEngine { _ ->
        respond(
          content = ByteReadChannel(
            """
              {
                "id": "1",
                "replies_count": 7,
                "in_reply_to_id": null
              }
            """.trimIndent()
          ),
          status = HttpStatusCode.OK,
          headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
      },
      json = json,
      appData = appDataProvider
    )
    println("result is ${client.get().getOrNull()}")
    assert(client.get().getOrNull() != null)
  }

  @Test
  fun `test get requests with query`() = runTest {
    var requestUrl = ""
    val client = ApiClient(
      engine = MockEngine { request ->
        requestUrl = request.url.toString()
        respond(
          content = ByteReadChannel(
            """
              {
                "id": "1",
                "replies_count": 7,
                "in_reply_to_id": null
              }
            """.trimIndent()
          ),
          status = HttpStatusCode.OK,
          headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
      },
      json = json,
      appData = appDataProvider
    )
    val result = client.getWithQuery()
    println("request url is $requestUrl result is ${result.getOrNull()}")
    assert(requestUrl == "https://chachako.com/v1/status?limit=2&offset=12")
  }

  @Test
  fun `test post requests`() = runTest {
    var requestBody = ""
    val client = ApiClient(
      engine = MockEngine { request ->
        requestBody = json.parseToJsonElement(request.body.cast<TextContent>().text)
          .cast<JsonObject>()["status"]?.jsonPrimitive?.content ?: ""
        respond(
          content = ByteReadChannel(
            """
              {
                "id": "1",
                "replies_count": 7,
                "in_reply_to_id": null
              }
            """.trimIndent()
          ),
          status = HttpStatusCode.OK,
          headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
      },
      json = json,
      appData = appDataProvider
    )
    client.post()
    println("request body $requestBody ${requestBody == "id114514"}")
    assert(requestBody == "id114514")
  }

  class ApiClient(
    engine: HttpClientEngine,
    json: Json,
    appData: AppDataProvider
  ) {
    private val httpClient = HttpClient(engine) {
      install(networkInterceptor(json))
      install(jsonContentHeader())
      install(ContentNegotiation) { json(json) }
      defaultRequest {
        val data = appData.getAppData()
        url.protocol = URLProtocol.HTTPS
        data.instanceUrl?.let {
          url.host = data.instanceUrl!!
        }
        data.token?.let {
          bearerAuth(it)
        }
      }
    }

    private val mastodonTestClient = MastodonNetworkClient(httpClient)

    suspend fun get() = mastodonTestClient.get<FakeResponse>("v1/status")
    suspend fun getWithQuery() = mastodonTestClient.get<FakeResponse>(
      url = "v1/status",
      query = listOf(
        "limit" to 2,
        "offset" to "12"
      )
    )
    suspend fun post() = mastodonTestClient.post<FakeResponse>("v1/status") {
      body(FakeBody("id114514"))
    }
  }

  @Module
  @InstallIn(SingletonComponent::class)
  class DependenciesModule {

    @Provides
    @Singleton
    fun provideAppDataPreferences() = object : AppDataProvider {
      override fun getAppData(): AppData = AppData("chachako.com", "114514")
    }

    @Provides
    @Singleton
    fun provideJson() = Json {
      isLenient = true
      explicitNulls = false
      ignoreUnknownKeys = true
    }
  }
}
