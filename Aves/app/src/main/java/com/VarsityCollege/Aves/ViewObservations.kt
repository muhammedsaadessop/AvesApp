package com.VarsityCollege.Aves

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ViewObservations : AppCompatActivity() {
    private val e = Errors()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_observation)
        supportActionBar?.hide()
        securGuard()
        setPage()
        upload()
        val loginBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ViewObservations, DisplayObservations::class.java)

                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }
        }

        onBackPressedDispatcher.addCallback(this, loginBack)
        val returnbutton = findViewById<ImageButton>(R.id.returnview)
        returnbutton.setOnClickListener()
        {
            val intent = Intent(this, DisplayObservations::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }
        val uploadImage:ImageView = findViewById(R.id.birdpic)
        uploadImage.setOnClickListener {
            val task = findViewById<EditText>(R.id.bird_nameedit)
            if (task.text.toString().isEmpty()) {
                val message = "TASK MUST BE ENTERED FIRST! "
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Choose an option")
                builder.setItems(arrayOf("Take a photo?", "Pick from gallery?","No Picture?")) { _, which ->
                    when (which) {
                        0 -> camera.launch(null)
                        1 -> {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"
                            galleryContent.launch(intent)
                        }
                        2 -> noPic()
                    }
                }
                val dialog = builder.create()
                dialog.show()
            }

        }


    }
    private val galleryContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val url: Uri? = result.data?.data

                when {
                    url != null -> {
                        val birdname = findViewById<EditText>(R.id.bird_nameedit)

                        val imageView = findViewById<ImageView>(R.id.birdpic)
                        imageView.setImageURI(url)

                        val store =
                            Firebase.storage.reference.child(birdname.text.toString().trim())

                        val choice = store.putFile(url)
                        choice.addOnSuccessListener {
                            Thread(Runnable {
                                Glide.get(applicationContext).clearDiskCache()
                            }).start()

                        }.addOnFailureListener {

                        }
                    }
                }
            }
        }

    // When you want to open the gallery:


    private val camera =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { photo: Bitmap? ->

            val birdname = findViewById<EditText>(R.id.bird_nameedit)

            val imageView = findViewById<ImageView>(R.id.birdpic)
            imageView.setImageBitmap(photo)


            val imageRef = Firebase.storage.reference.child(birdname.text.toString().trim())


            val imageStream = ByteArrayOutputStream()
            photo?.compress(Bitmap.CompressFormat.JPEG, 100, imageStream)
            val data = imageStream.toByteArray()

            val uploadDP = imageRef.putBytes(data)
            uploadDP.addOnSuccessListener {
                val message = "IMAGE UPLOADED "
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                val message = "INVALID IMAGE!"
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
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

    fun setPage() {
        try {
            // Connecting the ui elements to the variables.
            val birdname = findViewById<EditText>(R.id.bird_nameedit)
            val familyname = findViewById<EditText>(R.id.family_name)
            val description = findViewById<EditText>(R.id.birdnameedit)
            val colourdescrption = findViewById<EditText>(R.id.coldesc)
            val imageurl = findViewById<ImageView>(R.id.birdpic)
            val itemId = intent.getStringExtra("birdid")
            Log.d("birdid", itemId.toString())
            val db = FirebaseFirestore.getInstance()
            val user = FirebaseAuth.getInstance().currentUser

            val userEmail = user?.email


            val parts = userEmail!!.split('@', '.')
            val userID = parts[0] + parts[1]

            db.collection("aves").document(userID)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val birdObservationsMap =
                            document.data?.get("birdObservations") as? Map<String, Map<String, String>>
                        if (birdObservationsMap != null) {
                            val birdDetails = birdObservationsMap[itemId]
                            if (birdDetails != null) {
                                // Use the data from Firestore to populate the fields in your form
                                birdname.setText(birdDetails["birdname"])
                                familyname.setText(birdDetails["familyname"])
                                colourdescrption.setText(birdDetails["colourdesc"])
                                description.setText(birdDetails["description"])
                                val url = birdDetails["imagurl"]
                                if (url != null) {
                                    Glide.with(this)
                                        .load(url)
                                        .into(imageurl)
                                } else {
                                    Log.d("Firestore", "Image URL is null")
                                }

                            } else {
                                Log.d("Firestore", "No details for the specific bird in document")
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


        } catch (e: Exception) {
            // Handle the exception here

        }


    }

    private fun upload() {


        val birdname = findViewById<EditText>(R.id.bird_nameedit)
        val description = findViewById<EditText>(R.id.birdnameedit)
        val colourdesc = findViewById<EditText>(R.id.coldesc)
        val familyname = findViewById<EditText>(R.id.family_name)


        val save = findViewById<Button>(R.id.editbirds)
        save.setOnClickListener()
        {


            when {
                birdname.text.toString().isEmpty() -> {
                    Snackbar.make(birdname, e.emptyBirdName, Snackbar.LENGTH_SHORT).show()

                }

                description.text.toString().isEmpty() -> {
                    Snackbar.make(description, e.emptyDesc, Snackbar.LENGTH_SHORT).show()

                }

                familyname.text.toString().isEmpty() -> {

                    Snackbar.make(familyname, e.emptyFamilyName, Snackbar.LENGTH_SHORT).show()

                }


                colourdesc.text.toString().isEmpty() -> {
                    Snackbar.make(colourdesc, e.emptyColourDesc, Snackbar.LENGTH_SHORT).show()


                }


                else -> {
                    val user = FirebaseAuth.getInstance().currentUser

                    val userEmail = user?.email


                    val parts = userEmail!!.split('@', '.')
                    val userID = parts[0] + parts[1]
                    var picture: String

                    val db = FirebaseFirestore.getInstance()
                    Firebase.firestore
                    val storageRef =
                        Firebase.storage.reference.child(birdname.text.toString().trim())
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        picture = downloadUrl.toString()
                        if (picture.isEmpty()) {
                            noPic()

                        } else {
                            val birddetails = hashMapOf(

                                "birdname" to birdname.text.toString(),
                                "familyname" to familyname.text.toString(),
                                "colourdesc" to colourdesc.text.toString(),
                                "description" to description.text.toString(),
                                "imagurl" to picture

                            )
                            val birds = hashMapOf(
                                birdname.text.toString().lowercase() to birddetails


                            )
                            db.collection("aves").document(userID)
                                .set(hashMapOf("birdObservations" to birds), SetOptions.merge())
                                .addOnSuccessListener {
                                    Snackbar.make(
                                        description,
                                        "Added ${birdname.text.toString()}",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { exception ->
                                    Log.d(
                                        "failedwith",
                                        exception.toString()
                                    )
                                }


                        }

                    }
                }
            }

        }
    }

    fun noPic() {
        val birdname = findViewById<EditText>(R.id.birdname)
        val noPicstore = Firebase.storage.reference.child(birdname.text.toString().trim())
        val dispalys = findViewById<ImageView>(R.id.display)
        dispalys.setImageResource(R.drawable.icon)

        val convertNoImage = BitmapFactory.decodeResource(resources, R.drawable.icon)


        val drawPic = File(cacheDir, "bird.png")
        val gotten = FileOutputStream(drawPic)
        convertNoImage.compress(Bitmap.CompressFormat.PNG, 100, gotten)
        gotten.close()

        val url = Uri.fromFile(drawPic)
        val picsUpload = noPicstore.putFile(url)

        picsUpload.addOnSuccessListener {

        }.addOnFailureListener {

        }

    }
}