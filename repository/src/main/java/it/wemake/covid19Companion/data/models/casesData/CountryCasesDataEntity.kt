package it.wemake.covid19Companion.data.models.casesData

import it.wemake.covid19Companion.data.models.CountryInfoEntity

data class CountryCasesDataEntity(
    val displayName: String,
    val lastUpdated: Long,
    val countryInfo: CountryInfoEntity,
    val totalConfirmed: Int?,
    val totalDeaths: Int?,
    val totalRecovered: Int?,
    val totalConfirmedDelta: Int?,
    val totalDeathsDelta: Int?,
    val totalRecoveredDelta: Int?,
    val continent: String,
    val casesPerOneMillion: Double?,
    val deathsPerOneMillion: Double?,
    val recoveredPerOneMillion: Double?,
    var hasRegionalCasesData: Boolean
)