plugins {
  id(androidLibrary)
  id(kotlinSerialization)
}

android {
  namespace = "com.github.whitescent.mastify.core.navigation"
}

dependencies.implementation(libs.androidx.navigation.runtime)
