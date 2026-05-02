package com.huabu.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.remote.GiphyApiService
import com.huabu.app.data.remote.GiphyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

data class GifSearchState(
    val results: List<GiphyResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = ""
)

@HiltViewModel
class GifSearchViewModel @Inject constructor(
    private val giphyApiService: GiphyApiService,
    @Named("giphyApiKey") private val apiKey: String
) : ViewModel() {

    private val _state = MutableStateFlow(GifSearchState())
    val state: StateFlow<GifSearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTrending()
    }

    fun loadTrending() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, query = "")
            try {
                val response = giphyApiService.trending(apiKey = apiKey)
                _state.value = GifSearchState(results = response.data)
            } catch (e: Exception) {
                _state.value = GifSearchState(error = e.message)
            }
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        _state.value = _state.value.copy(query = query)
        if (query.isBlank()) {
            loadTrending()
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = giphyApiService.search(query = query, apiKey = apiKey)
                _state.value = _state.value.copy(results = response.data, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
