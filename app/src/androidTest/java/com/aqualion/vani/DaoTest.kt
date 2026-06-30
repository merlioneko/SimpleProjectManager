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
import org.junit.Assert.*


@RunWith(AndroidJUnit4::class)
class ProjectRepositoryTestUseCase {

    lateinit var projectRepository: ProjectRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appDatabase = AppDatabaseHelper(context).getDatabaseInMemory()
        projectRepository = ProjectRepositoryImpl(appDatabase)
    }

    @Test
    fun testNestedProject() = runTest {
        // 1. プロジェクトを3つ作成
        val time = LocalDateTime.now().toString()
        val p1 = Project(name = "Parent 1", createdAt = time, updatedAt = time)
        val p2 = Project(name = "Parent 2", createdAt = time, updatedAt = time)
        val child = Project(name = "Shared Child", createdAt = time, updatedAt = time)

        projectRepository.addProjects(listOf(p1, p2, child))
        
        val allProjects = projectRepository.getProjects().first()
        val p1Id = allProjects.find { it.name == "Parent 1" }!!.id
        val p2Id = allProjects.find { it.name == "Parent 2" }!!.id
        val childId = allProjects.find { it.name == "Shared Child" }!!.id

        // 2. 関係性を登録 (Child は P1 と P2 両方に属する)
        projectRepository.addProjectRelation(p1Id, childId)
        projectRepository.addProjectRelation(p2Id, childId)

        // 3. P1 の詳細を取得して Child が含まれているか確認
        val p1Detail = projectRepository.getProjectDetail(p1Id).first()
        assertNotNull(p1Detail)
        assertEquals(1, p1Detail!!.subProjects.size)
        assertEquals("Shared Child", p1Detail.subProjects[0].name)

        // 4. P2 の詳細を取得して Child が含まれているか確認
        val p2Detail = projectRepository.getProjectDetail(p2Id).first()
        assertNotNull(p2Detail)
        assertEquals(1, p2Detail!!.subProjects.size)
        assertEquals("Shared Child", p2Detail.subProjects[0].name)
    }
}
