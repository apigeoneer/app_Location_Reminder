package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceConstants
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class SaveReminderFragment : BaseFragment() {
    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    lateinit var geofencingClient: GeofencingClient

    private var reminderData = ReminderDataItem("", "", "", 0.0, 0.0, "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container,false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val poi = _viewModel.selectedPOI.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            reminderData = ReminderDataItem(
                title,
                description,
//                poi?.name,
                location,
                latitude,
                longitude,
                "1"
            )

//            // add a geofence request
//            if (_viewModel.validateEnteredData(reminderData)) {
//                checkDeviceLocationSettingsAndStartGeofence(true, reminderData)
//            }
//            _viewModel.validateAndSaveReminder(reminderData)

            checkDeviceLocationSettingsAndStartGeofence(true, reminderData)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false, reminderData)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun addGeofence(reminderData: ReminderDataItem) {
        // build the geofence obj
        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)

            .setCircularRegion(
                reminderData.latitude!!,
                reminderData.longitude!!,
                GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
            )

            .setExpirationDuration(GeofenceConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

//            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
//                    or Geofence.GEOFENCE_TRANSITION_DWELL
//                    or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

            .build()

        // build the geofence request
        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()


        // this pending intent starts the geofence broadcast receiver
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofence()
            intent.action = "action.ACTION_GEOFENCE_EVENT"
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // Add geofences
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofence added
                if (isAdded) {
                    Toast.makeText(context, "Geofence added!", Toast.LENGTH_SHORT).show()
                }
            }
            addOnFailureListener {
                // Failed to add geofence
                Toast.makeText(context, "Failed to add geofence", Toast.LENGTH_SHORT).show()
            }
        }

    }

    /**
     *  Check that a user has their device location enabled and if not,
     *  display an activity where they can turn it on.
     */
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true, reminderData: ReminderDataItem) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.coordinatorLayout,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence(true, reminderData)
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                // add a geofence request
                if (_viewModel.validateEnteredData(reminderData)) {
                    addGeofence(reminderData)
                }
                _viewModel.validateAndSaveReminder(reminderData)
            }
        }
    }

//    private fun saveReminderToDb(reminderDataItem: ReminderDataItem) {
//        viewLifecycleOwner.lifecycleScope.launch {
//            _viewModel.validateAndSaveReminder(reminderDataItem)
//        }
//    }

    companion object {
        private const val TAG="SaveReminderFragment"
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 23
    }
}
