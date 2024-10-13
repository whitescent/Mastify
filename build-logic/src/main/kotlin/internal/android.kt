package internal

import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get

@PublishedApi
internal inline fun Project.androidBaseExtension(block: BaseExtension.() -> Unit = {}) =
  (extensions["android"] as BaseExtension).apply(block)

internal typealias BaseExtension = CommonExtension<*, *, *, *, *, *>

internal fun BaseExtension.initAndroid(project: Project) = with(project) {
  compileSdk = 34
  namespace = "com.github.whitescent.mastify"
  defaultConfig {
    minSdk = 24
    if (this is ApplicationDefaultConfig) {
      // We set the target sdk to the same as to compile sdk, so that we can be compatible
      // with new Android features. This is because we always choose higher Android APIs as
      // the basis for development, which helps to improve the experience of high-version
      // Android users.
      targetSdk = compileSdk
      versionName = project.version.toString()
    }
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }
  compileOptions {
    sourceCompatibility(JavaToolchainVersion)
    targetCompatibility(JavaToolchainVersion)
    isCoreLibraryDesugaringEnabled = true
  }
  buildFeatures {
    buildConfig = true
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes.configureEach {
    buildConfigField("String", "VERSION_NAME", "\"${project.version}\"")
  }
  dependencies {
    "implementation"(libs.androidx.core.ktx)
    "coreLibraryDesugaring"(libs.android.desugar)
    // https://developer.android.com/jetpack/androidx/releases/test#declaring_dependencies
    "androidTestImplementation"(libs.bundles.androidx.test)
    importShared(":core:common")
  }
  configureJvmToolchain()
}
