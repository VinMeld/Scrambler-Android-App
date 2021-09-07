package com.example.scrambler

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val game = findViewById<Button>(R.id.buttonStart)
        val profile = findViewById<Button>(R.id.buttonProfile)
        val leaderboard = findViewById<Button>(R.id.buttonLeaderboard)
        val practice = findViewById<Button>(R.id.buttonPractice)

        game.setOnClickListener {
            startActivity(Intent(this@MenuActivity, GameActivity::class.java))
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
            startActivity(
                Intent(
                    this@MenuActivity,
                    PracticeActivity::class.java
                )
            )
        }
    }
}