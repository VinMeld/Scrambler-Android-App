package com.example.jumbler

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jumbler.utils.COL_NAME
import com.example.jumbler.utils.DATABASENAME
import com.example.jumbler.utils.TABLENAME
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.Executors


open class PracticeActivity : AppCompatActivity(), View.OnClickListener {
    private var word: String = ""
    private var lastWord: String = ""
    private var correct: Int = 0
    private var hintNumber: Int = 0
    private var wordListLength: Array<Array<String>> = arrayOf()
    private val wordHint: TextView by lazy { findViewById(R.id.practiceHint) }
    private val playerScore: TextView by lazy { findViewById(R.id.practiceScore) }
    private val textField: EditText by lazy { findViewById(R.id.practiceEditText) }
    private val imm: InputMethodManager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    private val scopeTimer: CoroutineScope = CoroutineScope(CoroutineName("Timer"))
    private var randomWordScrambled: String = ""
    private lateinit var sqLiteDatabaseObj: SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)
        val menu: Button = findViewById(R.id.buttonPracticeMenu)
        menu.setOnClickListener(this)
        val buttonHint: Button = findViewById(R.id.buttonHint)
        val buttonRestart: Button = findViewById(R.id.buttonPracticeRestart)
        val letDirectory = File(filesDir, "wordsData")
        for (i in 2..12) {
            val file = File(letDirectory, "words$i.txt")
            val inputAsString: String = FileInputStream(file).bufferedReader().use { it.readText() }
            wordListLength += (inputAsString.split(" ") as MutableList<String>).toTypedArray()
        }
        runOnUiThread {
            playerScore.text = correct.toString()
            textField.requestFocus()
            imm.showSoftInput(textField, InputMethodManager.SHOW_IMPLICIT)
        }
        if (word == "") {
            Log.e(TAG, "calling from word ==")
            startGame()
        }
        checkScrambler()

        buttonRestart.setOnClickListener {
            Log.e(TAG, "skip")
            hintNumber = 0
            runOnUiThread {
                textField.text.clear()
                playerScore.text = getString(R.string.score, correct)
            }
            Log.e(TAG, "calling from restart")
            startGame()
        }

        buttonHint.setOnClickListener {
            hintProcedure()
        }
    }

    private fun hintProcedure() {
        val scopeTimer = CoroutineScope(CoroutineName("Timer"))
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            if (word.length == hintNumber + 1) {
                hintNumber++
                val wordSolution: TextView = findViewById(R.id.practiceWordSolution)
                val hintWord: String = word.substring(0, hintNumber)
                runOnUiThread {
                    Log.e(TAG, "setting hint full")
                    wordHint.text = hintWord
                    wordSolution.text = getString(R.string.incorrect_answer, hintWord)
                }
                hintNumber = 0
                delay(1500L)
                runOnUiThread {
                    textField.text.clear()
                    wordHint.text = ""
                    wordSolution.text = ""
                }
                Log.e(TAG, "calling from hint")
                startGame()
            } else {
                hintNumber++
                val hintWord = word.substring(0, hintNumber)
                runOnUiThread {
                    wordHint.text = hintWord
                }
            }
        }
    }

    private fun checkScrambler() {
        val scopeTimer = CoroutineScope(CoroutineName("Timer"))
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            var lastGuess = ""
            while (true) {
                if (textField.text.length == word.length && lastGuess != textField.text.toString()) {
                    checkIfCorrect()
                }
                lastGuess = textField.text.toString()
                delay(100L)
            }
        }
    }

    private fun readDataFromDatabase(word: String): Boolean {
        Log.e(TAG, "in read from database");
        val letDirectory = File(filesDir, "dictData")
        //val letter = word.first()
        val file = File(letDirectory, "dictionary.txt")

        val inputAsString: String = try {
            FileInputStream(file).bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) {
            ""
        }
        Log.e(TAG, "Input as string: $inputAsString");
        Log.e(TAG, "Word: $word");
        Log.e(TAG, inputAsString.contains(" $word ").toString());
        return inputAsString.contains(" $word ")
    }

    private fun checkIfCorrect() {
        var correctBool = false
        if (word != "" || lastWord != word) {
            if (textField.text.toString().trim().length == word.length) {
                if (sameChars(
                        textField.text.toString().trim().lowercase(),
                        word.lowercase()
                    )
                ) {
                    if (textField.text.toString().trim().lowercase()
                            .contentEquals(word.lowercase())
                        && word != ""
                    ) {
                        Log.e(TAG, "Correct")
                        correctBool = true
                    }
                    if (!correctBool) {
                        val enteredWord: String = textField.text.toString().trim()
                        if (readDataFromDatabase(enteredWord)) {
                            if (hintNumber == 0) correct++
                            hintNumber = 0
                            runOnUiThread {
                                textField.text.clear()
                                wordHint.text = ""
                                playerScore.text = getString(R.string.score, correct)
                            }
                            Log.e(TAG, "checkifcorrect")
                            startGame()
                        }
//                        val queue: RequestQueue = Volley.newRequestQueue(this@PracticeActivity)
//                        val enteredWord: String = textField.text.toString().trim()
//                        val apiKey: String = getString(R.string.parse_application_id)
//                        val url =
//                            "https://www.dictionaryapi.com/api/v3/references/sd2/json/$enteredWord?key=$apiKey"
//                        val stringRequest = StringRequest(
//                            Request.Method.GET, url,
//                            { stringResponse ->
//                                Log.e(TAG, stringResponse)
//                                if (stringResponse.contains("meta")) {
//                                    Log.e(TAG, "Guessed a correct word from webster")
//                                    correctBool = true
//
//                                }
//                                if (correctBool) {
//                                    if (hintNumber == 0) correct++
//                                    hintNumber = 0
//                                    runOnUiThread {
//                                        textField.text.clear()
//                                        wordHint.text = ""
//                                        playerScore.text = getString(R.string.score, correct)
//                                    }
//                                    Log.e(TAG, "checkifcorrect")
//                                    startGame()
//                                }
//                            },
//                            { volleyError ->
//                                // handle error
//                                Log.e(TAG, "Error in getting word $volleyError")
//                            }
//                        )
//                        queue.add(stringRequest)
                    } else {
                        // If they got it right then cancel guess and restart
                        if (hintNumber == 0) correct++
                        hintNumber = 0
                        runOnUiThread {
                            textField.text.clear()
                            wordHint.text = ""
                            playerScore.text = getString(R.string.score, correct)
                        }
                        Log.e(TAG, "check if correct")
                        startGame()
                    }
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonPracticeMenu -> startActivity(Intent(this, MenuActivity::class.java))
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
        Log.e(TAG, "StartGame")
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            lastWord = word
            if (correct in 0..5) {
                generateWord(1, 1, 2)
            }
            if (correct in 6..10) {
                val newRandomNum = randomNumber(1, 10)
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
            randomWordScrambled = word
            while (randomWordScrambled == word) {
                Log.e(TAG, "Resetting randomwordscramble")
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

            val scrambledWord: TextView = findViewById(R.id.practiceScrambledWord)
            runOnUiThread {
                textField.requestFocus()
                imm.showSoftInput(textField, InputMethodManager.SHOW_IMPLICIT)
                scrambledWord.text = randomWordScrambled
            }
        }
    }

    companion object {
        private const val TAG = "PracticeActivity"
    }

    override fun onPause() {
        Log.e(TAG, "in pause()")
        runBlocking {
            scopeTimer.cancel()
        }
        super.onPause()
    }

    private fun sameChars(firstStr: String, secondStr: String): Boolean {
        val first: CharArray = firstStr.toCharArray()
        val second: CharArray = secondStr.toCharArray()
        Arrays.sort(first)
        Arrays.sort(second)
        return first.contentEquals(second)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt("correct", correct)
        savedInstanceState.putString("word", word)
        savedInstanceState.putInt("hintNumber", hintNumber)
        savedInstanceState.putString("randomWordScrambled", randomWordScrambled)
        savedInstanceState.putString("lastWord", lastWord)
        savedInstanceState.putSerializable("wordListLength", wordListLength)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        correct = savedInstanceState.getInt("correct")
        word = savedInstanceState.getString("word").toString()
        hintNumber = savedInstanceState.getInt("hintNumber")
        randomWordScrambled = savedInstanceState.getString("randomWordScrambled").toString()
        lastWord = savedInstanceState.getString("lastWord").toString()
        wordListLength =
            savedInstanceState.getSerializable("wordListLength") as Array<Array<String>>
        val hintWord = word.substring(0, hintNumber)
        runOnUiThread {
            wordHint.text = hintWord
            playerScore.text = getString(R.string.score, correct)
        }
    }

    private fun createDictionaryDatabase() {
        sqLiteDatabaseObj = openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null)
        val createTable = "CREATE TABLE IF NOT EXISTS $TABLENAME ($COL_NAME TEXT);"
        sqLiteDatabaseObj.execSQL(createTable)
        val scopeAddWords = CoroutineScope(CoroutineName("Timer"))
        scopeAddWords.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {

            val queue1: RequestQueue = Volley.newRequestQueue(this@PracticeActivity)
            val url1 = "https://vinaycat1.pythonanywhere.com/return"
            val stringRequest1 = StringRequest(
                Request.Method.GET, url1,
                { stringResponse ->
                    for (dictionaryWord in stringResponse.split(" ")) {
                        if (!readDataFromDatabase(dictionaryWord)) {
                            val chars: CharArray = dictionaryWord.toCharArray()
                            var isLetters = true
                            for (c in chars) {
                                if (!Character.isLetter(c)) {
                                    isLetters = false
                                    break
                                }
                            }
                            if (isLetters) {
                                Log.e("TAG", "Adding to datagbase $dictionaryWord")
                                with(sqLiteDatabaseObj) { this.execSQL("INSERT INTO $TABLENAME (${COL_NAME}) VALUES('$dictionaryWord');") }
                            }
                        }
                    }
                },
                { volleyError ->
                    // handle error
                    Log.e("TAG", "Error in getting word $volleyError")
                }
            )
            queue1.add(stringRequest1)

        }
    }
//    private fun readDataFromDatabase(word : String): Boolean{
//        val cursorCourses: Cursor = sqLiteDatabaseObj.rawQuery("SELECT $COL_NAME FROM $TABLENAME WHERE $COL_NAME = \"$word\"", null);
//        while (cursorCourses.moveToNext()) {
//            if(cursorCourses.getString(0) == word){
//                return true;
//            }
//            return false;
//        }
//        return false;
//    }
}