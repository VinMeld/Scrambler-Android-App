package com.example.jumbler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

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
        val file = File(letDirectory, "words.txt")
        val inputAsString: String = try {
            FileInputStream(file).bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) {
            ""
        }.toString()

        game.setOnClickListener {
            if (inputAsString != "") {
                startActivity(Intent(this@MenuActivity, GameActivity::class.java))
            } else {
                // TODO: Update all words text file to search for an the other file ?? what
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