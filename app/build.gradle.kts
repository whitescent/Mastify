/*
 * Copyright 2023 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("kotlin-parcelize")
  id("dagger.hilt.android.plugin")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.google.services)
  alias(libs.plugins.firebase.crashlytics)
}

android {

  namespace = "com.github.whitescent"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.github.whitescent.mastify"
    minSdk = 24
    targetSdk = 34
    versionCode = 20
    versionName = "1.4.24-alpha"
    testInstrumentationRunner = "com.github.whitescent.mastify.ui.CustomTestRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }
  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
    create("benchmark") {
      initWith(buildTypes.getByName("release"))
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("release")
      isDebuggable = false
      proguardFiles("benchmark-rules.pro")
    }
  }

  flavorDimensions += "version"

  productFlavors {
    create("libre") {
      dimension = "version"
      applicationIdSuffix = ".libre"
    }
    create("gms") {
      dimension = "version"
    }
  }

  kotlinOptions {
    jvmTarget = "17"
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }
  kotlinOptions {
    compileOptions {
      freeCompilerArgs = listOf(
        "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi",
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
        "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
        "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
        "-opt-in=com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi"
      )
    }
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  packaging {
    resources {
      excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
  }
  applicationVariants.all {
    addJavaSourceFoldersToModel(
      File(layout.buildDirectory.asFile.get(), "generated/ksp/$name/kotlin")
    )
  }
  androidResources {
    generateLocaleConfig = true
  }
}

// Disable gms plugin tasks on libre builds
androidComponents {
  beforeVariants(selector().withFlavor(dimension = "version", flavorName = "libre")) {
    tasks.configureEach {
      if (name.contains("GoogleServices") || name.contains("uploadCrashlyticsMapping")) {
        enabled = false
      }
    }
  }
}

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
  arg("mmkv.ktx.packageName", "$group.codegen")
}

dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.browser)
  implementation(libs.androidx.media3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.util)
  implementation(libs.androidx.compose.runtime)
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
  implementation(libs.androidx.monitor)
  implementation(libs.androidx.junit.ktx)
  implementation(libs.androidx.media3.ui)
  implementation(libs.androidx.window)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  implementation(libs.constraintlayout.compose)

  implementation(libs.com.google.dagger.hilt.android)
  implementation(libs.androidx.hilt.navigation.compose)
  androidTestImplementation(libs.hilt.android.testing)
  ksp(libs.com.google.dagger.hilt.compiler)
  kspAndroidTest(libs.com.google.dagger.hilt.compiler)

  implementation(libs.retrofit2)
  implementation(libs.okhttp3)
  implementation(libs.org.jetbrains.kotlinx.serialization.json)
  implementation(libs.kotlinx.serialization.converter)
  implementation(libs.okhttp3.logging.interceptor)
  implementation(libs.networkresult.calladapter)

  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.okhttp3.mockwebserver)
  testImplementation(libs.mockk)
  testImplementation(libs.mockk.agent)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockito.kotlin)
  androidTestImplementation(libs.mockito.kotlin)
  testImplementation(libs.room.testing)

  implementation(libs.room.runtime)
  ksp(libs.room.compiler)
  implementation(libs.room.ktx)
  implementation(libs.room.paging)

  implementation(libs.ktsoup.core)
  implementation(libs.ktsoup.fx)

  implementation(libs.work.runtime.ktx)
  implementation(libs.work.testing)

  "gmsImplementation"(platform(libs.firebase.bom))
  "gmsImplementation"(libs.firebase.crashlytics)

  implementation(libs.coil)
  implementation(libs.coil.compose)
  implementation(libs.coil.gif)
  implementation(libs.coil.video)
  implementation(libs.compose.media)
  implementation(libs.compose.shadow.plus)
  implementation(libs.compose.destinations.animations.core)
  ksp(libs.compose.destinations.ksp)

  implementation(libs.mmkv)
  implementation(libs.mmkv.ktx)
  ksp(libs.mmkv.ktx.compiler)

  implementation(libs.logcat)
  implementation(libs.jsoup)
  implementation(libs.scrollbar)
  implementation(libs.zoomable)
  implementation(libs.lottie)
  implementation(libs.splash)
  implementation(libs.haze)
  debugImplementation(libs.leakcanary)

  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.collections)
  coreLibraryDesugaring(libs.android.desugar)

  implementation(libs.zoomable)
}
