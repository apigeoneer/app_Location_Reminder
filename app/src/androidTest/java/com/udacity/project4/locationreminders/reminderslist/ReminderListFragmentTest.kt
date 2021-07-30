package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.util.concurrent.FakeTimeLimiter
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import net.bytebuddy.matcher.ClassLoaderParentMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    private lateinit var dataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setup() {
        dataSource = FakeDataSource(mutableListOf())
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), dataSource)

        stopKoin()

        val myModule = module {
            single {
                remindersListViewModel
            }
        }

        // new koin module
        startKoin {
            modules(listOf(myModule))
        }
    }

    @Test
    fun displayRemindersList() = runBlocking {

        val reminder1 = ReminderDTO("title1", "description1", "location1",
            16.78132279413486, 73.35721723965958)
        val reminder2 = ReminderDTO("title2", "description2", "location2",
            15.78132279413486, 83.35721723965958)

        val remindersList = listOf<ReminderDTO?>(reminder1, reminder2)

        dataSource.saveReminder(reminder1)
        dataSource.saveReminder(reminder2)

        // Given
        val reminders = (dataSource.getReminders() as? Result.Success)?.data

        val reminderFirst = reminders?.get(0)

        onView(
            Matchers.allOf(
                withText(reminderFirst?.location),
                childPosition(
                        childPosition(
                            ViewMatchers.withId(R.id.reminderCardView),
                            0
                        ),
                        2
                ),
                isDisplayed()
            )
        )
        .check(matches(withText(reminderFirst?.location)))
    }

    @Test
    fun navigateToAddReminder() {
        // When
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

//        onView(withId(R.id.addReminderFAB).perform(click()))

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    private fun childPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {
        return  object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description?) {
                description?.appendText("Child at position $position in parent")
                parentMatcher.describeTo(description)
            }

            override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return  parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }

        }
    }

}













