@file:Suppress("SpellCheckingInspection")

package com.github.whitescent.mastify.core.network.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.whitescent.mastify.core.model.AppData
import com.github.whitescent.mastify.core.model.AppDataProvider
import com.github.whitescent.mastify.core.network.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
class NetworkClientTest {
  @get:Rule
  var hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var httpClient: HttpClient

  @Inject
  lateinit var appDataProvider: AppDataProvider

  @Before
  fun setup() {
    ShadowLog.stream = System.out
    hiltRule.inject()
  }

  @Test
  fun `test mastodon url interceptor`() = runTest {
    var isUrlSuccess = false
    var isTokenSuccess = false
    httpClient.requestPipeline.intercept(HttpRequestPipeline.Transform) {
      isUrlSuccess = "https://chachako.com/v1/ip" == context.url.toString()
      isTokenSuccess = "Bearer 114514" == context.headers["Authorization"]
    }
    runCatching { httpClient.get("v1/ip") }
    assert(isUrlSuccess && isTokenSuccess)
  }

  @Module
  @InstallIn(SingletonComponent::class)
  class DependenciesModule {
    @Provides
    @Singleton
    fun provideHttpClient(
      json: Json,
      appDataProvider: AppDataProvider
    ) = NetworkModule().provideHttpClient(json)

    @Provides
    @Singleton
    fun provideJson() = Json {
      isLenient = true
      explicitNulls = false
      ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideAppDataPreferences() = object : AppDataProvider {
      override fun getAppData(): AppData = AppData("chachako.com", "114514")
    }
  }

  @Serializable
  data class TestBody(val age: Int)
}
