package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndDisplaysMainNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("KinoPulse", context.getString(R.string.app_name))

        composeTestRule.onNodeWithText("KinoPulse").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_search").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("search_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_favorites").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("tab_favorites").assertIsDisplayed()
    }
}
