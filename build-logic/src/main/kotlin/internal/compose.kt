package internal

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin

internal fun BaseExtension.initCompose(project: Project) = with(project) {
  apply<ComposeCompilerGradleSubplugin>()
  compileSdk = 34
  buildFeatures {
    compose = true
    buildConfig = true
  }
  defaultConfig {
    minSdk = 24
  }
  compileOptions {
    sourceCompatibility(JavaToolchainVersion)
    targetCompatibility(JavaToolchainVersion)
  }
  dependencies {
    "implementation"(platform(libs.androidx.compose.bom))
    importShared(":core:common-compose")
    "implementation"(libs.androidx.compose.ui)
    "implementation"(libs.androidx.compose.material3)
    "implementation"(libs.androidx.activity.compose)
    "implementation"(libs.androidx.lifecycle.runtime.compose)
    "implementation"(libs.androidx.lifecycle.viewmodel.compose)
    "debugImplementation"(libs.androidx.compose.ui.tooling)
  }
  configureJvmToolchain()
}
