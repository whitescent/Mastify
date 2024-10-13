// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  id("build-logic")
}

allprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
  }
}
