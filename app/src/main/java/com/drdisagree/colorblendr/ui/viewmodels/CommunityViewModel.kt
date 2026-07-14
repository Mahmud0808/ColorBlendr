package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.data.common.Utilities.getCommunityThemeRepository
import com.drdisagree.colorblendr.data.enums.CommunitySort
import com.drdisagree.colorblendr.data.models.CommunityTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Cache-first: emit the Room cache instantly, refresh from the index in the
// background. null = still loading, empty = nothing to show (offline first
// run). Showcase slices top-voted cards from this same list.
class CommunityViewModel : ViewModel() {

    private val repository = getCommunityThemeRepository()

    // null = loading; sorted by the consuming screen.
    private val _allThemes = MutableStateFlow<List<CommunityTheme>?>(null)
    val allThemes = _allThemes.asStateFlow()

    private val _sort = MutableStateFlow(CommunitySort.TRENDING)
    val sort = _sort.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getThemes().takeIf { it.isNotEmpty() }?.let {
                _allThemes.value = it
            }
            if (repository.refreshIndex()) {
                _allThemes.value = repository.getThemes()
            } else if (_allThemes.value == null) {
                _allThemes.value = emptyList()
            }
        }
    }

    fun setSort(sort: CommunitySort) {
        _sort.value = sort
    }

    // Refetch when the index has gone stale mid-session, so freshly merged
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
            repository.getThemes().takeIf { it.isNotEmpty() }?.let {
                _allThemes.value = it
            }
        }
    }

    companion object {
        private const val STALE_AFTER_MILLIS = 10L * 60 * 1000
    }
}
