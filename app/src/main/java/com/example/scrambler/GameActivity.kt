package com.example.scrambler

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Bundle
import com.example.scrambler.R
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import com.android.volley.Request
import com.example.scrambler.MenuActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import com.example.scrambler.GameActivity
import com.android.volley.VolleyError
import com.google.firebase.firestore.FieldValue
import java.util.*

class GameActivity : AppCompatActivity() {
    var word = ""
    var correct = 0
    var chances = 3
    var seconds = 0
    var firebaseUser = FirebaseAuth.getInstance().currentUser
    var userID = firebaseUser!!.uid
    var db = FirebaseFirestore.getInstance()
    var user = db.collection("Users").document(userID)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val menu = findViewById<Button>(R.id.buttonMenu)
        val correctWords = findViewById<TextView>(R.id.textViewCorrect)
        val scrambledWord = findViewById<TextView>(R.id.textWord)
        val enterScramble = findViewById<EditText>(R.id.textViewWord)
        val textViewChances = findViewById<TextView>(R.id.textViewChances)
        val buttonRestart = findViewById<Button>(R.id.buttonRestart)
        correctWords.text = correct.toString()
        textViewChances.text = chances.toString()
        val timerText = findViewById<TextView>(R.id.textViewTimer)
        word = setScrambledWord(scrambledWord)
        startGame(scrambledWord, enterScramble, textViewChances, timerText)
        // RESTART BUTTON
        buttonRestart.setOnClickListener {
            if (chances > 0) {
                val toast = Toast.makeText(applicationContext, "Keep trying! You're not done yet", Toast.LENGTH_SHORT)
                toast.show()
            } else {
                chances = 3
                correct = 0
                correctWords.text = correct.toString()
                textViewChances.text = chances.toString()
                enterScramble.visibility = View.VISIBLE
                scrambledWord.visibility = View.VISIBLE
                word = setScrambledWord(scrambledWord)
                startGame(scrambledWord, enterScramble, textViewChances, timerText)
            }
        }
        // MENU
        menu.setOnClickListener { startActivity(Intent(this@GameActivity, MenuActivity::class.java)) }
        // CHECK IF TYPED CORRECTLY
        enterScramble.setOnKeyListener { _, _, _ -> // IF ANSWER IS CORRECT
            if (enterScramble.text.toString().contentEquals(word)) {
                seconds = 1
                correct++
                correctWords.text = correct.toString()
                enterScramble.setText("")
                word = setScrambledWord(scrambledWord)
            }
            false
        }
    }

    protected fun randomNumber(low: Int, high: Int): Int {
        val r = Random()
        return r.nextInt(high - low) + low
    }

    protected fun setScrambledWord(scrambledWord: TextView): String {
        val wordNumber = randomNumber(1, 1000)
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://most-common-words.herokuapp.com/api/search?top=" +
                wordNumber
        //        if(word.isEmpty()){
//            word = "a";
//        }
        // while (word.length() < 3) {
        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    val parts = response.split(":").toTypedArray()[1]
                    word = parts.split(",").toTypedArray()[0].replace("\"", "")
                    Log.e(TAG, word)
                    if (word.length < 3) {
                        val arrayOfCharacters = word.toCharArray()
                        val randomWordScrambled = String(charArrayOf(arrayOfCharacters[1], arrayOfCharacters[0]))
                        scrambledWord.text = randomWordScrambled
                    } else {
                        val arrayOfCharacters = word.toCharArray()
                        while (word.contentEquals(String(arrayOfCharacters))) {
                            for (index in 0 until arrayOfCharacters.size - 2) {
                                val randomCharacter = randomNumber(0, arrayOfCharacters.size - 1)
                                val randomMover = randomNumber(0, arrayOfCharacters.size - 1)
                                val temp = arrayOfCharacters[randomCharacter]
                                arrayOfCharacters[randomCharacter] = arrayOfCharacters[randomMover]
                                arrayOfCharacters[randomMover] = temp
                            }
                        }
                        val randomWordScrambled = String(arrayOfCharacters)
                        // Display the first 500 characters of the response string.
                        scrambledWord.text = randomWordScrambled
                    }
                }) { scrambledWord.text = "That didn't work!" }
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
        // }
        return word
    }

    protected fun startGame(scrambledWord: TextView, enterScramble: EditText, textViewChances: TextView, timerText: TextView) {
        Log.e(TAG, "StartGame")
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (seconds < 10) {
                    runOnUiThread { timerText.text = seconds.toString() }
                    Log.e(TAG, "Seconds = $seconds")
                    seconds++
                } else {
                    if (chances == 1) {
                        runOnUiThread {
                            enterScramble.visibility = View.INVISIBLE
                            scrambledWord.visibility = View.INVISIBLE
                        }
                        timer.cancel()
                        // ADDING SCORE TO DATABASE
                        user.update("scores", FieldValue.arrayUnion(correct))
                    }
                    word = setScrambledWord(scrambledWord)
                    chances--
                    runOnUiThread { textViewChances.text = chances.toString() }
                    seconds = 1
                }
            }
        }, 0, 1000)
    }

    companion object {
        private const val TAG = "GameActivity"
    }
}