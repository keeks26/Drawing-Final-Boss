package com.keeks26.drawingfinalboss

import com.keeks26.drawingfinalboss.data.DrawingLevel
import com.keeks26.drawingfinalboss.data.PhotoCatalog
import com.keeks26.drawingfinalboss.data.PhotoPage
import com.keeks26.drawingfinalboss.data.ReferencePhoto
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun `switching levels cancels a stale response`() = runTest(dispatcher) {
        val catalog = object : PhotoCatalog {
            override suspend fun loadPage(difficulty: Int, cursor: String?): PhotoPage {
                delay(if (difficulty == 1) 1000 else 1)
                return PhotoPage(listOf(photo("level-$difficulty", difficulty)))
            }
        }
        val model = CatalogViewModel(catalog)
        model.selectLevel(DrawingLevel.FINAL_BOSS)
        advanceUntilIdle()
        assertEquals(DrawingLevel.FINAL_BOSS, model.state.value.level)
        assertEquals(3, model.state.value.photos.single().difficulty)
        assertFalse(model.state.value.loading)
    }

    @Test fun `failed next page preserves photos and cursor for retry`() = runTest(dispatcher) {
        var failNext = true
        var calls = 0
        val catalog = object : PhotoCatalog {
            override suspend fun loadPage(difficulty: Int, cursor: String?): PhotoPage {
                calls++
                if (cursor == null) return PhotoPage(listOf(photo("first")), "next")
                delay(1)
                if (failNext) throw IOException("offline")
                return PhotoPage(listOf(photo("second")))
            }
        }
        val model = CatalogViewModel(catalog)
        advanceUntilIdle()
        model.loadMore()
        model.loadMore() // Repeated taps should not duplicate requests.
        advanceUntilIdle()
        assertEquals(2, calls)
        assertEquals("first", model.state.value.photos.single().id)
        assertEquals("next", model.state.value.nextCursor)
        assertNotNull(model.state.value.error)
        failNext = false
        model.loadMore()
        advanceUntilIdle()
        assertEquals(listOf("first", "second"), model.state.value.photos.map { it.id })
        assertNull(model.state.value.error)
        assertNull(model.state.value.nextCursor)
    }

    private fun photo(id: String, difficulty: Int = 1) = ReferencePhoto(id, "level/1.jpg", difficulty)
}
