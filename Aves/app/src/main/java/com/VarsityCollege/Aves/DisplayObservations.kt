package com.VarsityCollege.Aves

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DisplayObservations:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_observations)
        supportActionBar?.hide()
        securGuard()
        print()
        val loginBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@DisplayObservations, Home::class.java)

                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }
        }
        onBackPressedDispatcher.addCallback(this, loginBack)
        val returnbutton = findViewById<ImageButton>(R.id.returntomenu3)
        returnbutton.setOnClickListener()
        {
            val intent = Intent(this , Home::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }
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
    // this gets the data from firebase and displays to the user
    // creates an intent allowing the user to edit the data
    fun print()
    {
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        val userEmail = user?.email

        val parts = userEmail!!.split('@', '.')
        val userID = parts[0] + parts[1]
        db.collection("aves").document(userID)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val birddata = document.data?.get("birdObservations") as? Map<String, Map<String, String>>
                    if (birddata != null) {
                        val listofdata = birddata.values.map { BirdDetails(it["birdname"]!!, it["familyname"]!!, it["colourdesc"]!!, it["description"]!!, it["imagurl"]!!) }
                        recyclerview.adapter = AvesAdapter(listofdata)
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
// data class for the adapter to store the data
data class BirdDetails(
    val birdname: String,
    val familyname: String,
    val colourdesc: String,
    val description: String,
    val imagurl: String
)
// this is the adapter that allows the user to print using the recycler view
class AvesAdapter(private val birds: List<BirdDetails>) : RecyclerView.Adapter<AvesAdapter.BirdViewHolder>() {

    inner class BirdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val birdName: TextView = view.findViewById(R.id.bird_nameedit2)
        val familyName: TextView = view.findViewById(R.id.family_name2)
        val birdImage: ImageView = view.findViewById(R.id.birdpics)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.observation_list_item, parent, false)
        return BirdViewHolder(view)
    }

    override fun onBindViewHolder(holder: BirdViewHolder, position: Int) {
        val bird = birds[position]
        holder.birdName.text = bird.birdname
        holder.familyName.text = bird.familyname

        Glide.with(holder.birdImage.context).load(bird.imagurl).into(holder.birdImage)




        holder.itemView.setOnClickListener {
            val clickedData = birds[position]
            val context = holder.itemView.context
            val intent = Intent(context, ViewObservations::class.java)
            intent.putExtra("birdid", clickedData.birdname.lowercase())


            context.startActivity(intent)

        }
    }

    override fun getItemCount() = birds.size
}
