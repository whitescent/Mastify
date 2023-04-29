package com.github.whitescent.mastify.screen.home

import androidx.lifecycle.ViewModel
import com.github.whitescent.mastify.data.repository.ApiRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeScreenModel @Inject constructor(
  private val apiRepository: ApiRepository,
  preferenceRepository: PreferenceRepository
) : ViewModel() {

}
