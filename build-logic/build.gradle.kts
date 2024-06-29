plugins {
  `kotlin-dsl`
}

dependencies {
  arrayOf(
    libs.android.gradle.plugin,
    libs.kotlin.gradle.plugin,
    libs.hilt.gradle.plugin,
    libs.room.gradle.plugin,
    libs.kotlin.serialization.gradle.plugin,
    libs.compose.compiler.gradle.plugin,
    libs.google.ksp.gradle.plugin,
    files(libs.javaClass.superclass.protectionDomain.codeSource.location)
  ).forEach(::implementation)
}
