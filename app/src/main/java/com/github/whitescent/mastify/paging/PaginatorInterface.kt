package com.github.whitescent.mastify.paging

interface PaginatorInterface<Key, Item> {
  suspend fun loadNextItems()
  suspend fun refresh()
}
