package com.udacity.project4.locationreminders.data.local

import android.R.attr
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest                                           // Medium => integrated test
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule=InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        dao = database.reminderDao()
        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getReminders_twoRemindersFoundInCache() = runBlockingTest {
        // Given
        val reminder1 = ReminderDTO("title1", "description1", "location1",
            16.78132279413486, 73.35721723965958)

        val reminder2 = ReminderDTO("title2", "description2", "location2",
            16.78132279413486, 73.35721723965958)

        dao.saveReminder(reminder1)
        dao.saveReminder(reminder2)

        // When
        val loadedReminder1 = repository.getReminder(reminder1.id)
        val loadedReminder2 = repository.getReminder(reminder2.id)

        loadedReminder1 as Result.Success
        loadedReminder2 as Result.Success

        // Then
        assertThat(loadedReminder1, `is`(notNullValue()))
        assertThat(loadedReminder1.data.title, `is`(reminder1.title))
        assertThat(loadedReminder1.data.location, `is`(reminder1.location))
        assertThat(loadedReminder1.data.latitude, `is`(reminder1.latitude))
        assertThat(loadedReminder1.data.longitude, `is`(reminder1.longitude))

        assertThat(loadedReminder2, `is`(notNullValue()))
        assertThat(loadedReminder2.data.title, `is`(reminder2.title))
        assertThat(loadedReminder2.data.location, `is`(reminder2.location))
        assertThat(loadedReminder2.data.latitude, `is`(reminder2.latitude))
        assertThat(loadedReminder2.data.longitude, `is`(reminder2.longitude))
    }

//    fun saveReminder() = runBlockingTest {
//        // Given
//        val reminder = ReminderDTO("title", "description", "location",
//            16.78132279413486, 73.35721723965958)
//
//        // When
//        dao.saveReminder(reminder)
//
//        // Then
////        assertThat(dao, contains(reminder))
//    }

    @Test
    fun getReminder_oneReminderFoundInCache() = runBlockingTest {
        // Given
        val reminder = ReminderDTO("title", "description", "location",
            16.78132279413486, 73.35721723965958)
        dao.saveReminder(reminder)

        // When
        val loadedReminder = repository.getReminder(reminder.id)
        loadedReminder as Result.Success

        // Then
        assertThat(loadedReminder, `is`(notNullValue()))
        assertThat(loadedReminder.data.title, `is`(reminder.title))
        assertThat(loadedReminder.data.location, `is`(reminder.location))
        assertThat(loadedReminder.data.latitude, `is`(reminder.latitude))
        assertThat(loadedReminder.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminder_noRemindersFoundInCache() = runBlockingTest {
//        // Given
        val reminder = ReminderDTO("title", "description", "location",
            16.78132279413486, 73.35721723965958)
//        dao.saveReminder(reminder)
//
        // When
        val loadedReminder = repository.getReminder(reminder.id)

        // Then
        assertThat(loadedReminder, `is`(nullValue()))
        loadedReminder as Result.Error
        assertThat(loadedReminder.message, `is`("Reminder not found!"))
    }

    @Test
    fun deleteAllReminders_deletesTheTwoSavedReminders() = runBlockingTest {
        // Given
        val reminder1 = ReminderDTO("title1", "description1", "location1",
            16.78132279413486, 73.35721723965958)

        val reminder2 = ReminderDTO("title2", "description2", "location2",
            16.78132279413486, 73.35721723965958)

        dao.saveReminder(reminder1)
        dao.saveReminder(reminder2)

        // When
        dao.deleteAllReminders()

        val loadedReminder = repository.getReminders()
        loadedReminder as Result.Success

        // Then
        assertThat(loadedReminder, `is`(nullValue()))
    }


}