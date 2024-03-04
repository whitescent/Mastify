/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.data.repository

import Mastify.codegen.PreferencesFactory
import Mastify.codegen.update
import com.github.whitescent.mastify.data.model.InstanceModel
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor() {

  val preference = PreferencesFactory()

  // Cache the client ID and client secret during login
  var instance: InstanceModel? = null
    private set

  fun saveInstanceData(domain: String, clientId: String, clientSecret: String) {
    instance = InstanceModel(domain, clientId, clientSecret)
  }

  fun setFirebaseAnalyticsEnabled(enabled: Boolean) {
    preference.userPreference.update {
      it.enableFirebaseAnalytics = enabled
    }
    Firebase.analytics.setAnalyticsCollectionEnabled(enabled)
  }

  fun setFirebaseCrashlyticsEnabled(enabled: Boolean) {
    preference.userPreference.update {
      it.enableFirebaseCrashlytics = enabled
    }
    Firebase.crashlytics.setCrashlyticsCollectionEnabled(enabled)
  }
}
