package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertNull

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
/**
 * Had we made the Dao a local unit test, we'd have to test it i=on the local version of SQLite
 * but this way, we test it on the version of SQLite on our Android device / emulator
 * which is better.
 */
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao


    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before                       // to make sure we have an entirely new db before every test case
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        dao = database.reminderDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getReminders_getsTwoReminders() = runBlockingTest {
        // Given 2 reminders in the db
        val reminder1 = ReminderDTO("title1", "description1", "location1",
            16.78132279413486, 73.35721723965958)

        val reminder2 = ReminderDTO("title2", "description2", "location2",
            15.78132279413486, 83.35721723965958)

        dao.saveReminder(reminder1)
        dao.saveReminder(reminder2)

        // When fetching reminders
        val remindersList = dao.getReminders()

        // Then 2 reminders are received with all the expected values
        assertThat(remindersList.size, `is`(2))

        assertThat(remindersList[0].id, `is`(reminder1.id))
        assertThat(remindersList[1].title, `is`(reminder2.title))
        assertThat(remindersList[0].description, `is`(reminder1.description))
        assertThat(remindersList[1].location, `is`(reminder2.location))
        assertThat(remindersList[0].latitude, `is`(reminder1.latitude))
        assertThat(remindersList[1].longitude, `is`(reminder2.longitude))
    }

    @Test
    fun getReminderById_getsOneReminderById() = runBlockingTest {
        // Given a reminder in the db
        val reminder = ReminderDTO("title", "description", "location",
            16.78132279413486, 73.35721723965958)

        dao.saveReminder(reminder)

        // When fetching its id after having saved it in the db
        val loadedReminder = dao.getReminderById(reminder.id)

        // Then the reminder contains all the expected values
//        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())

        assertThat(loadedReminder!!.id, `is`(reminder.id))
        assertThat(loadedReminder.title, `is`(reminder.title))
        assertThat(loadedReminder.description, `is`(reminder.description))
        assertThat(loadedReminder.location, `is`(reminder.location))
        assertThat(loadedReminder.latitude, `is`(reminder.latitude))
        assertThat(loadedReminder.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminderById_returnsNullForNonExistentId() = runBlockingTest {
        // Given a reminder id (that does not exist)
        val reminderId = UUID.randomUUID().toString()

        // When fetching the reminder from the id (non existent)
        val loadedReminder = dao.getReminderById(reminderId)

        // Then the loaded reminder is null
        assertNull(loadedReminder)
    }

    @Test
    fun deleteAllReminders_deletes2Reminders() = runBlockingTest {
        // Given 2 reminders in the db
        val reminder1 = ReminderDTO("title1", "description1", "location1",
            16.78132279413486, 73.35721723965958)

        val reminder2 = ReminderDTO("title2", "description2", "location2",
            15.78132279413486, 83.35721723965958)

        dao.saveReminder(reminder1)
        dao.saveReminder(reminder2)

        // When deleting all reminders
        dao.deleteAllReminders()

        // Then the reminders list becomes empty
        val remindersList = dao.getReminders()
        assertThat(remindersList.isEmpty(), `is`(true))
    }





}

















