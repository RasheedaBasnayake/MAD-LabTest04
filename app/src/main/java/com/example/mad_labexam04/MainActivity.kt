package com.example.mad_labexam04

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mad_labexam04.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NoteDatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var allNotes: List<Note>
    private val mainScope = MainScope()
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NoteDatabaseHelper(this)
        allNotes = db.getAllNotes()
        notesAdapter = NotesAdapter(allNotes, this)

        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }

        // Search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cancel the previous search job if still running
                searchJob?.cancel()
                // Start a new search job with a delay of 300 milliseconds
                searchJob = mainScope.launch {
                    delay(300)
                    filterNotes(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterNotes(query: String) {
        val filteredNotes = mutableListOf<Note>()
        if (query.isBlank()) {
            // If query is empty, display all notes
            filteredNotes.addAll(allNotes)
        } else {
            for (note in allNotes) {
                if (note.title.contains(query, true) || note.content.contains(query, true)) {
                    filteredNotes.add(note)
                }
            }
        }
        notesAdapter.refreshData(filteredNotes)
    }

    override fun onDestroy() {
        // Cancel the search job when the activity is destroyed
        searchJob?.cancel()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        notesAdapter.refreshData(db.getAllNotes())
    }
}
