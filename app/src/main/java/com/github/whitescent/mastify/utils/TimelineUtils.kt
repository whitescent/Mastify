package com.github.whitescent.mastify.utils

import com.github.whitescent.mastify.network.model.status.Status
import java.util.UUID

fun List<Status>.toViewData() = this.map { Status.ViewData(it) }

fun reorderedStatuses(statuses: List<Status>): List<Status> {
  if (statuses.isEmpty()) return emptyList()

  fun findReplyStatusById(id: String?) = id?.let {
    statuses.find { status -> status.id == it }
  }

  val id2index = hashMapOf<String, Boolean>()
  var reorderedStatuses = statuses.toMutableList()

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
        // 则添加 hasUnloadedInReplyTo 属性
        val unloadReplyStatusIndex =
          reorderedStatuses.indexOfFirst { it.id == replyStatusList.first().id }
        reorderedStatuses[unloadReplyStatusIndex] =
          reorderedStatuses[unloadReplyStatusIndex].copy(
            hasUnloadedReplyStatus = true,
            replyChainType = reorderedStatuses[unloadReplyStatusIndex].replyChainType
              .takeIf { it != Status.ReplyChainType.Null } ?: Status.ReplyChainType.End
          )
      } else {
        val finalReplyStatusList = ArrayDeque<Status>().apply {
          add(replyStatusList.first().copy(
            replyChainType = Status.ReplyChainType.Start,
            uuid = replyStatusList.first().id + UUID.randomUUID().toString()
          ))
        }
        // 给组合完成的回复链更新指定的属性，并且标记不需要重复获取回复链的 status
        replyStatusList.forEachIndexed { replyIndex, status ->
          when (replyIndex) {
            in 1 until replyStatusList.lastIndex -> {
              finalReplyStatusList.add(
                // 将回复数量大于等于 4 的帖子中，隐藏第一个到倒数第二个中间的帖子
                // 并在倒数第二个帖子标记这是一个多回复链的帖子，方便 UI 层更新对应的 line
                status.copy(
                  replyChainType = Status.ReplyChainType.Continue,
                  hasMultiReplyStatus = replyStatusList.size >= 4 &&
                    replyIndex == replyStatusList.lastIndex - 1,
                  shouldShow = !(replyStatusList.size >= 4 && replyIndex < replyStatusList.size - 2)
                )
              )
              id2index[status.id] = true
            }
            replyStatusList.lastIndex -> {
              finalReplyStatusList.add(status.copy(replyChainType = Status.ReplyChainType.End))
            }
          }
        }
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
  }
  return reorderedStatuses
}
