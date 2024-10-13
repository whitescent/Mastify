plugins {
  id(androidLibrary)
}

android {
  namespace = "com.github.whitescent.mastify.core.common"
}

dependencies {
  implementation(libs.androidx.browser)
}
