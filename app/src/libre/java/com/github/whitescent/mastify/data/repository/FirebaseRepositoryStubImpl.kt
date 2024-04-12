package com.github.whitescent.mastify.data.repository

import javax.inject.Inject

class FirebaseRepositoryStubImpl @Inject constructor(): FirebaseRepository {
  override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
    // Do nothing
  }
}
