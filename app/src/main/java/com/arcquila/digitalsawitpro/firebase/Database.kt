package com.arcquila.digitalsawitpro.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.security.auth.callback.Callback

object Database {
    val database = Firebase.database
    val resultRef = database.getReference("result")

    fun insertData(string: String) {
        resultRef.setValue(string)
    }




}