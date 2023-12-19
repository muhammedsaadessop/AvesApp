package com.VarsityCollege.Aves

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class BirdTiming : AppCompatActivity() {
    private val e = Errors()
    private var seconds = 0
    private var running = false
    private var wasRunning = false
    private var time: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bird_timing)
        loadBirdIds()
        if (savedInstanceState != null) {
            savedInstanceState.getInt("seconds")
            savedInstanceState.getBoolean("running")
            savedInstanceState.getBoolean("wasRunning")
        }

        val backBtn: Button = findViewById(R.id.backBtn)
        val historyBtn: Button = findViewById(R.id.historyBtn)

        runTimer()

        supportActionBar?.hide()
        val loginBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@BirdTiming, Home::class.java)

                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }


        }
        onBackPressedDispatcher.addCallback(this, loginBack)
        val back = findViewById<Button>(R.id.backBtn)
        back.setOnClickListener()
        {
            val intent = Intent(this, Home::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        val hist = findViewById<Button>(R.id.historyBtn)
        hist.setOnClickListener()
        {
            val intent = Intent(this, TimerHistory::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        securGuard()

        backBtn.setOnClickListener {

            if (!running) {
                intent = Intent(
                    this,
                    Home::class.java
                ) //This is to stop it from going to the page before.(I assume Home page)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please stop the time first!", Toast.LENGTH_SHORT).show()
            }
            // Button click action
        }
    }

    //Start button Functionality
    fun onStart(view: View?) {
        running = true
    }

    //Stop button Functionality

    fun onStop(view: View?) {
        running = false
        pausedTime = time



        val ids = findViewById<Spinner>(R.id.spinnerBtn)
        val chosenids = ids.selectedItem.toString()


        val files = FirebaseFirestore.getInstance()
        val loggedinuser = FirebaseAuth.getInstance().currentUser

        val regemail = loggedinuser?.email

        val parts = regemail!!.split('@', '.')
        val userID = parts[0] + parts[1]

        files.collection("aves").document(userID)
            .update(mapOf("birdObservations.$chosenids.time" to pausedTime))
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating document", e)
            }

        // Display a toast message
        Toast.makeText(this, "Time saved", Toast.LENGTH_SHORT).show()
    }

// antonio this will le the user set the data or reset the timer
    fun onReset(view: View?) {

        val savedchoices = AlertDialog.Builder(this)
        savedchoices.setTitle("Choose an option")
        savedchoices.setMessage("Would you like to reset the timer or load the saved time?")


        savedchoices.setPositiveButton("Reset Timer") { _, _ ->
            running = false
            seconds = 0
        }
        savedchoices.setNegativeButton("Load Saved Time") { _, _ ->

            val ids = findViewById<Spinner>(R.id.spinnerBtn)
            val chosenid = ids.selectedItem.toString()

            val timestore = FirebaseFirestore.getInstance()
            val user = FirebaseAuth.getInstance().currentUser

            val userEmail = user?.email

            val parts = userEmail!!.split('@', '.')
            val userID = parts[0] + parts[1]

            timestore.collection("aves").document(userID)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val data =
                            document.data?.get("birdObservations") as? Map<String, Map<String, String>>
                        if (data != null) {
                            val files = data[chosenid]
                            if (files != null) {
                                val loadtime = files["time"]
                                if (loadtime != null) {
                                    seconds = convertToSeconds(loadtime)
                                    running = true
                                }
                            }
                        }
                    }
                }

                .addOnFailureListener { exception ->
                    Log.d("Firestore", "get failed with ", exception)
                }
        }

        // Create and show the AlertDialog
        val display = savedchoices.create()
        display.show()
    }

    fun convertToSeconds(time: String): Int {
        val parts = time.split(":").map { it.toInt() }
        return parts[0] * 3600 + parts[1] * 60 + parts[2]
    }

    //Pause button Functionality
    // this will save the data to the database on pause
    override fun onPause() {
        super.onPause()
        wasRunning = running
        running = false

        val ids = findViewById<Spinner>(R.id.spinnerBtn)
        val chosenids = ids.selectedItem.toString()

        val files = FirebaseFirestore.getInstance()
        val loggedinuser = FirebaseAuth.getInstance().currentUser

        val regemail = loggedinuser?.email

        val parts = regemail!!.split('@', '.')
        val userID = parts[0] + parts[1]

        files.collection("aves").document(userID)
            .update(mapOf("birdObservations.$chosenids.time" to pausedTime))
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating document", e)
            }

        // Display a toast message
        Toast.makeText(this, "Time saved", Toast.LENGTH_SHORT).show()
    }

    //Resume button Functionality
    override fun onResume() {
        super.onResume()
        if (wasRunning) {
            running = true
        }
    }


    //Overriding and saving instance on screen data Functionality
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("seconds", seconds)
        outState.putBoolean("running", running)
        outState.putBoolean("wasRunning", wasRunning)
    }

    //Running the timer Functionality
    private fun runTimer() {
        val timeView = findViewById<TextView>(R.id.textview) //Finding the timer text view

        val handler = Handler(Looper.getMainLooper()) //Functionality call handler

        handler.post(object : Runnable {
            override fun run() //When the timer is running the following methods will explain how it should be done.
            {
                val hours = seconds / 3600 //how hours are calculated
                val minutes = seconds % 3600 / 60 //how the minutes are calculated
                val secs = seconds % 60 //how the time in seconds is calculated

                time = String.format(
                    Locale.getDefault(),
                    "%d:%02d:%02d",
                    hours,
                    minutes,
                    secs
                ) // the format will follow this here

                //Call the time string to the "timeView" which is the name of the textView in the xml file

                timeView.text = time

                if (running) //This here states that if the page is running the seconds must increment
                {
                    seconds++ //incrementation

                }

                handler.postDelayed(this, 1000) //Delayed by a 1000 of second.

            }

        })
    }

    companion object {
        var pausedTime: String? = null
    }

    private fun securGuard() {
// this checks user tokens
        // if invalid forces the user to login again
        // if deleted forces the user out of the app
        val tempusSecurity = FirebaseAuth.getInstance()
        tempusSecurity.addAuthStateListener { firebaseAuth ->
            when (firebaseAuth.currentUser) {
                null -> {
                    val intent = Intent(this, Login::class.java)

                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()

                }
            }
        }
    }

    fun loadBirdIds() {
        val birdids = findViewById<Spinner>(R.id.spinnerBtn)
        val hashmapdata = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        val userEmail = user?.email

        val parts = userEmail!!.split('@', '.')
        val userID = parts[0] + parts[1]
        hashmapdata.collection("aves").document(userID)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val idnames =
                        document.data?.get("birdObservations") as? Map<String, Map<String, String>>
                    if (idnames != null) {

                        val ids = idnames.keys.toList()

                        ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_spinner_item,
                            ids
                        ).also { adapter ->

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                            birdids.adapter = adapter
                        }
                    } else {
                        Log.d("Firestore", "No birdObservations in document")
                    }
                } else {
                    Log.d("Firestore", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
            }
    }

}