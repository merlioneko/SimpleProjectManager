package com.aqualion.vani

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.domain.Project
import org.junit.Before
import org.junit.runner.RunWith
import java.time.LocalDateTime
import android.content.Context
import android.util.Log
import com.aqualion.vani.data.AppDatabaseHelper
import com.aqualion.vani.domain.ProjectRepository
import com.aqualion.vani.data.ProjectRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test


@RunWith(AndroidJUnit4::class)
class ProjectRepositoryTestUseCase {

    lateinit var projectRepository: ProjectRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appDatabase = AppDatabaseHelper(context).getDatabaseInMemory()
        projectRepository = ProjectRepositoryImpl(appDatabase)
    }

    private val _id = 999
    private val testProject = Project(
        id=_id,
        name="test",
        createdAt = LocalDateTime.now().toString(),
        updatedAt = LocalDateTime.now().toString()
    )
    private val testNote = Note(
        id=_id,
        name="test",
        value="test",
        projectId=_id,
        createdAt = LocalDateTime.now().toString(),
        updatedAt = LocalDateTime.now().toString()
    )

    @Test
    fun insertAndRead() {
        insert()
        runTest {
            getRecord()
        }
    }

    fun insert() {
        val testDetail = ProjectDetail(
            project = testProject,
            notes = listOf(testNote)
        )
        projectRepository.saveProjectDetail(testDetail)
    }

    suspend fun getRecord() {
        projectRepository.getProjectDetail(_id).first {
            Log.d("test", it.toString())
            true
        }
    }
}