package com.github.whitescent.mastify.utils

fun getInstanceName(url: String): String? {
  val regex = Regex("""^https?://([^/]+)""")
  val matchResult = regex.find(url)
  return matchResult?.groups?.get(1)?.value
}
