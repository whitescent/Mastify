package com.github.whitescent.mastify.paging

interface PaginatorInterface<Key, Item> {
  suspend fun append()
  suspend fun refresh()
}
