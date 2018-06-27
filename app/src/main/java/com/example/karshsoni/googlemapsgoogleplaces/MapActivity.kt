package com.example.karshsoni.googlemapsgoogleplaces

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_map.*
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException


class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    val LOCATION_PERMISSION_REQUEST_CODE = 1234
    val TAG = "MapActivity"
    var mLocationPermissionsGranted: Boolean = false
    lateinit var mMap : GoogleMap
    private val DEFAULT_ZOOM = 15f
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val LAT_LNG_BOUNDS = LatLngBounds( LatLng(-40.0, -168.0), LatLng(71.0, 136.0))

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
            mMap.uiSettings.isMyLocationButtonEnabled = true
            init()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        Log.d(TAG, "in OnCreate: ")
        getLocationPermission()
        init()
    }

    private fun init() {
        Log.d(TAG, "init: initializing")

//        var geoDataClient = Places.getGeoDataClient(this)
//        var placeDetectionClient = Places.getPlaceDetectionClient(this)
//        var mGoogleApiClient = GoogleApiClient
//                .Builder(this)
//                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
//        .build()


//        var placeAutocompleteAdapter = PlaceAutocompleteAdapter(this, mGoogleApiClient, LAT_LNG_BOUNDS, null)

//        inputSearch.setAdapter(placeAutocompleteAdapter)

        inputSearch.setOnEditorActionListener({ textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.action == KeyEvent.ACTION_DOWN
                    || keyEvent.action == KeyEvent.KEYCODE_ENTER) {

                Log.d(TAG, "init: pressed")
                //execute our method for searching
                geoLocate()
            }

            false
        })
        icGps.setOnClickListener {
            getDeviceLocation()
        }
        hideSoftKeyboard()
    }

    private fun geoLocate(){
        Log.d(TAG, "geoLocate: geolocating")

        val searchString = inputSearch.text.toString()

        var geoCoder = Geocoder(this)
        var list: List<Address> = ArrayList()
        try {
            list = geoCoder.getFromLocationName(searchString,1)
        }catch (e: IOException){
            Log.e(TAG, "IOEception: "+e)
        }

        if(list.isNotEmpty()){
            val address = list[0]
            Log.d(TAG, "geoLocate: "+ address.toString())
//            Toast.makeText(this, address.toString(), Toast.LENGTH_LONG).show()

            moveCamera(LatLng(address.latitude, address.longitude), DEFAULT_ZOOM,
                    address.getAddressLine(0))
        }

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
                        var currentLocation = it.result
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

        if(title != "My Location"){
            var options = MarkerOptions()
                    .position(latLng)
                    .title(title)
            mMap.addMarker(options)
        }

        hideSoftKeyboard()
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