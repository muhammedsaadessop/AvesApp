package com.VarsityCollege.Aves

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference


class Home : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var userpos: Point
    private lateinit var permissions: LocationPermissionHelper
    private lateinit var sidemenu: DrawerLayout
    private lateinit var actions: ActionBarDrawerToggle
    private lateinit var find: FloatingActionButton
    private lateinit var findbird: EditText
    private var results: List<BirdSearchResult> = emptyList()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        // this initiates the floating menu and allows for the population of the buttons
        val quickmenu: FloatingActionMenu = findViewById(R.id.menu)

        val profiles: FloatingActionButton = findViewById(R.id.profile_button)
        profiles.setOnClickListener {
            val ProfilePage = Intent(this, Profile::class.java)

            startActivity(ProfilePage)
            overridePendingTransition(0, 0)
            finish()
            quickmenu.close(true)
        }

        val settings: FloatingActionButton = findViewById(R.id.settings_button)
        settings.setOnClickListener {

            val Settings = Intent(this, Map_settings::class.java)

            startActivity(Settings)
            overridePendingTransition(0, 0)
            finish()

            quickmenu.close(true)
        }

        val addbird: FloatingActionButton = findViewById(R.id.add)
        addbird.setOnClickListener {

            val intent = Intent(this, AddObservations::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

            quickmenu.close(true)
        }
        val avesSession: FloatingActionButton = findViewById(R.id.timer)
        avesSession.setOnClickListener {

            val intent = Intent(this, BirdTiming::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

            quickmenu.close(true)
        }
        val exit: FloatingActionButton = findViewById(R.id.exit)
        exit.setOnClickListener {
            signout()
            val intent = Intent(this, Login::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
            quickmenu.close(true)
        }
        //this initiates the side menu that allows for the intents of the side menu options
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    val intent = Intent(this, Profile::class.java)

                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.menu_settings -> {
                    val intent = Intent(this, Map_settings::class.java)

                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.menu_logout -> {
                    signout()
                    val intent = Intent(this, Login::class.java)

                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.addobservation -> {

                    val intent = Intent(this, AddObservations::class.java)
                    intent.putExtra("USER_LATITUDE", userpos.latitude())
                    intent.putExtra("USER_LONGITUDE", userpos.longitude())
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.menu_life_list -> {

                    val intent = Intent(this, DisplayObservations::class.java)

                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.timer -> {

                    val intent = Intent(this, BirdTiming::class.java)

                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.menu_locations -> {

                    val intent = Intent(this, HotspotLocations::class.java)

                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                else -> false
            }

        }
        details()
        map = findViewById(R.id.mapView)
        securGuard()
        sidemenu = findViewById(R.id.drawer_layout)
        actions =
            ActionBarDrawerToggle(this, sidemenu, R.string.nav_open, R.string.nav_close)

        sidemenu.addDrawerListener(actions)
        actions.syncState()
// loads the map type and style
        map?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS,
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    fetchHotspots()
                    addObservationToMap()
                }
            }
        )
        // this displays the data for the observations that was added to map
        val obsinfo = findViewById<CardView>(R.id.Observation_infobox)
        obsinfo.setCardBackgroundColor(Color.TRANSPARENT)
        obsinfo.cardElevation = 0f

        obsinfo.preventCornerOverlap = false
// makes sure the permissions are valid
        permissions = LocationPermissionHelper(WeakReference(this))
        permissions.checkPermissions {
            onMapReady()
            fetchHotspots()
            addObservationToMap()
        }

        find = findViewById(R.id.search_btn)
        findbird = findViewById(R.id.searchBirdText)

        find.setOnClickListener {
            toggleSearchInputVisibility()
        }
// this allows for searching of a bird
        val actionsearch = findViewById<ImageView>(R.id.search_initiate_button)
        actionsearch.setOnClickListener {

            val searchText = findbird.text.toString().trim()

            if (searchText.isNotEmpty()) {

                CoroutineScope(Dispatchers.IO).launch {

                    val userreturn = performSearch(searchText, "ZA")

                    withContext(Dispatchers.Main) {

                        results = userreturn
                        displaySearchResults(userreturn)
                    }
                }
            } else {

                results = emptyList()
                displaySearchResults(results)
            }
        }

        val dataload = findViewById<CardView>(R.id.infoBox)
        dataload.setCardBackgroundColor(Color.TRANSPARENT)
        dataload.cardElevation = 0f

        dataload.preventCornerOverlap = false


    }
// this is to populate the profile nav header in the side menu
    fun details() {
        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email
        if (userEmail != null) {
            val parts = userEmail!!.split('@', '.')
            val userID = parts[0] + parts[1]
            val storage = FirebaseStorage.getInstance()
            val imageRef = storage.getReference().child(userID)
            val sidemenuprofile: NavigationView = findViewById(R.id.nav_view)
            val email =
                sidemenuprofile.getHeaderView(0).findViewById<TextView>(R.id.nav_header_info)
            val name = sidemenuprofile.getHeaderView(0).findViewById<TextView>(R.id.nav_header_name)
            val image =
                sidemenuprofile.getHeaderView(0).findViewById<ImageView>(R.id.nav_header_image)
            email.text = userEmail
            imageRef.downloadUrl.addOnSuccessListener { Uri ->
                val urldownload = Uri.toString()

                val profileimage = RequestOptions().transform(CircleCrop())
                Glide.with(this)
                    .load(urldownload)
                    .apply(profileimage)
                    .into(image)
            }

            image.setOnClickListener()
            {


                val profileimage = AlertDialog.Builder(this)
                profileimage.setTitle("Choose an option")
                profileimage.setItems(
                    arrayOf(
                        "Take a photo?",
                        "Pick from gallery?",

                        )
                ) { _, which ->
                    when (which) {

                        0 -> camera.launch(null)
                        1 -> galleryContent.launch("imageURL/*")

                    }
                }
                val actionshow = profileimage.create()
                actionshow.show()

            }



            Log.d("userid", userID)
            val userfiles = FirebaseFirestore.getInstance()
            val data = userfiles.collection("aves").document(userID)
            data.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        val profiledata = document.get("userDetails") as Map<String, Any>
                        val names = profiledata["firstname"].toString()
                        val surnames = profiledata["surname"].toString()
                        name.text = names + "" + surnames

                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    private val galleryContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { url: Uri? ->


            when {
                url != null -> {
                    val user = FirebaseAuth.getInstance().currentUser

                    val userEmail = user?.email
                    val parts = userEmail!!.split('@', '.')
                    val userID = parts[0] + parts[1]
                    val navigationView: NavigationView = findViewById(R.id.nav_view)
                    var image = navigationView.getHeaderView(0)
                        .findViewById<ImageView>(R.id.nav_header_image)

                    image.setImageURI(url)

                    val store =
                        Firebase.storage.reference.child(userID.lowercase())

                    val choice = store.putFile(url)
                    choice.addOnSuccessListener {

                    }.addOnFailureListener {

                    }
                }
            }
        }
    private val camera =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { photo: Bitmap? ->
            val user = FirebaseAuth.getInstance().currentUser

            val userEmail = user?.email
            val parts = userEmail!!.split('@', '.')
            val userID = parts[0] + parts[1]
            val navigationView: NavigationView = findViewById(R.id.nav_view)
            var image =
                navigationView.getHeaderView(0).findViewById<ImageView>(R.id.nav_header_image)



            image.setImageBitmap(photo)


            val imageRef = Firebase.storage.reference.child(userID.lowercase())


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
// sign the user out if they click logout
    private fun signout() {
        FirebaseAuth.getInstance().signOut()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actions.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
// sets the api to ZA and allows for the population as per the hotspot
    private suspend fun performSearch(
        query: String,
        regionCode: String = "ZA"
    ): List<BirdSearchResult> {
        val results = mutableListOf<BirdSearchResult>()
        try {
            Log.d("performSearch", "Search Query: $query")

            val webserver = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.ebird.org/v2/ref/taxonomy/ebird?search=$query&region=$regionCode")
                .addHeader("X-eBirdApiToken", "p4emurtnu9qi")
                .build()
            val response = withContext(Dispatchers.IO) {
                webserver.newCall(request).execute()
            }

            val responseData = response.body?.string()


            Log.d("performSearch", "Response Data: $responseData")

            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                // Check if responseData starts with "SCIENTIFIC_NAME,COMMON_NAME,SPECIES_CODE,..." (indicating CSV format)
                if (responseData.startsWith("SCIENTIFIC_NAME,COMMON_NAME,SPECIES_CODE,")) {
                    val datareturn = parseSearchResultsCSV(responseData, query)
                    results.addAll(datareturn)
                    Log.d("performSearch", "Search results count: ${datareturn.size}")
                } else if (responseData.startsWith("[")) {
                    val bracketreturn = parseSearchResultsAsArray(responseData, query)
                    results.addAll(bracketreturn)
                    Log.d("performSearch", "Search results count: ${bracketreturn.size}")
                } else if (responseData.startsWith("{")) {
                    val curlyreturn = parseSearchResultsAsObject(responseData, query)
                    if (curlyreturn != null) {
                        results.add(curlyreturn)
                    }
                    Log.d("performSearch", "Search result added: $curlyreturn")
                } else {
                    Log.e("performSearch", "Unknown response format: $responseData")
                }
            } else {

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Home, "No results found.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {

            withContext(Dispatchers.Main) {

                Log.e("performSearch", "Error: ${e.message}", e)
                Toast.makeText(this@Home, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return results
    }

// this formats the search results
    private fun parseSearchResultsCSV(
        responseData: String?,
        filter: String = ""
    ): List<BirdSearchResult> {
        val results = mutableListOf<BirdSearchResult>()

        responseData?.let { csvData ->
            // Split the CSV data into lines
            val datarow = csvData.split('\n')

            for (i in 1 until datarow.size) {
                val value = datarow[i]

                val values = value.split(',')

                if (values.size >= 15) {
                    val scientificName = values[0]
                    val commonName = values[1]


                    if (commonName.contains(filter, ignoreCase = true)) {
                        val speciesCode = values[2]
                        val category = values[3]
                        val taxonOrder = values[4]
                        val comNameCodes = values[5]
                        val sciNameCodes = values[6]
                        val bandingCodes = values[7]
                        val order = values[8]
                        val familyComName = values[9]
                        val familySciName = values[10]
                        val reportAs = values[11]
                        val extinct = values[12].toBoolean()
                        val extinctYear = values[13].toIntOrNull()
                        val familyCode = values[14]

                        val birdSearchResult = BirdSearchResult(
                            scientificName,
                            commonName,
                            speciesCode,
                            category,
                            taxonOrder,
                            comNameCodes,
                            sciNameCodes,
                            bandingCodes,
                            order,
                            familyComName,
                            familySciName,
                            reportAs,
                            extinct,
                            extinctYear,
                            familyCode
                        )
                        results.add(birdSearchResult)
                    }
                }
            }
        }

        return results
    }

    private fun parseSearchResultsAsArray(
        responseData: String,
        filter: String
    ): List<BirdSearchResult> {
        val results = mutableListOf<BirdSearchResult>()
        try {
            val jsonArray = JSONArray(responseData)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val commonName = jsonObject.optString("COMMON_NAME")

                if (commonName.contains(filter, ignoreCase = true)) {
                    val scientificName = jsonObject.optString("SCIENTIFIC_NAME")
                    val speciesCode = jsonObject.optString("SPECIES_CODE")
                    val category = jsonObject.optString("CATEGORY")
                    val taxonOrder = jsonObject.optString("TAXON_ORDER")
                    val comNameCodes = jsonObject.optString("COM_NAME_CODES")
                    val sciNameCodes = jsonObject.optString("SCI_NAME_CODES")
                    val bandingCodes = jsonObject.optString("BANDING_CODES")
                    val order = jsonObject.optString("ORDER")
                    val familyComName = jsonObject.optString("FAMILY_COM_NAME")
                    val familySciName = jsonObject.optString("FAMILY_SCI_NAME")
                    val reportAs = jsonObject.optString("REPORT_AS")
                    val extinct = jsonObject.optBoolean("EXTINCT")
                    val extinctYear = jsonObject.optInt("EXTINCT_YEAR")
                    val familyCode = jsonObject.optString("FAMILY_CODE")

                    val birdSearchResult = BirdSearchResult(
                        scientificName,
                        commonName,
                        speciesCode,
                        category,
                        taxonOrder,
                        comNameCodes,
                        sciNameCodes,
                        bandingCodes,
                        order,
                        familyComName,
                        familySciName,
                        reportAs,
                        extinct,
                        extinctYear,
                        familyCode
                    )
                    results.add(birdSearchResult)
                }
            }

            Log.d("parseSearchResultsAsArray", "Parsed ${results.size} results")
        } catch (e: JSONException) {
            Log.e("parseSearchResultsAsArray", "Error parsing JSON: ${e.message}", e)
        }
        return results
    }

    private fun parseSearchResultsAsObject(
        responseData: String,
        filter: String
    ): BirdSearchResult? {
        try {
            val jsonObject = JSONObject(responseData)
            val commonName = jsonObject.optString("COMMON_NAME")

            if (commonName.contains(filter, ignoreCase = true)) {
                val scientificName = jsonObject.optString("SCIENTIFIC_NAME")
                val speciesCode = jsonObject.optString("SPECIES_CODE")
                val category = jsonObject.optString("CATEGORY")
                val taxonOrder = jsonObject.optString("TAXON_ORDER")
                val comNameCodes = jsonObject.optString("COM_NAME_CODES")
                val sciNameCodes = jsonObject.optString("SCI_NAME_CODES")
                val bandingCodes = jsonObject.optString("BANDING_CODES")
                val order = jsonObject.optString("ORDER")
                val familyComName = jsonObject.optString("FAMILY_COM_NAME")
                val familySciName = jsonObject.optString("FAMILY_SCI_NAME")
                val reportAs = jsonObject.optString("REPORT_AS")
                val extinct = jsonObject.optBoolean("EXTINCT")
                val extinctYear = jsonObject.optInt("EXTINCT_YEAR")
                val familyCode = jsonObject.optString("FAMILY_CODE")

                return BirdSearchResult(
                    scientificName,
                    commonName,
                    speciesCode,
                    category,
                    taxonOrder,
                    comNameCodes,
                    sciNameCodes,
                    bandingCodes,
                    order,
                    familyComName,
                    familySciName,
                    reportAs,
                    extinct,
                    extinctYear,
                    familyCode
                )
            }
        } catch (e: JSONException) {
            Log.e("parseSearchResultsAsObject", "Error parsing JSON: ${e.message}", e)
        }
        return null
    }

// this displays the search result
    private fun displaySearchResults(results: List<BirdSearchResult>) {
        val Message = if (results.isEmpty()) {
            "No results found."
        } else {
            val Text =
                results.joinToString("\n") { "${it.COMMON_NAME} (${it.SCIENTIFIC_NAME})" }
            "Search Results:\n\n$Text"
        }

        val alertDialog = AlertDialog.Builder(this@Home)
            .setTitle("Search Results")
            .setMessage(Message)
            .setPositiveButton("OK", null)
            .create()

        alertDialog.show()
    }

    private fun toggleSearchInputVisibility() {
        val visible = findViewById<TextInputLayout>(R.id.textInputLayout)
        if (visible.visibility == View.GONE) {
            visible.visibility = View.VISIBLE
        } else {
            visible.visibility = View.GONE
        }
    }


    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        map.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

// this check the user locations and reports it
    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        map.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        map.gestures.focalPoint = map.getMapboxMap().pixelForCoordinate(it)
        userpos = it
        Log.d("UserLocation", "Location: $userpos")
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    data class CustomPointAnnotation(
        val pointAnnotation: PointAnnotation,
        val hotspot: Hotspot
    )
// this populates hotspots from the ebird api as per the documentation
    fun fetchHotspots() {
    try {
        var maxDistance: Double = 50.0
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.ebird.org/v2/ref/hotspot/ZA-GT")
                .addHeader("X-eBirdApiToken", "p4emurtnu9qi")
                .build()
            val Hotspotdata = FirebaseFirestore.getInstance()
            val user = FirebaseAuth.getInstance().currentUser
            val userEmail = user?.email
            val parts = userEmail!!.split('@', '.')
            val userID = parts[0] + parts[1]
            val docRef = Hotspotdata.collection("aves").document(userID)
            try {
                val document =
                    docRef.get().await() // Use await() to make Firestore call asynchronous
                if (document != null) {
                    val userSettings = document.get("userSettings") as? Map<String, Any>
                    if (userSettings != null) {
                        val km = userSettings["km"] as? Boolean ?: false
                        Log.d("TAG", "User preference KM: $km")
                        val miles = userSettings["miles"] as? Boolean ?: false
                        Log.d("TAG", "User preference Miles: $miles")
                        val distance =
                            (userSettings["maxDistance"] as? String)?.toDoubleOrNull()
                        maxDistance =
                            distance ?: 10.0
                        Log.d("TAG", "Max Distance: $maxDistance")

                        val userPrefersKm = km && !miles
                        Log.d("TAG", "User Unit of Measure Flag: $userPrefersKm")
                        client.newCall(request).execute().use { response ->
                            val responseDatas = response.body?.string()
                            if (responseDatas != null) {
                                Log.d("Hotspots", responseDatas)
                                val hotdata = responseDatas.lines()
                                for (line in hotdata) {
                                    if (line.isNotBlank()) {
                                        try {
                                            val locationfield = line.split(',')
                                            if (locationfield.size >= 8) {
                                                val birdhotspot = Hotspot(
                                                    locId = locationfield[0],
                                                    locName = locationfield[6].trim('"'),
                                                    countryCode = locationfield[1],
                                                    lat = locationfield[4].toDouble(),
                                                    lng = locationfield[5].toDouble(),
                                                    comName = locationfield[2].trim('"'),
                                                    sciName = locationfield[3].trim('"'),
                                                    obsDt = locationfield[7]
                                                )
                                                val distance: Double
                                                if (userPrefersKm) {
                                                    distance = calculateDistanceKm(
                                                        userpos.latitude(),
                                                        userpos.longitude(),
                                                        birdhotspot.lat,
                                                        birdhotspot.lng
                                                    )
                                                } else {
                                                    distance = calculateDistanceMiles(
                                                        userpos.latitude(),
                                                        userpos.longitude(),
                                                        birdhotspot.lat,
                                                        birdhotspot.lng
                                                    )
                                                }
                                                // Only add the hotspot to the map if it is within the maximum distance
                                                if (distance <= maxDistance) {
                                                    try {
                                                        // Check for recent observations at this hotspot
                                                        val sightingsRequest = Request.Builder()
                                                            .url("https://api.ebird.org/v2/data/obs/${birdhotspot.locId}/recent")
                                                            .addHeader(
                                                                "X-eBirdApiToken",
                                                                "p4emurtnu9qi"
                                                            )
                                                            .build()
                                                        client.newCall(sightingsRequest).execute()
                                                            .use { sightingsResponse ->
                                                                val sightingsData =
                                                                    sightingsResponse.body?.string()
                                                                val hasObservations =
                                                                    !sightingsData.isNullOrEmpty() && sightingsData != "[]"
                                                                // Add the hotspot to the map with the appropriate icon
                                                                addHotspotToMap(
                                                                    birdhotspot,
                                                                    hasObservations
                                                                )
                                                            }
                                                    }catch(E:Exception)
                                                    {

                                                    }
                                                }
                                            } else {
                                                Log.e(
                                                    "Hotspots",
                                                    "Error parsing hotspot: $line - Insufficient fields"
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Log.e("Hotspots", "Error parsing hotspot: $line", e)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error fetching settings from Firestore", e)
            }
        }
    }catch (E:Exception)
    {

    }
    }

    //code attribution
    // Stack Overflow. (n.d.). Calculate distance between two GeoLocation. [online] Available at: https://stackoverflow.com/questions/30842011/calculate-distance-between-two-geolocation [Accessed 14 Oct. 2023].
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadiusKm * c
    }

    //code attribution
    //Stack Overflow. (n.d.). Calculate distance between two GeoLocation. [online] Available at: https://stackoverflow.com/questions/30842011/calculate-distance-between-two-geolocation [Accessed 14 Oct. 2023].
    fun calculateDistanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusMiles = 3958.8
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusMiles * c
    }

// this adds them to the map
    fun addHotspotToMap(hotspot: Hotspot, hasObservations: Boolean) {
try {


    val resolution = if (hasObservations) R.drawable.green_marker else R.drawable.red_marker
    bitmapFromDrawableRes(this@Home, resolution)?.let { iconImage ->
        try {
            val Api = map?.annotations
            val pointsAnnotationManager =
                Api?.createPointAnnotationManager()!!
            pointsAnnotationManager.iconAllowOverlap = true
            val Options: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(hotspot.lng, hotspot.lat))
                .withIconImage(iconImage)

            val eyemarker = pointsAnnotationManager?.create(Options)

            eyemarker?.let {
                val custom = CustomPointAnnotation(it, hotspot)
                this.custom.add(custom)
            }
        } catch (E: Exception) {
        }
    }

    map.getMapboxMap().addOnMapClickListener { clickPoint ->
        val Pixels = 50.0
        for (customAnnotation in custom) {
            val cooridnates = map.getMapboxMap()
                .pixelForCoordinate(customAnnotation.pointAnnotation.point)
            val Point = map.getMapboxMap().pixelForCoordinate(clickPoint)
            if (cooridnates != null && Point != null) {
                val dx = cooridnates.x - Point.x
                val dy = cooridnates.y - Point.y
                val range = Math.sqrt((dx * dx + dy * dy).toDouble())
                if (range < Pixels) {

                    onHotspotAnnotationClick(customAnnotation)
                    return@addOnMapClickListener true
                }
            }
        }
        false // No annotation was clicked
    }
}catch (E:Exception){}
    }
// once the user adds an observation it adds it to the map
    private fun addObservationToMap() {
        val storedobservations = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email
        val parts = userEmail!!.split('@', '.')
        val userID = parts[0] + parts[1]
        val obs = storedobservations.collection("aves").document(userID)


        val mapdata = mutableMapOf<PointAnnotation, Map<String, Any>>()

        obs.get().addOnSuccessListener { document ->
            if (document != null) {
                try {
                    val birdObservations = document.get("birdObservations") as? Map<String, Any>
                    birdObservations?.forEach { (key, value) ->
                        val birdDetails = value as? Map<String, Any>
                        val latitude = birdDetails?.get("latitude") as? Double
                        val longitude = birdDetails?.get("longitude") as? Double


                        bitmapFromDrawableRes(
                            this@Home,
                            R.drawable.baseline_birdobservation_marker_26
                        )?.let { iconImage ->
                            val Api = map?.annotations
                            val mangers =
                                Api?.createPointAnnotationManager()!!
                            mangers.iconAllowOverlap = true
                            val options: PointAnnotationOptions =
                                PointAnnotationOptions()
                                    .withPoint(Point.fromLngLat(longitude!!, latitude!!))
                                    .withIconImage(iconImage)

                            val points =
                                mangers.create(options)


                            mapdata[points] = value


                            mangers.addClickListener { clickedPointAnnotation ->
                                val clickedBirdDetails = mapdata[clickedPointAnnotation]

                                val birdname = clickedBirdDetails?.get("birdname") as? String
                                val familyname = clickedBirdDetails?.get("familyname") as? String
                                val colourdesc = clickedBirdDetails?.get("colourdesc") as? String
                                val description = clickedBirdDetails?.get("description") as? String
                                val imageUrl = clickedBirdDetails?.get("imagurl") as? String


                                val birdNameTextView: TextView = findViewById(R.id.birdName)
                                val birdNameText = "Bird Name:\n $birdname"
                                val birdNameSpannable = SpannableString(birdNameText)
                                birdNameSpannable.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    0, 9,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                birdNameSpannable.setSpan(
                                    ForegroundColorSpan(
                                        ContextCompat.getColor(
                                            this,
                                            R.color.dark_green
                                        )
                                    ),
                                    0, 9,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                birdNameTextView.text = birdNameSpannable

                                val familyNameTextView: TextView =
                                    findViewById(R.id.familyNameTextView)
                                val familyNameText = "Family Name: \n $familyname"
                                val familyNameSpannable = SpannableString(familyNameText)
                                familyNameSpannable.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    0, 11,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                familyNameSpannable.setSpan(
                                    ForegroundColorSpan(
                                        ContextCompat.getColor(
                                            this,
                                            R.color.dark_green
                                        )
                                    ),
                                    0, 11,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                familyNameTextView.text = familyNameSpannable


                                val colourDescriptionTextView: TextView =
                                    findViewById(R.id.colour_descriptionTextView)
                                val colourDescriptionText = "Colour Description:\n $colourdesc"
                                val colourDescriptionSpannable =
                                    SpannableString(colourDescriptionText)
                                colourDescriptionSpannable.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    0, 18,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                colourDescriptionSpannable.setSpan(
                                    ForegroundColorSpan(
                                        ContextCompat.getColor(
                                            this,
                                            R.color.dark_green
                                        )
                                    ),
                                    0, 18,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                colourDescriptionTextView.text = colourDescriptionSpannable

                                val birdDescriptionTextView: TextView =
                                    findViewById(R.id.bird_DescriptionTextView)
                                val birdDescriptionText = "Description:\n $description"
                                val birdDescriptionSpannable = SpannableString(birdDescriptionText)

                                val boldStartIndex = birdDescriptionText.indexOf("Description:")
                                val boldEndIndex = boldStartIndex + 12

                                birdDescriptionSpannable.setSpan(
                                    StyleSpan(Typeface.BOLD),

                                    boldStartIndex, boldEndIndex,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )

                                birdDescriptionSpannable.setSpan(
                                    ForegroundColorSpan(
                                        ContextCompat.getColor(
                                            this,
                                            R.color.dark_green
                                        )
                                    ),
                                    boldStartIndex, boldEndIndex,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )

                                birdDescriptionTextView.text = birdDescriptionSpannable


                                val colorImageView: ImageView = findViewById(R.id.colorImageView)

                                Picasso.get().load(imageUrl).into(colorImageView)

                                val observationInfoBox: CardView =
                                    findViewById(R.id.Observation_infobox)
                                observationInfoBox.visibility = View.VISIBLE



                                true
                            }

                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading document", e)
                }
            } else {
                Log.d(TAG, "No such document")
            }
        }
    }


    private fun onMapReady() {
        map.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        map.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }
    }

    private fun setupGesturesListener() {
        map.gestures.addOnMoveListener(onMoveListener)
    }

    private val marker = mutableMapOf<Long, Hotspot>()
    private val custom = mutableListOf<CustomPointAnnotation>()

    private suspend fun fetchHotspotDetails(locId: String): HotspotDetails {
        val server = OkHttpClient()
        val urlbuilder = Request.Builder()
            .url("https://api.ebird.org/v2/ref/hotspotInfo/$locId")
            .addHeader("X-eBirdApiToken", "p4emurtnu9qi")
            .build()
        val response = server.newCall(urlbuilder).execute()
        return if (response.isSuccessful) {
            val responseDatas = response.body?.string()
            if (responseDatas != null) {

                Gson().fromJson(responseDatas, HotspotDetails::class.java)
            } else {
                HotspotDetails()
            }
        } else {
            HotspotDetails()
        }
    }

    var selection: CustomPointAnnotation? = null
// if the user clicks on a hotspot it displays it to the user and what is there
    private fun onHotspotAnnotationClick(customAnnotation: CustomPointAnnotation) {
    try {


        selection = null
        val hotspot = customAnnotation.hotspot
        Log.d("Hotspot", "Hotspot Latitude: ${hotspot.lat}")
        Log.d("Hotspot", "Hotspot Longitude: ${hotspot.lng}")

        CoroutineScope(Dispatchers.IO).launch {
            val server = OkHttpClient()
            Log.d("Hotspot", "Fetching data for hotspot: ${hotspot.locId}")
            val urlbuilder = Request.Builder()
                .url("https://api.ebird.org/v2/data/obs/${hotspot.locId}/recent")
                .addHeader("X-eBirdApiToken", "p4emurtnu9qi")
                .build()
            server.newCall(urlbuilder).execute().use { response ->
                val responseDatas = response.body?.string()
                if (responseDatas != null && responseDatas != "[]") {
                    Log.d("API Response", responseDatas)
                    try {
                        val birdSightings = parseBirdSightings(responseDatas)
                        val scientificName = parseScientificName(responseDatas)
                        val locationName = parseLocationName(responseDatas)
                        val date = parseDate(responseDatas)
                        val hotspotDetails = fetchHotspotDetails(hotspot.locId)
                        withContext(Dispatchers.Main) {
                            val infodata =
                                findViewById<CardView>(R.id.infoBox)
                            infodata.visibility = View.VISIBLE
                            val birdDescriptionTextView =
                                infodata.findViewById<TextView>(R.id.birdDescriptionTextView)
                            val birdSightingsText = if (birdSightings.isEmpty()) {
                                "No recent sightings"
                            } else {
                                val birdSightingsFormatted = birdSightings.joinToString("\n• ")
                                "Birds sighted:\n\n• $birdSightingsFormatted"
                            }
                            val Sighting = SpannableString(birdSightingsText)
                            Sighting.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0, 14,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            Sighting.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        this@Home,
                                        R.color.dark_green
                                    )
                                ),
                                0,
                                14,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            birdDescriptionTextView.text = Sighting

                            val sciName =
                                infodata.findViewById<TextView>(R.id.sciNameTextView)
                            val scientific = if (scientificName != "N/A") {
                                val Formatted = scientificName.replace(", ", "\n• ")
                                "Scientific Name:\n\n• $Formatted"
                            } else {
                                "Scientific Name:\n\n• N/A"
                            }
                            val scientificNameSpannable = SpannableString(scientific)
                            scientificNameSpannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0, 16,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            scientificNameSpannable.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        this@Home,
                                        R.color.dark_green
                                    )
                                ),
                                0,
                                16,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            sciName.text = scientificNameSpannable

                            val locNameTextView =
                                infodata.findViewById<TextView>(R.id.locNameTextView)
                            val locationNameText = if (locationName != "N/A") {
                                val locationNameFormatted = locationName.replace(", ", "\n• ")
                                "Location Name:\n\n• $locationNameFormatted"
                            } else {
                                "Location Name:\n\n• N/A"
                            }
                            val locationNameSpannable = SpannableString(locationNameText)
                            locationNameSpannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0, 14,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            locationNameSpannable.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        this@Home,
                                        R.color.dark_green
                                    )
                                ),
                                0,
                                14,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            locNameTextView.text = locationNameSpannable

                            val dateTextView =
                                infodata.findViewById<TextView>(R.id.dateSightedTextView)
                            val dateText = if (date != "N/A") {
                                val dateFormatted = date.replace(", ", "\n• ")
                                "Date:\n\n• $dateFormatted"
                            } else {
                                "Date:\n\n• N/A"
                            }
                            val dateSpannable = SpannableString(dateText)
                            dateSpannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0, 6,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            dateSpannable.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        this@Home,
                                        R.color.dark_green
                                    )
                                ),
                                0,
                                6,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            dateTextView.text = dateSpannable


                            val closeButton = infodata.findViewById<ImageView>(R.id.closeButton)
                            closeButton.setOnClickListener {
                                infodata.visibility = View.GONE
                            }

                            val navigationButton =
                                infodata.findViewById<Button>(R.id.navigationButton)
                            navigationButton.setOnClickListener {
                                val hotspot = customAnnotation.hotspot

                                val intent = Intent(this@Home, Navigation::class.java)
                                intent.putExtra("HOTSPOT_LATITUDE", hotspot.lat)
                                intent.putExtra("HOTSPOT_LONGITUDE", hotspot.lng)
                                intent.putExtra("USER_LATITUDE", userpos.latitude())
                                intent.putExtra("USER_LONGITUDE", userpos.longitude())
                                startActivity(intent)
                                overridePendingTransition(0, 0)
                                finish()

                            }


                        }
                    } catch (e: Exception) {
                        Log.e("Sightings", "Error parsing JSON or updating UI", e)
                    }
                } else {
                    Log.d("Sightings", "No recent sightings for hotspot: ${hotspot.locId}")
                }
            }
        }

        selection = customAnnotation
    }catch (E:Exception){}
    }
// parse what is found at the locations and same for all below
    private fun parseBirdSightings(response: String): List<String> {
        val birdSightings = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                val jsonData = jsonArray.getJSONObject(i)
                val common = jsonData.optString("comName")
                if (common.isNotEmpty()) {
                    birdSightings.add(common)
                }
            }
        } catch (e: JSONException) {
            Log.e("Sightings", "Error parsing JSON: ${e.message}", e)
        }

        return birdSightings
    }

    private fun parseField(response: String, fieldName: String): String {
        try {
            val data = JSONArray(response)
            val value = mutableListOf<String>()
            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val fieldValue = jsonObject.optString(fieldName)
                if (fieldValue.isNotEmpty()) {
                    value.add(fieldValue)
                }
            }
            if (value.isNotEmpty()) {
                return value.joinToString(", ")
            }
        } catch (e: JSONException) {
            Log.e("Parsing", "Error parsing $fieldName: ${e.message}", e)
        }
        return "N/A"
    }

    private fun parseScientificName(response: String): String {
        return parseField(response, "sciName")
    }

    private fun parseLocationName(response: String): String {
        try {
            val data = JSONArray(response)
            for (i in 0 until data.length()) {
                val jdata = data.getJSONObject(i)
                val locs = jdata.optString("locName")
                if (locs.isNotEmpty()) {
                    return locs
                }
            }
        } catch (e: JSONException) {
            Log.e("Parsing", "Error parsing locationName: ${e.message}", e)
        }
        return "N/A"
    }

    private fun parseDate(response: String): String {
        try {
            val data = JSONArray(response)
            for (i in 0 until data.length()) {
                val jdata = data.getJSONObject(i)
                val date = jdata.optString("obsDt")
                if (date.isNotEmpty()) {
                    return date
                }
            }
        } catch (e: JSONException) {
            Log.e("Parsing", "Error parsing date: ${e.message}", e)
        }
        return "N/A"
    }


    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))


    /*Code atribution
    https://docs.mapbox.com/android/maps/examples/default-point-annotation*/
    private fun convertDrawableToBitmap(sourceD: Drawable?): Bitmap? {
        if (sourceD == null) {
            return null
        }
        return if (sourceD is BitmapDrawable) {
            sourceD.bitmap
        } else {

            val status = sourceD.constantState ?: return null
            val d = status.newDrawable().mutate()
            val bit: Bitmap = Bitmap.createBitmap(
                d.intrinsicWidth, d.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val paint = Canvas(bit)
            d.setBounds(0, 0, paint.width, paint.height)
            d.draw(paint)
            bit
        }
    }

    private fun initLocationComponent() {
        val place = map.location
        place.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@Home,
                    R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@Home,
                    R.drawable.mapbox_user_icon_shadow,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        place.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        place.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        map.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        map.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        map.gestures.removeOnMoveListener(onMoveListener)
    }

    //MapBox life cycle method
    override fun onDestroy() {
        super.onDestroy()
        map.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        map.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        map.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        this.permissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    data class HotspotDetails(
        val sciName: String? = null,
        val locName: String? = null,
        val obsDt: String? = null,
        val comName: String? = null
    )

    data class BirdSearchResult(
        val SCIENTIFIC_NAME: String,
        val COMMON_NAME: String,
        val SPECIES_CODE: String,
        val CATEGORY: String,
        val TAXON_ORDER: String,
        val COM_NAME_CODES: String,
        val SCI_NAME_CODES: String,
        val BANDING_CODES: String,
        val ORDER: String,
        val FAMILY_COM_NAME: String,
        val FAMILY_SCI_NAME: String,
        val REPORT_AS: String,
        val EXTINCT: Boolean,
        val EXTINCT_YEAR: Int?,
        val FAMILY_CODE: String

    )


}

