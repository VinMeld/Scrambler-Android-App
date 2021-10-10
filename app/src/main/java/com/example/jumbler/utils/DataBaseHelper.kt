package com.example.jumbler.utils

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors

val DATABASENAME = "dictionary.db"
val TABLENAME = "dict"
val COL_NAME = "word"

class DataBaseHelper(var context: Context) : SQLiteOpenHelper(
    context, DATABASENAME, null,
    1
) {
    fun readData(wordCheck: String): Boolean {
        val db = this.readableDatabase
        Log.e("TAG", context.filesDir.toString())
        Log.e("TAG", db.toString())
        val cursorCourses: Cursor = db.rawQuery("SELECT * FROM dict", null)
        // on below line we are creating a new array list.
        val courseModalArrayList: ArrayList<String> = ArrayList()
        // moving our cursor to first position.
        Log.e("TAG", cursorCourses.toString())
        while (cursorCourses.moveToNext()) {
            // move the cursor to next row if there is any to read it's data
            courseModalArrayList.add(cursorCourses.getString(cursorCourses.getColumnIndex(COL_NAME)))
            Log.e("TAG", "text array list:  $courseModalArrayList")
        }

        if (cursorCourses.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                Log.e("TAG", cursorCourses.toString())
                courseModalArrayList.add(cursorCourses.getString(0))
                courseModalArrayList.add(cursorCourses.getString(1))
                courseModalArrayList.add(cursorCourses.getString(2))
                courseModalArrayList.add(cursorCourses.getString(3))
                courseModalArrayList.add(cursorCourses.getString(4))
                courseModalArrayList.add(
                    cursorCourses.getString(
                        cursorCourses.getColumnIndex(
                            COL_NAME
                        )
                    )
                )
                Log.e("TAG", "adding to it again $courseModalArrayList")
            } while (cursorCourses.moveToNext())
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        // at last closing our cursor
        // and returning our array list.
        cursorCourses.close()
        Log.e("TAG", "words : $courseModalArrayList")
        return true


//        val db = this.readableDatabase
////        val query =
////            """Select * from """ + TABLENAME + """ WHERE word = '""" + wordCheck + """' COLLATE NOCASE"""
//        val query = "Select * from $TABLENAME"
//        val result = db.rawQuery(query, null)
//        result.moveToFirst()
//        if (result.moveToFirst()) {
//            do {
//                val test = result.getString(result.getColumnIndex(COL_NAME))
//                Log.e("TAG", test)
//            }
//            while (result.moveToNext())
//        }

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLENAME ($COL_NAME TEXT)"
        db?.execSQL(createTable)
        insertIntoDatabase(db)
        Log.e("TAG", "creating database")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        //onCreate(db);
    }

    private fun insertIntoDatabase(db: SQLiteDatabase?) {
        val scopeTimer = CoroutineScope(CoroutineName("Timer"))
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {

            val queue1: RequestQueue = Volley.newRequestQueue(context)
            val url1 = "http://10.0.0.218:8000/return"
            val stringRequest1 = StringRequest(
                Request.Method.GET, url1,
                { stringResponse ->
                    Log.e("TAG", "Adding to datagbase $stringResponse")
                    for (dictionaryWord in stringResponse.split(" ")) {
                        with(db) { this?.execSQL("INSERT INTO $TABLENAME ($COL_NAME) VALUES('$dictionaryWord');") }
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
}