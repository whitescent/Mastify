import internal.libs
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin

apply<SerializationGradleSubplugin>()

dependencies.add("implementation", libs.kotlinx.serialization.json)
