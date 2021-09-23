package com.example.jumbler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

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
                val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
                scopeFirebaseAdd.launch(Dispatchers.Default) {
                    try {
                        val timeoutMs = 1500
                        val sock = Socket()
                        val sockaddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)
                        sock.connect(sockaddr, timeoutMs)
                        sock.close()
                        startActivity(Intent(this@MenuActivity, GameActivity::class.java))
                    } catch (e: IOException) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Connect to wifi to play!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
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
                    )                } catch (e: IOException) {
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

    override fun onBackPressed() {
        return
    }
}