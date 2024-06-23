plugins {
  id(featureLibrary)
}

android {
  namespace = "com.github.whitescent.mastify.feature.foundation.common"
}

dependencies {
  arrayOf(
    projects.feature.foundation.home
  ).forEach(::api)
}
