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

package com.github.whitescent.benchmark.startup

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import com.github.whitescent.benchmark.BuildConfig
import com.github.whitescent.benchmark.utils.isLoggedIn
import com.github.whitescent.benchmark.utils.waitForObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalStateException

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

  @get:Rule
  val benchmarkRule = MacrobenchmarkRule()

  // By.hint requires O
  @RequiresApi(Build.VERSION_CODES.O)
  @Test
  fun startBenchmark() {
    val username = BuildConfig.MASTODON_USERNAME
    val password = BuildConfig.MASTODON_PASSWORD
    val site = BuildConfig.MASTODON_SITE

    benchmarkRule.measureRepeated(
      packageName = "com.github.whitescent.mastify",
      metrics = listOf(StartupTimingMetric()),
      iterations = 1,
      startupMode = StartupMode.COLD
    ) {
      startActivityAndWait()

      if (!device.isLoggedIn) {
        val loginInput = device.findObject(By.res("domain input"))

        loginInput.text = site

        device.waitForObject(By.res("login button"), 5000L)

        device.performActionAndWait(
          { device.findObject(By.res("login button")).click() },
          Until.newWindow(),
          2000
        )

        // Skip chrome's login screen, which is launched when chrome has never been used before
        try {
          device.waitForObject(By.text("Use without an account"), 3000L)
          device.findObject(By.text("Use without an account")).click()
        } catch (e:IllegalStateException) {}
          catch (e:NullPointerException) {}

        try {
          device.waitForObject(By.hint("电子邮件地址"), 5000L).text = username
          device.waitForObject(By.hint("密码"), 5000L).text = password
          device.findObject(By.text("登录")).click()
        } catch (e: IllegalStateException) {}

        device.waitForObject(By.text("同意授权"), 5000L)
        device.findObject(By.text("同意授权")).click()

        device.waitForObject(By.res("home timeline"), 5000L)
      }

      val column = device.findObject(By.res("home timeline"))

      repeat(20) {
        column.swipe(Direction.UP, 1f)
      }
      repeat(10) {
        column.swipe(Direction.DOWN, 1f)
      }
    }
  }
}
