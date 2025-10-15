package com.example.diaryapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.diaryapp.data.local.entities.NoteEntity

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotesLiveData(): LiveData<List<NoteEntity>>
    
    @Query("SELECT * FROM notes ORDER BY :orderBy")
    fun getAllNotesSorted(orderBy: String): LiveData<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?
    
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchNotes(query: String): LiveData<List<NoteEntity>>
    
    @Insert
    suspend fun insertNote(note: NoteEntity): Long
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
    
    @Query("SELECT * FROM notes")
    suspend fun getAllNotesOnce(): List<NoteEntity>
}