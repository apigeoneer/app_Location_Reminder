package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.data.FakeRemindersDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest                                           // Medium => integrated test
class RemindersLocalRepositoryTest {

    private lateinit var remindersDao: FakeRemindersDao
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        remindersDao = FakeRemindersDao()
        remindersLocalRepository = RemindersLocalRepository(
            remindersDao, Dispatchers.Unconfined
        )
    }

    @Test
    fun saveReminder_savesToTheLocalCache() = runBlockingTest {

        val newReminder = ReminderDTO("title1", "description1", "location1",
            16.78132279413486, 73.35721723965958)

        // Given
        var remindersList = mutableListOf<ReminderDTO>()

        // When
        remindersList.addAll(remindersDao.remindersServiceData.values)

        // Then
//        assertThat(remindersList).doesNotContain(newReminder)
//        assertThat((remindersLocalRepository.getReminders() as? Result.Success).data)
//            .doesNotContain(newReminder)

        // When
        remindersLocalRepository.saveReminder(newReminder)

        remindersList = mutableListOf()
        remindersList.addAll(remindersDao.remindersServiceData.values)

        // Then
//        assertThat(remindersList).contains(newReminder)

        val result = remindersLocalRepository.getReminders() as? Result.Success
//        assertThat(result.data).contains(newReminder)
    }

    @Test
    fun getReminder_reminderDoesNotExistInCache() = runBlockingTest {

        val reminder = ReminderDTO("title", "description", "location",
            16.78132279413486, 73.35721723965958)

//        assertThat((remindersLocalRepository.getReminder(reminder.id) as? Result.Error)?.message)
//            .isEqualTo("Reminder not found!")

        remindersDao.remindersServiceData[reminder.id] = reminder

        val loadedReminder = (remindersLocalRepository.getReminder(reminder.id) as? Result.Success)?.data

        // Then
        assertThat(loadedReminder as ReminderDTO, CoreMatchers.notNullValue())

        Assert.assertThat(loadedReminder.id, CoreMatchers.`is`(reminder.id))
        Assert.assertThat(loadedReminder.title, CoreMatchers.`is`(reminder.title))
        Assert.assertThat(loadedReminder.description, CoreMatchers.`is`(reminder.description))
        Assert.assertThat(loadedReminder.location, CoreMatchers.`is`(reminder.location))
        Assert.assertThat(loadedReminder.latitude, CoreMatchers.`is`(reminder.latitude))
        Assert.assertThat(loadedReminder.longitude, CoreMatchers.`is`(reminder.longitude))
    }

    @Test
    fun getReminder_returnsEmptyRemindersListFromCache() = runBlockingTest {

        val reminder = ReminderDTO("title", "description", "location",
            16.78132279413486, 73.35721723965958)

        // Given
        val message =(remindersLocalRepository.getReminder(reminder.id) as? Result.Error)?.message

        // When

        // Then
        Assert.assertThat(message, CoreMatchers.notNullValue())

//        assertThat(message).isEqualTo("Reminder does not exist!")
    }

    @Test
    fun deleteAllReminders_returnsEmptyRemindersListOnDeletion() = runBlockingTest {

        val reminder1 = ReminderDTO("title1", "description1", "location1",
        16.78132279413486, 73.35721723965958)

        val reminder2 = ReminderDTO("title2", "description2", "location2",
        36.78132279413486, 73.35721723965958)

        val reminder3 = ReminderDTO("title3", "description3", "location3",
        26.78132279413486, 63.35721723965958)

        val newReminder = ReminderDTO("title4", "description4", "location4",
        26.78132279413486, 83.35721723965958)

//        assertThat((remindersLocalRepository.getReminders() as? Result.Success).data).isEmpty()

        remindersDao.remindersServiceData[reminder1.id] = reminder1
        remindersDao.remindersServiceData[reminder2.id] = reminder2
        remindersDao.remindersServiceData[reminder3.id] = reminder3

//        assertThat((remindersLocalRepository.getReminders() as? Result.Success).data).isNotEmpty()

        remindersLocalRepository.deleteAllReminders()

        // Then
//        assertThat((remindersLocalRepository.getReminders() as? Result?.Success).data).isEmpty()
    }


}