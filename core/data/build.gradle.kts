plugins {
  id(androidLibrary)
  id(androidHilt)
  id(kotlinSerialization)
  id(mmkv)
}

android {
  namespace = "com.github.whitescent.mastify.core.data"
}

dependencies {
  arrayOf(
    projects.core.model,
    projects.core.network,
  ).forEach(::api)
}
