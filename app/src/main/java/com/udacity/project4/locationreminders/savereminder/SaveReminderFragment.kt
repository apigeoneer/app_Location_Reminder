package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceConstants
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    lateinit var geofencingClient: GeofencingClient
//    private val geofenceList = ArrayList<Geofence>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container,false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(context!!)

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
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db

            val reminderData = ReminderDataItem(
                title,
                description,
                location,
                latitude,
                longitude,
                "1"
            )

            // add a geofence request
            if (_viewModel.validateEnteredData(reminderData)) {
                addGeofence(reminderData)
            }

            // validate & save reminder (title + desc + geofence) to db
            saveReminderToDb(reminderData)

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

            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                    or Geofence.GEOFENCE_TRANSITION_DWELL
                    or Geofence.GEOFENCE_TRANSITION_EXIT)

            .build()

        // build the geofence request
        GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    private fun saveReminderToDb(reminderDataItem: ReminderDataItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            _viewModel.validateAndSaveReminder(reminderDataItem)
        }
    }
}
