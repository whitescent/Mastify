import com.android.build.api.dsl.LibraryExtension
import internal.initCompose

apply(plugin = androidLibrary)

extensions.configure<LibraryExtension> {
  initCompose(project)
}
