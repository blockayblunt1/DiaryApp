package com.example.diaryapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.diaryapp.BaseActivity
import com.example.diaryapp.R
import com.example.diaryapp.data.sqlite.DiaryDbHelper
import com.example.diaryapp.model.Note

class EditNoteActivity : BaseActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnSave: Button
    private lateinit var db: DiaryDbHelper
    private var noteId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        db = DiaryDbHelper(this)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnSave = findViewById(R.id.btnSave)

        noteId = intent.getLongExtra("id", 0L)
        if (noteId != 0L) {
            db.getById(noteId)?.let {
                etTitle.setText(it.title)
                etContent.setText(it.content)
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            if (title.isEmpty()) { etTitle.error = "Title required"; return@setOnClickListener }

            if (noteId == 0L) {
                db.insert(Note(title = title, content = content))
            } else {
                db.update(Note(id = noteId, title = title, content = content))
            }
            finish()
        }
    }
}
