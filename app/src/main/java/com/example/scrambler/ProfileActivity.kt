package com.example.scrambler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.scrambler.R
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.widget.Button
import com.example.scrambler.MainActivity
import com.example.scrambler.MenuActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.TextView
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.widget.Toast

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val logout = findViewById<Button>(R.id.signOut)
        val menu = findViewById<Button>(R.id.buttonMenuMain)
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
        }
        menu.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@ProfileActivity, MenuActivity::class.java))
        }
        val user = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        assert(user != null)
        val userID = user!!.uid
        val greetingTextView = findViewById<TextView>(R.id.welcome)
        val emailTextView = findViewById<TextView>(R.id.textEmailAddress)
        val usernameTextView = findViewById<TextView>(R.id.textUser)
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