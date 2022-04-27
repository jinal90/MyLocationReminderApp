package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

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
        stopKoin()
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
        startKoin {
            modules(listOf(myModule))
        }
        repository = get().koin.get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun test_firstLaunch_shouldShowEmptyReminderList() {

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withText("No Data")).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches((isDisplayed())))
        onView(withId(R.id.reminderDescription)).check(matches((isDisplayed())))
        onView(withText("Reminder Location")).check(matches((isDisplayed())))
    }

    @Test
    fun test_addReminderFabClick_navigatesToAddReminderScreen() {

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches((isDisplayed())))
        onView(withId(R.id.reminderDescription)).check(matches((isDisplayed())))
        onView(withText("Reminder Location")).check(matches((isDisplayed())))
    }

    @Test
    fun test_ReminderListDisplayed() {

        val mockReminder1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            1.1,
            1.2
        )
        runBlocking {
            repository.saveReminder(mockReminder1)
        }
        val mockReminder2 = ReminderDTO(
            "Title2",
            "Description2",
            "Location2",
            2.1,
            2.2
        )
        runBlocking {
            repository.saveReminder(mockReminder2)
        }
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withText("No Data")).check(matches(not(isDisplayed())))

        onView(withText("Title1")).check(matches(isDisplayed()))
        onView(withText("Description1")).check(matches(isDisplayed()))
        onView(withText("Location1")).check(matches(isDisplayed()))

        onView(withText("Title2")).check(matches(isDisplayed()))
        onView(withText("Description2")).check(matches(isDisplayed()))
        onView(withText("Location2")).check(matches(isDisplayed()))
    }

}