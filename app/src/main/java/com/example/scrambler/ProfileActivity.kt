package com.example.scrambler

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scrambler.Utils.Scrambler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Thread.sleep

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val logout = findViewById<Button>(R.id.signOut)
        val menu = findViewById<Button>(R.id.buttonMenuMain)
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("remember", "false");
            editor.apply()
            val preferencesEmail = getSharedPreferences("email", MODE_PRIVATE)
            val editorEmail = preferencesEmail.edit()
            editorEmail.putString("email", null);
            editorEmail.apply()
            val preferencesPassword = getSharedPreferences("email", MODE_PRIVATE)
            val editorPassword = preferencesPassword.edit()
            editorPassword.putString("password", null);
            editorPassword.apply()
            finish()
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
        }
        menu.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@ProfileActivity, MenuActivity::class.java))
        }
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        sleep(1000L)
        val userID =  (this.application as Scrambler).getCurrentUser()
        val greetingTextView = findViewById<TextView>(R.id.welcome)
        val emailTextView = findViewById<TextView>(R.id.textEmailAddress)
        val usernameTextView = findViewById<TextView>(R.id.textUser)
        if (userID != null) {
            reference.child(userID).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfile = snapshot.getValue(User::class.java)
                    if (userProfile != null) {
                        val username = userProfile.username
                        val email = userProfile.email
                        emailTextView.text = email
                        greetingTextView.text = "Welcome $username"
                        usernameTextView.text = username
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Something wrong happened!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}