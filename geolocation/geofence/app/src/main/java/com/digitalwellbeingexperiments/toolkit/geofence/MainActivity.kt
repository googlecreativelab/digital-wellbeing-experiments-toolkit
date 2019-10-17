// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.digitalwellbeingexperiments.toolkit.geofence

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import java.io.IOException

private const val DEFAULT_RADIUS_METERS = 150f
private const val AUTOCOMPLETE_REQUEST_CODE = 10001
private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 111

class MainActivity : AppCompatActivity() {

    private lateinit var triggersAdapter: TriggersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        triggersAdapter = TriggersAdapter()
        Places.initialize(this, getString(R.string.google_maps_key))

        findViewById<Button>(R.id.btn_add).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            } else {
                showPlaceSearch()
            }
        }

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            adapter = triggersAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        triggersAdapter.refresh()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        showPlaceSearch()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                when (resultCode) {
                    RESULT_OK -> {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        place.latLng?.let {
                            val description = place.name ?: reverseGeocode(it)
                            Log.w(TAG(), "GOT $description")

                            GeofenceApplication.instance.triggerManager.add(Trigger(
                                latLng = it,
                                radius = DEFAULT_RADIUS_METERS,
                                placeName = description
                            ),
                                successCallback = { isSuccess, errorMessage ->
                                    if (isSuccess) {
                                        triggersAdapter.refresh()
                                    } else {
                                        createSnackbar(
                                            window.decorView.findViewById(android.R.id.content),
                                            errorMessage
                                                ?: getString(R.string.error_unknown_trigger),
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                })
                        }
                    }
                    AutocompleteActivity.RESULT_ERROR -> {
                        val status = Autocomplete.getStatusFromIntent(data)
                        val message = status.statusMessage?: getString(R.string.error_unknown_trigger)
                        Log.w(TAG(), message)
                        createSnackbar(
                            window.decorView.findViewById(android.R.id.content),
                            message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    RESULT_CANCELED -> {
                        // The user canceled the operation.
                        Log.w(TAG(), "cancelled")
                    }
                }
            }
        }
    }

    private fun showPlaceSearch() {
        val fields: List<Place.Field> =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        ).build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun reverseGeocode(latlng: LatLng): String {
        fun fallback(fallback: String = "${latlng.latitude} ${latlng.longitude}"): String {
            return fallback
        }
        try {
            Geocoder(this).getFromLocation(latlng.latitude, latlng.longitude, 1)?.let { results ->
                if (results.isNotEmpty()) {
                    results.first().let {
                        if (it.maxAddressLineIndex == -1) return fallback()
                        var address = ""
                        for (i in 0..it.maxAddressLineIndex) {
                            address += it.getAddressLine(i)
                        }
                        return address
                    }
                } else {
                    return fallback()
                }
            }
        } catch (e: IOException) {
            return fallback()
        }
        return fallback()
    }
}
