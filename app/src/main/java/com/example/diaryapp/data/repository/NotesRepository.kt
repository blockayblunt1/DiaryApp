package com.example.diaryapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.diaryapp.data.local.NoteDao
import com.example.diaryapp.data.local.entities.NoteEntity
import com.example.diaryapp.data.local.entities.toDomainModel
import com.example.diaryapp.data.local.entities.toEntity
import com.example.diaryapp.model.Note
import com.example.diaryapp.model.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesRepository(private val noteDao: NoteDao) {
    
    fun getAllNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotesLiveData().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getAllNotesSorted(sortOption: SortOption): LiveData<List<Note>> {
        return noteDao.getAllNotesSorted(sortOption.sqlOrderBy).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun searchNotes(query: String): LiveData<List<Note>> {
        return noteDao.searchNotes(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getNoteById(id: Long): Note? {
        return withContext(Dispatchers.IO) {
            noteDao.getNoteById(id)?.toDomainModel()
        }
    }
    
    suspend fun insertNote(note: Note): Long {
        return withContext(Dispatchers.IO) {
            noteDao.insertNote(note.toEntity())
        }
    }
    
    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(note.toEntity())
        }
    }
    
    suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(note.toEntity())
        }
    }
    
    suspend fun deleteNoteById(id: Long) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteById(id)
        }
    }
    
    suspend fun getAllNotesForExport(): List<Note> {
        return withContext(Dispatchers.IO) {
            noteDao.getAllNotesOnce().map { it.toDomainModel() }
        }
    }
}