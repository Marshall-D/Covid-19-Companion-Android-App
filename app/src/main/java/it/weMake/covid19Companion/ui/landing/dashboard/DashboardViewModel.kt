package it.weMake.covid19Companion.ui.landing.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.weMake.covid19Companion.commons.Loading
import it.weMake.covid19Companion.commons.Success
import it.weMake.covid19Companion.commons.UiStateViewModel
import it.weMake.covid19Companion.mappers.toPresentation
import it.weMake.covid19Companion.models.casesData.CountryCasesData
import it.weMake.covid19Companion.models.casesData.GlobalStats
import it.weMake.covid19Companion.models.PagedData
import it.weMake.covid19Companion.utils.SORT_BY_CONFIRMED
import it.wemake.covid19Companion.domain.usecases.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class DashboardViewModel
    @Inject constructor(
        val getPagedCountriesCasesDataUseCase: GetPagedCountriesCasesDataUseCase,
        val updateCasesDataUseCase: UpdateCasesDataUseCase,
        val getCasesDataLastUpdatedUseCase: GetCasesDataLastUpdatedUseCase,
        val getGlobalCasesDataUseCase: GetGlobalCasesDataUseCase,
        val searchCountriesCasesDataUseCase: SearchCountriesCasesDataUseCase,
        val getUserCountryCasesDataUseCase: GetUserCountryCasesDataUseCase,
        val getUsernameUseCase: GetUsernameUseCase
    ) : UiStateViewModel() {

    val pagedCountriesCasesData: LiveData<PagedData<List<CountryCasesData>>>
        get() = _pagedCountriesCasesData

    private var _countriesCasesData: MutableLiveData<List<CountryCasesData>> =
        MutableLiveData()

    private var _pagedCountriesCasesData: MutableLiveData<PagedData<List<CountryCasesData>>> =
        MutableLiveData()

    val casesDataLastUpdated: LiveData<Long>
        get() = _casesDataLastUpdated

    private var _casesDataLastUpdated: MutableLiveData<Long> =
        MutableLiveData()

    val globalCasesData: LiveData<GlobalStats>
        get() = _globalCasesData

    private var _globalCasesData: MutableLiveData<GlobalStats> =
        MutableLiveData()

//    private var page: Int = -1

    private var _pagedSearchCountriesCasesData: MutableLiveData<PagedData<List<CountryCasesData>>> =
        MutableLiveData()

    val pagedSearchCountriesCasesData: LiveData<PagedData<List<CountryCasesData>>>
        get() = _pagedSearchCountriesCasesData

    private var _userCountryCasesData = MutableLiveData<CountryCasesData>()
    val userCountryCasesData: LiveData<CountryCasesData>
        get() = _userCountryCasesData

    private var sortBy = SORT_BY_CONFIRMED

    init {

//        loadNextPage()

        viewModelScope.launch {
            getCasesDataLastUpdatedUseCase().collect{ it ->
                _casesDataLastUpdated.value = it
            }
        }

        viewModelScope.launch {
            getGlobalCasesDataUseCase().collect{
                _globalCasesData.value = it.toPresentation()
            }
        }

        viewModelScope.launch {
            getUserCountryCasesDataUseCase().map { it?.toPresentation() }.collect {
                _userCountryCasesData.value = it
            }
        }

    }

//    fun loadNextPage(){
//        viewModelScope.launch(handler) {
//            getCountriesCasesDataUseCase(++page).collect{ countries ->
//
//                _pagedCountriesCasesData.value = PagedData(
//                    page,
//                    countries.map {
//                        it.toPresentation()
//                    }
//                )
//            }
//        }
//
//    }

    fun loadPage(page: Int, pageSize: Int = 10){
        viewModelScope.launch(handler) {

            getPagedCountriesCasesDataUseCase(page, pageSize, sortBy).map { data -> data.map { it.toPresentation() } }.collect{ it ->
                _pagedCountriesCasesData.value = PagedData(
                    page,
                    it
                )
            }
        }

    }


    fun updateCasesData(){
        _uiState.value = Loading
        viewModelScope.launch(handler) {
            updateCasesDataUseCase()
            _uiState.value = Success
        }
    }

    fun pagedSearch(searchQuery: String, page: Int, pageSize: Int = 10){
        viewModelScope.launch(handler) {
            searchCountriesCasesDataUseCase(searchQuery, page, pageSize).collect{ countries ->

                _pagedSearchCountriesCasesData.value = PagedData(
                    page,
                    countries.map {
                        it.toPresentation()
                    }
                )
            }
        }

    }

    fun setSortBy(sortBy: String){
        this.sortBy = sortBy
        loadPage(0, 20)
    }

    fun getUsername(): String =
        getUsernameUseCase()

}