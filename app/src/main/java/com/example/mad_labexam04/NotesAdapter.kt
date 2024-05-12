package com.example.mad_labexam04

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class NotesAdapter(private var notes: List<Note>, context: Context) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>(), Filterable {

    private val db: NoteDatabaseHelper = NoteDatabaseHelper(context)
    private var filteredNotes: List<Note> = notes

    // Implement Filterable interface methods
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<Note>()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(notes)
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                    for (note in notes) {
                        if (note.title.lowercase(Locale.getDefault()).contains(filterPattern) ||
                            note.content.lowercase(Locale.getDefault()).contains(filterPattern)
                        ) {
                            filteredList.add(note)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredNotes = results?.values as List<Note>
                notifyDataSetChanged()
            }
        }
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val updateButton: ImageView = itemView.findViewById(R.id.updateButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int = filteredNotes.size

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = filteredNotes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content

        holder.updateButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateNoteActivity::class.java).apply {
                putExtra("note_id", note.id)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(holder.itemView.context)
                .setBackground(holder.itemView.context.getDrawable(R.drawable.dialog_box))
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes") { dialog, _ ->
                    db.deleteNote(note.id)
                    refreshData(db.getAllNotes())
                    Toast.makeText(holder.itemView.context, "Note Deleted!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData(newNotes: List<Note>) {
        notes = newNotes
        filteredNotes = newNotes
        notifyDataSetChanged()
    }
}
