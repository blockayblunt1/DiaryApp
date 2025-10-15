package com.example.diaryapp.data.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.diaryapp.model.Note
import com.example.diaryapp.model.SortOption

class DiaryDbHelper(context: Context) :
    SQLiteOpenHelper(context, "diary.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE notes(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
    }
    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS notes"); onCreate(db)
    }

    fun insert(note: Note): Long {
        val cv = ContentValues().apply {
            put("title", note.title); put("content", note.content); put("createdAt", note.createdAt)
        }
        return writableDatabase.insert("notes", null, cv)
    }

    fun update(note: Note) {
        val cv = ContentValues().apply {
            put("title", note.title); put("content", note.content)
        }
        writableDatabase.update("notes", cv, "id=?", arrayOf(note.id.toString()))
    }

    fun delete(id: Long) { writableDatabase.delete("notes", "id=?", arrayOf(id.toString())) }

    fun getAll(sortOption: SortOption = SortOption.DATE_NEWEST): List<Note> {
        val list = mutableListOf<Note>()
        readableDatabase.rawQuery(
            "SELECT id,title,content,createdAt FROM notes ORDER BY ${sortOption.sqlOrderBy}", null
        ).use { c ->
            while (c.moveToNext()) {
                list += Note(
                    id = c.getLong(0),
                    title = c.getString(1),
                    content = c.getString(2),
                    createdAt = c.getLong(3)
                )
            }
        }
        return list
    }

    fun getById(id: Long): Note? =
        readableDatabase.rawQuery(
            "SELECT id,title,content,createdAt FROM notes WHERE id=? LIMIT 1",
            arrayOf(id.toString())
        ).use { c ->
            if (c.moveToFirst()) Note(
                id = c.getLong(0),
                title = c.getString(1),
                content = c.getString(2),
                createdAt = c.getLong(3)
            ) else null
        }
}
