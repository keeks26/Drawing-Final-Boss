package com.keeks26.drawingfinalboss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keeks26.drawingfinalboss.data.CatalogHttpException
import com.keeks26.drawingfinalboss.data.DrawingLevel
import com.keeks26.drawingfinalboss.data.InvalidCatalogException
import com.keeks26.drawingfinalboss.data.PhotoCatalog
import com.keeks26.drawingfinalboss.data.ReferencePhoto
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogState(
    val level: DrawingLevel = DrawingLevel.LEVEL_ONE,
    val photos: List<ReferencePhoto> = emptyList(),
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val nextCursor: String? = null,
    val fromCache: Boolean = false
)

class CatalogViewModel(private val catalog: PhotoCatalog) : ViewModel() {
    private val mutableState = MutableStateFlow(CatalogState())
    val state = mutableState.asStateFlow()
    private var request: Job? = null

    init { refresh() }

    fun selectLevel(level: DrawingLevel) {
        if (level != mutableState.value.level) loadFirst(level)
    }

    fun refresh() = loadFirst(mutableState.value.level)

    private fun loadFirst(level: DrawingLevel) {
        request?.cancel()
        mutableState.value = CatalogState(level = level)
        request = viewModelScope.launch {
            try {
                val page = catalog.loadPage(level.difficulty)
                mutableState.value = CatalogState(
                    level = level, photos = page.photos, loading = false,
                    nextCursor = page.nextCursor, fromCache = page.fromCache
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                mutableState.value = CatalogState(level = level, loading = false, error = error.userMessage())
            }
        }
    }

    fun loadMore() {
        val current = mutableState.value
        val cursor = current.nextCursor ?: return
        if (current.loading || current.loadingMore) return
        mutableState.value = current.copy(loadingMore = true, error = null)
        request = viewModelScope.launch {
            try {
                val page = catalog.loadPage(current.level.difficulty, cursor)
                mutableState.value = current.copy(
                    photos = (current.photos + page.photos).distinctBy { it.id },
                    loadingMore = false, nextCursor = page.nextCursor, error = null
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                mutableState.value = current.copy(loadingMore = false, error = error.userMessage())
            }
        }
    }
}

private fun IOException.userMessage(): String = when (this) {
    is CatalogHttpException -> when (status) {
        401, 403 -> "References are temporarily unavailable. Please try again later."
        429 -> "Too many requests. Please wait a moment and try again."
        else -> "The reference library is unavailable. Please try again."
    }
    is InvalidCatalogException -> "The reference library could not be read. Please try again later."
    else -> "Could not connect. Check your internet connection and try again."
}
