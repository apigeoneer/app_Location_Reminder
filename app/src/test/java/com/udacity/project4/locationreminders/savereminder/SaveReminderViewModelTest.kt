package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import junit.runner.Version.id
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import androidx.test.espresso.assertion.ViewAssertions.matches



@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    @Before
    fun setUp() {
//        stopKoin()
        val reminderDataItem = mutableListOf<ReminderDTO>()
        dataSource = FakeDataSource(reminderDataItem)
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

    }

    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    fun saveReminder_returnsErrorForReminderWithoutTitle() = mainCoroutineRule.runBlockingTest {
        // Given
        val reminder = ReminderDataItem("", "description", "location",
            16.78132279413486, 73.35721723965958)

        // When
        saveReminderViewModel.saveReminder(reminder)

        // Then
        assertThat(saveReminderViewModel.validateEnteredData(reminder)).isFalse()

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun saveReminder_returnsErrorForReminderWithoutLocation() {
        // Given
        val reminder = ReminderDataItem("title", "description", "",
            16.78132279413486, 73.35721723965958)

        // When
        saveReminderViewModel.saveReminder(reminder)

        // Then
        assertThat(saveReminderViewModel.validateEnteredData(reminder)).isFalse()

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun saveReminder_showsLoadingStatus() {
        mainCoroutineRule.pauseDispatcher()

        // Given
        val reminder = ReminderDataItem("title", "description", "location",
            16.78132279413486, 73.35721723965958)

        // When
        saveReminderViewModel.saveReminder(reminder)

        // Then
//        mainCoroutineRule.pauseDispatcher()                                         not here
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isTrue()

        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isFalse()
    }


//    fun validateEnteredData_validatesReminders() {
//
//    }
//
//    fun onClear_clears() {
//
//    }


}