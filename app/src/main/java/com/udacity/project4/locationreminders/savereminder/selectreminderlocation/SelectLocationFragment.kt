package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.geofence.GeofenceConstants
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "SelectLocationFragment"
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 222
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationPermissionGranted = false
    private var lastKnownLocation: Location?= null
    private val defaultLocation = LatLng(-33.852, 151.211)                       // Sydney
    private var selectedMarker: Marker? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
//
//        // Add style to the map
//        val mapStyleOptions: MapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.google_style)
//        map.setMapStyle(mapStyleOptions)

        return binding.root
    }

    /**
     * Check whether the user has granted fine location permission.
     * If not, request the permission
     */
    private fun getLocationPermission() {
        Log.d(TAG, "getLocationPermission")
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {

//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
//            )

            /**
             * comment1 => requestPermissions() is called in an activity, here we are actually in a fragment,
             * so we should call Fragment.requestPermissions(@NonNull permissions: Array<String!>, requestCode: Int)
             */
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )


        }
    }

    /**
     * Handle the result of the permission request
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")

        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                    updateLocationUI()

                } else {
                    /**
                     * If user doesn't grant the permission, give a proper message telling her/him
                     * why our app actually requires the location permission
                     */
                    _viewModel.showSnackBar.postValue("Location permission is required to add location to your reminder.")

                    /**
                     * this starts a sort of infinite iteration of [updateLocationUI, getLocationPermission & onRequestPermissionsResult]
                     */
                }
            }
        }
    }

    /**
     * Set up the map when the GoogleMap object is available
     */
    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG, "onMapReady")

        map = googleMap!!

        // Add style to the map
        val mapStyleOptions: MapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.google_style)
        map.setMapStyle(mapStyleOptions)

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()
        if (locationPermissionGranted) {
            // Get the current location of the device and set the position of the map.
            getDeviceLocation()

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false
        }

        // Put a marker to location that the user selected
        map.setOnMapClickListener { latLng ->
            selectedMarker?.remove()
            selectedMarker?.position = latLng
            map.addMarker(MarkerOptions().position(latLng)).also {
                selectedMarker = it
            }
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            onLocationSelected(latLng)
        }
    }

    /**
     * Set the location controls on the map. If the user has granted location permission,
     * enable the My Location layer and the related control on the map,
     * otherwise disable the layer and the control, and set the current location to null.
     */
    private fun updateLocationUI() {
        Log.d(TAG, "updateLocationUI")

        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    /**
     * Use the fused location provider to find the device's last-known location,
     * then use that location to position the map.
     */
    private fun getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation")
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation=task.result
                        if (lastKnownLocation != null) {
                            Log.d(TAG, "Last known location lat: $lastKnownLocation.latitude, lng: $lastKnownLocation.longitude")

                            // Zoom to the user location if it is fetched
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), GeofenceConstants.DEFAULT_ZOOM
                                )
                            )
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            map.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, GeofenceConstants.DEFAULT_ZOOM)
                            )
                            map.uiSettings?.isMyLocationButtonEnabled=false
                        }
                    } else {
                        Log.d(TAG, "Couldn't get current location")
                        Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    /**
     * When the user confirms on the selected location,
     * send back the selected location details to the view model.
     * and navigate back to the previous fragment to save the reminder and add the geofence
     */
    private fun onLocationSelected(latLng: LatLng) {
        Log.d(TAG, "onLocationSelected")

        _viewModel.latitude.postValue(latLng.latitude)
        _viewModel.longitude.postValue(latLng.longitude)

        val fromLocation = Geocoder(activity).getFromLocation(latLng.latitude, latLng.longitude, 2)
        _viewModel.reminderSelectedLocationStr.postValue(fromLocation[0].locality)

        // navigate back to the save reminder screen
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}
