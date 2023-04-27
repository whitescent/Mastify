package com.github.whitescent.mastify.data.repository

import com.github.whitescent.mastify.data.model.InstanceModel
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor() {

  private val mmkv = MMKV.defaultMMKV()

  val instance = MutableStateFlow(InstanceModel("", "", ""))
  val accessToken = MutableStateFlow<String?>(null)

  init {
    accessToken.value = mmkv.decodeString("accessToken")
  }

  fun saveClientData(instanceName: String, clientId: String, clientSecret: String) {
    instance.value = InstanceModel(instanceName, clientId, clientSecret)
    mmkv.encode("client", instance.value)
  }

  fun saveAccessToken(token: String) {
    accessToken.value = token
    mmkv.encode("accessToken", token)
  }

}
