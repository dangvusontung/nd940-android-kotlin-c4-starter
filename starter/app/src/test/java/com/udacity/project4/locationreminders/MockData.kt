package com.udacity.project4.locationreminders

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

    val emptyLocationReminderDTO = ReminderDTO(
        title = "alasodasdd",
        description = "adadasd",
        location = "",
        latitude = 21.028511,
        longitude = 105.804817,
    )

    val fakeReminders = mutableListOf(
        normalReminderDTO,
        normalReminderDTO,
        normalReminderDTO,
    )

    const val fakeNoReminderMessage = "No remiders"

    const val fakeCannotFindReminderMessage = "Can't get reminder"

    const val fakeSaveReminderMessage = "Reminder Saved !"
}