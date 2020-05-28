package it.weMake.covid19Companion.ui.landing.settings

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.android.support.DaggerFragment
import it.weMake.covid19Companion.R
import it.weMake.covid19Companion.broadcastReceivers.GeofenceBroadcastReceiver
import it.weMake.covid19Companion.databinding.FragmentSettingsBinding
import it.weMake.covid19Companion.models.washHandsReminderLocations.WashHandsReminderLocation
import it.weMake.covid19Companion.ui.landing.settings.washHandsReminderLocation.WashHandsReminderLocationBottomDialogFragment
import it.weMake.covid19Companion.utils.*
import javax.inject.Inject


//Make sure you're extending DaggerFragment instead of Fragment so Dagger knows how to inject parameters for us
class SettingsFragment : DaggerFragment(),View.OnClickListener {

    lateinit var binding: FragmentSettingsBinding
    lateinit var reminderLocationAdapter: ReminderLocationAdapter

    //Dagger injects viewModelFactory for us
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    //viewModelFactory provides the SettingsViewModel for us
    protected val viewModel: SettingsViewModel by viewModels { viewModelFactory }

    private val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2
    private val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 3
    lateinit var geofencingUtils: GeofencingUtils

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        geofencingUtils = GeofencingUtils(requireContext())

        reminderLocationAdapter = ReminderLocationAdapter(
            {
                openWashHandsReminderLocationDialog(false, it)
            },
            {
                viewModel.updateWashHandsReminderLocation(it)
                if (it.enabled){
                    geofencingUtils.addGeoFences(listOf(it))
                }else{
                    geofencingUtils.removeGeofences(listOf(it.id.toString()))
                }
            })
        binding.remHandLocRV.adapter = reminderLocationAdapter

        val washHandsInterval = viewModel.getWashHandsInterval()
        updateUIIntervalWashHand(washHandsInterval)
        val drinkWaterInterval = viewModel.getDrinkWaterInterval()
        updateUIIntervalDrinkWater(drinkWaterInterval)
        val useCustomNotificationTone = viewModel.getUseCustomNotificationTone()
        binding.useCustomNotificationToneS.isChecked = useCustomNotificationTone
        val remindUserToWashHandsWhenArrivedAtLocation = viewModel.getRemindUserToWashHandsWhenArrivedAtLocation()
        binding.remHandLocS.isChecked = remindUserToWashHandsWhenArrivedAtLocation
        showHideWashHandsReminderLocations(remindUserToWashHandsWhenArrivedAtLocation)

