package com.example.jumbler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jumbler.utils.Jumbler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.protobuf.LazyStringArrayList
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.Collections.max
import java.util.concurrent.Executors

open class GameActivity : AppCompatActivity(), View.OnClickListener {
    private var word = ""
    private var randomWordScrambled = ""
    private var correct = 0
    private var chances = 3
    private var seconds = 0
    private val scopeTimer = CoroutineScope(CoroutineName("Timer"))
    private val getUser = CoroutineScope(CoroutineName("getUser"))
    private var menu: Button? = null
    private var correctWords: TextView? = null
    private var scrambledWord: TextView? = null
    private var enterScramble: EditText? = null
    private var textViewChances: TextView? = null
    private var buttonRestart: Button? = null
    private var textViewFlash: TextView? = null
    private var timerText: TextView? = null
    private var textViewHighScore: TextView? = null
    private var lastWord = ""
    private var highscore = 0
    private val user1: MutableMap<String, Any?> = HashMap()
    private var apiKey: String? = null
    private var wordListLength = arrayOf<Array<String>>()
    private var wordsArray = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        menu = findViewById(R.id.buttonMenu)
        menu?.setOnClickListener(this)
        correctWords = findViewById(R.id.textViewCorrect)
        scrambledWord = findViewById(R.id.textWord)
        enterScramble = findViewById(R.id.textEditWord)
        textViewChances = findViewById(R.id.textViewChances)
        buttonRestart = findViewById(R.id.buttonRestart)
        textViewFlash = findViewById(R.id.textViewFlash)
        timerText = findViewById(R.id.textViewTimer)
        textViewHighScore = findViewById(R.id.textViewHighScore)
        apiKey = getString(R.string.parse_application_id)
        runOnUiThread {
            timerText!!.visibility = View.INVISIBLE
            correctWords!!.text = getString(R.string.score, correct)
            if (chances != 1) textViewChances!!.text =
                getString(R.string.attempts_remaining, chances)
            if (chances == 1) textViewChances!!.text =
                getString(R.string.attempt_remaining, chances)
        }
        val path = filesDir
        val letDirectory = File(path, "wordsData")

