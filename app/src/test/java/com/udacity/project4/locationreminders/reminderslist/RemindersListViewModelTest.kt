package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.core.IsNot.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

//    // To test Coroutines running on Dispatchers.Main
//    @get:Rule
//    var mainCoroutineRule = MainCoroutineRule()

    private val reminder1 = ReminderDTO("title1", "description1", "location1",
        16.78132279413486, 73.35721723965958)

    private val reminder2 = ReminderDTO("title2", "description2", "location2",
        36.78132279413486, 73.35721723965958)

    private val reminder3 = ReminderDTO("title3", "description3", "location3",
        26.78132279413486, 63.35721723965958)

    private val newReminder = ReminderDTO("title4", "description4", "location4",
        26.78132279413486, 83.35721723965958)

    val list = listOf<ReminderDTO>(reminder1, reminder2, reminder3)

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_getRemindersList() {
        // Given
        val remindersList = mutableListOf(reminder1, reminder2)
        dataSource = FakeDataSource(remindersList)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider
            .getApplicationContext(), dataSource)

        // When
        remindersListViewModel.loadReminders()

        // Then
        //assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), (not(emptyList())))
    }

    @Test
    fun loadReminders_checkShowLoading() {
        // Given
        dataSource = FakeDataSource(mutableListOf())
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        //mainCoroutineRule.pauseDispatcher()

        // When
        remindersListViewModel.loadReminders()

        // Then
        //assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun returnError() {
        // Given
        dataSource = FakeDataSource(null)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        // When
        remindersListViewModel.loadReminders()

        // Then
        //assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("No reminders found"))
    }


}