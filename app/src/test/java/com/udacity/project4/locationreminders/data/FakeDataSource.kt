package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource : ReminderDataSource {

    private var reminderList: MutableList<ReminderDTO> = mutableListOf()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(reminderList)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        for (reminder in reminderList) {
            if (reminder.id == id) {
                return Result.Success(reminder)
            }
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }


}