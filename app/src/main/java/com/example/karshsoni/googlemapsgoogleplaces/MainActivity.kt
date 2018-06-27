package com.example.karshsoni.googlemapsgoogleplaces

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    val ERROR_DIALOG_REQUEST = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isServicesOK()){
            init()
        }

    }

    fun init(){
        btnMap.setOnClickListener {
            Log.d(TAG, "Init: In Init");
            val intent = Intent(this@MainActivity, MapActivity::class.java)
            startActivity(intent)
        }
    }

    fun isServicesOK():Boolean{
        Log.d(TAG, "isServicesOK: Checking google service version")

        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when {
            available == ConnectionResult.SUCCESS -> {
                Log.d(TAG, "isServicesOK: Google Play Services Working")
                return true
            }
            GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
                Log.d(TAG, "isServicesOK: Error occured but we can fix it.")
                val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST)
                dialog.show()
            }
            else -> Toast.makeText(this, "You can't make map request.", Toast.LENGTH_LONG).show()
        }
        return false
    }

}
