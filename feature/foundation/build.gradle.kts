plugins {
  id(featureLibrary)
}

android {
  namespace = "com.github.whitescent.mastify.feature.foundation"
}

dependencies {
  api(projects.feature.foundation.common)
}
