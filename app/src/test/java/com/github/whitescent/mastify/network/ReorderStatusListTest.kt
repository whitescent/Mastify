package com.github.whitescent.mastify.network

import com.github.whitescent.mastify.network.ReorderStatusListTest.Status
import com.github.whitescent.mastify.network.ReorderStatusListTest.Status.ReplyChainType.Continue
import com.github.whitescent.mastify.network.ReorderStatusListTest.Status.ReplyChainType.End
import com.github.whitescent.mastify.network.ReorderStatusListTest.Status.ReplyChainType.Null
import com.github.whitescent.mastify.network.ReorderStatusListTest.Status.ReplyChainType.Start
import org.junit.Assert
import org.junit.Test

class ReorderStatusListTest {

  data class Status(
    val id: String,
    val inReplyToId: String? = null,
    val replyChainType: ReplyChainType = Null,
    val hasUnloadedReplyStatus: Boolean = false,
    val hasMultiReplyStatus: Boolean = false,
    val shouldShow: Boolean = true
  ) {
    val isInReplyTo inline get() = inReplyToId != null
    enum class ReplyChainType {
      Start, Continue, End, Null
    }
  }

  @Test
  fun `case 1`() {
    val actual = listOf(
      Status("1"),
      Status("2", "3"),
      Status("3"),
      Status("4"),
      Status("5"),
      Status("6")
    )
    val expected = listOf(
      Status("1"),
      Status("3", replyChainType = Start),
      Status("2", "3", End),
      Status("4"),
      Status("5"),
      Status("6")
    )
    Assert.assertEquals(expected, reorderedStatuses(actual))
  }

  @Test
  fun `case 2`() {
    val actual = listOf(
      Status("1"),
      Status("2", "3"),
      Status("3", "6"),
      Status("4"),
      Status("5", "6"),
      Status("6"),
      Status("7"),
    )
    val expected = listOf(
      Status("1"),
      Status("6", replyChainType = Start),
      Status("3", "6", replyChainType = Continue),
      Status("2", "3", replyChainType = End),
      Status("4"),
      Status("6", replyChainType = Start),
      Status("5", "6", End),
      Status("7")
    )
    Assert.assertEquals(expected, reorderedStatuses(actual))
  }

  @Test
  fun `case 3`() {
    val actual = listOf(
      Status("1", "6"),
      Status("2"),
      Status("3"),
      Status("4"),
      Status("5"),
      Status("6"),
    )
    val expected = listOf(
      Status("6", replyChainType = Start),
      Status("1", "6", replyChainType = End),
      Status("2"),
      Status("3"),
      Status("4"),
      Status("5"),
    )
    Assert.assertEquals(expected, reorderedStatuses(actual))
  }

  @Test
  fun `case 4`() {
    val actual = listOf(
      Status("1", "7"),
      Status("2"),
      Status("3"),
    )
    val expected = listOf(
      Status("1", "7", replyChainType = End, hasUnloadedReplyStatus = true),
      Status("2"),
      Status("3"),
    )
    Assert.assertEquals(expected, reorderedStatuses(actual))
  }

  @Test
  fun `case 5`() {
    val actual = listOf(
      Status("1"),
      Status("2"),
      Status("3", "4"),
      Status("4", "5"),
      Status("5", "9"),
      Status("6"),
      Status("7"),
    )
    val expected = listOf(
      Status("1"),
      Status("2"),
      Status("5", "9", replyChainType = Continue, hasUnloadedReplyStatus = true),
      Status("4", "5", replyChainType = Continue),
      Status("3", "4", replyChainType = End),
      Status("6"),
      Status("7"),
    )
    Assert.assertEquals(expected, reorderedStatuses(actual))
  }

