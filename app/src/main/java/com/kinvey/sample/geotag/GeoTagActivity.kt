/**
 * Copyright (c) 2014 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.kinvey.sample.geotag

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.callback.KinveyPingCallback
import com.kinvey.android.callback.KinveyReadCallback
import com.kinvey.android.callback.KinveyUserCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.UserStore
import com.kinvey.java.Query
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.query.MongoQueryFilter.MongoQueryFilterBuilder
import com.kinvey.java.store.StoreType
import com.kinvey.sample.geotag.Constants.COLLECTION_NAME
import com.kinvey.sample.geotag.Constants.REQUEST_LOCATION
import kotlinx.android.synthetic.main.activity_geo_tag.*
import timber.log.Timber
import java.io.IOException

/**
 * @author edwardf
 * @since 2.0
 */
class GeoTagActivity : AppCompatActivity(), OnMapClickListener, OnInfoWindowClickListener {
    // reference the dialog used to allow user users to edit a note
    private var mEditNote: AlertDialog? = null
    // reference the daialog used to display legal info for using google maps
    private var mLegalDialog: AlertDialog? = null
    // reference to the currently selected map marker
    private var mCurMarker: Marker? = null
    // reference to the kinvey client, used for contacting Kinvey's BaaS
    private var mKinveyClient: Client<User>? = null
    private var dataStore: DataStore<GeoTagEntity>? = null
    // reference to EditText in note field, needed to clear
    private var mNote: EditText? = null
    private var googleMap: GoogleMap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geo_tag)
        setSupportActionBar(mainToolbar)
        bindViews(savedInstanceState)
        mKinveyClient = Builder<User>(this).build()
        dataStore = DataStore.collection(COLLECTION_NAME, GeoTagEntity::class.java, StoreType.NETWORK, mKinveyClient)
        authUser()
        setupMap()
    }

    // binding xml view elements to their reference (in this sample their is only one)
    private fun bindViews(savedInstanceState: Bundle?) {
        geoTagMapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        if (!verifyLocationPermissions(this)) {
            requestLocationPermissions(this)
        }
    }

    private fun authUser() {
        //login and fire off the ping call to ensure we can communicate with Kinvey
        if (mKinveyClient?.isUserLoggedIn == false) {
            try {
                UserStore.login(mKinveyClient!!, object : KinveyUserCallback<User> {
                    override fun onSuccess(result: User) {
                        testKinveyService()
                    }
                    override fun onFailure(error: Throwable) {
                        Toast.makeText(this@GeoTagActivity, "Couldn't login -> " + error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: IOException) {
                Timber.e(e)
            }
        }
    }

    private fun setupMap() {
        // ensure the current device can even support running google services,
        // which are required for using google maps.
        val googAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (googAvailable != ConnectionResult.SUCCESS) {
            Timber.i("googAvailable fail!")
            GooglePlayServicesUtil.getErrorDialog(googAvailable, this, 0).show()
        } else {
            geoTagMapView?.getMapAsync(OnMapReadyCallback {
                googleMap = it
                setListeners()
                enableLocation(true)
            })
        }
    }
    
    private fun enableLocation(enable: Boolean) {
        if (verifyLocationPermissions(this)) {
            googleMap?.isMyLocationEnabled = enable
        }
    }

    private fun verifyLocationPermissions(context: Context): Boolean {
        // Check if we have location permission
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_LOCATION -> enableLocation(true)
        }
    }

    // setting the appropriate listeners, this sample will listen for clicks on
    // the map (to show a marker)
    // as well as clicks on the info window (to show dialog for creating a new note)
    private fun setListeners() {
        googleMap?.setOnMapClickListener(this)
        googleMap?.setOnInfoWindowClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        geoTagMapView?.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        geoTagMapView.onResume()
    }

    // on pause makes callbacks to mapview as well as force-dismisses dialogs to
    // avoid window leaks.
    override fun onPause() {
        super.onPause()
        mLegalDialog?.dismiss()
        mEditNote?.dismiss()
        geoTagMapView?.onPause()
    }

    // Using ActionbarSherlock to handle options, the two are refresh and legal
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_geo_tag, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // depending on which option is tapped, call either to show legal or refresh the dataset
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_auth -> {
                authUser()
                return true
            }
            R.id.menu_item_legal -> {
                legal()
                true
            }
            R.id.menu_item_refresh -> {
                refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refresh() {
        notesOnScreen
    }

    // lazily instantiate and pops a dialog containing legal information for
    // using google maps.
    private fun legal() {
        mLegalDialog = AlertDialog.Builder(this).create()
        mLegalDialog?.setTitle(resources.getString(R.string.menu_legal))
        mLegalDialog?.setMessage(""/*GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this)*/)
        mLegalDialog?.setButton(Dialog.BUTTON_POSITIVE, "Close") { _, _ -> mLegalDialog?.cancel() }
        mLegalDialog?.show()
    }

    // listen for clicks on the map, clearing the current marker (if there is one)
    // and setting a new marker, showing the default info window for adding a note.
    override fun onMapClick(latlng: LatLng) {
        if (mCurMarker?.isVisible == true) {
            mCurMarker?.remove()
        }
        showMarkerWindow(latlng)
    }

    private fun showMarkerWindow(latlng: LatLng) {
        mCurMarker = googleMap?.addMarker(
        MarkerOptions()
            .position(latlng)
            .title(resources.getString(R.string.marker_default))
            .draggable(true))
        mCurMarker?.showInfoWindow()
    }

    // listen for clicks on the info window, lazily instantiating the
    // "edit marker" dialog
    override fun onInfoWindowClick(mark: Marker) {
        if (mEditNote == null) {
            mEditNote = AlertDialog.Builder(this).create()
            mEditNote?.setTitle(resources.getString(R.string.enter_note))
            val dv = layoutInflater.inflate(R.layout.dialog_geoloc_note, null)
            mNote = dv.findViewById<View>(R.id.geotag_note_edit) as EditText
            mEditNote?.setView(dv)
            mEditNote?.setButton(Dialog.BUTTON_NEGATIVE, "Cancel") { _, _ -> mNote?.setText("") }
            mEditNote?.setButton(Dialog.BUTTON_POSITIVE, "Save")
            { _, _ -> addNoteToLocation(mark.position, mNote?.text.toString()) }
        }
        mNote?.setText("")
        mEditNote?.show()
    }

    fun addNoteToLocation(loc: LatLng, note: String) {
        // update UI
        if (mCurMarker?.isVisible == true) {
            mCurMarker?.title = note
            mCurMarker?.showInfoWindow()
        }
        // update kinvey
        // first create an entity to put into the collection
        val curTag = GeoTagEntity()
        curTag.note = note
        curTag.coords = convertLatLngToLocation(loc)
        // let Kinvey auto-generate an ID for this entity
        // then save the entity to kinvey
        dataStore?.save(curTag, object : KinveyClientCallback<GeoTagEntity> {
            override fun onSuccess(result: GeoTagEntity) {
                val msg = String.format("Save successful%nnote: %s", result.note)
                Timber.i(msg)
                Toast.makeText(this@GeoTagActivity, msg, Toast.LENGTH_LONG).show()
            }
            override fun onFailure(error: Throwable) {
                val msg = String.format("Save failed%nerror: %s", error.message)
                Timber.e(msg)
                Toast.makeText(this@GeoTagActivity, msg, Toast.LENGTH_LONG).show()
            }
        })
    }

    // first get world coordinates that are being drawn on screen
    // now that we have a bounding box of what's on screen, use a
    // SimpleQuery to query Kinvey's backend `withinBox`
    val notesOnScreen: Unit
        get() {
            // first get world coordinates that are being drawn on screen
            val topleft: LatLng = googleMap?.projection?.visibleRegion?.farLeft ?: LatLng(0.0, 0.0)
            val btmRight: LatLng = googleMap?.projection?.visibleRegion?.nearRight ?: LatLng(0.0, 0.0)
            // now that we have a bounding box of what's on screen, use a
            // SimpleQuery to query Kinvey's backend `withinBox`
            val geoquery = Query(MongoQueryFilterBuilder())
            geoquery.withinBox("_geoloc", topleft.latitude, topleft.longitude, btmRight.latitude, btmRight.longitude)
            dataStore?.find(geoquery, object : KinveyReadCallback<GeoTagEntity> {
                override fun onSuccess(result: KinveyReadResponse<GeoTagEntity>?) {
                    val msg = "query successfull, with a size of -> " + result!!.result!!.size
                    Timber.i(msg)
                    Toast.makeText(this@GeoTagActivity, msg, Toast.LENGTH_LONG).show()
                    addGeoTagMarkers(result.result)
                }
                override fun onFailure(error: Throwable) {
                    val msg = "kinvey query fetch failed, " + error.message
                    Toast.makeText(this@GeoTagActivity, msg, Toast.LENGTH_LONG).show()
                    Timber.e(msg)
                }
            })
        }

    private fun addGeoTagMarkers(result: List<GeoTagEntity>?) {
        result?.forEach { gte -> addGeoTagMarker(gte) }
    }

    private fun addGeoTagMarker(gte: GeoTagEntity) {
        googleMap?.addMarker(MarkerOptions()
            .position(convertLocationToLatLng(gte.coords))
            .title(gte.note)
            .draggable(false))?.showInfoWindow()
    }

    fun testKinveyService() {
        mKinveyClient?.ping(object : KinveyPingCallback {
            override fun onSuccess(result: Boolean) {
                Toast.makeText(this@GeoTagActivity, "kinvey ping success!", Toast.LENGTH_LONG).show()
            }
            override fun onFailure(error: Throwable) {
                Toast.makeText(this@GeoTagActivity, "kinvey ping failed, check res/strings for appkey and appsecret", Toast.LENGTH_LONG).show()
            }
        })
    }

    companion object {
        /*
         * the below two methods are just static helper methods for translating
	     * between Location objects and LatLng objects.

	     * Note the Google Maps API uses LatLng, while Android uses Location.
	     */
        fun convertLatLngToLocation(latlng: LatLng): List<Double> {
            return listOf(latlng.longitude, latlng.latitude)
        }

        fun convertLocationToLatLng(loc: List<Double>?): LatLng {
            return if (loc != null && loc.size >= 2) { LatLng(loc[0], loc[1]) }
            else LatLng(0.0, 0.0)
        }

        private val PERMISSIONS_LOCATION = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}