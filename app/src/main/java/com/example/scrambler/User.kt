package com.example.scrambler

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class User(var username: String? = "", var email: String? = "") {
    fun addUuid(UUID: String?) {
        val user: MutableMap<String, Any?> = HashMap()
        user["username"] = username
        user["scores"] = listOf(0)
        val db = FirebaseFirestore.getInstance()
        if (UUID != null) {
            db.collection("Users").document(UUID)
                .set(user)
                .addOnSuccessListener {
                    Log.d(
                        "Main Activity",
                        "DocumentSnapshot successfully written!"
                    )
                }
                .addOnFailureListener { e -> Log.w("Main Activity", "Error writing document", e) }
        }
    }
}