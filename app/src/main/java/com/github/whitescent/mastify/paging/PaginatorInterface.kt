package com.github.whitescent.mastify.paging

import androidx.compose.foundation.lazy.LazyListState

interface PaginatorInterface<Key, Item> {
  suspend fun append()

  // Temporarily, a solution has not been found yet to prevent flickering and maintain
  // the current position in the list when adding a new list without passing lazyState
  suspend fun refresh(lazyState: LazyListState)
}
