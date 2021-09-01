package com.example.scrambler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.example.scrambler.Utils.Scrambler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.lang.StringBuilder
import java.util.*

class PersonalLeaderboardActivity : AppCompatActivity(), View.OnClickListener {
    private var globalLeaderboard: TextView? = null
    private var leaderboard: TextView? = null
    private var menu: Button? = null
    private val scopeLeaderboard = CoroutineScope(CoroutineName("Leaderboard"))
    private var progressbar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_leaderboard)
        menu = findViewById(R.id.buttonMenuLeaderboard)
        leaderboard = findViewById(R.id.textViewLeaderboard)
        globalLeaderboard = findViewById(R.id.textViewGlobal)
        progressbar = findViewById(R.id.progressBarLeaderboard)
        menu?.setOnClickListener(this)
        leaderboard!!.visibility = View.INVISIBLE
        globalLeaderboard!!.visibility = View.INVISIBLE
        scopeLeaderboard.launch(Dispatchers.Default) {
            val userID =  (this@PersonalLeaderboardActivity.application as Scrambler).getCurrentUser()
            val db = FirebaseFirestore.getInstance()
            val user = db.collection("Users")
            var personalLeaderboard = launch {
                var scores: MutableList<Int>?
                if (userID != null) {
                    user.document(userID).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userSnapShot = task.result
                            if (userSnapShot != null) {
                                // Log.e(TAG, (String) userSnapShot.get("scores"));
                                @Suppress("UNCHECKED_CAST")
                                scores = userSnapShot["scores"] as MutableList<Int>?
                                if (scores != null) {
                                    scores?.sortDescending()
                                    Log.e(TAG, "Personal Leaderboard $scores")
                                    val leaderboardText = StringBuilder()
                                    for (i in scores!!.indices) {
                                        if(i < 10) {
                                            if(scores!![i] != 0) {
                                                leaderboardText.append(i + 1).append(". ")
                                                    .append(scores!![i])
                                                    .append("\n")
                                            }
                                        }
                                    }
                                    leaderboard!!.text = leaderboardText
                                } else {
                                    leaderboard!!.text = "You have not gotten any correct!"
                                }
                            } else {
                                Log.d(TAG, "No such document")
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.exception)
                        }
                    }
                }
            }
            var globalLeaderboardScope = launch {
                user.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userSnapShot = task.result
                        if (userSnapShot != null) {
                            class UserObject(var username: String, var score: Int) {
                                override fun toString(): String {
                                    return "$username: $score"
                                }
                            }

                            val scores = mutableListOf<UserObject?>()
                            for (documents in userSnapShot) {
                                (documents.get("scores") as MutableList<Int>?)?.forEachIndexed { _, score ->
                                    if(score != 0){
                                        val newEntry =
                                            UserObject(documents.get("username") as String, score)
                                        scores.add(newEntry)
                                    }
                                }
                            }
                            class CustomComparator : Comparator<UserObject?> {
                                override fun compare(o1: UserObject?, o2: UserObject?): Int {
                                    if (o1 == null || o2 == null)
                                        return 0
                                    return o2.score.compareTo(o1.score)
                                }
                            }
                            Collections.sort(scores, CustomComparator())
                            var leaderboardString = ""
                            Log.e(TAG, scores.toString())
                            (scores.forEachIndexed { index, scoreObj ->
                                if(index < 10){
                                    leaderboardString += ((index + 1).toString() + ". " + scoreObj.toString() + "\n")
                                }
                            })
                            globalLeaderboard!!.text = leaderboardString
                        } else {
                            Log.d(TAG, "No such document")
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.exception)
                    }
                }
            }
            delay(1000L)
            while (!personalLeaderboard.isCompleted && !globalLeaderboardScope.isCompleted) {
                delay(100L)
            }
            runOnUiThread {
                progressbar!!.visibility = View.INVISIBLE
                leaderboard!!.visibility = View.VISIBLE
                globalLeaderboard!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonMenuLeaderboard -> startActivity(Intent(this, MenuActivity::class.java))
        }
    }

    companion object {
        private const val TAG = "PersonalLeaderboard"
    }
}