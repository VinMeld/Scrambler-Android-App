package com.example.jumbler.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.Executors


class NetworkChangeReceiver : BroadcastReceiver() {
    private val user1: MutableMap<String, Any?> = HashMap()
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var userID: String = ""
    override fun onReceive(context: Context, intent: Intent?) {
        Log.e("TAG", "in onReceive")
        val connMgr = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = connMgr
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobile = connMgr
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifi!!.isAvailable || mobile!!.isAvailable) {
            // WIFI ON
            val letDirectory = File(context.filesDir, "scores")
            val file = File(letDirectory, "score.txt")
            if (file.exists()) {
                val inputAsString: String = try {
                    FileInputStream(file).bufferedReader().use { it.readText() }
                } catch (e: FileNotFoundException) {
                    ""
                }
                if (inputAsString != "") {
                    retrieveID(inputAsString)
                    val correctInput: Array<Int> =
                        inputAsString.split(" ").map { it.toInt() }.toTypedArray()
                    for (i in 2..correctInput.size) {
                        val correct = correctInput[i]
                        val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
                        scopeFirebaseAdd.launch(Dispatchers.Default) {
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
                                    db.collection("Users").document(userID)
                                        .set(user1)
                                        .addOnSuccessListener {
                                            Log.d(
                                                "TAG",
                                                "DocumentSnapshot successfully written!"
                                            )
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
                    file.delete()
                }
            }
            Log.d("Network Available ", "Flag No 1")
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

    private fun retrieveID(inputAsString: String) {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val email = inputAsString.split(" ")[0]
        val password = inputAsString.split(" ")[1]
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    userID = user.uid
                } else {
                    userID = "failed"
                }
            } else {
                userID = "failed"
            }
        }.addOnCanceledListener {
            userID = "failed"
        }
    }
}