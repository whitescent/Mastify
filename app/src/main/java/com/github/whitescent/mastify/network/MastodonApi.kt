package com.github.whitescent.mastify.network

import at.connyduck.calladapter.networkresult.NetworkResult
import com.github.whitescent.mastify.network.model.account.AccessToken
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Status
import com.github.whitescent.mastify.network.model.instance.AppCredentials
import com.github.whitescent.mastify.network.model.instance.InstanceInfo
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MastodonApi {

  companion object {
    const val DOMAIN_HEADER = "domain"
    const val PLACEHOLDER_DOMAIN = "domain.placeholder"
  }

  @GET("api/v1/instance")
  suspend fun fetchInstanceInfo(
    @Header(DOMAIN_HEADER) domain: String
  ): NetworkResult<InstanceInfo>

  @FormUrlEncoded
  @POST("api/v1/apps")
  suspend fun authenticateApp(
    @Header(DOMAIN_HEADER) domain: String,
    @Field("client_name") clientName: String,
    @Field("redirect_uris") redirectUris: String,
    @Field("scopes") scopes: String,
    @Field("website") website: String
  ): NetworkResult<AppCredentials>

  @FormUrlEncoded
  @POST("oauth/token")
  suspend fun fetchOAuthToken(
    @Header(DOMAIN_HEADER) domain: String,
    @Field("client_id") clientId: String,
    @Field("client_secret") clientSecret: String,
    @Field("redirect_uri") redirectUri: String,
    @Field("code") code: String,
    @Field("grant_type") grantType: String
  ): NetworkResult<AccessToken>

  @GET("api/v1/accounts/verify_credentials")
  suspend fun accountVerifyCredentials(
    @Header(DOMAIN_HEADER) domain: String? = null,
    @Header("Authorization") auth: String? = null
  ): NetworkResult<Account>

  @GET("api/v1/timelines/home")
  @Throws(Exception::class)
  suspend fun homeTimeline(
    @Query("max_id") maxId: String? = null,
    @Query("min_id") minId: String? = null,
    @Query("limit") limit: Int? = null
  ): Response<List<Status>>

  @POST("api/v1/statuses/{id}/favourite")
  suspend fun favouriteStatus(
    @Path("id") statusId: String
  ): NetworkResult<Status>

  @POST("api/v1/statuses/{id}/unfavourite")
  suspend fun unfavouriteStatus(
    @Path("id") statusId: String
  ): NetworkResult<Status>

}
