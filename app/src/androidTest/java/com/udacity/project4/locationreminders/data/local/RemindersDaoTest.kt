package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
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

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun setupDatabase() {
        application = ApplicationProvider.getApplicationContext()
        remindersDatabase = Room.inMemoryDatabaseBuilder(application, RemindersDatabase::class.java)
            .allowMainThreadQueries().build()
    }

    @Before
    fun initDb() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        remindersDatabase.close()
    }

    @Test
    fun testSaveAndGetReminder() = runBlocking {
        val reminderExpected = ReminderDTO(
            "Title", "Description",
            "Location", 1.1, 1.2
        )

        remindersDatabase.reminderDao().saveReminder(reminderExpected)
        val reminderReturned =
            remindersDatabase.reminderDao().getReminderById(reminderExpected.id)

        assertThat(reminderReturned, `is`(reminderExpected))
    }

    @Test
    fun testErrorFetchingReminder() = runBlocking {

        val randomId = "someId"
        val reminderReturned = remindersDatabase.reminderDao().getReminderById(randomId)
        assertNull(reminderReturned)
    }

    @Test
    fun testDeleteAllReminders() = runBlocking {
        val reminderExpected = ReminderDTO(
            "Title", "Description",
            "Location", 1.1, 1.2
        )

        remindersDatabase.reminderDao().saveReminder(reminderExpected)
        remindersDatabase.reminderDao().deleteAllReminders()
        val reminderReturned =
            remindersDatabase.reminderDao().getReminders()

        assertThat(reminderReturned, `is`(emptyList()))
    }
}