package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.MockData
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun test_loadReminders_success() = runBlockingTest {
        //GIVEN
        val fakeDataSource = FakeDataSource(MockData.fakeReminders)
        val remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

        //WHEN
        remindersListViewModel.loadReminders()

        //THEN
        MatcherAssert.assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue().size,
            `is`(MockData.fakeReminders.size)
        )
    }

    @Test
    fun test_loadRemindersWithNullDataSource_error() = runBlockingTest {
        //GIVEN
        val fakeDataSource = FakeDataSource(null)
        val remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

        //WHEN
        remindersListViewModel.loadReminders()

        //THEN
        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            `is`(MockData.fakeNoReminderMessage)
        )
    }

    @Test
    fun test_loadReminders_showLoading() = runBlockingTest {
        //GIVEN
        val fakeDataSource = FakeDataSource(MockData.fakeReminders)
        val remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

        //WHEN
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        //THEN
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))

        //WHEN
        mainCoroutineRule.resumeDispatcher()

        //THEN
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
    }

    @After
    fun tearDown(){
        stopKoin()
    }
}