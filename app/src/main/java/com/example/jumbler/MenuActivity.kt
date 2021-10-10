package com.example.jumbler

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.util.*
import java.util.concurrent.Executors

class MenuActivity : AppCompatActivity() {
    private val user1: MutableMap<String, Any?> = HashMap()
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var userID: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
        scopeFirebaseAdd.launch(Dispatchers.Default) {
            val letDirectory1 = File(filesDir, "scores")
            val preferencesEmail: SharedPreferences by lazy {
                getSharedPreferences(
                    "email",
                    MODE_PRIVATE
                )
            }
            val email: String = preferencesEmail.getString("email", "").toString()
            val file1 = File(letDirectory1, email)
            if (file1.exists()) {
                val inputAsString: String = try {
                    FileInputStream(file1).bufferedReader().use { it.readText() }
                } catch (e: FileNotFoundException) {
                    ""
                }
                if (inputAsString != "") {
                    retrieveID(inputAsString)
                    while (userID == "") {
                        delay(500L)
                    }
                    val correctInput: List<String> = inputAsString.trim().split(" ")
                    for (i in 2 until correctInput.size) {
                        val correct1 = correctInput[i]
                        Log.e("TAG", correct1)
                        Log.e("TAG", Integer.parseInt(correct1.trim()).toString())
                        val correct = Integer.parseInt(correct1.trim())

                        val job1 = launch {
                            getUserInformation()
                        }
                        while (!job1.isCompleted) {
                            delay(1000L)
                        }
                        if (userID != "failed") {
                            if (user1["username"] != null && correct != 0) {
                                val scores = user1["scores"] as MutableList<Int>
                                scores.add(correct)
                                user1["scores"] = scores
                                Log.e("TAG", user1.toString())
                                Log.e("TAG", "in user $user1")
                                db.collection("Users").document(userID)
                                    .set(user1)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "TAG",
                                            "DocumentSnapshot successfully written!"
                                        )
                                        file1.delete()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "TAG",
                                            "Error writing document",
                                            e
                                        )
                                    }
                            }
                        }
                    }
                }
            }
        }

        val game: Button = findViewById(R.id.buttonStart)
        val profile: Button = findViewById(R.id.buttonProfile)
        val leaderboard: Button = findViewById(R.id.buttonLeaderboard)
        val practice: Button = findViewById(R.id.buttonPractice)
        Log.e("|TAG", filesDir.toString())
        val letDirectory = File(filesDir, "wordsData")
        val file = File(letDirectory, "words3.txt")
        val inputAsString: String = try {
            FileInputStream(file).bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) {
            ""
        }.toString()

        game.setOnClickListener {
            if (inputAsString != "") {
               // val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
//                scopeFirebaseAdd.launch(Dispatchers.Default) {
//                    try {
//                        val timeoutMs = 1500
//                        val sock = Socket()
//                        val sockaddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)
//                        sock.connect(sockaddr, timeoutMs)
//                        sock.close()
                        startActivity(Intent(this@MenuActivity, GameActivity::class.java))
//                    } catch (e: IOException) {
//                        Snackbar.make(
//                            findViewById(android.R.id.content),
//                            "Connect to wifi to play!",
//                            Snackbar.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.dictionary_error),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        profile.setOnClickListener {
            startActivity(
                Intent(
                    this@MenuActivity,
                    ProfileActivity::class.java
                )
            )
        }

        leaderboard.setOnClickListener {
            val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
            scopeFirebaseAdd.launch(Dispatchers.Default) {
                try {
                    val timeoutMs = 1500
                    val sock = Socket()
                    val sockaddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)
                    sock.connect(sockaddr, timeoutMs)
                    sock.close()
                    startActivity(
                        Intent(
                            this@MenuActivity,
                            PersonalLeaderboardActivity::class.java
                        )
                    )
                } catch (e: IOException) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Connect to wifi to view!",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        }

        practice.setOnClickListener {
            if (inputAsString != "") {
                startActivity(
                    Intent(
                        this@MenuActivity,
                        PracticeActivity::class.java
                    )
                )
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.dictionary_error),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun retrieveID(inputAsString: String) {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        Log.e("TAG", "in retrieveID input string is: $inputAsString")
        val email = inputAsString.split(" ")[0].trim()
        val password = inputAsString.split(" ")[1].trim()
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    userID = user.uid
                }
            } else {
                userID = "failed"
            }
        }.addOnCanceledListener {
            userID = "failed"
        }
    }

    private fun getUserInformation() {

        val user: CollectionReference = db.collection("Users")
        var username: String
        var newScores: MutableList<Int>
        val getUser: CoroutineScope = CoroutineScope(CoroutineName("getUser"))
        getUser.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            Log.e("TAG", "in user information")
            val job1: Job = launch {
                while (userID == "") {
                    delay(100L)
                }
                if (userID != "failed") {
                    user.document(userID).get().addOnSuccessListener { document ->
                        if (document != null) {
                            Log.e("TAG", document["username"].toString())
                            username = document["username"] as String
                            val scores = document["scores"]
                            if (scores != null) {
                                Log.e("TAG", "Setting user information")
                                newScores = (scores as MutableList<Int>?)!!
                                user1["username"] = username
                                user1["scores"] = newScores
                                Log.e("TAG", "trying for multiple scores")
                            }
                        } else {
                            Log.d("TAG", "No such document")
                        }
                    }.addOnFailureListener { exception ->
                        Log.d("TAG", "get failed with ", exception)
                    }
                } else {
                    user1["failed"] = "failed"
                }
            }
            while (!job1.isCompleted && user1.isEmpty()) {
                Log.e("TAG", "waiting for username to not be null")
                delay(1000L)
            }
            Log.e("TAG", "user 1 is not null : $user1")
        }
    }


    override fun onBackPressed() {
        return
    }
}