package com.github.whitescent.mastify.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AccountEntity(
  @PrimaryKey(autoGenerate = true) val id: Long,
  val domain: String,
  var accessToken: String,
  var clientId: String?, // nullable for backward compatibility
  var clientSecret: String?, // nullable for backward compatibility
  var isActive: Boolean,
  var accountId: String = "",
  var username: String = "",
  var displayName: String = "",
  var profilePictureUrl: String = "",
  var header: String = "",
  var followersCount: Long = 0,
  var followingCount: Long = 0,
  var statusesCount: Long = 0
) {

  val fullName: String
    get() = "@$username@$domain"

  fun logout() {
    // deleting credentials so they cannot be used again
    accessToken = ""
    clientId = null
    clientSecret = null
  }

  fun isLoggedIn() = accessToken.isNotEmpty()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AccountEntity

    if (id == other.id) return true
    return domain == other.domain && accountId == other.accountId
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + domain.hashCode()
    result = 31 * result + accountId.hashCode()
    return result
  }
}
