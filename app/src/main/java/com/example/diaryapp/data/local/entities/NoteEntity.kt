package com.example.diaryapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.diaryapp.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

// Extension functions to convert between Entity and Domain model
fun NoteEntity.toDomainModel(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt
    )
}