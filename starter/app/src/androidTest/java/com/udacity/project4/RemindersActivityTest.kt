package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.ToastMatcher
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var activityScenario: ActivityScenario<RemindersActivity>
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    //    TODO: add End to End testing to the app
    @Test
    fun test_addReminder_success() {
        val data = MockData.normalReminderDTO
        activityScenario = launchActivity()
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(
            ViewMatchers.withId(R.id.addReminderFAB)
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withId(
                R.id.reminderTitle
            )
        ).perform(
            ViewActions.typeText(data.title),
            ViewActions.closeSoftKeyboard()
        )

        Espresso.onView(
            ViewMatchers.withId(
                R.id.reminderDescription
            )
        ).perform(
            ViewActions.typeText(data.description),
            ViewActions.closeSoftKeyboard()
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selectLocation)
        ).perform(
            ViewActions.click()
        )

        Thread.sleep(3000)

        Espresso.onView(
            ViewMatchers.withId(R.id.map)
        ).perform(
            ViewActions.longClick()
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.btn_save)
        ).perform(
            ViewActions.click()
        )

        Thread.sleep(2000)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.reminderTitle),
                withText(data.title)
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.reminderDescription),
                withText(data.description)
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.saveReminder)
        ).perform(
            ViewActions.click()
        )

        Thread.sleep(1000)

        Espresso.onView(
            ViewMatchers.withText(R.string.reminder_saved)
        ).inRoot(
            ToastMatcher().apply {
                matches(ViewMatchers.isDisplayed())
            }
        )

        Espresso.onView(
            withText(data.title)
        ).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )

        Espresso.onView(
            withText(data.description)
        ).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
    }

    @Test
    fun test_addReminderWithEmptyTitle_error() {
        val data = MockData.emptyTitleReminderDTO

        activityScenario = launchActivity()
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(
            ViewMatchers.withId(
                R.id.addReminderFAB
            )
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.reminderTitle)
        ).perform(
            ViewActions.typeText(data.title),
            ViewActions.closeSoftKeyboard()
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.reminderDescription)
        ).perform(
            ViewActions.typeText(data.description),
            ViewActions.closeSoftKeyboard()
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selectLocation)
        ).perform(
            ViewActions.click()
        )

        Thread.sleep(3000)

        Espresso.onView(
            ViewMatchers.withId(R.id.map)
        ).perform(
            ViewActions.longClick()
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.btn_save)
        ).perform(
            ViewActions.click()
        )

        Thread.sleep(2000)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.reminderTitle),
                withText(data.title)
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.reminderDescription),
                withText(data.description)
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.saveReminder)
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText(
                R.string.err_enter_title
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        )
    }

}
