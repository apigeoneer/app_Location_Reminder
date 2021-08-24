package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceConstants
import com.udacity.project4.locationreminders.geofence.GeofenceHelper
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.launch
import org.koin.android.ext.android.bind
import org.koin.android.ext.android.inject


class SaveReminderFragment : BaseFragment() {
    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofenceHelper: GeofenceHelper
    private lateinit var geofencingClient: GeofencingClient

    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var cntxt: Context
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container,false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        geofenceHelper = GeofenceHelper(context)
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections
                    .actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
//        val poi = _viewModel.selectedPOI.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            reminderDataItem = ReminderDataItem(
                title,
                description,
//                poi?.name,
                location,
                latitude,
                longitude
            )

            if (_viewModel.validateEnteredData(reminderDataItem)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (isBackgroundPermissionEnabled()) {
                        checkDeviceLocationSettingsAndStartGeofence()
                    } else {
                        Log.d(TAG, "::::::: background permission denied alert :::::::")
                        AlertDialog.Builder(requireActivity())
                            .setTitle(R.string.permission_title)
                            .setMessage(R.string.background_permission_denied_explanation)
                            .setPositiveButton("OK") { dialogInterface, i ->
                                requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                    BACKGROUND_LOCATION_PERMISSION_INDEX)
                            }
                            .setNegativeButton("Cancel") { dialogInterface, i ->
                                dialogInterface.dismiss()
                            }
                            .create()
                            .show()
                    }
                } else {
                    checkDeviceLocationSettingsAndStartGeofence()
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == BACKGROUND_LOCATION_PERMISSION_INDEX) {
            if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Snackbar.make(binding.root, R.string.background_permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            } else {
                checkDeviceLocationSettingsAndStartGeofence()
            }
        }

//        if (grantResults.isEmpty() || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
//            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
//                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
//                    PackageManager.PERMISSION_DENIED) ) {
//            // If user denies permission, provide an explanation for why it is needed
//            Toast.makeText(requireContext(), R.string.permission_denied_explanation, Toast.LENGTH_LONG).show()
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                checkDeviceLocationSettingsAndStartGeofence()
//            }
//        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cntxt = context
    }

    override fun onDestroy() {
        super.onDestroy()
        // make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    /**
     * When we want to add a Geofence, the flow should be as follows:
     *
     * 1 -  check if all the required permissions have been granted (foreground and background).
     *      If there is any non - granted permission, request it properly.
     *
     * 2 -  If all the required permissions have been granted, then we should proceed to check if the device location is on.
     *      If the device location is not on, show the location settings dialog and ask the user to enable it.
     *
     * 3 -  We should automatically attempt to add a Geofence when we are certain that
     *      the required permissions have been granted and the device location is on.
     */

//    @TargetApi(29)
//    private fun foregroundPermissionGranted(): Boolean {
//        Log.d(TAG, "::::::: foreground Background Permission Granted :::::::")
//        val isForegroundPermissionGranted = PackageManager.PERMISSION_GRANTED ==
//                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//        val isBackgroundPermissionGranted = if (runningQOrLater) {
//            PackageManager.PERMISSION_GRANTED ==
//                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//        } else {
//            return true
//        }
//
//        return isForegroundPermissionGranted && isBackgroundPermissionGranted
//    }
//
//    @TargetApi(29)
//    private fun requestForegroundLocationPermissions() {
//        Log.d(TAG, "::::::: request Foreground Background Location Permissions :::::::")
//        if (foregroundPermissionGranted())
//            return
//
//        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//
//        val resultCode = when {
//                runningQOrLater -> {
//                    permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
//                }
//                else -> REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
//            }
//
//        requestPermissions(permissions, resultCode)
//    }

    // Checks if background permission is enabled or not (applicable only for android Q and later)
    private fun isBackgroundPermissionEnabled(): Boolean {
        var isBackgroundPermissionEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isBackgroundPermissionEnabled = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            Log.d(TAG, "isBackgroundPermissionEnabled: VERSION.SDK_INT < Q")
        }
        return isBackgroundPermissionEnabled
    }

    /**
     * Add a geofencing request
     */
    private fun addGeofence(reminderDataItem: ReminderDataItem) {
        // build the geofence obj
//        val geofence = Geofence.Builder()
//            .setRequestId(reminderDataItem.id)
//
//            .setCircularRegion(
//                reminderDataItem.latitude!!,
//                reminderDataItem.longitude!!,
//                GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
//            )
//
//            .setExpirationDuration(GeofenceConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
//
////            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
////                    or Geofence.GEOFENCE_TRANSITION_DWELL
////                    or Geofence.GEOFENCE_TRANSITION_EXIT)
//            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
//
//            .build()

        val geofence = geofenceHelper.getGeofence(reminderDataItem.id,
            LatLng(reminderDataItem.latitude!!, reminderDataItem.longitude!!),
            GeofenceConstants.GEOFENCE_RADIUS_IN_METERS,
            Geofence.GEOFENCE_TRANSITION_ENTER
        )

          // build the geofence request
//        val geofencingRequest = GeofencingRequest.Builder().apply {
//            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//            addGeofence(geofence)
//        }.build()

        val geofencingRequest = geofenceHelper.getGeofencingRequest(geofence)


        // this pending intent starts the geofence broadcast receiver
//        val geofencePendingIntent: PendingIntent by lazy {
//            val intent = Intent(cntxt, GeofenceBroadcastReceiver::class.java)
//            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
//            // addGeofences() and removeGeofence()
//            intent.action = "action.ACTION_GEOFENCE_EVENT"
//            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        }

        val geofencePendingIntent = geofenceHelper.getGeofencePendingIntent()

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
                    _viewModel.saveReminder(reminderDataItem)
                    Log.d(TAG, "::::::: addGeofence: geofence added :::::::")
                    Toast.makeText(cntxt, "Geofence added!", Toast.LENGTH_SHORT).show()
                }
            }
            addOnFailureListener {
                // Failed to add geofence
                Log.d(TAG, "::::::: addGeofence: geofence failed to be added :::::::")
                Toast.makeText(cntxt, "Failed to add geofence", Toast.LENGTH_SHORT).show()
            }
        }

    }

    /**
     *  Check that a user has their device location enabled and if not,
     *  display an activity where they can turn it on.
     */
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
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
                    // for activity
//                    exception.startResolutionForResult(requireActivity(),
//                        REQUEST_TURN_DEVICE_LOCATION_ON)
                    // for fragment
                    startIntentSenderForResult(exception.resolution.intentSender, REQUEST_CODE_LOCATION_SETTING,
                        null, 0, 0, 0, null)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG,"Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.coordinatorLayout,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                Log.d(TAG, "::::::: checkDeviceLocationSettingsAndStartGeofence: location permission granted :::::::")
                // add a geofence request
//                if (_viewModel.validateEnteredData(reminderData)) {
//                    createAndAddAGeofence()
//                }
//                _viewModel.validateAndSaveReminder(reminderData)
//                if (foregroundBackgroundPermissionGranted()) {
//                    addGeofence(reminderDataItem)
//                } else {
//                    requestForegroundBackgroundLocationPermissions()
//                }

                addGeofence(reminderDataItem)
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
        private const val REQUEST_CODE_LOCATION_SETTING = 222
        private const val LOCATION_PERMISSION_INDEX = 24
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 25
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 26

    }
}
