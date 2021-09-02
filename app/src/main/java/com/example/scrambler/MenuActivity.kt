package com.example.scrambler

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.scrambler.GameActivity
import com.example.scrambler.PersonalLeaderboardActivity

class MenuActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val game = findViewById<Button>(R.id.buttonStart)
        val profile = findViewById<Button>(R.id.buttonProfile)
        val leaderboard = findViewById<Button>(R.id.buttonLeaderboard)
        game.setOnClickListener(this)
        profile.setOnClickListener(this)
        leaderboard.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonStart -> startActivity(Intent(this@MenuActivity, GameActivity::class.java))
            R.id.buttonProfile -> startActivity(
                Intent(
                    this@MenuActivity,
                    ProfileActivity::class.java
                )
            )
            R.id.buttonLeaderboard -> startActivity(
                Intent(
                    this@MenuActivity,
                    PersonalLeaderboardActivity::class.java
                )
            )
        }
    }
}