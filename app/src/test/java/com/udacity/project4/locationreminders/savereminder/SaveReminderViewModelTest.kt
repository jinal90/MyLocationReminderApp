package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var mockApp: Application

    @Before
    fun setup() {
        stopKoin()
        MockitoAnnotations.initMocks(this)
        mockApp = ApplicationProvider.getApplicationContext()
        val dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(mockApp, dataSource)
    }

    @After
    fun teardown() {

    }

    @Test
    fun testValidateAndSaveReminder() = mainCoroutineRule.runBlockingTest {
        val title = "Title"
        val description = "Description"
        val location = "LocationName"
        val latitude = 1.1
        val longitude = 1.2

        val reminderData = ReminderDataItem(title, description, location, latitude, longitude)
        viewModel.validateAndSaveReminder(reminderData)
        assertThat(viewModel.reminderDataItem.value, `is`(reminderData))
    }

    @Test
    fun testShowLoadingLiveData() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()

        val title = "Title"
        val description = "Description"
        val location = "LocationName"
        val latitude = 1.1
        val longitude = 1.2

        val reminderData = ReminderDataItem(title, description, location, latitude, longitude)

        viewModel.validateAndSaveReminder(reminderData)
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test
    fun testShowToastLiveDataWhenTitleIsEmpty() = mainCoroutineRule.runBlockingTest {

        val title = ""
        val description = "Description"
        val location = "LocationName"
        val latitude = 1.1
        val longitude = 1.2

        val reminderData = ReminderDataItem(title, description, location, latitude, longitude)

        viewModel.validateAndSaveReminder(reminderData)
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun testShowToastLiveDataWhenLocationIsEmpty() = mainCoroutineRule.runBlockingTest {

        val title = "Title"
        val description = "Description"
        val location = ""
        val latitude = 1.1
        val longitude = 1.2

        val reminderData = ReminderDataItem(title, description, location, latitude, longitude)

        viewModel.validateAndSaveReminder(reminderData)
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }
}