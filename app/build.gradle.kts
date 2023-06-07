@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.resolve.compatibilityTypeMap

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
  id("kotlin-kapt")
  id("kotlin-parcelize")
  id("dagger.hilt.android.plugin")
  alias(libs.plugins.ksp)
}

android {

  namespace = "com.github.whitescent"
  compileSdk = 33

  defaultConfig {
    applicationId = "com.github.whitescent.mastify"
    minSdk = 21
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }
  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
  }
  buildFeatures {
    compose = true
  }
  packagingOptions {
    resources {
      excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
  }
  applicationVariants.all {
    addJavaSourceFoldersToModel(
      File(buildDir, "generated/ksp/$name/kotlin")
    )
  }
}

dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.browser)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.util)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.startup.runtime)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.paging.compose)
  implementation(libs.androidx.paging.runtime)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  implementation(libs.accompanist.systemuicontroller)
  implementation(libs.constraintlayout.compose)

  implementation(libs.com.google.dagger.hilt.android)
  implementation(libs.androidx.hilt.navigation.compose)
  kapt(libs.com.google.dagger.hilt.compiler)

  implementation(libs.retrofit2)
  implementation(libs.okhttp3)
  testImplementation(libs.okhttp3.mockwebserver)
  implementation(libs.org.jetbrains.kotlinx.serialization.json)
  implementation(libs.kotlinx.serialization.converter)
  implementation(libs.okhttp3.logging.interceptor)
  implementation(libs.networkresult.calladapter)

  testImplementation(libs.mockk)
  testImplementation(libs.mockk.agent)
  testImplementation(libs.kotlinx.coroutines.test)

  implementation(libs.room.runtime)
  ksp(libs.room.compiler)
  implementation(libs.room.ktx)
  implementation(libs.room.paging)

  implementation(libs.coil)
  implementation(libs.coil.compose)
  implementation(libs.coil.gif)
  implementation(libs.coil.video)
  implementation(libs.compose.html)
  implementation(libs.compose.destinations.core)
  ksp(libs.compose.destinations.ksp)
  implementation(libs.mmkv)
  testImplementation(libs.mockito.kotlin)
  implementation(libs.jsoup)
  implementation(libs.zoomable)
  implementation(libs.lottie)

  implementation(libs.kotlinx.datetime)

}
