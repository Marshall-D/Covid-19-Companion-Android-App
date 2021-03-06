package it.wemake.covid19Companion.remote.models.casesData

data class CountryCasesDataRemoteModel(
    val updated: Long,
    val country: String,
    val countryInfo: CountryInfoRemoteModel,
    val cases: Int?,
    val todayCases: Int?,
    val deaths: Int?,
    val todayDeaths: Int?,
    val recovered: Int?,
    val todayRecovered: Int?,
    val continent: String,
    val casesPerOneMillion: Double?,
    val deathsPerOneMillion: Double?,
    val recoveredPerOneMillion: Double?
)