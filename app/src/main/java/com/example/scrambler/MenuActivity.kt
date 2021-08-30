package com.example.scrambler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.scrambler.R
import android.content.Intent
import android.widget.Button
import com.example.scrambler.GameActivity
import com.example.scrambler.ProfileActivity
import com.example.scrambler.PersonalLeaderboardActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val game = findViewById<Button>(R.id.buttonStart)
        val profile = findViewById<Button>(R.id.buttonProfile)
        val leaderboard = findViewById<Button>(R.id.buttonLeaderboard)
        game.setOnClickListener { startActivity(Intent(this@MenuActivity, GameActivity::class.java)) }
        profile.setOnClickListener { startActivity(Intent(this@MenuActivity, ProfileActivity::class.java)) }
        leaderboard.setOnClickListener {
            startActivity(Intent(this@MenuActivity,
                    PersonalLeaderboardActivity::class.java))
        }
    }
}