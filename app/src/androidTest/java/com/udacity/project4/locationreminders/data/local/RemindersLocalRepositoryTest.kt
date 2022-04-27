package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setupRepository() {
        application = ApplicationProvider.getApplicationContext()
        remindersDatabase = Room.inMemoryDatabaseBuilder(application, RemindersDatabase::class.java)
            .allowMainThreadQueries().build()

        remindersLocalRepository =
            RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Main)
    }

    @Test
    fun testSaveAndGetReminder() = runBlocking {
        val reminderExpected = ReminderDTO(
            "Title", "Description",
            "Location", 1.1, 1.2
        )

        remindersLocalRepository.saveReminder(reminderExpected)
        val reminderReturned =
            remindersLocalRepository.getReminder(reminderExpected.id) as Result.Success

        assertThat(reminderReturned.data, `is`(reminderExpected))
    }

    @Test
    fun testErrorFetchingReminder() = runBlocking {

        val randomId = "someId"
        val reminderReturned = remindersLocalRepository.getReminder(randomId) as Result.Error

        assertThat(reminderReturned.message, `is`("Reminder not found!"))
    }

    @Test
    fun testDeleteAllReminders() = runBlocking {
        val reminderExpected = ReminderDTO(
            "Title", "Description",
            "Location", 1.1, 1.2
        )

        remindersLocalRepository.saveReminder(reminderExpected)
        remindersLocalRepository.deleteAllReminders()
        val reminderReturned =
            remindersLocalRepository.getReminder(reminderExpected.id) as Result.Error

        assertThat(reminderReturned.message, `is`("Reminder not found!"))
    }
}