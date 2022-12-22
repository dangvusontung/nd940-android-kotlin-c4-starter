package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.MockData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import org.mockito.Mock

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {
//    TODO: Create a fake data source to act as a double to the real data source

    private var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (!shouldReturnError) Result.Success(reminders) else Result.Error(MockData.fakeNoReminderMessage)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val data = reminders.firstOrNull { it -> it.id == id }
        return data?.let {
            Result.Success(it)
        } ?: Result.Error(MockData.fakeCannotFindReminderMessage)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
}