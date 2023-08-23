package com.github.whitescent.mastify.utils

import com.github.whitescent.mastify.network.model.status.Status

/*
 * Copyright 2023 HarukeyUA
 * Modified by whitescent
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
  val statusWithoutParent = repliesIndexes.values.filter { it.parent == null }
  return extractContent(reorderStatusWithChildren(statusWithoutParent))
}

fun reorderStatusWithChildren(statusNode: List<StatusNode>): List<StatusNode> {
  // Sort the statusNode with children into a logical position
  // so that the newest posts are displayed at the top and not at the bottom
  if (statusNode.size == 1) return statusNode
  val result = statusNode.filter { it.children.isEmpty() }.toMutableList()
  val origin = result.toMutableList()
  val statusNodeWithChildren = statusNode.filter { it.children.isNotEmpty() }
  statusNodeWithChildren.forEach { node ->
    val childWithMaxId = node.children.getMaxRecursively()
    for (index in origin.indices) {
      if (origin[index].contentId < childWithMaxId!!.contentId) {
        val insertIndex = result.indexOf(origin[index])
        result.add(insertIndex, node)
        break
      }
    }
  }
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

data class StatusNode(
  var parent: StatusNode? = null,
  val children: MutableList<StatusNode> = mutableListOf(),
  val content: Status,
) {
  val contentId by lazy { content.id.toLong() }
}

fun List<StatusNode>.getMaxRecursively(): StatusNode? {
  val maxNode = this.maxByOrNull { it.contentId } ?: return null
  val maxChild = maxNode.children.getMaxRecursively()
  return if (maxChild != null && maxChild.contentId > maxNode.contentId) maxChild else maxNode
}
