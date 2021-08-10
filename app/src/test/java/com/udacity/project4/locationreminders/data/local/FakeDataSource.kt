package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
// the reminders MutableList represents what's going to be in our FakeDataSource
// by default, it is empty
class FakeDataSource(var reminders: MutableList<ReminderDTO>?) : ReminderDataSource {

    // Create a fake data source to act as a double to the real data source

    private var shouldReturnError = false

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    // implement members of the ReminderDataSource
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
//        if (shouldReturnError)
//                return Result.Error("Error getting reminders")

        // if reminders isn't null, then return a Success result with our list of reminders
        // and if it is null, then return an Error result
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Reminders not found")
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
//        if (shouldReturnError)
//            return Result.Error("Error getting Reminder")

        reminders?.forEach {
            return when(id) {
                it.id -> Result.Success(it)
                else -> Result.Error("No reminder found with id $id")
            }
        }

        return Result.Error("No reminder found with id $id")
    }

//    fun setReturnError(value: Boolean) {
//        shouldReturnError = value
//    }

}