import androidx.room.gradle.RoomExtension
import androidx.room.gradle.RoomGradlePlugin
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import internal.libs

apply<KspGradleSubplugin>()
apply<RoomGradlePlugin>()

extensions.configure<RoomExtension> {
  schemaDirectory(layout.projectDirectory.dir("schemas").asFile.absolutePath)
}

dependencies {
  "implementation"(libs.room.ktx)
  "ksp"(libs.room.compiler)
}
