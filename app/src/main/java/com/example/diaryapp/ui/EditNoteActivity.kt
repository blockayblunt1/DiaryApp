package com.example.diaryapp.ui

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.example.diaryapp.BaseActivity
import com.example.diaryapp.R
import com.example.diaryapp.data.local.DiaryDatabase
import com.example.diaryapp.data.repository.NotesRepository
import com.example.diaryapp.ui.edit.EditNoteViewModel
import com.example.diaryapp.ui.edit.EditNoteViewModelFactory

class EditNoteActivity : BaseActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etContent: TextInputEditText
    private var noteId: Long = 0L
    
    private val viewModel: EditNoteViewModel by viewModels {
        EditNoteViewModelFactory(
            NotesRepository(DiaryDatabase.getDatabase(this).noteDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        val btnSave = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave)

        noteId = intent.getLongExtra("id", 0L)
        
        // Load note if editing existing note
        viewModel.loadNote(noteId)
        
        // Observe note data
        viewModel.note.observe(this) { note ->
            note?.let {
                etTitle.setText(it.title)
                etContent.setText(it.content)
            }
        }
        
        // Observe save result
        viewModel.saveResult.observe(this) { success ->
            if (success) {
                finish()
            } else {
                etTitle.error = "Title required"
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            viewModel.saveNote(title, content, noteId)
        }
    }
}
