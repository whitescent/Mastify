/*
 * Copyright 2023 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.timeline

import org.junit.Assert
import org.junit.Test

private const val timelineFetchNumber = 3

class TimelineListTest {

  @Test
  fun `case 1`() {
    val savedList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7")
    )
    val apiList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7")
    )
    Assert.assertEquals(apiList, timelineListHandler(savedList, apiList))
  }

  @Test
  fun `case 2`() {
    val savedList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7")
    )
    val apiList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
    )
    Assert.assertEquals(apiList, timelineListHandler(savedList, apiList))
  }

  @Test
  fun `case 3`() {
    val savedList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7")
    )
    val apiList = emptyList<StatusTestItem>()
    Assert.assertEquals(apiList, timelineListHandler(savedList, apiList))
  }

  @Test
  fun `case 4`() {
    val savedList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7"),
      StatusTestItem("6")
    )
    val apiList = listOf(
      StatusTestItem("12"),
      StatusTestItem("11"),
      StatusTestItem("10"),
    )
    val expected = listOf(
      StatusTestItem("12"),
      StatusTestItem("11"),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7"),
      StatusTestItem("6")
    )
    Assert.assertEquals(expected, timelineListHandler(savedList, apiList))
  }

  @Test
  fun `case 5`() {
    val savedList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7"),
      StatusTestItem("6")
    )
    val apiList = listOf(
      StatusTestItem("15"),
      StatusTestItem("14"),
      StatusTestItem("13"),
    )
    val expected = listOf(
      StatusTestItem("15"),
      StatusTestItem("14"),
      StatusTestItem("13", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7"),
      StatusTestItem("6")
    )
    Assert.assertEquals(expected, timelineListHandler(savedList, apiList))
  }

  @Test
  fun `case 6`() {
    val savedList = listOf(
      StatusTestItem("15"),
      StatusTestItem("14"),
      StatusTestItem("13", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7"),
      StatusTestItem("6")
    )
    val apiList = listOf(
      StatusTestItem("15"),
      StatusTestItem("14"),
      StatusTestItem("13"),
    )
    val expected = listOf(
      StatusTestItem("15"),
      StatusTestItem("14"),
      StatusTestItem("13", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7"),
      StatusTestItem("6")
    )
    Assert.assertEquals(expected, timelineListHandler(savedList, apiList))
  }

  @Test
  fun `case 7`() {
    var savedList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7"),
      StatusTestItem("6")
    )
    var apiList = mutableListOf(
      StatusTestItem("15"),
      StatusTestItem("14"),
      StatusTestItem("13"),
    )
    var expected = mutableListOf(
      StatusTestItem("15"),
      StatusTestItem("14"),
      StatusTestItem("13", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7"),
      StatusTestItem("6")
    )
    savedList = timelineListHandler(savedList, apiList)
    Assert.assertEquals(expected, savedList)
    // Simulating post deletion
    apiList = mutableListOf(
      StatusTestItem("15"),
      StatusTestItem("14"),
      StatusTestItem("12"),
    )
    expected = mutableListOf(
      StatusTestItem("15"),
      StatusTestItem("14"),
      StatusTestItem("12", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8"),
      StatusTestItem("7"),
      StatusTestItem("6")
    )
    savedList = timelineListHandler(savedList, apiList)
    Assert.assertEquals(expected, savedList)
  }

  @Test
  fun `case 8`() {
    var savedList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8")
    )
    var apiList = mutableListOf(
      StatusTestItem("13"),
      StatusTestItem("12"),
      StatusTestItem("11"),
    )
    var expected = mutableListOf(
      StatusTestItem("13"),
      StatusTestItem("12"),
      StatusTestItem("11", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8")
    )
    savedList = timelineListHandler(savedList, apiList)
    Assert.assertEquals(expected, savedList)
    // Simulating post addition
    apiList = mutableListOf(
      StatusTestItem("16"),
      StatusTestItem("15"),
      StatusTestItem("14"),
    )
    expected = mutableListOf(
      StatusTestItem("16"),
      StatusTestItem("15"),
      StatusTestItem("14", true),
      StatusTestItem("13"),
      StatusTestItem("12"),
      StatusTestItem("11", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8")
    )
    savedList = timelineListHandler(savedList, apiList)
    Assert.assertEquals(expected, savedList)
    // Simulating post addition
    apiList = mutableListOf(
      StatusTestItem("19"),
      StatusTestItem("18"),
      StatusTestItem("17"),
    )
    expected = mutableListOf(
      StatusTestItem("19"),
      StatusTestItem("18"),
      StatusTestItem("17", true),
      StatusTestItem("16"),
      StatusTestItem("15"),
      StatusTestItem("14", true),
      StatusTestItem("13"),
      StatusTestItem("12"),
      StatusTestItem("11", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8")
    )
    savedList = timelineListHandler(savedList, apiList)
    Assert.assertEquals(expected, savedList)
  }

  @Test
  fun `case 9`() {
    var savedList = listOf(
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8")
    )
    var apiList = mutableListOf(
      StatusTestItem("13"),
      StatusTestItem("12"),
      StatusTestItem("11"),
    )
    var expected = mutableListOf(
      StatusTestItem("13"),
      StatusTestItem("12"),
      StatusTestItem("11", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8")
    )
    savedList = timelineListHandler(savedList, apiList)
    Assert.assertEquals(expected, savedList)
    // Simulating post deletion
    apiList = mutableListOf(
      StatusTestItem("12"),
      StatusTestItem("11"),
      StatusTestItem("10"),
    )
    expected = mutableListOf(
      StatusTestItem("12"),
      StatusTestItem("11", true),
      StatusTestItem("10"),
      StatusTestItem("9"),
      StatusTestItem("8")
    )
    savedList = timelineListHandler(savedList, apiList)
    Assert.assertEquals(expected, savedList)
  }
}

private data class StatusTestItem(
  val id: String,
  val hasUnloadedStatus: Boolean = false
)

private fun timelineListHandler(
  oldItems: List<StatusTestItem>,
  newItems: List<StatusTestItem>
): List<StatusTestItem> {
  when (newItems.isEmpty()) {
    true -> return emptyList()
    else -> {
      if (oldItems.size < timelineFetchNumber || newItems.size < timelineFetchNumber) return newItems
      else {
        val lastStatusOfNewItems = newItems.last()
        if (oldItems.any { it.id == lastStatusOfNewItems.id }) {
          val indexInSavedList = oldItems.indexOfFirst {
            it.id == lastStatusOfNewItems.id
          } + 1
          val statusListAfterFetchNumber =
            oldItems.subList(indexInSavedList, oldItems.size)
          val statusListWithUnloaded =
            (oldItems - statusListAfterFetchNumber.toSet()).filter { it.hasUnloadedStatus }
          val statusListBeforeFetchNumber = newItems.toMutableList().apply {
            replaceAll { new ->
              statusListWithUnloaded.find { saved -> saved.id == new.id } ?: new
            }
          }
          return statusListBeforeFetchNumber + statusListAfterFetchNumber
        } else {
          // If the last currentStatus returned by the API cannot be found in the saved currentStatus list,
          // This means that the number of statuses in the user's timeline exceeds
          // the number of statuses in a single API request,
          // and we need to display 'Load More' button
          val newStatusList = newItems.toMutableList()
          newStatusList[newStatusList.lastIndex] =
            newStatusList[newStatusList.lastIndex].copy(hasUnloadedStatus = true)
          newItems.forEach {
            // Here we need to consider whether currentStatus already
            // contains the hasUnloadedStatus
            // If so, we need to add a new currentStatus list on top of this instead of overwriting it
            if (oldItems.any { saved -> saved.id == it.id }) {
              val removeIndex = oldItems.indexOfFirst { it.hasUnloadedStatus }.let {
                if (it == -1) 0 else it + 1
              }
              return newStatusList + oldItems.subList(removeIndex, oldItems.size)
            }
          }
          return newStatusList + oldItems
        }
      }
    }
  }
}
