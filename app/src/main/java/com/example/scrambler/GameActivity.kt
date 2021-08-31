package com.example.scrambler

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Bundle
import com.example.scrambler.R
import android.content.Intent
import android.text.method.KeyListener
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.example.scrambler.MenuActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import com.example.scrambler.GameActivity
import com.android.volley.VolleyError
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import java.util.*

class GameActivity : AppCompatActivity(), View.OnClickListener {
    private var word = ""
    private var correct = 0
    private var chances = 3
    private var seconds = 0
    private var firebaseUser = FirebaseAuth.getInstance().currentUser
    private var userID = firebaseUser!!.uid
    private var db = FirebaseFirestore.getInstance()
    private var user = db.collection("Users").document(userID)
    private var correctGuess = false
    private val scopeTimer = CoroutineScope(CoroutineName("Timer"))
    private var menu: Button? = null
    private var correctWords: TextView? = null
    private var scrambledWord: TextView? = null
    private var enterScramble: EditText? = null
    private var textViewChances: TextView? = null
    private var buttonRestart: Button? = null
    private var textViewFlash: TextView? = null
    private var timerText: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        menu = findViewById(R.id.buttonMenu)
        menu?.setOnClickListener(this)
        correctWords = findViewById(R.id.textViewCorrect)
        scrambledWord = findViewById(R.id.textWord)
        enterScramble = findViewById(R.id.textViewWord)
        textViewChances = findViewById(R.id.textViewChances)
        buttonRestart = findViewById(R.id.buttonRestart)
        textViewFlash = findViewById(R.id.textViewFlash)
        timerText = findViewById(R.id.textViewTimer)
        runOnUiThread {
            timerText!!.visibility = View.INVISIBLE
            correctWords!!.text = correct.toString()
            textViewChances!!.text = chances.toString()
        }
        Log.e(TAG, "starting scramble from creation")
        word = setScrambledWord()
        // RESTART BUTTON
        buttonRestart!!.setOnClickListener {
            if (chances > 0) {
                val toast = Toast.makeText(applicationContext, "Keep trying! You're not done yet", Toast.LENGTH_SHORT)
                toast.show()
            } else {
                chances = 3
                correct = 0
                runOnUiThread {
                    correctWords!!.text = correct.toString()
                    textViewChances!!.text = chances.toString()
                    enterScramble!!.setText("")
                    enterScramble!!.visibility = View.VISIBLE
                    scrambledWord!!.visibility = View.VISIBLE
                    textViewFlash!!.visibility = View.INVISIBLE
                }

                sleep(1000)
                Log.e(TAG, "starting scramble from RESTART")
                word = setScrambledWord()
            }
        }
        // CHECK IF TYPED CORRECTLY
//        enterScramble!!.setOnKeyListener { _, _, _ -> // IF ANSWER IS CORRECT
//            checkIfCorrect()
//            false
//        }
    }
    private fun checkIfCorrect(): Boolean {
        return enterScramble!!.text.toString().contentEquals(word)

    }
    private fun correctProcedure() {
        runOnUiThread {
            enterScramble!!.setText("")
            correctWords!!.text = correct.toString()
            textViewFlash!!.visibility = View.VISIBLE
            textViewFlash!!.text = "Correct!"
        }
        correct++
        sleep(1000L)
        correctGuess = true
        Log.e(TAG, "starting scramble from getting it right")
        runOnUiThread {word = setScrambledWord()}
    }
    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonMenu -> startActivity(Intent(this, MenuActivity::class.java))
        }
    }
    protected fun randomNumber(low: Int, high: Int): Int {
        val r = Random()
        return r.nextInt(high - low) + low
    }

    protected fun setScrambledWord(): String {
        Log.e(TAG, "Scrambling word")
        val wordNumber = randomNumber(1, 1000)
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://most-common-words.herokuapp.com/api/search?top=" +
                wordNumber
        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    val parts = response.split(":").toTypedArray()[1]
                    word = parts.split(",").toTypedArray()[0].replace("\"", "")
                    Log.e(TAG, word)
                    if (word.length < 3) {
                        val arrayOfCharacters = word.toCharArray()
                        val randomWordScrambled = String(charArrayOf(arrayOfCharacters[1], arrayOfCharacters[0]))
                        runOnUiThread {
                            scrambledWord!!.text = randomWordScrambled
                        }
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
                        runOnUiThread {
                            textViewFlash!!.visibility = View.INVISIBLE
                            scrambledWord!!.text = randomWordScrambled
                        }
                        seconds = 1
                        startGame()
                    }
                }) { scrambledWord!!.text = "That didn't work!" }
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
        sleep(100L)
        return word
    }

    protected fun startGame() {
        Log.e(TAG, "StartGame")
        runOnUiThread{
            timerText!!.visibility = View.VISIBLE
        }

        scopeTimer.launch(Dispatchers.Default) {
            val waitLength = 11
            val job1 = launch(Dispatchers.Unconfined) {
                while(seconds < waitLength) {
                    runOnUiThread { timerText!!.text = seconds.toString() }
                    Log.e(TAG, "Seconds = $seconds")
                    delay(1000L)
                    seconds += 1
                }
            }
            var correctBool = false
            while(!job1.isCompleted) {
                delay(500L)
                if(checkIfCorrect()){
                    correctBool = true
                    break
                }
            }
            when {
                correctBool -> {
                    job1.cancelAndJoin();
                    correctProcedure()
                }
                !correctBool && chances == 1 -> {
                    chances--
                    runOnUiThread {
                        textViewFlash!!.text = "You Lose!"
                        textViewFlash!!.visibility = View.VISIBLE
                        textViewChances!!.text = chances.toString()
                        enterScramble!!.visibility = View.INVISIBLE
                        scrambledWord!!.visibility = View.INVISIBLE
                        enterScramble!!.visibility = View.INVISIBLE
                    }
                    user.update("scores", FieldValue.arrayUnion(correct))
                    runOnUiThread { textViewChances!!.text = chances.toString() }
                }
                !correctBool -> {
                    runOnUiThread {
                        textViewFlash!!.text = word
                        textViewFlash!!.visibility = View.VISIBLE
                    }
                    Log.e(TAG, "here")
                    job1.cancelAndJoin()
                    chances--
                    runOnUiThread { textViewChances!!.text = chances.toString() }
                    delay(1000L)
                    seconds = 1
                    Log.e(TAG, "starting scramble from missing")
                    word = setScrambledWord()
                }
            }
        }
    }

    override fun onStop() {
        user.update("scores", FieldValue.arrayUnion(correct))
        runBlocking {
            scopeTimer.cancel()
        }
        super.onStop()
    }
    companion object {
        private const val TAG = "GameActivity"
    }
}