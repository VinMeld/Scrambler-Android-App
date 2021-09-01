package com.example.scrambler

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import com.example.scrambler.Utils.Scrambler
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.Executors

class GameActivity : AppCompatActivity(), View.OnClickListener {
    private var word = ""
    var randomWordScrambled = ""
    private var correct = 0
    private var chances = 3
    private var seconds = 0
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
        startGame()
        // RESTART BUTTON
        buttonRestart!!.setOnClickListener {
            if (chances > 0) {
                val toast = Toast.makeText(
                    applicationContext,
                    "Keep trying! You're not done yet",
                    Toast.LENGTH_SHORT
                )
                toast.show()
            } else {
                chances = 3
                correct = 0
                seconds = 1
                runOnUiThread {
                    correctWords!!.text = correct.toString()
                    textViewChances!!.text = chances.toString()
                    enterScramble!!.setText("")
                    enterScramble!!.visibility = View.VISIBLE
                    textViewFlash!!.visibility = View.INVISIBLE
                }
                Log.e(TAG, "starting scramble from RESTART")
                startGame()
            }
        }
    }

    private fun correctProcedure() {
        correct++
        runOnUiThread {
            enterScramble!!.setText("")
            correctWords!!.text = correct.toString()
            textViewFlash!!.visibility = View.VISIBLE
            textViewFlash!!.text = "Correct!"
        }
        sleep(1000L)
        Log.e(TAG, "starting scramble from getting it right")
        startGame()
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

    @SuppressLint("SetTextI18n")
    protected fun startGame() {
        Log.e(TAG, "StartGame")
        runOnUiThread {
            timerText!!.visibility = View.VISIBLE
        }
        scopeTimer.launch(Dispatchers.Default) {
            delay(100L)
            val waitLength = 16
            val guess = launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                // Wait for UI to actually change on screen.
                delay(800L)
                while (seconds < waitLength) {
                    runOnUiThread { timerText!!.text = seconds.toString() }
                    Log.e(TAG, "Seconds = $seconds")
                    delay(1000L)
                    seconds += 1
                }
            }
            val wordCall = launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                val wordNumber = randomNumber(1, 1000)
                // Instantiate the RequestQueue.
                val queue = Volley.newRequestQueue(this@GameActivity)
                val url = "https://most-common-words.herokuapp.com/api/search?top=" +
                        wordNumber
                Log.e(TAG, "in word call")
                val stringRequest = StringRequest(Request.Method.GET, url,
                    { stringResponse ->
                        val parts = stringResponse.split(":").toTypedArray()[1]
                        word = parts.split(",").toTypedArray()[0].replace("\"", "")
                        Log.e(TAG, word)
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
                        Log.e(TAG, "Resetting randomwordscramble")
                        randomWordScrambled = String(arrayOfCharacters)
                    },
                    { volleyError ->
                        // handle error
                        Log.e(TAG, "Error in getting word $volleyError")
                    }
                )
                queue.add(stringRequest)
            }
            // Wait for word to generate
            while (!wordCall.isCompleted) {
                delay(500L)
            }
            delay(100L)
            Log.e(TAG, "wordcall is complete")

            // Display timer and set random word
            runOnUiThread {
                scrambledWord!!.text = randomWordScrambled
                textViewFlash!!.visibility = View.INVISIBLE
                timerText!!.visibility = View.VISIBLE
            }
            // Set timer to 0
            seconds = 1
            Log.e(TAG, "Reset timer $seconds")
            // Wait for guess to be completed
            var correctBool = false
            while (!guess.isCompleted) {
                delay(500L)
                if (enterScramble!!.text.toString().lowercase().trim().contentEquals(word.lowercase())) {
                    Log.e(TAG, "Correct")
                    correctBool = true
                    break
                }
            }
            when {
                // If they got it right then cancel guess and restart
                correctBool -> {
                    seconds = 1
                    guess.cancelAndJoin()
                    correctProcedure()
                }
                // If they got it wrong and they have no chances left set up loss stuff and push
                // too database
                !correctBool && chances == 1 -> {
                    chances--
                    runOnUiThread {
                        textViewFlash!!.text = "You ran out of chances! The word was '$word'."
                        textViewFlash!!.visibility = View.VISIBLE
                        textViewChances!!.text = chances.toString()
                        enterScramble!!.visibility = View.INVISIBLE
                    }
                    addToDatabase()
                    runOnUiThread { textViewChances!!.text = chances.toString() }
                }
                // If they got it wrong, subtract chances restart
                !correctBool -> {
                    runOnUiThread {
                        textViewFlash!!.text = word
                        textViewFlash!!.visibility = View.VISIBLE
                    }
                    Log.e(TAG, "Got wrong")
                    guess.cancelAndJoin()
                    chances--
                    runOnUiThread { textViewChances!!.text = chances.toString() }
                    delay(1000L)
                    seconds = 1
                    Log.e(TAG, "starting scramble from missing")
                    startGame()
                }
            }
        }
    }

    override fun onPause() {
        Log.e(TAG, "in pause()")
        if(chances != 0 ){
            addToDatabase()
        }
        runBlocking {
            scopeTimer.cancel()
        }
        super.onPause()
    }

    companion object {
        private const val TAG = "GameActivity"
    }

    private fun addToDatabase() {
        val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
        scopeFirebaseAdd.launch(Dispatchers.Default) {
            val userID = (this@GameActivity.application as Scrambler).getCurrentUser()
            val db = FirebaseFirestore.getInstance()
            val user = db.collection("Users")
            val user1: MutableMap<String, Any?> = HashMap()
            val job1 = launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                if (userID != null && correct != 0) {
                    user.document(userID).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val username = document["username"]
                            val scores = document["scores"]
                            if (scores != null) {
                                scores::class.simpleName?.let { Log.e(TAG, it) }
                                val newScores = scores as MutableList<Int>?
                                newScores?.add(correct)
                                user1["username"] = username
                                user1["scores"] = newScores
                                Log.e(TAG, "trying for multiple scores")
                            }
                        } else {
                            Log.d(TAG, "No such document")
                        }
                    }.addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                    }
                }
            }
            while (!job1.isCompleted) {
                delay(1000L)
            }
            if (user1["username"] != null && userID != null) {
                Log.e(TAG, user1.toString())
                db.collection("Users").document(userID)
                    .set(user1)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
        }
    }
}