package com.whitescent.mastify.data.repository

import com.github.whitescent.mastify.data.repository.FirebaseRepository
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(): FirebaseRepository {
  override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
    Firebase.crashlytics.setCrashlyticsCollectionEnabled(enabled)
  }
}
