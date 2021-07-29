package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.data.FakeDataSource


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private val reminder1 = ReminderDTO("title1", "description1", "location1",
        16.78132279413486, 73.35721723965958)

    private val reminder2 = ReminderDTO("title2", "description2", "location2",
        36.78132279413486, 73.35721723965958)

    private val reminder3 = ReminderDTO("title3", "description3", "location3",
        26.78132279413486, 63.35721723965958)

    private val newReminder = ReminderDTO("title4", "description4", "location4",
        26.78132279413486, 83.35721723965958)

    val remindersList = listOf(reminder1, reminder2, reminder3)


    private lateinit var dataSource: FakeDataSource
    private lateinit var remindersLocalRepository: RemindersLocalRepository


    @Before
    fun setup() {

    }

    fun saveReminder() {

    }

    fun getReminders() {

    }

    fun getReminder() {

    }

    fun deleteAllReminders() {

    }


}