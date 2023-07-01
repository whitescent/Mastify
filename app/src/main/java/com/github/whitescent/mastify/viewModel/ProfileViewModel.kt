package com.github.whitescent.mastify.viewModel

import androidx.lifecycle.ViewModel
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
  preferenceRepository: PreferenceRepository
) : ViewModel() {

  // val account = preferenceRepository.account
  // var statuses by mutableStateOf<List<Status>>(emptyList())
  //   private set
  // var profile by mutableStateOf<Account?>(null)
  //   private set
  //
  // fun initProfilePage() = viewModelScope.launch(Dispatchers.Main) {
  //   val list = apiRepository.getAccountStatuses(
  //     instanceName = account!!.instanceName,
  //     token = account.accessToken,
  //     id = account.id
  //   )
  //   list?.let {
  //     statuses = it
  //   }
  //   apiRepository.accountVerifyCredentials(
  //     instanceName = account.instanceName,
  //     token = account.accessToken
  //   )?.let {
  //     profile = it
  //   }
  // }
}
