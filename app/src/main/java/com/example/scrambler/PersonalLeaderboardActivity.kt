package com.example.scrambler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scrambler.Utils.LeaderboardAdapter
import com.example.scrambler.Utils.LeaderboardItem
import com.example.scrambler.Utils.Scrambler
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.util.*

class PersonalLeaderboardActivity : AppCompatActivity(), View.OnClickListener {
    private var leaderboardTab: TabLayout? = null
    private var leaderboardIsEmpty: Boolean = false
    private var emptyLeaderboardText: TextView? = null
    private var leaderboardRecycler: RecyclerView? = null
    private var globalLeaderboardRecycler: RecyclerView? = null
    private var menu: Button? = null
    private val scopeLeaderboard = CoroutineScope(CoroutineName("Leaderboard"))
    private var progressbar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_leaderboard)

        menu = findViewById(R.id.buttonMenuLeaderboard)
        leaderboardTab = findViewById(R.id.leaderboardTabs)
        emptyLeaderboardText = findViewById(R.id.emptyLeaderboardText)
        leaderboardRecycler = findViewById(R.id.leaderboardRecycler)
        globalLeaderboardRecycler = findViewById(R.id.globalLeaderboardRecycler)
        progressbar = findViewById(R.id.progressBarLeaderboard)

        menu?.setOnClickListener(this)
        leaderboardRecycler!!.visibility = View.INVISIBLE
        globalLeaderboardRecycler!!.visibility = View.INVISIBLE

        scopeLeaderboard.launch(Dispatchers.Default) {
            val userID =
                (this@PersonalLeaderboardActivity.application as Scrambler).getCurrentUser()
            val db = FirebaseFirestore.getInstance()
            val user = db.collection("Users")
            val personalLeaderboard = launch {
                var scores: MutableList<Int>?
                if (userID != null) {
                    user.document(userID).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userSnapshot = task.result
                            if (userSnapshot != null) {
                                @Suppress("UNCHECKED_CAST")
                                scores = userSnapshot["scores"] as MutableList<Int>?
                                if (scores != null) {
                                    scores?.sortDescending()
                                    Log.e(TAG, "Personal Leaderboard $scores")
                                    val personalRankings = ArrayList<LeaderboardItem>()
                                    for (i in scores!!.indices) {
                                        if (i < 10 && scores!![i] != 0) personalRankings.add(LeaderboardItem(i + 1, scores!![i].toString()))
                                    }

                                    runOnUiThread {
                                        if (personalRankings.isNotEmpty()) {
                                            val adapter = LeaderboardAdapter()
                                            adapter.setRankings(personalRankings)
                                            leaderboardRecycler?.adapter = adapter
                                            leaderboardRecycler?.layoutManager = LinearLayoutManager(parent)
                                        } else {
                                            leaderboardIsEmpty = true
                                            emptyLeaderboardText?.visibility = View.VISIBLE
                                        }
                                    }
                                } else {
                                    runOnUiThread {
                                        leaderboardIsEmpty = true
                                        emptyLeaderboardText?.visibility = View.VISIBLE
                                    }
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
            val globalLeaderboardScope = launch {
                user.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userSnapshot = task.result
                        if (userSnapshot != null) {
                            class UserObject(var username: String, var score: Int) {
                                override fun toString(): String {
                                    return "$username: $score"
                                }
                            }

                            val scores = mutableListOf<UserObject?>()
                            for (documents in userSnapshot) {
                                (documents.get("scores") as MutableList<Int>?)?.forEachIndexed { _, score ->
                                    if (score != 0) {
                                        val newEntry =
                                            UserObject(documents.get("username") as String, score)
                                        scores.add(newEntry)
                                    }
                                }
                            }
                            class CustomComparator : Comparator<UserObject?> {
                                override fun compare(o1: UserObject?, o2: UserObject?): Int {
                                    if (o1 == null || o2 == null) return 0
                                    return o2.score.compareTo(o1.score)
                                }
                            }
                            Collections.sort(scores, CustomComparator())
                            Log.e(TAG, scores.toString())
                            val globalRankings = ArrayList<LeaderboardItem>()
                            (scores.forEachIndexed { index, scoreObj ->
                                if (index < 10) globalRankings.add(LeaderboardItem(index + 1, scoreObj.toString()))
                            })

                            runOnUiThread {
                                val adapter = LeaderboardAdapter()
                                adapter.setRankings(globalRankings)
                                globalLeaderboardRecycler?.adapter = adapter
                                globalLeaderboardRecycler?.layoutManager = LinearLayoutManager(parent)
                            }
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
                leaderboardRecycler!!.visibility = View.VISIBLE
                leaderboardTab!!.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        if (tab?.position == 0) {
                            leaderboardRecycler!!.visibility = View.VISIBLE
                            if (leaderboardIsEmpty) emptyLeaderboardText?.visibility = View.VISIBLE
                            globalLeaderboardRecycler!!.visibility = View.INVISIBLE
                        } else {
                            leaderboardRecycler!!.visibility = View.INVISIBLE
                            emptyLeaderboardText?.visibility = View.INVISIBLE
                            globalLeaderboardRecycler!!.visibility = View.VISIBLE
                        }
                    }
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })
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
