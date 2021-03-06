package it.wemake.covid19Companion.domain.usecases

import it.wemake.covid19Companion.domain.repository.ICovid19CasesRepository
import javax.inject.Inject

class GetPagedCountriesCasesDataUseCase @Inject constructor(
    private val covid19CasesRepository: ICovid19CasesRepository
) {

    suspend operator fun invoke(page: Int, pageSize: Int, sortBy: String) = covid19CasesRepository.getPagedCountriesCasesData(page, pageSize, sortBy)

}