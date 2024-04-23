/*
 * Copyright 2024 WhiteScent
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

package com.github.whitescent.mastify.ui

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.mastify.MainActivity
import com.github.whitescent.mastify.screen.login.Login
import com.github.whitescent.mastify.ui.theme.MastifyTheme
import com.github.whitescent.mastify.viewModel.LoginViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AppUiTest {

  @get:Rule
  var hiltRule = HiltAndroidRule(this)

  @get:Rule
  var composeTestRule = createAndroidComposeRule<MainActivity>()

  lateinit var loginViewModel: LoginViewModel

  @Before
  fun setup() {
    hiltRule.inject()
  }

  @Test
  fun myTest() {
    composeTestRule.activity.setContent {
      loginViewModel = hiltViewModel()
      MastifyTheme {
        Login(loginViewModel)
      }
    }
    composeTestRule.onNodeWithTag("domain input").performTextInput("w a")
    Assert.assertEquals("w a", loginViewModel.loginInput.text.toString())

    // Hide keyboard; otherwise the text is covered by the keyboard
    composeTestRule.runOnUiThread {
      val imm =
        composeTestRule.activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
      imm.hideSoftInputFromWindow(composeTestRule.activity.currentFocus?.windowToken, 0)
    }
    composeTestRule.waitUntil(20000L) {
      !composeTestRule.activity.window.decorView.rootWindowInsets.isVisible(WindowInsetsCompat.Type.ime())
    }

    composeTestRule.onNodeWithText("Invalid domain entered")
      .assertExists()
      .assertIsDisplayed()
  }
}
