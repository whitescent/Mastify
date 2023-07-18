package com.github.whitescent.benchmark.baselineprofile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

  @RequiresApi(Build.VERSION_CODES.P)
  @get:Rule
  val baselineProfileRule = BaselineProfileRule()

  @RequiresApi(Build.VERSION_CODES.P)
  @Test
  fun startup() =
    baselineProfileRule.collect("com.github.whitescent.mastify") {
      startActivityAndWait()
    }
}
