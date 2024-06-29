import com.google.devtools.ksp.gradle.KspGradleSubplugin
import dagger.hilt.android.plugin.HiltGradlePlugin
import internal.libs

apply<HiltGradlePlugin>()
apply<KspGradleSubplugin>()

dependencies {
  "implementation"(libs.hilt.android)
  "implementation"(libs.androidx.hilt.navigation.compose)
  "ksp"(libs.hilt.compiler)
  "kspTest"(libs.hilt.android.testing)
  "androidTestImplementation"(libs.hilt.android.testing)
}

plugins.withType<AndroidUnitTestPlugin> {
  dependencies.add("testImplementation", libs.hilt.android.testing)
}
