package com.VarsityCollege.Aves

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TimerHistory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timer_history_page)
        supportActionBar?.hide()

        val rtButton = findViewById<ImageButton>(R.id.returnbtn)
        rtButton.setOnClickListener {
            val intent = Intent(this, BirdTiming::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        print()
    }

    private fun print() {
        val recyclerviewhist = findViewById<RecyclerView>(R.id.recyclerViewhist)
        recyclerviewhist.layoutManager = LinearLayoutManager(this)
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email

        if (userEmail != null) {
            val parts = userEmail.split('@', '.')
            val userID = parts[0] + parts[1]
            db.collection("aves").document(userID)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val birdData = document.data?.get("birdObservations") as? Map<String, Map<String, String>>
                        if (birdData != null) {
                            val listofdata = birdData.values.map {
                                BirdDetails(
                                    it["birdname"] ?: "",
                                    it["familyname"] ?: "",
                                    it["colourdesc"] ?: "",
                                    it["description"] ?: "",
                                    it["imagurl"] ?: "",
                                    it["time"] ?: ""
                                )
                            }

                            recyclerviewhist.adapter = AvesAdapter(listofdata)
                            Log.d("Firestore", listofdata.toString())
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

    data class BirdDetails(
        val birdname: String,
        val familyname: String,
        val colourdesc: String,
        val description: String,
        val imagurl: String,
        val time: String
    )

    class AvesAdapter(private val birds: List<BirdDetails>) : RecyclerView.Adapter<AvesAdapter.BirdViewHolder>() {

        inner class BirdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val birdName: TextView = view.findViewById(R.id.bird_name)
            val time: TextView = view.findViewById(R.id.bird_time_date)
            val birdImage: ImageView = view.findViewById(R.id.bird_image)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.aves_timer_cv, parent, false)
            return BirdViewHolder(view)
        }

        override fun getItemCount() = birds.size

        override fun onBindViewHolder(holder: BirdViewHolder, position: Int) {
            val bird = birds[position]
            holder.birdName.text = bird.birdname
            holder.time.text = bird.time


            Glide.with(holder.itemView.context).load(bird.imagurl).into(holder.birdImage)

            holder.itemView.setOnClickListener {
                val clickedData = birds[position]
                val context = holder.itemView.context
                val intent = Intent(context, ViewObservations::class.java)
                intent.putExtra("birdid", clickedData.birdname.lowercase())
                context.startActivity(intent)
            }
            }
        }
}