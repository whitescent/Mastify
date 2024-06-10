package com.github.whitescent.mastify.core.data.repository

import Mastify.core.codegen.AppDataPreferences
import com.github.whitescent.mastify.core.model.AppData
import com.github.whitescent.mastify.core.model.AppDataProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor(
  private val appData: AppDataPreferences,
) : AppDataProvider {
  override fun getAppData(): AppData = appData.get()
}
