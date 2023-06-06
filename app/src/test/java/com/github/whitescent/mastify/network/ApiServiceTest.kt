package com.github.whitescent.mastify.network

import android.os.Build
import android.util.Log
import com.github.whitescent.BuildConfig
import com.github.whitescent.mastify.di.NetworkModule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock
import retrofit2.Retrofit
import java.net.IDN
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class ApiServiceTest {

  private val networkModule = NetworkModule()

  private val builder = OkHttpClient.Builder()
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .proxy(
      Proxy(
        Proxy.Type.HTTP,
        InetSocketAddress.createUnresolved(IDN.toASCII("127.0.0.1"), 1080)
      )
    )
    .addInterceptor(InstanceSwitchAuthInterceptor(mock()))
    .build()

  private val api = networkModule
    .providesApi(
      networkModule.providesRetrofit(
        builder,
        networkModule.providesJson()
      )
    )

  @Test
  fun `test fetchInstanceInfo`() {
    runBlocking {
      val response = api.fetchInstanceInfo("m.cmx.im")
      Assert.assertEquals(true, response.isSuccess)
    }
  }
}
