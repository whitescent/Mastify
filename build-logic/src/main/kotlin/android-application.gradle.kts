import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.AppPlugin
import internal.initAndroid
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

apply<AppPlugin>()
apply<KotlinAndroidPluginWrapper>()

extensions.configure<ApplicationExtension> {
  initAndroid(project)
}
