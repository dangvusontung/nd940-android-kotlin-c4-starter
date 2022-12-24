package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val TAG = "SelectLocationFragment"

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var currentLocation = LatLng(21.0461, 105.7963)

    private var selectedLocation: LatLng? = null
    private var pointOfInterest: PointOfInterest? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                showCurrentLocation()
            } else {
                // When not granted, navigate user to setting
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
                    }.show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(onMapReadyCallback)

//        TODO: call this function after the user confirms on the selected location
        binding.btnSave.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    // from the base project, I would have named it "saveLocation"
    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.apply {
            if (selectedLocation != null) {
                // Because we checked for nullability of selected location, we can "force-unwrap" it properties here
                latitude.value = selectedLocation!!.latitude
                longitude.value = selectedLocation!!.longitude
            } else if (pointOfInterest != null) {
                // The same with PoI
                latitude.value = pointOfInterest!!.latLng.latitude
                longitude.value = pointOfInterest!!.latLng.longitude
            }
            selectedPOI.value = pointOfInterest
            reminderSelectedLocationStr.value =
                pointOfInterest?.name ?: getString(R.string.dropped_pin)
        }

        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NONE
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

    private val onMapReadyCallback = OnMapReadyCallback { googleMap ->
        googleMap?.let {
            map = googleMap
        }

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

//        TODO: add style to the map
        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "cannot find map style: $exception")
        }

//        TODO: put a marker to location that the user selected
        onLongClick(map)
        onPoiClick(map)
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentLocation() {
        map.isMyLocationEnabled = true
        try {
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // TODO: zoom to the user location after taking his permission
                    task.result?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                    }
                    currentLocation.let {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.latitude, it.longitude),
                                ZOOM_SIZE
                            )
                        )
                    }
                }
            }
        } catch (exception: SecurityException) {
            Log.e(TAG, "Exception: $exception")
        }
    }

    private fun onPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            Log.d(TAG, "onPoiClick: onPointClick")
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.3f, Long: %2$.3f",
                poi.latLng.latitude,
                poi.latLng.longitude
            )
            val poiMarker =
                map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name).snippet(snippet))
            poiMarker.showInfoWindow()
            pointOfInterest = poi
        }
    }

    private fun onLongClick(map: GoogleMap) {
        map.setOnMapClickListener {
            Log.d(TAG, "onLongClick: ${it.latitude}, ${it.longitude}")
            it?.let {
              map.clear()
              val snippet = String.format(
                  Locale.getDefault(),
                  "Lat: %1$.3f, Long: %2$.3f",
                  it.latitude,
                  it.longitude
              )

              val marker = MarkerOptions().position(it).title(getString(R.string.here))
                  .snippet(snippet)

              map.addMarker(marker).showInfoWindow()
              this.selectedLocation = it
          }
        }
    }

    companion object {
        private const val ZOOM_SIZE = 15f
    }
}
