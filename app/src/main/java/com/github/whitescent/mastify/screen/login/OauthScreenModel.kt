package com.github.whitescent.mastify.screen.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.account.AccessToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OauthScreenModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  preferenceRepository: PreferenceRepository,
  private val api: MastodonApi,
  private val accountRepository: AccountRepository
) : ViewModel() {

  val instance = preferenceRepository.instance
  val code: String? = savedStateHandle["code"]

  fun fetchAccessToken(navigateToApp: () -> Unit) {

    val domain = instance!!.name
    val clientId = instance.clientId
    val clientSecret = instance.clientSecret

    viewModelScope.launch(Dispatchers.IO) {
      api.fetchOAuthToken(
        domain = domain,
        clientId = clientId,
        clientSecret = clientSecret,
        redirectUri = "mastify://oauth",
        code = code!!,
        grantType = "authorization_code"
      ).fold(
        { accessToken ->
          fetchAccountDetails(accessToken, domain, clientId, clientSecret)
          withContext(Dispatchers.Main) {
            navigateToApp()
          }
        },
        {

        }
      )
    }
  }

  private suspend fun fetchAccountDetails(
    accessToken: AccessToken,
    domain: String,
    clientId: String,
    clientSecret: String
  ) {
    api.accountVerifyCredentials(
      domain = domain,
      auth = "Bearer ${accessToken.accessToken}"
    ).fold(
      { newAccount ->
         accountRepository.addAccount(
           accessToken = accessToken.accessToken,
           domain = domain,
           clientId = clientId,
           clientSecret = clientSecret,
           newAccount = newAccount
         )
      },
      { e ->

      }
    )
  }

}
