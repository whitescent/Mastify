package com.github.whitescent.mastify.network

import com.github.whitescent.mastify.network.ReorderStatusListTest.Status
import org.junit.Assert
import org.junit.Test

class ReorderStatusListTest {

  data class Status(
    val id: String,
    var inReplyToId: String? = null,
  )

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
      Status("3"),
      Status("2", "3"),
      Status("4"),
      Status("5"),
      Status("6")
    )
    Assert.assertEquals(expected, testReorderStatuses(actual))
  }

  @Test
  fun `case 2`() {
    val actual = mutableListOf(
      Status("1"),
      Status("2", "3"),
      Status("3"),
      Status("4"),
      Status("5"),
      Status("6")
    )
    val expected = listOf(
      Status("1"),
      Status("3"),
      Status("2", "3"),
      Status("4"),
      Status("5"),
      Status("6")
    )
    Assert.assertEquals(expected, testReorderStatuses(actual))
    actual.addAll(listOf(Status("7", "8"), Status("8")))
    Assert.assertEquals(expected.size + 2, testReorderStatuses(actual).size)
  }
}

fun testReorderStatuses(statuses: List<Status>): List<Status> {
  val repliesIndexes = mutableMapOf<String, StatusNode>()
  statuses.forEach { repliesIndexes[it.id] = StatusNode(content = it) }
  repliesIndexes.forEach { (_, statusNode) ->
    val parent = repliesIndexes[statusNode.content.inReplyToId]
    parent?.also {
      statusNode.parent = parent
      parent.children.add(statusNode)
    }
  }

  return extractContent(repliesIndexes.values.filter { it.parent == null })
}

private fun extractContent(statusNode: List<StatusNode>): List<Status> {
  return statusNode.flatMap {
    if (it.children.isEmpty()) {
      listOf(it.content)
    } else {
      buildList {
        add(it.content)
        addAll(extractContent(it.children))
      }
    }
  }
}

private data class StatusNode(
  var parent: StatusNode? = null,
  val children: MutableList<StatusNode> = mutableListOf(),
  val content: Status,
)
