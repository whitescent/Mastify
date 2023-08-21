package com.github.whitescent.mastify.extensions

// get all items size from 0 to index
fun <A, B> Map<A, List<B>>.getSizeOfIndex(index: Int): Int {
  if (index == 0) return 0
  var count = 0
  this.onEachIndexed { currentIndex, entry ->
    count += entry.value.size
    if (currentIndex == index - 1) return count + index
  }
  return -1
}

fun String.insertString(insert: String, index: Int): String {
  val start = this.substring(0, index)
  val end = this.substring(index)
  return start + insert + end
}
