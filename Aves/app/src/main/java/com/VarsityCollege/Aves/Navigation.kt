package com.VarsityCollege.Aves
import android.Manifest
import android.content.Intent

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.dropin.NavigationView
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView

class Navigation : AppCompatActivity() {

    private val tripProgressFormatter: TripProgressUpdateFormatter by lazy {

        val Options = DistanceFormatterOptions.Builder(this).build()

        TripProgressUpdateFormatter.Builder(this)
            .distanceRemainingFormatter(DistanceRemainingFormatter(Options))
            .timeRemainingFormatter(TimeRemainingFormatter(this))
            .estimatedTimeToArrivalFormatter(EstimatedTimeToArrivalFormatter(this))
            .build()
    }
// this reads to the api and creates a session
    private val tripProgressApi: MapboxTripProgressApi by lazy {
        MapboxTripProgressApi(tripProgressFormatter)
    }




    private lateinit var tripProgressView: MapboxTripProgressView

// permissions to ensure peak functioning
    private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2
    private lateinit var mapboxNavigation: MapboxNavigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation)
        supportActionBar?.hide()
        securGuard()
        val loginBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@Navigation, Home::class.java)

                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }
        }
        onBackPressedDispatcher.addCallback(this, loginBack)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
// building with the access token
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
        //get the location of the user and the end point
        mapboxNavigation = MapboxNavigationApp.current()!!
        val intial = Point.fromLngLat(
            intent.getDoubleExtra("USER_LONGITUDE", 0.0),
            intent.getDoubleExtra("USER_LATITUDE", 0.0)
        )

        val end = Point.fromLngLat(
            intent.getDoubleExtra("HOTSPOT_LONGITUDE", 0.0),
            intent.getDoubleExtra("HOTSPOT_LATITUDE", 0.0)
        )
// set the options for the route
        val options = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .coordinatesList(listOf(intial, end))
            .steps(true)
            .alternatives(true)
            .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
            .voiceUnits(DirectionsCriteria.METRIC)
            .build()

// begin the build and route
        mapboxNavigation.requestRoutes(
            options,
            object : NavigationRouterCallback {
                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: RouterOrigin) {
                    mapboxNavigation.setNavigationRoutes(routes)

                    if (ContextCompat.checkSelfPermission(this@Navigation, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this@Navigation,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                    } else if (ContextCompat.checkSelfPermission(this@Navigation, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this@Navigation,
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
                    } else {
                        mapboxNavigation.startTripSession()
                    }


                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    //error here
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    // fail here
                }
            }
        )

        tripProgressView = findViewById(R.id.tripProgressView)


// cancel the basic route for a more advanced one
        val endui = findViewById<Button>(R.id.cancelButton)
        endui.setOnClickListener {

            tripProgressView.visibility = View.GONE
            endui.visibility = View.GONE

        }
// checks the progress
        val inprogrss = object : RouteProgressObserver {
            override fun onRouteProgressChanged(routeProgress: RouteProgress) {
                tripProgressApi.getTripProgress(routeProgress).let { update ->
                    tripProgressView.render(update)
                }
            }
        }
        mapboxNavigation.registerRouteProgressObserver(inprogrss)
        val menu: FloatingActionMenu = findViewById(R.id.menu)

        val Homes: FloatingActionButton = findViewById(R.id.home)
        Homes.setOnClickListener {
            val intent = Intent(this, Home::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
            menu.close(true)
        }

        val settings: FloatingActionButton = findViewById(R.id.settings_button)
        settings.setOnClickListener {

            val intent = Intent(this, Map_settings::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

            menu.close(true)
        }

        val exit_app: FloatingActionButton = findViewById(R.id.exit_app)
        exit_app.setOnClickListener {

            this.finishAffinity()
            menu.close(true)
        }

        val exit: FloatingActionButton = findViewById(R.id.exit)
        exit.setOnClickListener {
            signout()
            val intent = Intent(this, Login::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
            menu.close(true)
        }
    }
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
}


