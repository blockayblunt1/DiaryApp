package com.example.diaryapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diaryapp.databinding.RowNoteBinding
import com.example.diaryapp.model.Note

class NotesAdapter(
    private val onClick: (Note) -> Unit,
    private val onDelete: (Note) -> Unit,
    private val onExportOne: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.VH>() {

    private val data = mutableListOf<Note>()
    fun submit(list: List<Note>) { data.clear(); data.addAll(list); notifyDataSetChanged() }

    class VH(val b: RowNoteBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = RowNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }
    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val n = data[position]
        holder.b.tvTitle.text = n.title
        holder.b.tvDate.text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
            .format(java.util.Date(n.createdAt))
        holder.b.root.setOnClickListener { onClick(n) }
        holder.b.btnDelete.setOnClickListener { onDelete(n) }
        holder.b.btnExportOne.setOnClickListener { onExportOne(n) }
    }
}
