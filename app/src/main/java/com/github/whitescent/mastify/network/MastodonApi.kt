/*
 * Copyright 2024 WhiteScent
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

import at.connyduck.calladapter.networkresult.NetworkResult
import com.github.whitescent.mastify.network.model.account.AccessToken
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Relationship
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.instance.AppCredentials
import com.github.whitescent.mastify.network.model.instance.InstanceInfo
import com.github.whitescent.mastify.network.model.notification.Notification
import com.github.whitescent.mastify.network.model.search.SearchResult
import com.github.whitescent.mastify.network.model.status.Hashtag
import com.github.whitescent.mastify.network.model.status.MediaUploadResult
import com.github.whitescent.mastify.network.model.status.NewStatus
import com.github.whitescent.mastify.network.model.status.Poll
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.StatusContext
import com.github.whitescent.mastify.network.model.trends.News
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MastodonApi {

  companion object {
    const val DOMAIN_HEADER = "domain"
    const val PLACEHOLDER_DOMAIN = "domain.placeholder"
  }

  @GET("api/v1/instance")
  suspend fun fetchInstanceInfo(
    @Header(DOMAIN_HEADER) domain: String? = null
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

  @GET("api/v1/statuses/{id}")
  suspend fun status(
    @Path("id") statusId: String
  ): Response<Status>

  @GET("api/v1/trends/statuses")
  suspend fun trendingStatus(
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int = 0
  ): NetworkResult<List<Status>>

  @GET("api/v1/trends/links")
  suspend fun trendingNews(
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int = 0
  ): NetworkResult<List<News>>

  @POST("api/v1/statuses/{id}/bookmark")
  suspend fun bookmarkStatus(
    @Path("id") statusId: String,
  ): NetworkResult<Status>

  @POST("api/v1/statuses/{id}/unbookmark")
  suspend fun unbookmarkStatus(
    @Path("id") statusId: String,
  ): NetworkResult<Status>

  @POST("api/v1/statuses")
  suspend fun createStatus(
    @Header("Idempotency-Key") idempotencyKey: String,
    @Body status: NewStatus,
  ): Response<Status>

  @POST("api/v1/statuses/{id}/favourite")
  suspend fun favouriteStatus(
    @Path("id") statusId: String
  ): NetworkResult<Status>

  @POST("api/v1/statuses/{id}/unfavourite")
  suspend fun unfavouriteStatus(
    @Path("id") statusId: String
  ): NetworkResult<Status>

  @POST("api/v1/statuses/{id}/reblog")
  suspend fun reblogStatus(
    @Path("id") statusId: String,
  ): NetworkResult<Status>

  @POST("api/v1/statuses/{id}/unreblog")
  suspend fun unreblogStatus(
    @Path("id") statusId: String,
  ): NetworkResult<Status>

  @GET("api/v1/statuses/{id}/context")
  suspend fun statusContext(
    @Path("id") statusId: String
  ): NetworkResult<StatusContext>

  @GET("api/v1/accounts/{id}")
  suspend fun account(
    @Path("id") accountId: String
  ): NetworkResult<Account>

  @POST("api/v1/accounts/{id}/block")
  suspend fun blockAccount(
    @Path("id") accountId: String,
  ): NetworkResult<Relationship>

  @POST("api/v1/accounts/{id}/unblock")
  suspend fun unblockAccount(
    @Path("id") accountId: String,
  ): NetworkResult<Relationship>

  @FormUrlEncoded
  @POST("api/v1/accounts/{id}/mute")
  suspend fun muteAccount(
    @Path("id") accountId: String,
    @Field("notifications") notifications: Boolean? = null,
    @Field("duration") duration: Int? = null,
  ): NetworkResult<Relationship>

  @POST("api/v1/accounts/{id}/unmute")
  suspend fun unmuteAccount(
    @Path("id") accountId: String,
  ): NetworkResult<Relationship>

  @GET("api/v1/accounts/relationships")
  suspend fun relationships(
    @Query("id[]") accountIds: List<String>
  ): NetworkResult<List<Relationship>>

  @GET("api/v1/accounts/{id}/statuses")
  suspend fun accountStatuses(
    @Path("id") accountId: String,
    @Query("max_id") maxId: String? = null,
    @Query("since_id") sinceId: String? = null,
    @Query("limit") limit: Int? = null,
    @Query("exclude_replies") excludeReplies: Boolean? = null,
    @Query("only_media") onlyMedia: Boolean? = null,
    @Query("pinned") pinned: Boolean? = null
  ): NetworkResult<List<Status>>

  @GET("/api/v1/custom_emojis")
  suspend fun getCustomEmojis(): NetworkResult<List<Emoji>>

  @GET("api/v2/search")
  suspend fun searchSync(
    @Query("q") query: String?,
    @Query("type") type: String? = null,
    @Query("resolve") resolve: Boolean? = null,
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    @Query("following") following: Boolean? = null,
  ): NetworkResult<SearchResult>

  @GET("api/v1/timelines/public")
  suspend fun publicTimeline(
    @Query("local") local: Boolean? = null,
    @Query("max_id") maxId: String? = null,
    @Query("since_id") sinceId: String? = null,
    @Query("limit") limit: Int? = null,
  ): NetworkResult<List<Status>>

  @FormUrlEncoded
  @POST("api/v1/polls/{id}/votes")
  suspend fun voteInPoll(
    @Path("id") id: String,
    @Field("choices[]") choices: List<Int>
  ): NetworkResult<Poll>

  @Multipart
  @POST("api/v2/media")
  suspend fun uploadMedia(
    @Part file: MultipartBody.Part,
    @Part description: MultipartBody.Part? = null,
    @Part focus: MultipartBody.Part? = null
  ): Response<MediaUploadResult>

  @GET("api/v1/tags/{name}")
  suspend fun tag(@Path("name") name: String): NetworkResult<Hashtag>

  @GET("api/v1/timelines/tag/{hashtag}")
  suspend fun hashtagTimeline(
    @Path("hashtag") hashtag: String,
    @Query("any[]") any: List<String>?,
    @Query("local") local: Boolean?,
    @Query("max_id") maxId: String?,
    @Query("since_id") sinceId: String?,
    @Query("limit") limit: Int?
  ): Response<List<Status>>

  @POST("api/v1/tags/{name}/follow")
  suspend fun followTag(@Path("name") name: String): NetworkResult<Hashtag>

  @POST("api/v1/tags/{name}/unfollow")
  suspend fun unfollowTag(@Path("name") name: String): NetworkResult<Hashtag>

  @FormUrlEncoded
  @POST("api/v1/accounts/{id}/follow")
  suspend fun followAccount(
    @Path("id") accountId: String,
    @Field("reblogs") showReblogs: Boolean? = null,
    @Field("notify") notify: Boolean? = null
  ): NetworkResult<Relationship>

  @POST("api/v1/accounts/{id}/unfollow")
  suspend fun unfollowAccount(@Path("id") accountId: String): NetworkResult<Relationship>

  @GET("api/v1/accounts/lookup")
  suspend fun lookupAccount(
    @Query("acct") acct: String
  ): Response<Account>

  @GET("api/v1/notifications")
  suspend fun notifications(
    /** Return results older than this ID */
    @Query("max_id") maxId: String? = null,
    /** Return results immediately newer than this ID */
    @Query("min_id") minId: String? = null,
    /** Maximum number of results to return. Defaults to 15, max is 30 */
    @Query("limit") limit: Int? = null,
    /** Types to excludes from the results */
    @Query("exclude_types[]") excludes: List<String>? = null
  ): Response<List<Notification>>
}
