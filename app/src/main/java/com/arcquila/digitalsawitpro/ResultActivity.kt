package com.arcquila.digitalsawitpro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.arcquila.digitalsawitpro.databinding.ActivityResultBinding
import com.arcquila.digitalsawitpro.firebase.Database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ResultActivity : AppCompatActivity() {

    private lateinit var _binding : ActivityResultBinding
    private val binding get() = _binding

    companion object{
        var EXTRA_RESULT = "EXTRA_RESULT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Database.resultRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue().toString()
                binding.result.text = value
                Log.d("TAG", "Value is $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }

        })
    }
}