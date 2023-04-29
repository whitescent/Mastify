package com.github.whitescent.mastify.data.repository

import com.github.whitescent.mastify.data.model.AccountModel
import com.github.whitescent.mastify.data.model.InstanceModel
import com.tencent.mmkv.MMKV
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor() {

  private val mmkv = MMKV.defaultMMKV()

  // Cache the client ID and client secret during login
  // and delete this cached data after obtaining the user's access token
  var instance: InstanceModel? = null
    private set

  var account: AccountModel? = null
    private set

  init {
    anyAccountLoggedIn()
  }

  fun saveInstanceData(instanceName: String, clientId: String, clientSecret: String) {
    instance = InstanceModel(instanceName, clientId, clientSecret)
  }

  fun saveAccount(account: AccountModel) {
    this.account = account
    mmkv.remove("instance")
    mmkv.encode("account_${account.username}@${account.instanceName}", account)
  }

  fun anyAccountLoggedIn(): Boolean {
    mmkv.allKeys()?.forEach {
      if (it.startsWith("account_")) {
        println("account $it")
        val savedAccount = mmkv.decodeParcelable(it, AccountModel::class.java)
        if (savedAccount?.isLoggedIn == true) {
          account = savedAccount
          return true
        }
      }
    }
    return false
  }
}
