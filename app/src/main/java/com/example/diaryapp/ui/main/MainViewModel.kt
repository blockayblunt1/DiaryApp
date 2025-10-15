package com.example.diaryapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.repository.NotesRepository
import com.example.diaryapp.model.Note
import com.example.diaryapp.model.SortOption
import com.example.diaryapp.prefs.Settings
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: NotesRepository,
    private val settings: Settings
) : ViewModel() {
    
    private val _searchQuery = MutableLiveData<String>()
    private val _sortOption = MutableLiveData<SortOption>()
    
    init {
        _sortOption.value = settings.sortOption
        _searchQuery.value = ""
    }
    
    val notes: LiveData<List<Note>> = _searchQuery.switchMap { query ->
        when {
            query.isNullOrBlank() -> {
                _sortOption.switchMap { sortOption ->
                    repository.getAllNotesSorted(sortOption)
                }
            }
            else -> repository.searchNotes(query)
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
        settings.sortOption = sortOption
        // Trigger refresh by re-emitting current search query
        _searchQuery.value = _searchQuery.value
    }
    
    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
        }
    }
    
    fun getAllNotesForExport(callback: (List<Note>) -> Unit) {
        viewModelScope.launch {
            val notes = repository.getAllNotesForExport()
            callback(notes)
        }
    }
    
    val currentSortOption: SortOption
        get() = settings.sortOption
}

class MainViewModelFactory(
    private val repository: NotesRepository,
    private val settings: Settings
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, settings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}