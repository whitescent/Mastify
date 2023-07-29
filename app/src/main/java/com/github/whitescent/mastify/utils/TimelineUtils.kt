package com.github.whitescent.mastify.utils

import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import java.util.UUID

fun List<Status>.toViewData() = this.map { Status.ViewData(it) }

suspend fun reorderedStatuses(statuses: List<Status>, api: MastodonApi): List<Status> {
  if (statuses.isEmpty()) return emptyList()

  val id2index = hashMapOf<String, Boolean>()
  var reorderedStatuses = statuses.toMutableList()

  fun findReplyStatusById(id: String?) = id?.let {
    statuses.find { status -> status.id == it }
  }

  fun markAllReplyStatus(replyList: List<Status>): List<Status> {
    val result = mutableListOf<Status>()
    replyList.forEachIndexed { index, status ->
      when (index) {
        0 -> result.add(
          status.copy(
            replyChainType = Status.ReplyChainType.Start,
            hasUnloadedReplyStatus = status.isInReplyTo
          )
        )
        in 1 until replyList.lastIndex -> {
          result.add(
            // 将回复数量大于等于 4 的帖子中，隐藏第一个到倒数第二个中间的帖子
            // 并在倒数第二个帖子标记这是一个多回复链的帖子，方便 UI 层更新对应的 line
            status.copy(
              replyChainType = Status.ReplyChainType.Continue,
              hasMultiReplyStatus = replyList.size >= 4 && index == replyList.lastIndex - 1,
              shouldShow = !(replyList.size >= 4 && index < replyList.size - 2)
            )
          )
        }
        replyList.lastIndex -> {
          result.add(status.copy(replyChainType = Status.ReplyChainType.End))
        }
      }
      id2index[status.id] = true
      result[index] = result[index].copy(uuid = UUID.randomUUID().toString())
    }
    return result
  }

  statuses.forEach { currentStatus ->
    if (currentStatus.isInReplyTo && id2index[currentStatus.id] == null) {
      val replyStatusList = ArrayDeque<Status>().apply { add(currentStatus) }
      var replyToStatus = currentStatus.inReplyToId

      // 获取回复链
      while (true) {
        val replyTo = findReplyStatusById(replyToStatus)
        if (replyTo != null) {
          replyStatusList.addFirst(replyTo)
          replyToStatus = replyTo.inReplyToId
        } else break
      }

      if (replyStatusList.size == 1) {
        // 如果为 1 时，则代表在当前的 timeline List 中找不到这个 id
        // 则进行 api 请求获取回复链
        var currentReplyStatus: String? = replyStatusList.first().inReplyToId
        var replyCount = 1
        while (currentReplyStatus != null && replyCount <= 3) {
          api.status(currentReplyStatus).fold(
            {
              replyStatusList.addFirst(it)
              replyCount++
              currentReplyStatus = if (it.isInReplyTo) {
                it.inReplyToId
              } else {
                null
              }
            },
            {
              it.printStackTrace()
            }
          )
        }
      }
      val finalReplyStatusList = markAllReplyStatus(replyStatusList)
      // 删除原本的 status，并替换为获取到的回复链
      val tempList = reorderedStatuses.toMutableList()
      val startAt = reorderedStatuses.indexOfFirst { finalReplyStatusList.last().id == it.id }
      reorderedStatuses.forEachIndexed { reorderedIndex, status ->
        if (
          reorderedIndex >= startAt && finalReplyStatusList.any { replyList ->
            status.id == replyList.id
          }
        ) {
          tempList.remove(status)
        }
      }
      reorderedStatuses = tempList
      reorderedStatuses.addAll(startAt, finalReplyStatusList)
    }
  }
  return reorderedStatuses
}
