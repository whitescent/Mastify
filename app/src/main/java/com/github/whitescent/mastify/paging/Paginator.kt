package com.github.whitescent.mastify.paging

class Paginator<Key, Item>(
  private val initialKey: Key,
  private inline val onLoadUpdated: (LoadState) -> Unit,
  private inline val onRequest: suspend (nextKey: Key) -> Result<List<Item>>,
  private inline val getNextKey: suspend (List<Item>) -> Key,
  private inline val onError: suspend (Throwable?) -> Unit,
  private inline val onAppend: suspend (items: List<Item>, newKey: Key) -> Unit,
  private inline val onRefresh: suspend (items: List<Item>) -> Unit
) : PaginatorInterface<Key, Item> {

  private var currentKey = initialKey
  private var loadState = LoadState.NotLoading

  override suspend fun append() {
    if (loadState == LoadState.Append) return
    loadState = LoadState.Append
    onLoadUpdated(loadState)
    try {
      val result = onRequest(currentKey).getOrElse {
        onError(it)
        loadState = LoadState.Error
        onLoadUpdated(loadState)
        return
      }
      currentKey = getNextKey(result)
      onAppend(result, currentKey)
      loadState = LoadState.NotLoading
      onLoadUpdated(loadState)
    } catch (e: Exception) {
      onError(e)
      loadState = LoadState.Error
      onLoadUpdated(loadState)
      return
    }
  }

  override suspend fun refresh() {
    if (loadState == LoadState.Refresh) return
    currentKey = initialKey
    loadState = LoadState.Refresh
    onLoadUpdated(loadState)
    try {
      val result = onRequest(currentKey).getOrElse {
        onError(it)
        loadState = LoadState.Error
        onLoadUpdated(loadState)
        return
      }
      currentKey = getNextKey(result)
      onRefresh(result)
      loadState = LoadState.NotLoading
      onLoadUpdated(loadState)
    } catch (e: Exception) {
      onError(e)
      loadState = LoadState.Error
      onLoadUpdated(loadState)
      return
    }
  }
}

enum class LoadState {
  Refresh, Append, Error, NotLoading
}
