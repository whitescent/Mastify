@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  id("com.android.test")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.github.whitescent.benchmark"
  compileSdk = 34

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }

  defaultConfig {
    minSdk = 24
    targetSdk = 34

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    // This benchmark buildType is used for benchmarking, and should function like your
    // release build (for example, with minification on). It"s signed with a debug key
    // for easy local/CI testing.
    create("benchmark") {
      isDebuggable = true
      signingConfig = getByName("debug").signingConfig
      matchingFallbacks += listOf("release")
    }
  }

  targetProjectPath = ":app"
  experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
  implementation(libs.androidx.test.ext.junit)
  implementation(libs.androidx.test.espresso.core)
  implementation(libs.uiautomator)
  implementation(libs.profileinstaller)
  implementation(libs.benchmark.macro.junit4)
}
