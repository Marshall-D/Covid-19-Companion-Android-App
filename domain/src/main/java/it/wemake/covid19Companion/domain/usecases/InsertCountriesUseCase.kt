package it.wemake.covid19Companion.domain.usecases

import it.wemake.covid19Companion.domain.models.CountryDomainModel
import it.wemake.covid19Companion.domain.repository.ICovid19CasesRepository
import javax.inject.Inject

class InsertCountriesUseCase @Inject constructor(
    private val covid19CasesRepository: ICovid19CasesRepository
) {

    suspend operator fun invoke(countries: List<CountryDomainModel>) =
        covid19CasesRepository.insertCountries(countries)

}