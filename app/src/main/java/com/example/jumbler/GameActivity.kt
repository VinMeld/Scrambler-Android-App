package com.example.jumbler

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jumbler.utils.Jumbler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.Collections.max
import java.util.concurrent.Executors

open class GameActivity : AppCompatActivity(), View.OnClickListener {
    private var word: String = ""
    private var lastWord: String = ""
    private var randomWordScrambled: String = ""
    private var correct: Int = 0
    private var highScore: Int = 0
    private var chances: Int = 3
    private var waitLength: Int = 20
    private var seconds: Int = waitLength
    private var wordListLength: Array<Array<String>> = arrayOf()
    private val timerText: TextView by lazy { findViewById(R.id.gameTimer) }
    private val playerScore: TextView by lazy { findViewById(R.id.gameScore) }
    private val gameHighScore: TextView by lazy { findViewById(R.id.gameHighScore) }
    private val scrambledWord: TextView by lazy { findViewById(R.id.gameScrambledWord) }
    private val textField: EditText by lazy { findViewById(R.id.gameEditText) }
    private val gameWordSolution: TextView by lazy { findViewById(R.id.gameWordSolution) }
    private val gameRemainingChances: TextView by lazy { findViewById(R.id.gameRemainingChances) }
    private val imm: InputMethodManager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    private val userID: String by lazy { (this@GameActivity.application as Jumbler).getCurrentUser() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val scopeTimer: CoroutineScope = CoroutineScope(CoroutineName("Timer"))
    private val getUser: CoroutineScope = CoroutineScope(CoroutineName("getUser"))
    private val user1: MutableMap<String, Any?> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val menu: Button = findViewById(R.id.buttonMenu)
        menu.setOnClickListener(this)
        val buttonRestart: Button = findViewById(R.id.buttonRestart)

        runOnUiThread {
            timerText.visibility = View.INVISIBLE
            textField.requestFocus()
            imm.showSoftInput(textField, InputMethodManager.SHOW_IMPLICIT)
            playerScore.text = getString(R.string.score, correct)
            if (chances != 1) gameRemainingChances.text =
                getString(R.string.attempts_remaining, chances)
            if (chances == 1) gameRemainingChances.text =
                getString(R.string.attempt_remaining, chances)
        }

        val letDirectory = File(filesDir, "wordsData")
        for (i in 2..12) {
            val file = File(letDirectory, "words$i.txt")
            val inputAsString: String = FileInputStream(file).bufferedReader().use { it.readText() }
            wordListLength += (inputAsString.split(" ") as MutableList<String>).toTypedArray()
        }

        Log.e(TAG, "starting scramble from creation")
        startGame()

        // RESTART BUTTON
        buttonRestart.setOnClickListener {
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
                seconds = waitLength
                runOnUiThread {
                    playerScore.text = getString(R.string.score, correct)
                    if (chances != 1) gameRemainingChances.text =
                        getString(R.string.attempts_remaining, chances)
                    if (chances == 1) gameRemainingChances.text =
                        getString(R.string.attempt_remaining, chances)
                    textField.setText("")
                    textField.visibility = View.VISIBLE
                    gameWordSolution.visibility = View.INVISIBLE
                    gameHighScore.visibility = View.INVISIBLE
                }
                Log.e(TAG, "starting scramble from RESTART")
                startGame()
            }
        }
    }

    private fun correctProcedure() {
        correct++
        Log.e(TAG, "correct procedure $highScore : $correct")
        if (highScore + 1 == correct) {
            Log.e(TAG, "showing highScore")
            val newHighScore = CoroutineScope(CoroutineName("newHighScore"))
            newHighScore.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                val job1: Job = launch {
                    runOnUiThread {
                        gameHighScore.visibility = View.VISIBLE
                    }
                }
                job1.join()
            }
        }
        runOnUiThread {
            textField.text.clear()
            playerScore.text = getString(R.string.score, correct)
            gameWordSolution.text = getString(R.string.correct_answer)
            gameWordSolution.visibility = View.VISIBLE
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
        return Random().nextInt(high - low) + low
    }

    private fun generateWord(first: Int, second: Int, third: Int) {
        when (randomNumber(1, 3)) {
            1 -> {
                val randomNum1 = randomNumber(0, wordListLength[first].size)
                word = wordListLength[first][randomNum1]
            }
            2 -> {
                val randomNum1 = randomNumber(0, wordListLength[second].size)
                word = wordListLength[second][randomNum1]
            }
            3 -> {
                val randomNum1 = randomNumber(0, wordListLength[third].size)
                word = wordListLength[third][randomNum1]
            }
        }
    }

    private fun startGame() {
        var correctWord = ""
        Log.e(TAG, "StartGame")
        runOnUiThread {
            textField.text.clear()
            timerText.visibility = View.VISIBLE
            textField.requestFocus()
            imm.showSoftInput(textField, InputMethodManager.SHOW_IMPLICIT)
        }
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            delay(1000L)
            val guess: Job = launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                // Wait for UI to actually change on screen.
                while (seconds >= 0) {
                    runOnUiThread { timerText.text = seconds.toString() }
                    Log.e(TAG, "Seconds = $seconds")
                    delay(1000L)
                    seconds--
                }
            }
            launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                lastWord = word
                if (correct in 0..5) {
                    generateWord(1, 1, 2)
                }
                if (correct in 6..10) {
                    val newRandomNum: Int = randomNumber(1, 10)
                    if (newRandomNum in 1..3) {
                        generateWord(2, 2, 3)
                    } else {
                        generateWord(3, 4, 5)
                    }
                }
                if (correct in 11..15) {
                    when (randomNumber(1, 10)) {
                        in 1..2 -> {
                            generateWord(2, 3, 3)
                        }
                        in 3..4 -> {
                            generateWord(4, 4, 5)
                        }
                        else -> {
                            generateWord(6, 8, 8)
                        }
                    }
                }
                if (correct in 16..20) {
                    waitLength = 15
                    when (randomNumber(1, 20)) {
                        in 1..2 -> {
                            generateWord(3, 3, 4)
                        }
                        in 3..4 -> {
                            generateWord(3, 4, 5)
                        }
                        in 5..10 -> {
                            generateWord(6, 7, 8)
                        }
                        else -> {
                            generateWord(9, 10, 11)
                        }
                    }
                }
                if (correct > 20) {
                    waitLength = 15
                    when (randomNumber(1, 30)) {
                        in 1..2 -> {
                            generateWord(5, 5, 5)
                        }
                        in 3..10 -> {
                            generateWord(6, 6, 7)
                        }
                        in 11..20 -> {
                            generateWord(7, 7, 8)
                        }
                        else -> {
                            generateWord(9, 10, 11)
                        }
                    }
                }
                Log.e(TAG, word)
                randomWordScrambled = word
                while (randomWordScrambled == word) {
                    Log.e(TAG, "Resetting randomWordScrambled")
                    val a: CharArray = word.toCharArray()
                    for (i in a.indices) {
                        val j: Int = Random().nextInt(a.size)
                        // Swap letters
                        val temp: Char = a[i]
                        a[i] = a[j]
                        a[j] = temp
                    }
                    randomWordScrambled = String(a)
                }
                correctWord = word
                Log.e(TAG, "scrambled word $randomWordScrambled")
                seconds = waitLength
                // Display timer and set random word
                runOnUiThread {
                    scrambledWord.text = randomWordScrambled
                    gameWordSolution.visibility = View.INVISIBLE
                    timerText.visibility = View.VISIBLE
                }
            }
            var correctBool = false
            var lastGuess = ""
            while (!guess.isCompleted) {
                if (seconds == 0) {
                    word = ""
                }
                if (textField.text.toString().trim().length == word.length) {
                    if (sameChars(
                            textField.text.toString().trim().lowercase(),
                            word.lowercase()
                        )
                    ) {
                        if (textField.text.toString().lowercase().trim()
                                .contentEquals(word.lowercase())
                            && word != ""
                        ) {
                            Log.e(TAG, "Correct")
                            correctBool = true
                        }
                        if (lastGuess != textField.text.toString()) {
                            lastGuess = textField.text.toString()
                            if (!correctBool) {
                                val queue: RequestQueue = Volley.newRequestQueue(this@GameActivity)
                                val enteredWord: String = textField.text.toString().trim()
                                val apiKey: String = getString(R.string.parse_application_id)
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
                    if (correctBool) break
                }
                delay(500L)
            }
            when {
                // If they got it right then cancel guess and restart
                correctBool -> {
                    seconds = waitLength
                    guess.cancelAndJoin()
                    correctProcedure()
                }
                // If they got it wrong and they have no chances left, set up loss stuff and push
                // to database
                !correctBool && chances == 1 -> {
                    chances--
                    runOnUiThread {
                        gameWordSolution.text = getString(R.string.game_over_text, correctWord)
                        gameWordSolution.visibility = View.VISIBLE
                        gameRemainingChances.text = getString(R.string.attempts_remaining, chances)
                        textField.visibility = View.INVISIBLE
                    }
                    addToDatabase()
                }
                // If they got it wrong, subtract chances and restart
                !correctBool -> {
                    runOnUiThread {
                        textField.text.clear()
                        gameWordSolution.text = getString(R.string.incorrect_answer, correctWord)
                        gameWordSolution.visibility = View.VISIBLE
                    }
                    Log.e(TAG, "Got wrong")
                    guess.cancelAndJoin()
                    chances--
                    runOnUiThread {
                        if (chances != 1) gameRemainingChances.text =
                            getString(R.string.attempts_remaining, chances)
                        if (chances == 1) gameRemainingChances.text =
                            getString(R.string.attempt_remaining, chances)
                    }
                    delay(1000L)
                    seconds = waitLength
                    Log.e(TAG, "starting scramble from missing")
                    startGame()
                }
            }
        }
    }

    private fun sameChars(firstStr: String, secondStr: String): Boolean {
        val first: CharArray = firstStr.toCharArray()
        val second: CharArray = secondStr.toCharArray()
        Arrays.sort(first)
        Arrays.sort(second)
        return first.contentEquals(second)
    }
    override fun onSaveInstanceState(data: Bundle) {
        super.onSaveInstanceState(data)
        data.putInt("param", seconds)
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
        val user: CollectionReference = db.collection("Users")
        var username: String
        var newScores: MutableList<Int>
        getUser.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            Log.e(TAG, "in user information")
            val job1: Job = launch {
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
            Log.e(TAG, "in get user highScore $scores")
            if (scores == 0) {
                highScore = -2
            } else if (scores is List<*>) {
                try {
                    val listScores: MutableList<Int> = scores as MutableList<Int>
                    highScore = max(listScores)
                } catch (e: ClassCastException) {
                    val listScores: MutableList<Long> = scores as MutableList<Long>
                    // Using max() produces a fatal casting error
                    var highestScore = 0
                    for (score in listScores) if (score > highestScore) highestScore = score.toInt()
                    highScore = highestScore
                }
            }
        }
    }

    private fun addToDatabase() {
        val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
        scopeFirebaseAdd.launch(Dispatchers.Default) {
            val job1 = launch {
                getUserInformation()
            }
            while (!job1.isCompleted) {
                delay(1000L)
            }
            if (user1["username"] != null && correct != 0) {
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