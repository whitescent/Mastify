@file:Suppress("UnstableApiUsage")

include(":app:benchmark")

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}
dependencyResolutionManagement {
  enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://androidx.dev/storage/compose-compiler/repository/")
  }
}
rootProject.name = "Mastify"
include(":app")
