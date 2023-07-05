package com.github.whitescent.mastify.di

import android.os.Build
import at.connyduck.calladapter.networkresult.NetworkResultCallAdapterFactory
import com.github.whitescent.BuildConfig
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.network.InstanceSwitchAuthInterceptor
import com.github.whitescent.mastify.network.MastodonApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @OptIn(ExperimentalSerializationApi::class)
  @Provides
  @Singleton
  fun providesJson(): Json =
    Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }

  @Provides
  @Singleton
  fun providesHttpClient(
    accountRepository: AccountRepository
  ): OkHttpClient {
    return OkHttpClient.Builder()
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .addInterceptor(
        HttpLoggingInterceptor()
          .apply {
            if (BuildConfig.DEBUG) {
              setLevel(HttpLoggingInterceptor.Level.BASIC)
            }
          }
      )
      .addInterceptor { chain ->
        // Add a custom User-Agent
        val requestWithUserAgent = chain.request().newBuilder()
          .header(
            name = "User-Agent",
            value = "Mastify/${BuildConfig.VERSION_NAME} " +
              "Android/${Build.VERSION.RELEASE} " +
              "OkHttp/${OkHttp.VERSION}"
          )
          .build()
        chain.proceed(requestWithUserAgent)
      }
      .addInterceptor(InstanceSwitchAuthInterceptor(accountRepository))
      .build()
  }

  @Provides
  @Singleton
  fun providesRetrofit(
    httpClient: OkHttpClient,
    json: Json
  ): Retrofit {
    return Retrofit.Builder().baseUrl("https://" + MastodonApi.PLACEHOLDER_DOMAIN)
      .client(httpClient)
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .addCallAdapterFactory(NetworkResultCallAdapterFactory.create())
      .build()
  }

  @Provides
  @Singleton
  fun providesApi(retrofit: Retrofit): MastodonApi = retrofit.create()

}
