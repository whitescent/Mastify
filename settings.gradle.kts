enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

rootProject.name = "Mastify"
include(":app")
include(":feature:login")
include(":core:data")
include(":core:model")
include(":core:ui")
include(":core:navigation")
include(":core:network")
include(":core:common")
include(":core:common-compose")
include(":core:common-strings")
