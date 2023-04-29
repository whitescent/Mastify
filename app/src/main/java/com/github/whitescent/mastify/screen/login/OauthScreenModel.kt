package com.github.whitescent.mastify.screen.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.data.model.AccountModel
import com.github.whitescent.mastify.data.repository.ApiRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.network.model.request.OauthTokenBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OauthScreenModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val apiRepository: ApiRepository,
  private val preferenceRepository: PreferenceRepository
) : ViewModel() {

  val instance = preferenceRepository.instance
  val code: String? = savedStateHandle["code"]

  fun getAccessToken(navigateToApp: () -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val result = apiRepository.getAccessToken(
        instanceName = instance!!.name,
        postBody = OauthTokenBody(
          clientId = instance.clientId,
          clientSecret = instance.clientSecret,
          redirectUri = "mastify://oauth",
          grantType = "authorization_code",
          code = code!!,
          scope = "read write push"
        )
      )
      result?.let {
        val accountProfile = apiRepository.getProfile(instance.name, it.accessToken)
        accountProfile?.let { profile ->
          preferenceRepository.saveAccount(
            AccountModel(
              username = profile.name,
              instanceName = instance.name,
              note = profile.source.note,
              accessToken = it.accessToken,
              avatar = profile.avatar,
              header = profile.header,
              id = profile.id,
              followersCount = profile.followersCount,
              followingCount = profile.followingCount,
              statusesCount = profile.statusesCount,
              isLoggedIn = true
            )
          )
        }
        withContext(Dispatchers.Main) {
          navigateToApp()
        }
      }
    }
  }

}
