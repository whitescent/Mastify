import internal.androidBaseExtension
import internal.libs

androidBaseExtension {
  testOptions.unitTests {
    isIncludeAndroidResources = true
    isReturnDefaultValues = true
  }
}

dependencies {
  "testImplementation"(libs.bundles.androidx.test)
  "testImplementation"(libs.robolectric)
  "testImplementation"(libs.kotlinx.coroutines.test)
}
