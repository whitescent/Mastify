package com.github.whitescent.mastify.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.whitescent.mastify.network.model.account.Fields
import com.github.whitescent.mastify.network.model.emoji.Emoji

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
  val emojis: List<Emoji>,
  val fields: List<Fields>,
  val followersCount: Long,
  val followingCount: Long,
  val statusesCount: Long
) {

  val fullname: String get() = "@$username@$domain"

  val realDisplayName inline get() = this.displayName.ifEmpty { this.username }
  val isEmptyHeader get() = this.header.contains("missing.png")

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
