import com.android.build.api.dsl.ApplicationExtension
import internal.initCompose

apply(plugin = androidApplication)

extensions.configure<ApplicationExtension> { initCompose(project) }
