plugins {
  id(androidLibraryCompose)
}

android {
  namespace = "com.github.whitescent.mastify.core.ui"
}

dependencies {
  implementation(libs.androidx.compose.material3)
  implementation(libs.jsoup)
  api(libs.coil.compose)
}
