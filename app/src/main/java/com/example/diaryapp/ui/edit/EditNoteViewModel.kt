package com.example.diaryapp.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.repository.NotesRepository
import com.example.diaryapp.model.Note
import kotlinx.coroutines.launch

class EditNoteViewModel(
    private val repository: NotesRepository
) : ViewModel() {
    
    private val _note = MutableLiveData<Note?>()
    val note: LiveData<Note?> = _note
    
    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadNote(noteId: Long) {
        if (noteId == 0L) {
            _note.value = null // New note
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val loadedNote = repository.getNoteById(noteId)
                _note.value = loadedNote
            } catch (e: Exception) {
                _note.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun saveNote(title: String, content: String, noteId: Long = 0L) {
        if (title.isBlank()) {
            _saveResult.value = false
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (noteId == 0L) {
                    // Insert new note
                    val newNote = Note(
                        title = title.trim(),
                        content = content.trim(),
                        createdAt = System.currentTimeMillis()
                    )
                    repository.insertNote(newNote)
                } else {
                    // Update existing note
                    val updatedNote = Note(
                        id = noteId,
                        title = title.trim(),
                        content = content.trim(),
                        createdAt = _note.value?.createdAt ?: System.currentTimeMillis()
                    )
                    repository.updateNote(updatedNote)
                }
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class EditNoteViewModelFactory(
    private val repository: NotesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditNoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}