        binding.addRemLocationTV.setOnClickListener(this)
        attachObservers()
        return binding.root
    }

    override fun onClick(v: View) {
        when(v.id){

            R.id.add_rem_location_TV -> openWashHandsReminderLocationDialog(true)

        }
    }

    private fun attachObservers() {

        binding.remWashHandsS.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                openWashHandsDialog()
            } else {
                setIntervalWashHand(0)
            }
        }

        binding.remDrinkWaterS.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                openDrinkWaterDialog()
            } else {
                setIntervalDrinkWater(0)
            }
        }

        binding.useCustomNotificationToneS.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUseCustomNotificationTone(isChecked)
        }

        binding.remHandLocS.setOnCheckedChangeListener{ _, isChecked ->

            if(isChecked){

                if (checkHasFineLocationPermission()){
                    if (checkHasBackgroundLocationPermission()){
                        if (viewModel.washHandsReminderLocationsLiveData.value.isNullOrEmpty()){
                            openWashHandsReminderLocationDialog(true)
                        }else{
                            geofencingUtils.addGeoFences(viewModel.washHandsReminderLocationsLiveData.value!!)
                        }
                    }else{
                        requestBackgroundLocationPermission()
                    }
                }else{
                    requestFineLocationPermission()
                }

            }else{
                if (!viewModel.washHandsReminderLocationsLiveData.value.isNullOrEmpty()){
                    geofencingUtils.removeGeofences(viewModel.washHandsReminderLocationsLiveData.value!!.filter { it.enabled }.map {
                        it.id.toString()
                    })
                }

            }

            viewModel.setRemindUserToWashHandsWhenArrivedAtLocation(isChecked)
            showHideWashHandsReminderLocations(isChecked)

        }

        viewModel.washHandsReminderLocationsLiveData.observe(viewLifecycleOwner, Observer {
            reminderLocationAdapter.refill(it)
        })

        viewModel.insertedWashHandsReminderLocationsLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null){
                geofencingUtils.addGeoFences(listOf(it))
                viewModel.resetInsertedWashHandsReminderLocationsLiveData()
            }
        })

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {

            MY_PERMISSIONS_REQUEST_FINE_LOCATION ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    requestBackgroundLocationPermission()
                }else{
                    binding.remHandLocS.isChecked = false
                    showLongToast(requireContext(), getString(R.string.accept_permissions))
                }

            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    openWashHandsReminderLocationDialog(true)
                }else{
                    binding.remHandLocS.isChecked = false
                    showLongToast(requireContext(), getString(R.string.accept_permissions))
                }

        }
    }

    private fun setIntervalWashHand(intervalInMinutes: Int) {
        viewModel.setWashHandsInterval(intervalInMinutes)
        updateUIIntervalWashHand(intervalInMinutes)

        createCancelWashHandsAlarm(requireContext(), minutesToMilliSecs(intervalInMinutes))

    }

    private fun updateUIIntervalWashHand(intervalInMinutes: Int) {
        if (intervalInMinutes == 0) {
            binding.remHandTime.text = getString(R.string.default_text_wash_hands_reminder)
            binding.remWashHandsS.isChecked = false
        } else {
            binding.remHandTime.text = getString(
                R.string.placeholder_wash_hands_reminder,
                convertIntervalToText(intervalInMinutes)
            )
            binding.remWashHandsS.isChecked = true
        }
    }

    private fun setIntervalDrinkWater(intervalInMinutes: Int) {
        viewModel.setDrinkWaterInterval(intervalInMinutes)
        updateUIIntervalDrinkWater(intervalInMinutes)

        createCancelDrinkWaterAlarm(requireContext(), minutesToMilliSecs(intervalInMinutes))
    }

    private fun updateUIIntervalDrinkWater(intervalInMinutes: Int) {
        if (intervalInMinutes == 0) {
            binding.remWater.text = getString(R.string.default_text_drink_water_reminder)
            binding.remDrinkWaterS.isChecked = false
        } else {
            binding.remWater.text = getString(
                R.string.placeholder_drink_water_reminder,
                convertIntervalToText(intervalInMinutes)
            )
            binding.remDrinkWaterS.isChecked = true
        }
    }

    fun convertIntervalToText(intervalInMinutes: Int): String {
        val hour = intervalInMinutes / 60
        val minute = intervalInMinutes % 60

        var intervalText = ""

        if (hour > 0) {
            intervalText += "$hour hour"
            if (hour > 1)
                intervalText += "s"

            intervalText += " "
        }

        if (minute > 0) {

            if (hour > 0)
                intervalText += "and "

            intervalText += "$minute minute"
            if (minute > 1)
                intervalText += "s"
        }

        return intervalText
    }

    fun openWashHandsDialog() {
        val reminderDialog = ReminderDialog (
            {intervalInMinutes ->
                setIntervalWashHand(intervalInMinutes)
            }, {
                updateUIIntervalWashHand(0)
            })
        reminderDialog.show(childFragmentManager, "Select wash hands interval dialog")
    }

    fun openDrinkWaterDialog() {
        val reminderDialog = ReminderDialog (
            {intervalInMinutes ->
                setIntervalDrinkWater(intervalInMinutes)
            }, {
                updateUIIntervalDrinkWater(0)
            })

        reminderDialog.show(childFragmentManager, "Select drink water interval dialog")
    }

    private fun requestFineLocationPermission() {
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            Toast.makeText(
                context,
                "Please allow Covid-19 Companion to access your Location so that it knows when you've arrived at preset locations and remind you to wash your hands",
                Toast.LENGTH_LONG
            ).show()
        }
        // No explanation needed; request the permission
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_FINE_LOCATION
        )

        // MY_PERMISSIONS_REQUEST_FINE_LOCATION is an
        // app-defined int constant. The callback method gets the
        // result of the request.
    }

    private fun checkHasFineLocationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    private fun requestBackgroundLocationPermission() {
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            Toast.makeText(
                context,
                "Please allow Covid-19 Companion to access your Location in the background so that it knows when you've arrived at preset locations and remind you to wash your hands",
                Toast.LENGTH_LONG
            ).show()
        }
        // No explanation needed; request the permission
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
        )

        // MY_PERMISSIONS_REQUEST_FINE_LOCATION is an
        // app-defined int constant. The callback method gets the
        // result of the request.
    }

    private fun checkHasBackgroundLocationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    private fun showHideWashHandsReminderLocations(show: Boolean){

        if (show){
            binding.remHandLocRV.show()
            binding.addRemLocationTV.show()
        }else{
            binding.remHandLocRV.makeDisappear()
            binding.addRemLocationTV.makeDisappear()
        }

    }

    private fun openWashHandsReminderLocationDialog(isNewLocation: Boolean, reminderLocation: WashHandsReminderLocation = WashHandsReminderLocation()){
        WashHandsReminderLocationBottomDialogFragment(
            isNewLocation,
            reminderLocation,
            {
                viewModel.insertWashHandsReminderLocation(it)
            },
            {reminderLocation, addressChanged ->
                viewModel.updateWashHandsReminderLocation(reminderLocation)
                if (addressChanged){
                    geofencingUtils.removeGeofences(listOf(reminderLocation.id.toString()))
                    geofencingUtils.addGeoFences(listOf(reminderLocation))
                }
            }
        ).show(childFragmentManager, "WashHandsReminderLocation")
    }

}
