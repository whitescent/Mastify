package com.github.whitescent.mastify.network.model.account

import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.utils.FormatFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Account(
  val id: String,
  val username: String,
  @SerialName("display_name") val displayName: String,
  val note: String,
  val url: String,
  val avatar: String,
  val header: String,
  @SerialName("followers_count") val followersCount: Long,
  @SerialName("following_count") val followingCount: Long,
  @SerialName("statuses_count") val statusesCount: Long,
  @SerialName("created_at") val createdAt: String,
  val source: Source,
  val fields: List<Fields>
) {

  val domain get() = FormatFactory.getInstanceName(url)

  fun toAccountEntity(
    dbId: Long,
    accessToken: String,
    clientId: String?,
    clientSecret: String?,
    isActive: Boolean,
  ): AccountEntity {
    return AccountEntity(
      id = dbId,
      domain = domain,
      accessToken = accessToken,
      clientId = clientId,
      clientSecret = clientSecret,
      isActive = isActive,
      accountId = id,
      username = username,
      displayName = displayName,
      profilePictureUrl = avatar,
      followingCount = followersCount,
      followersCount = followersCount,
      header = header,
      statusesCount = statusesCount
    )
  }
}

@Serializable
data class Source(
  val note: String,
  val fields: List<Fields>
)

@Serializable
data class Fields(
  val name: String,
  val value: String,
  @SerialName("verified_at") val verifiedAt: String?
)