        for(i in 2..12){
            val file = File(letDirectory, "words$i.txt")
            val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
            wordListLength += (inputAsString.split(" ") as MutableList<String>).toTypedArray()
        }
        Log.e(TAG, "starting scramble from creation")
        startGame()
        // RESTART BUTTON
        buttonRestart?.setOnClickListener {
            if (chances > 0) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.restart_keep_trying),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                getUserHighScore()
                chances = 3
                correct = 0
                seconds = 1
                runOnUiThread {
                    correctWords!!.text = getString(R.string.score, correct)
                    if (chances != 1) textViewChances!!.text =
                        getString(R.string.attempts_remaining, chances)
                    if (chances == 1) textViewChances!!.text =
                        getString(R.string.attempt_remaining, chances)
                    enterScramble!!.setText("")
                    enterScramble!!.visibility = View.VISIBLE
                    textViewFlash!!.visibility = View.INVISIBLE
                    textViewHighScore!!.visibility = View.INVISIBLE
                }
                Log.e(TAG, "starting scramble from RESTART")
                startGame()
            }
        }
    }

    private fun correctProcedure() {
        correct++
        Log.e(TAG, "correct procedure $highscore : $correct")
        if (highscore + 1 == correct) {
            Log.e(TAG, "showing highscore")
            val newHighscore = CoroutineScope(CoroutineName("newHighscore"))
            newHighscore.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                val job1 = launch {
                    runOnUiThread {
                        textViewHighScore!!.visibility = View.VISIBLE
                    }
                }
                job1.join()
            }
        }
        runOnUiThread {
            enterScramble!!.text.clear()
            correctWords!!.text = getString(R.string.score, correct)
            textViewFlash!!.text = getString(R.string.correct_answer)
            textViewFlash!!.visibility = View.VISIBLE
        }
        Log.e(TAG, "starting scramble from getting it right")
        startGame()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonMenu -> startActivity(Intent(this@GameActivity, MenuActivity::class.java))
        }
    }

    private fun randomNumber(low: Int, high: Int): Int {
        val r = Random()
        return r.nextInt(high - low) + low
    }
    private fun generateWord(first :Int, second : Int, third : Int){
        val randomNum = randomNumber(1, 3)
        if(randomNum == 1) {
            val randomNum1 = randomNumber(0, wordListLength[first].size)
            word = wordListLength[first][randomNum1]
        } else if (randomNum == 2){
            val randomNum1 = randomNumber(0, wordListLength[second].size)
            word = wordListLength[second][randomNum1]
        } else if (randomNum == 3){
            val randomNum1 = randomNumber(0, wordListLength[third].size)
            word = wordListLength[third][randomNum1]
        }
    }
    private fun startGame() {
        Log.e(TAG, "StartGame")
        runOnUiThread {
            timerText!!.visibility = View.VISIBLE
        }
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            delay(1000L)
            val waitLength = 15
            val guess = launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                // Wait for UI to actually change on screen.
                while (seconds <= waitLength) {
                    runOnUiThread { timerText!!.text = seconds.toString() }
                    Log.e(TAG, "Seconds = $seconds")
                    delay(1000L)
                    seconds += 1
                }
            }
            launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                lastWord = word
                if(correct in 0..5){
                    generateWord(0,1,2)
                }
                if(correct in 6..10){
                    val newRandomNum = randomNumber(1, 10)
                    if(newRandomNum in 1..3) {
                        generateWord(0,1,2)
                    } else {
                        generateWord(3,4,5)
                    }
                }
                if(correct in 11..15){
                    val newRandomNum = randomNumber(1, 10)
                    if(newRandomNum in 1..2) {
                        generateWord(0,1,2)
                    } else if(newRandomNum in 3..4) {
                        generateWord(3,4,5)
                    } else{
                        generateWord(6,7,8)
                    }
                }
                if(correct in 16..20) {
                    val newRandomNum = randomNumber(1, 20)
                    if (newRandomNum in 1..2) {
                        generateWord(0, 1, 2)
                    } else if (newRandomNum in 3..4) {
                        generateWord(3, 4, 5)
                    } else if (newRandomNum in 5..10) {
                        generateWord(6, 7, 8)
                    } else {
                        generateWord(9, 10, 11)
                    }
                }
                if(correct > 20){
                    val newRandomNum = randomNumber(1, 30)
                    if (newRandomNum in 1..2) {
                        generateWord(0, 1, 2)
                    } else if (newRandomNum in 3..10) {
                        generateWord(3, 4, 5)
                    } else if (newRandomNum in 11..20) {
                        generateWord(6, 7, 8)
                    } else {
                        generateWord(9, 10, 11)
                    }
                }
                randomWordScrambled = word
                while(randomWordScrambled == word) {
                    val random = Random()
                    Log.e(TAG, "Resetting randomwordscramble")
                    val a: CharArray = word.toCharArray()
                    for (i in a.indices) {
                        val j: Int = random.nextInt(a.size)
                        // Swap letters
                        val temp = a[i]
                        a[i] = a[j]
                        a[j] = temp
                    }
                    randomWordScrambled = String(a)
                }
                seconds = 1
                // Display timer and set random word
                runOnUiThread {
                    scrambledWord!!.text = randomWordScrambled
                    textViewFlash!!.visibility = View.INVISIBLE
                    timerText!!.visibility = View.VISIBLE
                }
            }
            var correctBool = false

            while (!guess.isCompleted) {
                if (enterScramble!!.text.toString().trim().length == word.length) {
                    if (sameChars(
                            enterScramble!!.text.toString().trim().lowercase(),
                            word.lowercase()
                        )
                    ) {
                        if (enterScramble!!.text.toString().lowercase().trim()
                                .contentEquals(word.lowercase())
                            && word != ""
                        ) {
                            Log.e(TAG, "Correct")
                            correctBool = true
                        }
                        if (!correctBool) {
                            val queue = Volley.newRequestQueue(this@GameActivity)
                            val enteredWord = enterScramble!!.text.toString().trim()
                            val url =
                                "https://www.dictionaryapi.com/api/v3/references/sd2/json/$enteredWord?key=$apiKey"
                            val stringRequest = StringRequest(Request.Method.GET, url,
                                { stringResponse ->
                                    Log.e(TAG, stringResponse)
                                    if (stringResponse.contains("meta")) {
                                        Log.e(TAG, "Guessed a correct word from webster")
                                        correctBool = true
                                    }
                                },
                                { volleyError ->
                                    // handle error
                                    Log.e(TAG, "Error in getting word $volleyError")
                                }
                            )
                            queue.add(stringRequest)
                        }
                    }
                }
                if (correctBool) {
                    break
                }
                delay(500L)
            }
            when {
                // If they got it right then cancel guess and restart
                correctBool -> {
                    seconds = 1
                    guess.cancelAndJoin()
                    correctProcedure()
                }
                // If they got it wrong and they have no chances left, set up loss stuff and push
                // to database
                !correctBool && chances == 1 -> {
                    chances--
                    runOnUiThread {
                        textViewFlash!!.text = getString(R.string.game_over_text, word)
                        textViewFlash!!.visibility = View.VISIBLE
                        textViewChances!!.text = getString(R.string.attempts_remaining, chances)
                        enterScramble!!.visibility = View.INVISIBLE
                    }
                    addToDatabase()
                }
                // If they got it wrong, subtract chances and restart
                !correctBool -> {
                    runOnUiThread {
                        textViewFlash!!.text = getString(R.string.incorrect_answer, word)
                        textViewFlash!!.visibility = View.VISIBLE
                    }
                    Log.e(TAG, "Got wrong")
                    guess.cancelAndJoin()
                    chances--
                    runOnUiThread {
                        if (chances != 1) textViewChances!!.text =
                            getString(R.string.attempts_remaining, chances)
                        if (chances == 1) textViewChances!!.text =
                            getString(R.string.attempt_remaining, chances)
                    }
                    delay(1000L)
                    seconds = 1
                    Log.e(TAG, "starting scramble from missing")
                    startGame()
                }
            }
        }
    }

    private fun sameChars(firstStr: String, secondStr: String): Boolean {
        val first = firstStr.toCharArray()
        val second = secondStr.toCharArray()
        Arrays.sort(first)
        Arrays.sort(second)
        return first.contentEquals(second)
    }

    override fun onPause() {
        Log.e(TAG, "in pause()")
        if (chances != 0) {
            addToDatabase()
        }
        runBlocking {
            scopeTimer.cancel()
        }
        super.onPause()
    }

    override fun onResume() {
        getUserInformation()
        getUserHighScore()
        Log.e(TAG, "on resume()")
        super.onResume()
    }

    companion object {
        private const val TAG = "GameActivity"
    }

    private fun getUserInformation() {
        val userID = (this@GameActivity.application as Jumbler).getCurrentUser()
        val db = FirebaseFirestore.getInstance()
        val user = db.collection("Users")
        var username: String
        var newScores: MutableList<Int>
        getUser.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            Log.e(TAG, "in user information")
            val job1 = launch {
                if (userID != null) {
                    user.document(userID).get().addOnSuccessListener { document ->
                        if (document != null) {
                            username = document["username"] as String
                            val scores = document["scores"]
                            if (scores != null) {
                                Log.e(TAG, "Setting user information")
                                newScores = (scores as MutableList<Int>?)!!
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
            while (!job1.isCompleted && user1.isEmpty()) {
                Log.e(TAG, "waiting for username to not be null")
                delay(1000L)
            }
            Log.e(TAG, "user 1 is not null : $user1")
        }
    }

    private fun getUserHighScore() {
        val getHighScore = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
        getHighScore.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            getUserInformation()

            while (user1["scores"] == null) {
                Log.e(TAG, "waiting for user1 to not be null ? $user1")
                delay(1000L)
            }

            val scores = user1["scores"]
            Log.e(TAG, "in get user highscore $scores")
            if (scores == 0) {
                highscore = -2
            } else if (scores is List<*>) {
                try {
                    val listScores: MutableList<Int> = scores as MutableList<Int>
                    highscore = max(listScores)
                } catch (e: ClassCastException) {
                    val listScores: MutableList<Long> = scores as MutableList<Long>
                    // Using max() produces a fatal casting error
                    var highestScore = 0
                    for (score in listScores) if (score > highestScore) highestScore = score.toInt()
                    highscore = highestScore
                }
            }
        }
    }

    private fun addToDatabase() {
        val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
        val userID = (this@GameActivity.application as Jumbler).getCurrentUser()
        val db = FirebaseFirestore.getInstance()

        scopeFirebaseAdd.launch(Dispatchers.Default) {
            val job1 = launch {
                getUserInformation()
            }
            while (!job1.isCompleted) {
                delay(1000L)
            }
            if (user1["username"] != null && userID != null && correct != 0) {
                val scores = user1["scores"] as MutableList<Int>
                scores.add(correct)
                user1["scores"] = scores
                Log.e(TAG, user1.toString())
                db.collection("Users").document(userID)
                    .set(user1)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
        }
    }
}