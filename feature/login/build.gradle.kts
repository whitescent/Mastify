plugins {
  id(featureLibrary)
}

android {
  namespace = "com.github.whitescent.mastify.feature.login"
}

dependencies {
  implementation(libs.compose.shadowplus)
}
