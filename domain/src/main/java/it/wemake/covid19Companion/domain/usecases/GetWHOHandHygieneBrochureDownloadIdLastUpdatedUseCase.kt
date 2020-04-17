package it.wemake.covid19Companion.domain.usecases

import it.wemake.covid19Companion.domain.repository.ISharedPreferencesRepository
import javax.inject.Inject

class GetWHOHandHygieneBrochureDownloadIdLastUpdatedUseCase @Inject constructor(
    private val sharedPreferencesRepository: ISharedPreferencesRepository
) {

    operator fun invoke() = sharedPreferencesRepository.getWHOHandHygieneBrochureDownloadId()

}