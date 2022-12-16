package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.MockData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //    TODO: Add testing implementation to the RemindersDao.kt
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDB() {
        database.close()
    }

    @Test
    fun test_getReminders_success() = runBlockingTest {
        //GIVEN
        val fakeReminder = MockData.normalReminderDTO
        database.reminderDao().saveReminder(fakeReminder)

        //WHEN
        val reminders = database.reminderDao().getReminders()

        //THEM
        MatcherAssert.assertThat(reminders.size, CoreMatchers.`is`(1))
        MatcherAssert.assertThat(reminders[0].id, CoreMatchers.`is`(fakeReminder.id))
    }

    @Test
    fun test_saveReminder_success() = runBlockingTest {
        //GIVEN
        val fakeReminder = MockData.normalReminderDTO
        database.reminderDao().saveReminder(fakeReminder)

        //WHEN
        val reminder = database.reminderDao().getReminderById(fakeReminder.id)
        val id = reminder?.id ?: ""

        //THEN
        MatcherAssert.assertThat(reminder, notNullValue())
        MatcherAssert.assertThat(id, CoreMatchers.`is`(fakeReminder.id))
    }

    @Test
    fun test_deleteAllReminders_success() = runBlockingTest {
        //GIVEN
        val fakeReminderList = MockData.fakeReminders
        fakeReminderList.forEach {
            database.reminderDao().saveReminder(it)
        }

        //WHEN
        database.reminderDao().deleteAllReminders()
        val reminders = database.reminderDao().getReminders()

        //THEN
        MatcherAssert.assertThat(reminders, CoreMatchers.`is`(emptyList()))
    }
}