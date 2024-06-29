import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import internal.initAndroid
import org.jetbrains.kotlin.gradle.internal.ParcelizeSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

apply<LibraryPlugin>()
apply<KotlinAndroidPluginWrapper>()
apply<ParcelizeSubplugin>()

extensions.configure<LibraryExtension> {
  initAndroid(project)
}
