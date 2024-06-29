plugins {
  id(androidLibrary)
  id(androidHilt)
  id(androidUnitTest)
  id(kotlinSerialization)
}

android {
  namespace = "com.github.whitescent.mastify.core.network"
}

dependencies {
  arrayOf(
    projects.core.model,
    libs.ktor.json,
    libs.ktor.okhttp,
    libs.ktor.mock,
    libs.ktor.logging,
    libs.ktor.content.negotiation,
  ).forEach(::implementation)
}
