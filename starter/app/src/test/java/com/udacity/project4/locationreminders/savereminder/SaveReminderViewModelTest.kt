package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.MockData
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

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
import org.mockito.Mock
import org.mockito.Mockito

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun test_onClear_success() = runBlockingTest {
        //GIVEN
        val fakeDataSource = FakeDataSource()
        val saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        saveReminderViewModel.apply {
            reminderTitle.value = "Title"
            reminderDescription.value = "Description"
            reminderSelectedLocationStr.value = "LocationString"
            selectedPOI.value = PointOfInterest(LatLng(21.32323, 15.21323), "Title", "Description")
            latitude.value = 21.32323
            longitude.value = 15.21323
        }

        //WHEN
        saveReminderViewModel.onClear()

        //THEN
        MatcherAssert.assertThat(
            saveReminderViewModel.reminderTitle.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.reminderDescription.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.selectedPOI.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.latitude.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.longitude.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
    }

    @Test
    fun test_validateEnteredData_success() {
        validateEnteredData(
            reminder = MockData.normalReminderDTO,
            expectedResult = true,
            expectedMessageRedId = null
        )
    }

    @Test
    fun test_validateEnteredDataWithEmptyTitle_success() {
        validateEnteredData(
            reminder = MockData.emptyTitleReminderDTO,
            expectedResult = false,
            expectedMessageRedId = R.string.err_enter_title
        )
    }

    @Test
    fun test_validateEnteredDataWithEmptyLocation_success() {
        validateEnteredData(
            reminder = MockData.emptyLocationReminderDTO,
            expectedResult = false,
            expectedMessageRedId = R.string.err_select_location
        )
    }

    @Test
    fun test_saveReminder_success() {
        //GIVEN
        val fakeDataSource = FakeDataSource()
        val application: Application = Mockito.mock(Application::class.java)
        val saveReminderViewModel =
            SaveReminderViewModel(application, fakeDataSource)
        val normalDTO = MockData.normalReminderDTO
        val reminderData = ReminderDataItem(
            normalDTO.title,
            normalDTO.description,
            normalDTO.location,
            normalDTO.latitude,
            normalDTO.longitude,
            normalDTO.id
        )

        //WHEN
        Mockito.`when`(application.getString(R.string.reminder_saved))
            .thenReturn(MockData.fakeSaveReminderMessage)
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminderData)

        //THEN
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )

        //WHEN
        mainCoroutineRule.resumeDispatcher()

        //THEN
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            `is`(MockData.fakeSaveReminderMessage)
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.navigationCommand.getOrAwaitValue(),
            CoreMatchers.`is`(NavigationCommand.Back)
        )

    }

    private fun validateEnteredData(
        reminder: ReminderDTO,
        expectedResult: Boolean,
        expectedMessageRedId: Int?
    ) {
        //GIVEN
        val fakeDataSource = FakeDataSource()
        val saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        val reminderData = ReminderDataItem(
            reminder.title,
            reminder.description,
            reminder.location,
            reminder.latitude,
            reminder.longitude,
            reminder.id
        )


        //WHEN
        val result = saveReminderViewModel.validateEnteredData(reminderData)

        MatcherAssert.assertThat(result, CoreMatchers.`is`(expectedResult))
        if (!expectedResult)
            MatcherAssert.assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
                CoreMatchers.`is`(expectedMessageRedId)
            )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

}