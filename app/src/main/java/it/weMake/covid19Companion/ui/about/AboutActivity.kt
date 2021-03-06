package it.weMake.covid19Companion.ui.about

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.android.support.DaggerAppCompatActivity
import it.weMake.covid19Companion.BuildConfig
import it.weMake.covid19Companion.R
import it.weMake.covid19Companion.databinding.ActivityAboutBinding
import it.weMake.covid19Companion.services.DownloadManagerIntentService
import it.weMake.covid19Companion.ui.about.adapters.AppReleasesAdapter
import it.weMake.covid19Companion.ui.about.adapters.DevelopmentTeamAdapter
import it.weMake.covid19Companion.ui.about.adapters.ResourcesAdapter
import it.weMake.covid19Companion.utils.*
import java.io.File
import javax.inject.Inject

const val EXTRA_FROM_NOTIFICATION = "FROM_NOTIFICATION"

class AboutActivity : DaggerAppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAboutBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected val viewModel: AboutViewModel by viewModels { viewModelFactory }

    private lateinit var appReleasesAdapter: AppReleasesAdapter
    private var updateAvailable: Boolean = false
    private lateinit var resourcesAdapter: ResourcesAdapter
    private lateinit var developmentTeamAdapter: DevelopmentTeamAdapter

    private val downloadManagerBroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action){

                ACTION_DOWNLOAD_RUNNING -> {
                    if (intent.getLongExtra(EXTRA_DOWNLOAD_ID, 0) == viewModel.getAppUpdateDownloadId() && binding.appUpdatingPB.visibility == View.GONE){
                        binding.updateAppMB.makeDisappear()
                        binding.appUpdatingPB.show()
                    }
                }

                else -> {
                    if (intent.getLongExtra(EXTRA_DOWNLOAD_ID, 0) == viewModel.getAppUpdateDownloadId())
                        binding.appUpdatingPB.makeDisappear()
                }

            }
        }
    }
    private val downloadManagerIntentFilter = IntentFilter(ACTION_DOWNLOAD_STOPPED).apply {
        addAction(ACTION_DOWNLOAD_COMPLETED)
        addAction(ACTION_DOWNLOAD_RUNNING)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val versionNumber = BuildConfig.VERSION_CODE
        updateAvailable = versionNumber < viewModel.getLatestVersionCode()
        appReleasesAdapter = AppReleasesAdapter(versionNumber)
        val isFromNotification = intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)
        resourcesAdapter = ResourcesAdapter()
        developmentTeamAdapter = DevelopmentTeamAdapter()

        binding.versionTV.text = BuildConfig.VERSION_NAME
        binding.releasesRV.adapter = appReleasesAdapter
        if (updateAvailable){
            binding.newReleaseIV.show()
            binding.updateAppMB.show()
        }
        binding.resourcesRV.adapter = resourcesAdapter
        binding.developmentTeamRV.adapter = developmentTeamAdapter

        attachObservers()
        binding.updateAppMB.setOnClickListener(this)
        binding.releasesCV.setOnClickListener(this)
        binding.resourcesTV.setOnClickListener(this)
        binding.developmentTeamTV.setOnClickListener(this)

        if (isFromNotification){
            showHideReleases()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadManagerBroadcastReceiver, downloadManagerIntentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadManagerBroadcastReceiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            updateApp()
        }else{
            showLongToast(this, getString(R.string.storage_permission_request_app_update))
        }

    }

    override fun onClick(v: View) {
        when(v.id){

            R.id.updateAppMB -> updateApp()

            R.id.releasesCV -> showHideReleases()

            R.id.resourcesTV -> showHideResources()

            R.id.developmentTeamTV -> showHideDevelopmentTeam()

        }
    }

    private fun attachObservers() {
        viewModel.appReleasesLiveData.observe(this, Observer {
            appReleasesAdapter.refill(it)
        })

        viewModel.sourcesLiveData.observe(this, Observer {
            resourcesAdapter.refill(it)
        })

        viewModel.teamMembersLiveData.observe(this, Observer {
            developmentTeamAdapter.refill(it)
        })
    }

    private fun updateApp(){
        if (isStoragePermissionGranted()){
            if (latestAppApkFileExists()){
                openAppUpdateApk(this, viewModel.latestVersionName)
            }else{
                downloadLatestAppApk()
            }
        }
    }

    private fun downloadLatestAppApk() {
        binding.updateAppMB.makeDisappear()
        binding.appUpdatingPB.show()
        DownloadManagerIntentService.startActionDownloadAppUpdate(this, viewModel.latestVersionName)
    }

    private fun latestAppApkFileExists(): Boolean{
        val file = File(ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_DOCUMENTS)[0].path + "/" + getString(
            R.string.placeholder_app_version_file_name, viewModel.latestVersionName))
        return file.exists()
    }

    private fun showHideReleases(){
        val shouldShowReleases = binding.releasesRV.visibility != View.VISIBLE

        if (shouldShowReleases){
            binding.releasesRV.show()
            binding.releasesIV.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            if (updateAvailable)
                binding.whatsNewTV.show()
        }else{
            binding.releasesRV.makeDisappear()
            binding.releasesIV.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            if (updateAvailable)
                binding.whatsNewTV.makeDisappear()
        }

    }

    private fun showHideResources(){
        val shouldShowResources = binding.resourcesRV.visibility != View.VISIBLE

        if (shouldShowResources){
            binding.resourcesRV.show()
            binding.resourcesTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_black_24dp, 0)
        }else{
            binding.resourcesRV.makeDisappear()
            binding.resourcesTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_black_24dp, 0)
        }

    }

    private fun showHideDevelopmentTeam(){
        val shouldShowResources = binding.developmentTeamRV.visibility != View.VISIBLE

        if (shouldShowResources){
            binding.developmentTeamRV.show()
            binding.developmentTeamTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_black_24dp, 0)
        }else{
            binding.developmentTeamRV.makeDisappear()
            binding.developmentTeamTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_black_24dp, 0)
        }

    }

}