  @Test
  fun `case 6`() {
    val actual = listOf(
      Status("1"),
      Status("2", "3"),
      Status("3", "4"),
      Status("4", "5"),
      Status("5", "6"),
      Status("6"),
      Status("7"),
    )
    val expected = listOf(
      Status("1"),
      Status("6", replyChainType = Start),
      Status("5", "6", replyChainType = Continue, shouldShow = false),
      Status("4", "5", replyChainType = Continue, shouldShow = false),
      Status("3", "4", replyChainType = Continue, hasMultiReplyStatus = true),
      Status("2", "3", replyChainType = End),
      Status("7"),
    )
    Assert.assertEquals(expected, reorderedStatuses(actual))
  }

  // @Test
  // fun `case 7`() {
  //   var actual = listOf(
  //     Status("1"),
  //     Status("2", "3"),
  //     Status("3", "7"),
  //     Status("4")
  //   )
  //   var expected = listOf(
  //     Status("1"),
  //     Status("3", "7", replyChainType = Continue, hasUnloadedReplyStatus = true),
  //     Status("2", "3", replyChainType = End),
  //     Status("4")
  //   )
  //   actual = reorderedStatuses(actual)
  //   Assert.assertEquals(expected, actual)
  //   val appendItem = listOf(
  //     Status("5"),
  //     Status("6"),
  //     Status("7"),
  //   )
  //   actual = reorderedStatuses(actual + appendItem) // second reorder
  //   expected = listOf(
  //     Status("1"),
  //     Status("5", replyChainType = Start),
  //     Status("3", "5", replyChainType = Continue),
  //     Status("2", "3", replyChainType = End),
  //     Status("4"),
  //     Status("6"),
  //     Status("7"),
  //   )
  //   Assert.assertEquals(expected, actual)
  // }

  @Test
  fun `case 8`() {
    val actual = listOf(
      Status("1"),
      Status("3", replyChainType = Start),
      Status("2", "3", replyChainType = End),
      Status("4"),
    )
    val expected = listOf(
      Status("1"),
      Status("3", replyChainType = Start),
      Status("2", "3", replyChainType = End),
      Status("4"),
    )
    Assert.assertEquals(expected, reorderedStatuses(actual))
  }

  @Test
  fun `case 9`() {
    val actual = listOf(
      Status("1"),
      Status("2", "3"),
      Status("3", "4"),
      Status("4", "5"),
      Status("5", "6"),
      Status("6", "10"),
      Status("7"),
      Status("8"),
    )
    val expected = listOf(
      Status("1"),
      Status("6", "10", replyChainType = Continue, hasUnloadedReplyStatus = true),
      Status("5", "6", replyChainType = Continue, shouldShow = false),
      Status("4", "5", replyChainType = Continue, shouldShow = false),
      Status("3", "4", replyChainType = Continue, hasMultiReplyStatus = true),
      Status("2", "3", replyChainType = End),
      Status("7"),
      Status("8"),
    )
    Assert.assertEquals(expected, reorderedStatuses(actual))
  }
}

private fun reorderedStatuses(statuses: List<Status>): List<Status> {
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
            replyChainType = if (status.isInReplyTo) Continue else Start,
            hasUnloadedReplyStatus = status.isInReplyTo
          )
        )
        in 1 until replyList.lastIndex -> {
          result.add(
            // 将回复数量大于等于 4 的帖子中，隐藏第一个到倒数第二个中间的帖子
            // 并在倒数第二个帖子标记这是一个多回复链的帖子，方便 UI 层更新对应的 line
            status.copy(
              replyChainType = Continue,
              hasMultiReplyStatus = replyList.size >= 4 && index == replyList.lastIndex - 1,
              shouldShow = !(replyList.size >= 4 && index < replyList.size - 2)
            )
          )
        }
        replyList.lastIndex -> {
          result.add(status.copy(replyChainType = End))
        }
      }
      id2index[status.id] = true
    }
    return result
  }

  statuses.forEach { currentStatus ->
    if (currentStatus.isInReplyTo && id2index[currentStatus.id] == null && currentStatus.replyChainType == Null) {
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
              .takeIf { it != Null } ?: End
          )
      } else {
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
  }
  return reorderedStatuses
}
