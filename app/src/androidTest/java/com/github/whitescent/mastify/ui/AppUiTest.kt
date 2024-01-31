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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.github.whitescent.mastify.MainActivity
import com.github.whitescent.mastify.screen.login.Login
import com.github.whitescent.mastify.viewModel.LoginViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

// This test doesn't work right now
// I haven't figured out how to use hiltViewModel in a unit test :(

@HiltAndroidTest
class AppUiTest {

  @get:Rule
  var hiltRule = HiltAndroidRule(this)

  @get:Rule
  var composeTestRule = createAndroidComposeRule<MainActivity>()

  @Inject
  lateinit var loginViewModel: LoginViewModel

  @Before
  fun setup() {
    hiltRule.inject()
  }

  @Test
  fun myTest() {
    composeTestRule.setContent {
      Login(loginViewModel)
    }
    composeTestRule.onNodeWithTag("domain input").performTextInput("w a")
    composeTestRule.waitUntil(2000L) { true }
    composeTestRule.onNodeWithText("Invalid domain entered").assertIsDisplayed()
  }
}
