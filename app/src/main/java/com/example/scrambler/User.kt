package com.example.scrambler

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import java.util.HashMap

class User {
    var username: String? = null
    var email: String? = null

    constructor() {}

    val TAG = "MainActivity"

    constructor(username: String?, email: String?) {
        this.username = username
        this.email = email
    }

    fun setUuid(UUID: String?) {
        val user: MutableMap<String, Any?> = HashMap()
        user["username"] = username
        user["scores"] = listOf(0)
        val db = FirebaseFirestore.getInstance()
        if (UUID != null) {
            db.collection("Users").document(UUID)
                .set(user)
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }
    }
}