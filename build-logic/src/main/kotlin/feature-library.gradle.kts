import internal.libs

apply(plugin = androidLibrary)
apply(plugin = androidLibraryCompose)
apply(plugin = androidHilt)

dependencies {
  "implementation"(project(":core:ui"))
  "implementation"(project(":core:navigation"))
  "implementation"(project(":core:common-strings"))
  "api"(project(":core:data"))
  "api"(libs.androidx.navigation.compose)
  "implementation"(libs.hilt.android)
  "implementation"(libs.androidx.hilt.navigation.compose)
  "ksp"(libs.hilt.compiler)
}
