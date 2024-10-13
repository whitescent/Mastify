plugins {
  id(androidApplicationCompose)
  id(androidHilt)
  id(mmkv)
}

android {
  namespace = "com.github.whitescent.mastify"

  defaultConfig {
    applicationId = "com.github.whitescent.mastify"
    versionCode = 1
  }

  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("debug")
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
}

dependencies {
  arrayOf(
    libs.work.runtime.ktx,
    projects.core.ui,
    projects.core.navigation,
    projects.feature.login,
    projects.feature.foundation
  ).forEach(::implementation)
}
