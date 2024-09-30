@file:Suppress("ConstPropertyName")

package com.github.whitescent.mastify.core.network.api

import com.github.whitescent.mastify.core.model.network.response.AccessToken
import com.github.whitescent.mastify.core.model.network.response.Account
import com.github.whitescent.mastify.core.model.network.response.AppCredentials
import com.github.whitescent.mastify.core.model.network.response.InstanceInfo
import com.github.whitescent.mastify.core.network.NetworkResult
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Url

interface MastodonApi {
  @GET("")
  suspend fun fetchInstanceInfo(
    @Url url: String
  ): NetworkResult<InstanceInfo>

  @FormUrlEncoded
  @POST("")
  suspend fun authenticateApp(
    @Url url: String,
    @Field("client_name") clientName: String,
    @Field("redirect_uris") redirectUris: String,
    @Field("scopes") scopes: String,
    @Field("website") website: String
  ): NetworkResult<AppCredentials>

  @FormUrlEncoded
  @POST("")
  suspend fun fetchOAuthToken(
    @Url url: String,
    @Field("client_id") clientId: String,
    @Field("client_secret") clientSecret: String,
    @Field("redirect_uri") redirectUri: String,
    @Field("code") code: String,
    @Field("grant_type") grantType: String
  ): NetworkResult<AccessToken>

  @GET("")
  suspend fun accountVerifyCredentials(
    @Url url: String
  ): NetworkResult<Account>
}
