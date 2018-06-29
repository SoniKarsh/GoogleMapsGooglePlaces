package com.example.karshsoni.googlemapsgoogleplaces

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_map.*
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.Polyline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList


class MapActivity : AppCompatActivity(),
        GoogleMap.OnMarkerClickListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        PlaceSelectionListener {

    lateinit var destinationLocation : LatLng
    var listOfLocations: ArrayList<LatLng> = ArrayList()
    val baseUrl = "https://maps.googleapis.com"
    val API_KEY = "AIzaSyBeAd7xFcadbnddISY8ar2Pg_WAfgi3NFY"
    var polygonCount = 0
    var polylineCount = 0
    var circleCount = 0
    var polyline : Polyline? = null
    var polygon : Polygon? = null
    var circle : Circle? = null


    override fun onMarkerClick(p0: Marker?): Boolean {
        Log.d(TAG, "onMarkerClick: Clicked");
        showRouteToDestination(currentLocation, destinationLocation)
        return true
    }

    override fun onPlaceSelected(p0: Place?) {
        Log.i(TAG, "Place: " + p0!!.getName())
        moveCamera(LatLng(p0.latLng.latitude
                , p0.latLng.longitude), DEFAULT_ZOOM,
                p0.address.toString())
        destinationLocation = p0.latLng
    }

    override fun onError(p0: Status?) {
        Log.i(TAG, "An error occurred: " + p0)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    val LOCATION_PERMISSION_REQUEST_CODE = 1234
    val TAG = "MapActivity"
    var mLocationPermissionsGranted: Boolean = false
    lateinit var mMap : GoogleMap
    private val DEFAULT_ZOOM = 15f
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    lateinit var currentLocation: Location

    override fun onMapReady(p0: GoogleMap?) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_LONG).show()
        Log.d(TAG, "onMapReady: Map is Ready")
        mMap = p0!!

        if (mLocationPermissionsGranted){
            getDeviceLocation()
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false
            fun rand(start: Int, end: Int) = Random().nextInt(end + 1 - start) + start
            btnMapType.setOnClickListener {
                when (rand(1, 4)) {
                    1 ->
                        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    2 ->
                        mMap.mapType = GoogleMap.MAP_TYPE_NONE
                    3 ->
                        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    4 ->
                        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    else -> Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }
            }
            init()
            mMap.setOnMarkerClickListener(this)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        Log.d(TAG, "in OnCreate: ")


        var builder = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())

        var retrofit: Retrofit = builder.build()

        var client: RoutingGoogleMaps = retrofit.create(RoutingGoogleMaps::class.java)

//        var call = client.sendRequestForRouting("23.0225713, 72.571322",
//                "21.1702535, 72.83118469999999",
//                API_KEY)

        var call = client.sendRequest("https://maps.googleapis.com/maps/api/directions/json?origin=Ahmedabad&destination=surat&key=AIzaSyBeAd7xFcadbnddISY8ar2Pg_WAfgi3NFY")

        call.enqueue(object : Callback<GoogleApiResponseModel> {
            override fun onFailure(call: Call<GoogleApiResponseModel>?, t: Throwable?) {
                Toast.makeText(applicationContext, "error:( ", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<GoogleApiResponseModel>?, response: Response<GoogleApiResponseModel>?) {
                var googleApiResponseModel = response!!.body()
                Log.d(TAG, "onResponse: "+ googleApiResponseModel)
                fillList(googleApiResponseModel!!)
            }
        })

        getLocationPermission()
        init()
        btnShowPolygon.setOnClickListener{
            if(polylineCount!=0 && polylineCount % 2 == 1){
                polylineCount = 0
                polyline!!.remove()
                polyline = null

            }else if(circleCount!=0 && circleCount % 2 == 1){
                circleCount = 0
                circle!!.remove()
                circle = null
            }else{
                Toast.makeText(this, "Good to Go", Toast.LENGTH_SHORT).show()
                polygonCount = 0
            }
            showPolygon(listOfLocations)
        }
        btnPolyline.setOnClickListener {
            if(polygonCount!=0 && polygonCount % 2 == 1){
                polygonCount = 0
                polygon!!.remove()
                polygon = null
            }else if(circleCount!=0 && circleCount % 2 == 1){
                circleCount = 0
                circle!!.remove()
                circle = null
            }else{
                Toast.makeText(this, "Good to Go", Toast.LENGTH_SHORT).show()
                polylineCount = 0
            }
            showPolyline(listOfLocations)
        }
        btnCircle.setOnClickListener {
            if(polylineCount!=0 && polylineCount % 2 == 1){
                polylineCount = 0
                polyline!!.remove()
                polyline = null
            }else if(polygonCount!=0 && polygonCount % 2 == 1){
                polygonCount = 0
                polygon!!.remove()
                polygon = null
            }else{
                Toast.makeText(this, "Good to Go", Toast.LENGTH_SHORT).show()
                circleCount = 0
            }
            showCircle(listOfLocations)
        }

    }


    private fun fillList(googleApiResponseModel: GoogleApiResponseModel){
        var routes = googleApiResponseModel.routes
        var legs = routes!![0]!!.legs
//        Log.d(TAG, "fillList: "+legs!![1])
        var startLocation = legs!![0]!!.startLocation
        var endLocation = legs[0]!!.endLocation
//        Log.d(TAG, "fillList: "+ startLocation+"\n"+endLocation)
        var stepsItem = legs[0]!!.steps
//        Log.d(TAG, "fillList: "+ stepsItem!![0]!!.polyline)
        var decodePoly1 = DecodePoly()
//        Log.d(TAG, "fillList: yo"+ decodePoly1.decodePoly(stepsItem[0]!!.polyline!!.points.toString()))
        var polyList: ArrayList<ArrayList<LatLng>> = ArrayList()
        for (i in 0 until stepsItem!!.size){
            polyList.add(decodePoly1.decodePoly(stepsItem[i]!!.polyline!!.points.toString()) as ArrayList<LatLng>)
        }
        drawOnMap(polyList)
    }


    private fun drawOnMap(polyList: ArrayList<ArrayList<LatLng>>){
        var polylineOptions = PolylineOptions()
                .color(Color.BLUE)
//                .addAll(polyList!![0])
        var latLng: LatLng?
        for(i in polyList.indices)
        {
            for(j in polyList[i].indices){
                latLng = LatLng(polyList[i][j].latitude, polyList[i][j].longitude)
                polylineOptions.add(latLng)
            }
        }
        var xPoly = mMap.addPolyline(polylineOptions)
    }


    private fun init() {
        Log.d(TAG, "init: initializing")

        var autocompleteFragment = fragmentManager
                .findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment

        autocompleteFragment.setOnPlaceSelectedListener(this)

        icGps.setOnClickListener {
            getDeviceLocation()
        }

        hideSoftKeyboard()
    }

    private fun showRouteToDestination(myLocation: Location, destLocation: LatLng){
        val uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)", myLocation.latitude, myLocation.longitude, "Home Sweet Home", destLocation.latitude, destLocation.longitude, "Travel HERE")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.`package` = "com.google.android.apps.maps"
        startActivity(intent)
    }

    private fun getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the current devices location ")
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            if (mLocationPermissionsGranted){
                var location = mFusedLocationProviderClient.lastLocation as Task
                location.addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.d(TAG, "getDeviceLocation: found Location")
                        currentLocation = it.result
                        moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude),
                                DEFAULT_ZOOM, "My Location")
                    }else{
                        Log.d(TAG, "getDeviceLocation: Current location is null")
                        Toast.makeText(this, "unable to get current location", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }catch (e: SecurityException){
            Log.e(TAG, "getDeviceLocation: SecurityException: "+ e.message)
        }

    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        var icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)

        if(title != "My Location"){
            listOfLocations.add(latLng)
            var options = MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(icon)
            mMap.addMarker(options)

        }
        hideSoftKeyboard()
    }

    private fun showPolygon(listOfLocation: ArrayList<LatLng>){
        var polygonOptions = PolygonOptions()
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE)
        for(i in listOfLocation){
            polygonOptions.add(i)
        }

        if(polygon!=null && polygon!!.isVisible){
            polygonCount = 1
        }else{
            polygon = mMap.addPolygon(polygonOptions)
            polygonCount = 1
        }

    }

    private fun showPolyline(listOfLocation: ArrayList<LatLng>){

        var polylineOptions = PolylineOptions()
                .color(Color.BLACK)
        for(i in listOfLocation){
            polylineOptions.add(i)
        }

        if (polyline!=null && polyline!!.isVisible){
            polylineCount = 1
        }else{
            polyline = mMap.addPolyline(polylineOptions)
            polylineCount = 1
        }

    }

    private fun showCircle(listOfLocation: ArrayList<LatLng>){

        var circleOptions = CircleOptions()
                .fillColor(Color.CYAN)
                .center(listOfLocation[0])
                .radius(25.00)

        if(circle!=null && circle!!.isVisible){
            circleCount = 1
        }else{
            circle = mMap.addCircle(circleOptions)
            circleCount = 1
        }
    }

    private fun initMap() {
        Log.d(TAG, "initMap: initializing map")
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this@MapActivity)
    }

    private fun getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        if(ContextCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(applicationContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true
                initMap()
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE)
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult: onRequestPermissionsResult Called")
        mLocationPermissionsGranted = false
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    for (i in 0 until grantResults.size) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false
                            Log.d(TAG, "onRequestPermissionsResult: permission failed")
                            return
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    mLocationPermissionsGranted = true
                    //initialize our map
                    initMap()
                }
            }
        }
    }

    private fun hideSoftKeyboard(){
        var inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = this.currentFocus
        if (view == null) {
            view = View(this)
        }
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


}