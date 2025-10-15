package com.example.diaryapp

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diaryapp.data.local.DiaryDatabase
import com.example.diaryapp.data.repository.NotesRepository
import com.example.diaryapp.databinding.ActivityMainBinding
import com.example.diaryapp.ExternalExport
import com.example.diaryapp.model.Note
import com.example.diaryapp.model.SortOption
import com.example.diaryapp.prefs.Settings
import com.example.diaryapp.ui.EditNoteActivity
import com.example.diaryapp.ui.SettingsActivity
import com.example.diaryapp.ui.main.MainViewModel
import com.example.diaryapp.ui.main.MainViewModelFactory

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NotesAdapter
    private lateinit var settings: Settings
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            NotesRepository(DiaryDatabase.getDatabase(this).noteDao()),
            Settings(this)
        )
    }

    private val requestStoragePerm = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* no-op; we always try export after this */ }
    
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { 
        // Recreate activity to apply font changes
        recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settings = Settings(this)
        adapter = NotesAdapter(
            onClick = { openEdit(it.id) },
            onDelete = { confirmDelete(it.id) },
            onExportOne = { exportSingleToExternal(it) }
        )

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.fabAdd.setOnClickListener { openEdit(0L) }
        binding.btnSettings.setOnClickListener {
            settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
        }
        binding.btnExportAll.setOnClickListener { exportAllToExternal() }
        binding.btnSort.setOnClickListener { showSortDialog() }

        binding.search.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(q: String?): Boolean { 
                viewModel.updateSearchQuery(q ?: "")
                return true 
            }
            override fun onQueryTextChange(q: String?): Boolean { 
                viewModel.updateSearchQuery(q ?: "")
                return true 
            }
        })

        // Observe notes from ViewModel
        viewModel.notes.observe(this) { notes ->
            adapter.submit(notes)
            binding.empty.text = if (notes.isEmpty()) "No notes yet" else ""
        }
    }
    

    private fun openEdit(id: Long) {
        startActivity(Intent(this, EditNoteActivity::class.java).putExtra("id", id))
    }

    private fun confirmDelete(id: Long) {
        AlertDialog.Builder(this)
            .setMessage("Delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteNote(id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** External Storage: export a single note to Downloads */
    private fun exportSingleToExternal(note: Note) {
        if (Build.VERSION.SDK_INT <= 28) {
            requestStoragePerm.launch(arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ))
        }
        val text = "# ${note.title}\n${note.content}\n(${java.util.Date(note.createdAt)})"
        val filename = "note_${note.title.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${note.id}.txt"
        val uri = ExternalExport.saveToDownloads(this, filename, text)
        AlertDialog.Builder(this)
            .setTitle("Exported note")
            .setMessage("Saved as $filename\n$uri")
            .setPositiveButton("OK", null)
            .show()
    }

    /** External Storage: export all notes to Downloads (scoped-storage safe) */
    private fun exportAllToExternal() {
        if (Build.VERSION.SDK_INT <= 28) {
            requestStoragePerm.launch(arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ))
        }
        viewModel.getAllNotesForExport { notes ->
            val text = notes.joinToString("\n\n") {
                "# ${it.title}\n${it.content}\n(${java.util.Date(it.createdAt)})"
            }
            val name = "diary_export_${System.currentTimeMillis()}.txt"
            val uri = ExternalExport.saveToDownloads(this, name, text)
            AlertDialog.Builder(this)
                .setTitle("Exported all notes")
                .setMessage("Saved as $name\n$uri")
                .setPositiveButton("OK", null)
                .show()
        }
    }
    
    private fun showSortDialog() {
        val options = SortOption.values()
        val currentIndex = viewModel.currentSortOption.ordinal
        
        AlertDialog.Builder(this)
            .setTitle("Sort by")
            .setSingleChoiceItems(
                options.map { it.displayName }.toTypedArray(),
                currentIndex
            ) { dialog, which ->
                viewModel.updateSortOption(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
