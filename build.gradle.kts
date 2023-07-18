import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

buildscript {
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath(libs.android.gradle.plugin)
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.hilt.gradle.plugin)
  }
}

allprojects {
  tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
      if (project.hasProperty("mastify.enableComposeCompilerReports")) {
        freeCompilerArgs.addAll(
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
            project.buildDir.absolutePath + "/compose_metrics",
        )
        freeCompilerArgs.addAll(
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
            project.buildDir.absolutePath + "/compose_metrics",
        )
      }
    }
  }
}
