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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

buildscript {
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath(libs.android.gradle.plugin)
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.hilt.gradle.plugin)
  }
}

plugins {
  // https://github.com/google/dagger/issues/3965#issuecomment-1662360344
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.firebase.crashlytics) apply false
}

allprojects {
  tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
      if (project.hasProperty("mastify.enableComposeCompilerReports")) {
        freeCompilerArgs.addAll(
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
            layout.buildDirectory.asFile.get() + "/compose_metrics",
        )
        freeCompilerArgs.addAll(
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
            layout.buildDirectory.asFile.get() + "/compose_metrics",
        )
      }
    }
  }
}
