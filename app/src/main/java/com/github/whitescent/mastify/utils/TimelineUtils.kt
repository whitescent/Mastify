package com.github.whitescent.mastify.utils

import com.github.whitescent.mastify.network.model.status.Status

/*
 * Copyright 2023 HarukeyUA
 * SPDX-License-Identifier: GPL-3.0-only
 */

fun reorderStatuses(statuses: List<Status>): List<Status> {
  val repliesIndexes = mutableMapOf<String, StatusNode>()
  statuses.forEach { repliesIndexes[it.id] = StatusNode(content = it) }
  repliesIndexes.forEach { (_, statusNode) ->
    val parent = repliesIndexes[statusNode.content.inReplyToId]
    parent?.also {
      statusNode.parent = parent
      parent.children.add(statusNode)
    }
  }
  val result = extractContent(repliesIndexes.values.filter { it.parent == null })
  println("come ${statuses.size} result ${result.size}")
  return result
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
