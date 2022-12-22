package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

private const val TAG = "SaveReminderFragment"

class SaveReminderFragment : BaseFragment() {

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var accessFineLocationGranted = false
            if (result.containsKey(Manifest.permission.ACCESS_FINE_LOCATION)) {
                accessFineLocationGranted =
                    result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            }

            val accessBackgroundLocationGranted: Boolean =
                if (runningQOrLater && result.containsKey(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                } else {
                    true
                }

            if (!accessBackgroundLocationGranted && !accessFineLocationGranted) {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                    .show()
                return@registerForActivityResult
            }

            if (accessFineLocationGranted && accessBackgroundLocationGranted) {
                startGeofencing()
            }
        }

    private var cacheReminderDataItem: ReminderDataItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
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

            val reminderDataItem = ReminderDataItem(
                title = title,
                description = description,
                location = location,
                longitude = longitude,
                latitude = latitude,
            )

            if (_viewModel.validateEnteredData(reminderDataItem)) {
                cacheReminderDataItem = reminderDataItem
                startGeofencing()
            }

            geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun startGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            // TODO: Start
            checkLocation()
        } else {
            requestPermissions()
        }
    }

    private fun checkLocation(resolve: Boolean = true) {
        Log.d(TAG, "checkLocation: checkLocation")
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "checkLocation: ${sendEx.message}")
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkLocation(false) // Try again
                }.show()
            }
        }

        locationSettingResponseTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                addGeofenceForClue()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue() {
        cacheReminderDataItem?.let { reminderData ->
            val geofence = Geofence.Builder()
                .setRequestId(reminderData.id)
                .setCircularRegion(
                    reminderData.latitude ?: 0.0,
                    reminderData.longitude ?: 0.0,
                    GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
            intent.action = ACTION_GEOFENCE_EVENT
            val geofencePendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    _viewModel.saveReminder(reminderData)
                    Toast.makeText(
                        requireContext(), R.string.geofence_added,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("Add Geofence", geofence.requestId)
                }
                addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        R.string.geofences_not_added,
                        Toast.LENGTH_SHORT
                    ).show()
                    if (exception.message != null) {
                        Log.w(TAG, " ${exception.message!!}")
                    }
                }
            }
        }
    }

    //Check if permission is granted, from lesson 2
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundPermissionApproved =
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        val backgroundPermissionApproved =
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

        return foregroundPermissionApproved && backgroundPermissionApproved
    }

    @TargetApi(29)
    private fun requestPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (runningQOrLater) permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION

        requestPermissionLauncher.launch(permissions)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: ")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkLocation()
        }
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
        const val GEOFENCE_RADIUS_IN_METERS = 100f
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 1000
    }
}
