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

import java.io.FileInputStream
import java.util.Properties

val mastodonProperties = Properties().apply {
  load(FileInputStream(project.file("mastodon.properties")))
}

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
    testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
  }

  buildFeatures {
    buildConfig = true
  }

  buildTypes {
    // This benchmark buildType is used for benchmarking, and should function like your
    // release build (for example, with minification on). It"s signed with a debug key
    // for easy local/CI testing.
    create("benchmark") {
      isDebuggable = true
      signingConfig = getByName("debug").signingConfig
      matchingFallbacks += listOf("release")

      buildConfigField("String", "MASTODON_USERNAME", "\"${mastodonProperties["MASTODON_USERNAME"]}\"")
      buildConfigField("String", "MASTODON_PASSWORD", "\"${mastodonProperties["MASTODON_PASSWORD"]}\"")
      buildConfigField("String", "MASTODON_SITE", "\"${mastodonProperties["MASTODON_SITE"]}\"")
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
