package com.udacity.project4.locationreminders.reminderslist

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ibm.icu.impl.Assert.fail
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.core.IsNot.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule=InstantTaskExecutorRule()

    // To test Coroutines running on Dispatchers.Main
    @get:Rule
    var mainCoroutineRule=MainCoroutineRule()

    private val reminder1=ReminderDTO(
        "title1", "description1", "location1",
        16.78132279413486, 73.35721723965958
    )

    private val reminder2=ReminderDTO(
        "title2", "description2", "location2",
        36.78132279413486, 73.35721723965958
    )

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_getsRemindersListForPopulatedRemindersList() {
        // Given a fresh view model & a data source containing a populated reminders list
        val remindersList=mutableListOf(reminder1, reminder2)
        dataSource=FakeDataSource(remindersList)
        remindersListViewModel=RemindersListViewModel(
            ApplicationProvider
                .getApplicationContext(), dataSource
        )

        // When loading reminders
        remindersListViewModel.loadReminders()

        // Then the reminders list is not empty
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), (not(emptyList())))
    }

    @Test
    fun loadReminders_showsLoadingForEmptyRemindersList() {
        // Given a fresh view model & a data source containing an empty reminders list
        dataSource=FakeDataSource(mutableListOf())
        remindersListViewModel=RemindersListViewModel(
            ApplicationProvider
                .getApplicationContext(), dataSource
        )
        mainCoroutineRule.pauseDispatcher()

        // When loading reminders
        remindersListViewModel.loadReminders()

        // Then showLoading is true as there are no reminders
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_notShowsLoadingForPopulatedRemindersList() {
        // Given a fresh view model & a data source containing an empty reminders list
        dataSource=FakeDataSource(mutableListOf(reminder1, reminder2))
        remindersListViewModel=RemindersListViewModel(
            ApplicationProvider
                .getApplicationContext(), dataSource
        )

        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_returnsErrorForNullRemindersList() {
        // Given a fresh view model & a data source containing null
        dataSource=FakeDataSource(null)
        remindersListViewModel=
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        // When loading reminders
        remindersListViewModel.loadReminders()

        // Then a snack bar with the message "No reminders found" is shown
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Reminders not found"))
    }


}