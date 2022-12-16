package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.MockData
import com.udacity.project4.locationreminders.MockRemindersLocalRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
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

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
@get:Rule
val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: MockRemindersLocalRepository


    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        remindersLocalRepository =
            MockRemindersLocalRepository(database.reminderDao(), Dispatchers.Unconfined)
    }

    @Test
    fun test_saveReminder_success() = runBlockingTest {
        //GIVEN
        val reminder = MockData.normalReminderDTO

        //THEN
        val data = (remindersLocalRepository.getReminders() as Result.Success).data
        assertThat(data.contains(reminder), `is`(false))

        //WHEN
        remindersLocalRepository.saveReminder(reminder)

        //THEN
        val result = (remindersLocalRepository.getReminders() as Result.Success).data
        assertThat(result.contains(reminder), `is`(true))

    }

    @Test
    fun test_getReminder_success() = runBlockingTest {
        //WHEN
        val reminder = MockData.normalReminderDTO

        //THEN
        val current = (remindersLocalRepository.getReminders() as Result.Success).data
        assertThat(current.contains(reminder), `is`(false))

        //WHEN
        remindersLocalRepository.saveReminder(reminder)

        //THEN
        val resultReminder =
            (remindersLocalRepository.getReminder(reminder.id) as Result.Success).data
        assertThat(resultReminder, `is`(CoreMatchers.notNullValue()))
    }

    @Test
    fun test_getReminder_failure() = runBlockingTest {
        //GIVEN
        val searchId = MockData.normalReminderDTO.id

        //WHEN
        val message = (remindersLocalRepository.getReminder(searchId) as Result.Error).message

        //THEN
        assertThat(message, `is`(MockData.fakeCannotFindReminderMessage))
    }

    @Test
    fun test_deleteAllReminders_success() = runBlockingTest {
        //GIVEN
        val reminder = MockData.normalReminderDTO

        //THEN
        val current = (remindersLocalRepository.getReminders() as Result.Success).data
        assertThat(current.contains(reminder), `is`(false))

        //WHEN
        remindersLocalRepository.saveReminder(reminder)

        //THEN
        val after = (remindersLocalRepository.getReminders() as Result.Success).data
        assertThat(after.contains(reminder), `is`(true))

        //WHEN
        remindersLocalRepository.deleteAllReminders()

        //THEN
        val result = (remindersLocalRepository.getReminders() as Result.Success).data
        assertThat(result, `is`(emptyList()))
    }
}