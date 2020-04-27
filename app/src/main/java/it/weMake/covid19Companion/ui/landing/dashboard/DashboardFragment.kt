package it.weMake.covid19Companion.ui.landing.dashboard

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import it.weMake.covid19Companion.R
import it.weMake.covid19Companion.databinding.FragmentDashboardBinding
import it.weMake.covid19Companion.services.DownloadManagerIntentService
import it.weMake.covid19Companion.ui.landing.dashboard.adapters.GlobalCasesStatsAdapter
import it.weMake.covid19Companion.ui.landing.dashboard.adapters.DashboardAdapter
import it.weMake.covid19Companion.utils.*
import java.io.File
import javax.inject.Inject

class DashboardFragment : DaggerFragment(), View.OnClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected val dashboardViewModel: DashboardViewModel by viewModels { viewModelFactory }

    lateinit var fragmentBinding: FragmentDashboardBinding
    lateinit var globalCasesStatsAdapter: GlobalCasesStatsAdapter
    lateinit var dashboardAdapter: DashboardAdapter

    private lateinit var handler: Handler
    private val autoScrollDelayedTime: Long = 2500
    private val autoScrollCountryStatsRunnable: Runnable = Runnable {

        val lastVisibleItemPosition = (fragmentBinding.casesStatsRV.layoutManager!! as LinearLayoutManager).findLastVisibleItemPosition()

        if (lastVisibleItemPosition != 2){
            fragmentBinding.casesStatsRV.smoothScrollToPosition(lastVisibleItemPosition + 1)
        }else{
            fragmentBinding.casesStatsRV.smoothScrollToPosition(0)
        }

        autoScrollCountryStatsDelayed()

    }

    private val downloadManagerBroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            fragmentBinding.handHygienePB.makeDisappear()
        }

    }
    private val downloadManagerIntentFilter = IntentFilter(ACTION_DOWNLOAD_STOPPED).apply {
        addAction(ACTION_DOWNLOAD_COMPLETED)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        fragmentBinding = FragmentDashboardBinding.inflate(inflater, container, false)

        handler = Handler()
        globalCasesStatsAdapter = GlobalCasesStatsAdapter()
        dashboardAdapter = DashboardAdapter()

        fragmentBinding.casesStatsRV.adapter = globalCasesStatsAdapter
        fragmentBinding.casesRV.adapter = dashboardAdapter

        attachObservers()

        fragmentBinding.searchIV.setOnClickListener(this)
        fragmentBinding.handHygieneCV.setOnClickListener(this)

        return fragmentBinding.root
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(downloadManagerBroadcastReceiver, downloadManagerIntentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(downloadManagerBroadcastReceiver)
    }

    override fun onClick(v: View) {

        when(v.id){

            R.id.searchIV -> {

                if (fragmentBinding.searchET.visibility == View.GONE){

                    fragmentBinding.searchET.show()
                    fragmentBinding.searchIV.setImageResource(R.drawable.ic_close_ash_24dp)

                }else{

                    fragmentBinding.searchET.makeDisappear()
                    fragmentBinding.searchIV.setImageResource(R.drawable.ic_search)
                    fragmentBinding.searchET.setText("")

                }

            }

            R.id.handHygieneCV -> {
                if (isStoragePermissionGranted()){
                    if (WHOHandHygieneBrochureExists()){
                        val intent = Intent(Intent.ACTION_VIEW)
                        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.path + File.pathSeparator + WHO_HAND_HYGIENE_PDF)
                        val data = FileProvider.getUriForFile(requireContext().applicationContext, requireContext().packageName +".fileprovider", file)
                        val type = "application/pdf"
                        intent.setDataAndType(data, type)
                        intent.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
                        startActivity(intent)
                    }else{
                        downloadHandHygienePDF()
                    }
                }
            }

        }

    }

    private val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            downloadHandHygienePDF()
        }

    }

    private fun attachObservers(){

        dashboardViewModel.casesDataLastUpdated.observe(viewLifecycleOwner, Observer {
            if(it == "Never"){
                fragmentBinding.lastUpdatedValueTV.text = it
            }else {
                fragmentBinding.lastUpdatedValueTV.text = it.getTimeFromToday()
            }
        })

        dashboardViewModel.globalCasesData.observe(viewLifecycleOwner, Observer {
            it?.let {
                globalCasesStatsAdapter.updateGlobalCasesData(it)
                autoScrollCountryStatsDelayed()
            }
        })

        dashboardViewModel.pagedCountriesCasesData.observe(viewLifecycleOwner, Observer {
            dashboardAdapter.addPage(it)
        })

//        dashboardViewModel.updateCasesSummary()

        fragmentBinding.searchET.addTextChangedListener {
            dashboardViewModel.search(it.toString())
        }

        fragmentBinding.casesRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dashboardAdapter.itemCount != 3){
                    if(dy > 0){
                        Log.i("RecyclerView scrolled: ", "scroll up!")
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        if (layoutManager.findLastVisibleItemPosition() == 17)
                            dashboardViewModel.loadPage(dashboardAdapter.pageBottom + 1)
                    }
                    else{
                        Log.i("RecyclerView scrolled: ", "scroll down!")
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        if (layoutManager.findFirstVisibleItemPosition() == 3 && dashboardAdapter.pageTop != 0)
                            dashboardViewModel.loadPage(dashboardAdapter.pageTop - 1)
                    }
                }

//                if( !recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && dashboardAdapter.itemCount != 3)
//                    dashboardViewModel.loadPage(dashboardAdapter.pageBottom + 1)
            }

        })

        dashboardViewModel.loadPage(0, 20)
    }

    private fun autoScrollCountryStatsDelayed(){
        handler.postDelayed(autoScrollCountryStatsRunnable, autoScrollDelayedTime)
    }

    private fun downloadHandHygienePDF(){
        fragmentBinding.handHygienePB.show()
        showLongToast(requireContext(), "Downloading Hand Hygiene Brochure(477kb)")
        DownloadManagerIntentService.startActionDownloadWHOHandHygieneBrochure(requireContext())
    }

    private fun WHOHandHygieneBrochureExists(): Boolean{
        val file = File(ContextCompat.getExternalFilesDirs(requireContext(), Environment.DIRECTORY_DOCUMENTS)[0].path + "/" + WHO_HAND_HYGIENE_PDF)
        return file.exists()
    }

    private fun isStoragePermissionGranted(): Boolean{

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(context!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                return true
            }else{
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST)
                return false
            }
        }else{
            return true
        }

    }

}
