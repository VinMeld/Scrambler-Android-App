package com.example.scrambler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.scrambler.R
import android.widget.TextView
import android.content.Intent
import android.util.Log
import android.widget.Button
import com.example.scrambler.MenuActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.example.scrambler.PersonalLeaderboardActivity
import java.lang.StringBuilder
import java.util.*

class PersonalLeaderboardActivity : AppCompatActivity() {
    private var scores: MutableList<Int>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_leaderboard)
        val menu = findViewById<Button>(R.id.buttonMenuLeaderboard)
        val leaderboard = findViewById<TextView>(R.id.textViewLeaderboard)
        menu.setOnClickListener { startActivity(Intent(this@PersonalLeaderboardActivity, MenuActivity::class.java)) }
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val userID = firebaseUser.uid
        val db = FirebaseFirestore.getInstance()
        val user = db.collection("Users").document(userID)
        user.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userSnapShot = task.result
                if (userSnapShot != null) {
                    // Log.e(TAG, (String) userSnapShot.get("scores"));
                    @Suppress("UNCHECKED_CAST")
                    scores = userSnapShot["scores"] as MutableList<Int>?
                    scores?.sortDescending()
                    if (scores != null) {
                        val leaderboardText = StringBuilder()
                        for (i in scores!!.indices) {
                            leaderboardText.append(i + 1).append(". ").append(scores!![i]).append("\n")
                        }
                        leaderboard.text = leaderboardText
                    } else {
                        leaderboard.text = "You have not gotten any correct!"
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            } else {
                Log.d(TAG, "get failed with ", task.exception)
            }
        }
    }

    companion object {
        private const val TAG = "PersonalLeaderboard"
    }
}