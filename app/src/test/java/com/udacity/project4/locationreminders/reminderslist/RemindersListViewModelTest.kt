package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var mockApp: Application
    private lateinit var dataSource: FakeDataSource

    @Before
    fun setup() {
        stopKoin()
        MockitoAnnotations.initMocks(this)
        mockApp = ApplicationProvider.getApplicationContext()
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(mockApp, dataSource)
    }

    @Test
    fun testShowLoadingLiveData() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), Matchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), Matchers.`is`(false))
    }

    @Test
    fun testLoadReminders() = mainCoroutineRule.runBlockingTest {
        val title = "Title"
        val description = "Description"
        val location = "LocationName"
        val latitude = 1.1
        val longitude = 1.2

        val reminderData = ReminderDTO(title, description, location, latitude, longitude)
        dataSource.saveReminder(reminderData)
        viewModel.loadReminders()
        val reminderDataItem =
            ReminderDataItem(title, description, location, latitude, longitude, reminderData.id)
        val reminderList: MutableList<ReminderDataItem> = mutableListOf()
        reminderList.add(reminderDataItem)
        MatcherAssert.assertThat(viewModel.remindersList.value, Matchers.`is`(reminderList))
    }

    @Test
    fun testSnackbarErrorMessage() = mainCoroutineRule.runBlockingTest {
        dataSource.errorScenario = true
        viewModel.loadReminders()
        MatcherAssert.assertThat(
            viewModel.showSnackBar.value,
            Matchers.`is`("Error loading reminders.")
        )
    }

}