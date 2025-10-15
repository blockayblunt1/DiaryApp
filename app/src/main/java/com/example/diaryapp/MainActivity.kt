package com.example.diaryapp

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diaryapp.data.sqlite.DiaryDbHelper
import com.example.diaryapp.databinding.ActivityMainBinding
import com.example.diaryapp.ExternalExport
import com.example.diaryapp.model.Note
import com.example.diaryapp.model.SortOption
import com.example.diaryapp.prefs.Settings
import com.example.diaryapp.ui.EditNoteActivity
import com.example.diaryapp.ui.SettingsActivity

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DiaryDbHelper
    private lateinit var adapter: NotesAdapter
    private lateinit var settings: Settings

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

        db = DiaryDbHelper(this)
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
            override fun onQueryTextSubmit(q: String?): Boolean { loadNotes(q); return true }
            override fun onQueryTextChange(q: String?): Boolean { loadNotes(q); return true }
        })

        loadNotes()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh the notes list when returning from EditNoteActivity
        loadNotes(binding.search.query?.toString())
    }

    private fun loadNotes(query: String? = null) {
        val list = db.getAll(settings.sortOption).filter { n ->
            query.isNullOrBlank() || n.title.contains(query, true)
        }
        adapter.submit(list)
        binding.empty.text = if (list.isEmpty()) "No notes yet" else ""
    }

    private fun openEdit(id: Long) {
        startActivity(Intent(this, EditNoteActivity::class.java).putExtra("id", id))
    }

    private fun confirmDelete(id: Long) {
        AlertDialog.Builder(this)
            .setMessage("Delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                db.delete(id); loadNotes(binding.search.query?.toString())
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
        val text = db.getAll().joinToString("\n\n") {
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
    
    private fun showSortDialog() {
        val options = SortOption.values()
        val currentIndex = settings.sortOption.ordinal
        
        AlertDialog.Builder(this)
            .setTitle("Sort by")
            .setSingleChoiceItems(
                options.map { it.displayName }.toTypedArray(),
                currentIndex
            ) { dialog, which ->
                settings.sortOption = options[which]
                loadNotes(binding.search.query?.toString())
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
