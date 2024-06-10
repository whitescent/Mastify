@file:Suppress("SpellCheckingInspection")

package com.github.whitescent.mastify.core.network.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.whitescent.mastify.core.network.NetworkModule
import com.github.whitescent.mastify.core.network.NetworkResult
import com.github.whitescent.mastify.core.network.networkInterceptor
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
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import javax.inject.Singleton

@Config(
  manifest = Config.NONE,
  application = HiltTestApplication::class
)
@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class NetworkResultTest {
  @get:Rule
  var hiltRule = HiltAndroidRule(this)

  @Before
  fun setup() {
    ShadowLog.stream = System.out
    hiltRule.inject()
  }

  @Test
  fun `test network result transform`() = runTest {
    val mockEngine = MockEngine { _ ->
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
    }
    val client = ApiClient(mockEngine, DependenciesModule().provideJson())
    val result = client.fetchStatus()
    println("result is ${result.getOrNull()}")
    assert(result.getOrNull() != null)
  }

  @Test
  fun `test error network result transform`() = runTest {
    val mockEngine = MockEngine { _ ->
      respondError(
        status = HttpStatusCode.NotFound,
        content = """
          {
            "error": "Record not found"
          }
        """.trimIndent(),
        headers = headersOf(HttpHeaders.ContentType, "application/json")
      )
    }
    val client = ApiClient(mockEngine, DependenciesModule().provideJson())
    val result = client.fetchStatus()
    result.onFailure {
      println("error is ${it.response} connection ${it.isConnectionError}")
    }
    assert(result.isFailure)
  }

  class ApiClient(
    engine: HttpClientEngine,
    json: Json
  ) {
    private val httpClient = HttpClient(engine) {
      install(networkInterceptor(json))
      install(ContentNegotiation) { json(json) }
    }

    suspend fun fetchStatus() = httpClient.get("v1/status").body<NetworkResult<FakeResponse>>()
  }

  @Module
  @InstallIn(SingletonComponent::class)
  class DependenciesModule {

    @Provides
    @Singleton
    fun provideJson() = Json {
      isLenient = true
      explicitNulls = false
      ignoreUnknownKeys = true
    }
  }
}
