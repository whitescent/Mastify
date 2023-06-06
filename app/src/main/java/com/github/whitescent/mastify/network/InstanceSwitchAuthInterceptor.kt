package com.github.whitescent.mastify.network

import android.util.Log
import com.github.whitescent.mastify.data.repository.AccountRepository
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

class InstanceSwitchAuthInterceptor(
  private val accountRepository: AccountRepository
) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest: Request = chain.request()

    // only switch domains if the request comes from retrofit
    return if (originalRequest.url.host == MastodonApi.PLACEHOLDER_DOMAIN) {
      val builder: Request.Builder = originalRequest.newBuilder()
      val instanceHeader = originalRequest.header(MastodonApi.DOMAIN_HEADER)
      if (instanceHeader != null) {
        // use domain explicitly specified in custom header
        builder.url(swapHost(originalRequest.url, instanceHeader))
        builder.removeHeader(MastodonApi.DOMAIN_HEADER)
      } else {
        val currentAccount = accountRepository.activeAccount
        if (currentAccount != null) {
          val accessToken = currentAccount.accessToken
          if (accessToken.isNotEmpty()) {
            // use domain of current account
            builder.url(swapHost(originalRequest.url, currentAccount.domain))
              .header("Authorization", "Bearer %s".format(accessToken))
          }
        }
      }

      val newRequest: Request = builder.build()

      if (MastodonApi.PLACEHOLDER_DOMAIN == newRequest.url.host) {
        Log.w("ISAInterceptor", "no user logged in or no domain header specified - can't make request to " + newRequest.url)
        return Response.Builder()
          .code(400)
          .message("Bad Request")
          .protocol(Protocol.HTTP_2)
          .body("".toResponseBody("text/plain".toMediaType()))
          .request(chain.request())
          .build()
      }

      chain.proceed(newRequest)
    } else {
      chain.proceed(originalRequest)
    }
  }

  companion object {
    private fun swapHost(url: HttpUrl, host: String): HttpUrl {
      return url.newBuilder().host(host).build()
    }
  }
}
