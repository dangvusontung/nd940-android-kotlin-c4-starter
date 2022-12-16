package com.udacity.project4

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

object MockData {
    val normalReminderDTO = ReminderDTO(
        title = "adadsds",
        description = "adadasd",
        location = "alasodasdd",
        latitude = 21.028511,
        longitude = 105.804817,
    )

    val emptyTitleReminderDTO = ReminderDTO(
        title = "",
        description = "adadasd",
        location = "alasodasdd",
        latitude = 21.028511,
        longitude = 105.804817,
    )

    val fakeReminders = mutableListOf(
        normalReminderDTO,
        normalReminderDTO,
        normalReminderDTO,
    )

    val fakeCannotFindReminderMessage = "Reminder not found!"

    val fakeNoReminderMessage = "No remiders"

    val fakeSaveReminderMessage = "Reminder Saved !"
}