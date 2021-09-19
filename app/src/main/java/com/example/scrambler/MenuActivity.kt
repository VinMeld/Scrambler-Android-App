package com.example.scrambler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scrambler.utils.Scrambler
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val game = findViewById<Button>(R.id.buttonStart)
        val profile = findViewById<Button>(R.id.buttonProfile)
        val leaderboard = findViewById<Button>(R.id.buttonLeaderboard)
        val practice = findViewById<Button>(R.id.buttonPractice)
        val path = filesDir
        Log.e("|TAG", path.toString())
        val letDirectory = File(path, "wordsData")
        val file = File(letDirectory, "words.txt")
        val inputAsString = try {
            FileInputStream(file).bufferedReader().use { it.readText() }
        } catch(e: FileNotFoundException) {
            ""
        }.toString()
        game.setOnClickListener {
            if (inputAsString != "") {
                startActivity(Intent(this@MenuActivity, GameActivity::class.java))
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Get on a network and restart app!",
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
            startActivity(
                Intent(
                    this@MenuActivity,
                    PersonalLeaderboardActivity::class.java
                )
            )
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
                    "Get on a network and restart app!",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}