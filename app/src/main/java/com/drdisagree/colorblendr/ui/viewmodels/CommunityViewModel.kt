package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.data.common.Utilities.getCommunityThemeRepository
import com.drdisagree.colorblendr.data.enums.CommunitySort
import com.drdisagree.colorblendr.data.models.CommunityTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Showcase double buffer: render the bucket persisted last session instantly,
// then restock it in the background for the next launch. Network fetch blocks
// UI only on first run (empty cache); null = still loading, empty = nothing
// to show (offline first run).
class CommunityViewModel : ViewModel() {

    private val repository = getCommunityThemeRepository()

    private val _showcase = MutableStateFlow<List<CommunityTheme>?>(null)
    val showcase = _showcase.asStateFlow()

    // null = loading; sorted by the screen.
    private val _allThemes = MutableStateFlow<List<CommunityTheme>?>(null)
    val allThemes = _allThemes.asStateFlow()

    private val _sort = MutableStateFlow(CommunitySort.UPVOTES)
    val sort = _sort.asStateFlow()

    init {
        viewModelScope.launch {
            var themes = repository.getBufferedShowcase()
            if (themes.isEmpty()) {
                themes = repository.fetchShowcaseNow(SHOWCASE_COUNT)
            }
            _showcase.value = themes

            repository.prepareNextShowcase(SHOWCASE_COUNT, themes.map { it.id })
        }

        viewModelScope.launch {
            // Cached list immediately, refreshed list when the fetch lands.
            _allThemes.value = repository.getThemes()
            if (repository.refreshIndex()) {
                _allThemes.value = repository.getThemes()
            }
        }
    }

    fun setSort(sort: CommunitySort) {
        _sort.value = sort
    }

    // Refetch the index when it has gone stale mid-session, so freshly merged
    // creations show up without an app restart.
    fun refreshIfStale() {
        viewModelScope.launch {
            if (repository.isStale(STALE_AFTER_MILLIS) && repository.refreshIndex()) {
                _allThemes.value = repository.getThemes()
            }
        }
    }

    // Cheap Room re-read so vote count edits show up when returning from the
    // details screen; no network.
    fun refreshFromCache() {
        viewModelScope.launch {
            repository.getThemes().takeIf { it.isNotEmpty() }?.let { themes ->
                _allThemes.value = themes
            }
            repository.getBufferedShowcase().takeIf { it.isNotEmpty() }?.let { themes ->
                _showcase.value = themes
            }
        }
    }

    companion object {
        private const val SHOWCASE_COUNT = 6
        private const val STALE_AFTER_MILLIS = 10L * 60 * 1000
    }
}
