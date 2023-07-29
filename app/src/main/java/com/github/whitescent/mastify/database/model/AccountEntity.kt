package com.github.whitescent.mastify.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.whitescent.mastify.network.model.account.Fields

@Entity
data class AccountEntity(
  @PrimaryKey(autoGenerate = true) val id: Long,
  val domain: String,
  val accessToken: String,
  val clientId: String?, // nullable for backward compatibility
  val clientSecret: String?, // nullable for backward compatibility
  val isActive: Boolean,
  val accountId: String,
  val username: String = "",
  val displayName: String = "",
  val note: String = "",
  val profilePictureUrl: String = "",
  val header: String = "",
  val createdAt: String = "",
  val fields: List<Fields> = emptyList(),
  val followersCount: Long = 0,
  val followingCount: Long = 0,
  val statusesCount: Long = 0
) {

  val fullName: String
    get() = "@$username@$domain"

  val realDisplayName inline get() = this.displayName.ifEmpty { this.username }

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
