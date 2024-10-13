package internal

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

internal fun Project.importShared(
  modulePath: String,
  extraDependencies: (DependencyHandlerScope.() -> Unit)? = null,
) {
  if (project.path != modulePath) dependencies {
    "implementation"(project(modulePath))
    extraDependencies?.invoke(this)
  }
}

internal fun Project.configureJvmToolchain() {
  plugins.withType<JavaBasePlugin> {
    configure<JavaPluginExtension> {
      toolchain.languageVersion.set(JavaLanguageVersion.of(JavaToolchainVersion))
    }
  }
  plugins.withType<KotlinBasePlugin> {
    configure<KotlinProjectExtension> {
      jvmToolchain(JavaToolchainVersion)
    }
  }
}

internal fun Project.configureKotlin(block: KotlinProjectExtension.() -> Unit) {
  plugins.withType<KotlinBasePlugin> { extensions.configure(block) }
}

internal fun Project.optIn(name: String) = configureKotlinCompilationTask {
  compilerOptions.optIn.add(name)
}

internal fun Project.configureKotlinCompilationTask(configuration: KotlinCompilationTask<*>.() -> Unit) =
  configureKotlin { tasks.withType(configuration) }

fun Project.configureCompile() {
  configureKotlinCompilationTask {
    optIn("kotlin.contracts.ExperimentalContracts")
    optIn("kotlinx.serialization.ExperimentalSerializationApi")
    optIn("kotlinx.serialization.InternalSerializationApi")
  }
}
