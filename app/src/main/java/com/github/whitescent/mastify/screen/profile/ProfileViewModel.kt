package com.github.whitescent.mastify.screen.profile

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.data.repository.ApiRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.network.model.response.account.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
  private val apiRepository: ApiRepository,
  preferenceRepository: PreferenceRepository
) : ViewModel() {

  val account = preferenceRepository.account
  var statuses by mutableStateOf<List<Status>>(emptyList())
    private set

  fun initProfilePage() = viewModelScope.launch(Dispatchers.Main) {
    val list = apiRepository.getAccountStatuses(
      instanceName = account!!.instanceName,
      token = account.accessToken,
      id = account.id
    )
    list?.let {
      statuses = it
    }
  }

}
