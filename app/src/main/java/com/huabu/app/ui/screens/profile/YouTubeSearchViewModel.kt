package com.huabu.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.remote.YouTubeApiService
import com.huabu.app.data.remote.YouTubeSearchItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

data class YouTubeSearchState(
    val results: List<YouTubeSearchItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class YouTubeSearchViewModel @Inject constructor(
    private val youTubeApiService: YouTubeApiService,
    @Named("youtubeApiKey") private val apiKey: String
) : ViewModel() {

    private val _state = MutableStateFlow(YouTubeSearchState())
    val state: StateFlow<YouTubeSearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String, categoryId: String = "10") {
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.value = YouTubeSearchState()
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = youTubeApiService.search(
                    query = query,
                    videoCategoryId = categoryId,
                    apiKey = apiKey
                )
                _state.value = YouTubeSearchState(results = response.items)
            } catch (e: Exception) {
                _state.value = YouTubeSearchState(error = e.message)
            }
        }
    }

    fun clear() {
        searchJob?.cancel()
        _state.value = YouTubeSearchState()
    }
}
