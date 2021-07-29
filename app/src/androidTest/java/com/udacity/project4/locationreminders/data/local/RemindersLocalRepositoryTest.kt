package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

    val list = listOf<ReminderDTO>(reminder1, reminder2, reminder3)



    //private lateinit var remindersDataSource: FakeDataSource
    private lateinit var remindersLocalRepository: RemindersLocalRepository

}