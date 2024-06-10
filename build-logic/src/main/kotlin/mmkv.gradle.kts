import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import internal.libs

apply<KspGradleSubplugin>()

extensions.configure<KspExtension> {
  arg("mmkv.ktx.packageName", "$group.codegen")
}

dependencies {
  "implementation"(libs.mmkv.ktx)
  "ksp"(libs.mmkv.ktx.compiler)
}
