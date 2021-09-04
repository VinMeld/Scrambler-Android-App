package com.example.scrambler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scrambler.Utils.Scrambler
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val logout = findViewById<Button>(R.id.signOut)
        val menu = findViewById<Button>(R.id.buttonMenuMain)
        val deleteAccount = findViewById<Button>(R.id.buttonDeleteAccount)
        val coroutineDeleteStuff = CoroutineScope(CoroutineName("Delete everything"))
        deleteAccount.setOnClickListener {
            coroutineDeleteStuff.launch(Default){
                val userID = (this@ProfileActivity.application as Scrambler).getCurrentUser()
                val reference = FirebaseDatabase.getInstance().getReference("Users")
                // Realtime
                launch{
                    if (userID != null) {
                        reference.child(userID).removeValue()
                    }
                }
                // Auth
                launch {
                    val user = FirebaseAuth.getInstance().currentUser
                    // Get auth credentials from the user for re-authentication. The example below shows
                    // email and password credentials but there are multiple possible providers,
                    // such as GoogleAuthProvider or FacebookAuthProvider.
                    val credential = EmailAuthProvider
                        .getCredential("user@example.com", "password1234")

                    // Prompt the user to re-provide their sign-in credentials
                    user?.reauthenticate(credential)?.addOnCompleteListener { Log.d(TAG, "User re-authenticated.") }
                    user?.delete()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "User account deleted.")
                        } else{
                            Log.d(TAG, "Failed to delete user")
                        }
                    }
                }
                // Firestore
                launch {
                    val db = FirebaseFirestore.getInstance()
                    userID?.let { it1 ->
                        db.collection("Users").document(it1)
                            .delete()
                            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                    }
                }
                val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("remember", "false")
                editor.apply()
                val preferencesEmail = getSharedPreferences("email", MODE_PRIVATE)
                val editorEmail = preferencesEmail.edit()
                editorEmail.putString("email", null)
                editorEmail.apply()
                val preferencesPassword = getSharedPreferences("email", MODE_PRIVATE)
                val editorPassword = preferencesPassword.edit()
                editorPassword.putString("password", null)
                editorPassword.apply()
                FirebaseAuth.getInstance().signOut()
                finish()
                startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
            }
        }
        logout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("remember", "false")
            editor.apply()
            val preferencesEmail = getSharedPreferences("email", MODE_PRIVATE)
            val editorEmail = preferencesEmail.edit()
            editorEmail.putString("email", null)
            editorEmail.apply()
            val preferencesPassword = getSharedPreferences("email", MODE_PRIVATE)
            val editorPassword = preferencesPassword.edit()
            editorPassword.putString("password", null)
            editorPassword.apply()
            finish()
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
        }
        menu.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@ProfileActivity, MenuActivity::class.java))
        }
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        val userID = (this.application as Scrambler).getCurrentUser()
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
                        runOnUiThread {
                            emailTextView?.text = email
                            greetingTextView?.text = "Welcome $username"
                            usernameTextView?.text = username
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Something wrong happened!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}