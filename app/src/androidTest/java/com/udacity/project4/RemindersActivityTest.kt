package com.udacity.project4


import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.base.DataBindingViewHolder
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
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

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        unregister(dataBindingIdlingResource)
    }

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

    @Test
    fun test_saveReminderFlow() {

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withText("Location Reminders")).check(matches(isDisplayed()))
        onView(withText("No Data")).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Test Title"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Test Description"))
        onView(withText("Reminder Location")).check(matches((isDisplayed())))
            .perform(click())
        onView(withText("OK")).perform(click())
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.btnConfirm)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    `is`(
                        getActivity(appContext)?.window?.decorView
                    )
                )
            )
        ).check(
            matches(
                isDisplayed()
            )
        )

        onView(withText("Test Title")).check(matches(isDisplayed()))
    }

    @Test
    fun test_titleAndDescriptionEmptyToast() {

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withText("Reminder Location")).check(matches((isDisplayed())))
            .perform(click())

        onView(withText(R.string.enter_title_description_message)).inRoot(
            withDecorView(
                not(
                    `is`(
                        getActivity(appContext)?.window?.decorView
                    )
                )
            )
        ).check(
            matches(
                isDisplayed()
            )
        )
    }

    @Test
    fun test_oneReminderDisplayed() {

        val mockReminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            1.1,
            1.2
        )
        runBlocking {
            repository.saveReminder(mockReminder)
        }

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withText("Location Reminders")).check(matches(isDisplayed()))
        onView(withText("No Data")).check(matches(not(isDisplayed())))
        onView(withText("Title")).check(matches(isDisplayed()))
        onView(withText("Description")).check(matches(isDisplayed()))
        onView(withText("Location")).check(matches(isDisplayed()))
    }

    @Test
    fun test_reminderDetailScreenDisplayed() {

        val mockReminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            1.1,
            1.2
        )
        runBlocking {
            repository.saveReminder(mockReminder)
        }

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        onView(withId(R.id.reminderssRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<DataBindingViewHolder<ReminderDataItem>>(
                    0,
                    click()
                )
            )
        onView(withText("Reminder title: Title")).check(matches(isDisplayed()))
        onView(withText("Description: Description")).check(matches(isDisplayed()))
        onView(withText("Reminder set for location: Location")).check(matches(isDisplayed()))
        onView(withText("Located at: (1.1, 1.2)")).check(matches(isDisplayed()))
    }

}
