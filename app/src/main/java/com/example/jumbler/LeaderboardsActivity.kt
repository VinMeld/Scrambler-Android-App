package com.example.jumbler

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
import com.example.jumbler.utils.Jumbler
import com.example.jumbler.utils.LeaderboardAdapter
import com.example.jumbler.utils.LeaderboardItem
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.util.*

class LeaderboardsActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboards)

        var leaderboardIsEmpty = false
        val leaderboardTab: TabLayout = findViewById(R.id.leaderboardTabs)
        val leaderboardRecycler: RecyclerView = findViewById(R.id.leaderboardRecycler)
        val globalLeaderboardRecycler: RecyclerView = findViewById(R.id.globalLeaderboardRecycler)
        val emptyLeaderboardText: TextView = findViewById(R.id.emptyLeaderboardText)
        val menu: Button = findViewById(R.id.buttonMenuLeaderboard)
        menu.setOnClickListener(this)
        val progressBar: ProgressBar = findViewById(R.id.progressBarLeaderboard)

        leaderboardRecycler.visibility = View.INVISIBLE
        globalLeaderboardRecycler.visibility = View.INVISIBLE

        val scopeLeaderboard = CoroutineScope(CoroutineName("Leaderboard"))
        scopeLeaderboard.launch(Dispatchers.Default) {
            val userID: String =
                (this@LeaderboardsActivity.application as Jumbler).getCurrentUuid()
            val user: CollectionReference = FirebaseFirestore.getInstance().collection("Users")
            val personalLeaderboard: Job = launch {
                var scores: MutableList<Int>?
                user.document(userID).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userSnapshot = task.result
                        if (userSnapshot != null) {
                            scores = userSnapshot["scores"] as MutableList<Int>?
                            if (scores != null) {
                                scores?.sortDescending()
                                Log.e(TAG, "Personal Leaderboard $scores")
                                val personalRankings: ArrayList<LeaderboardItem> =
                                    ArrayList<LeaderboardItem>()
                                for (i in scores!!.indices) {
                                    if (i < 10 && scores!![i] != 0) personalRankings.add(
                                        LeaderboardItem(i + 1, scores!![i].toString())
                                    )
                                }

                                runOnUiThread {
                                    if (personalRankings.isNotEmpty()) {
                                        val adapter = LeaderboardAdapter()
                                        adapter.setRankings(personalRankings)
                                        leaderboardRecycler.adapter = adapter
                                        leaderboardRecycler.layoutManager =
                                            LinearLayoutManager(parent)
                                    } else {
                                        leaderboardIsEmpty = true
                                        emptyLeaderboardText.visibility = View.VISIBLE
                                    }
                                    progressBar.visibility = View.INVISIBLE
                                }
                            } else {
                                runOnUiThread {
                                    leaderboardIsEmpty = true
                                    progressBar.visibility = View.INVISIBLE
                                    emptyLeaderboardText.visibility = View.VISIBLE
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

                            val scores: MutableList<UserObject?> = mutableListOf()
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
                            val globalRankings: ArrayList<LeaderboardItem> =
                                ArrayList<LeaderboardItem>()
                            (scores.forEachIndexed { index, scoreObj ->
                                if (index < 10) globalRankings.add(
                                    LeaderboardItem(
                                        index + 1,
                                        scoreObj.toString()
                                    )
                                )
                            })

                            runOnUiThread {
                                val adapter = LeaderboardAdapter()
                                adapter.setRankings(globalRankings)
                                globalLeaderboardRecycler.adapter = adapter
                                globalLeaderboardRecycler.layoutManager =
                                    LinearLayoutManager(parent)
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
                leaderboardRecycler.visibility = View.VISIBLE
                leaderboardTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        if (tab?.position == 0) {
                            leaderboardRecycler.visibility = View.VISIBLE
                            if (leaderboardIsEmpty) emptyLeaderboardText.visibility = View.VISIBLE
                            globalLeaderboardRecycler.visibility = View.INVISIBLE
                        } else {
                            leaderboardRecycler.visibility = View.INVISIBLE
                            emptyLeaderboardText.visibility = View.INVISIBLE
                            globalLeaderboardRecycler.visibility = View.VISIBLE
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
