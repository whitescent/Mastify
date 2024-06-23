package com.github.whitescent.mastify.core.data.repository

import Mastify.core.codegen.AppDataPreferences
import Mastify.core.codegen.PreferencesFactory
import Mastify.core.codegen.update
import com.github.whitescent.mastify.core.model.AppData
import com.github.whitescent.mastify.core.model.AppDataProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor(
  private val preferences: PreferencesFactory = PreferencesFactory(),
  private val appData: AppDataPreferences,
) : AppDataProvider {
  override fun getAppData(): AppData = appData.get()

  fun updateAppData(appData: AppData) {
    preferences.appData.update {
      it.token = appData.token
      it.instanceUrl = appData.instanceUrl
    }
  }
}
