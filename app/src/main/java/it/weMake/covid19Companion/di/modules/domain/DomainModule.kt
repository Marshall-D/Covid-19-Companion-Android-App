package it.weMake.covid19Companion.di.modules.domain

import dagger.Module
import dagger.Provides
import it.wemake.covid19Companion.data.impl.*
import it.wemake.covid19Companion.domain.usecases.*

@Module
class DomainModule {

    @Provides
    fun provideUpdateCasesDataUseCase(
        covid19CasesRepository: Covid19CasesRepository
    ): UpdateCasesDataUseCase = UpdateCasesDataUseCase(covid19CasesRepository)

    @Provides
    fun provideGetGlobalCasesDataUseCase(
        covid19CasesRepository: Covid19CasesRepository
    ): GetGlobalCasesDataUseCase = GetGlobalCasesDataUseCase(covid19CasesRepository)

    @Provides
    fun provideGetWHOHandHygieneBrochureDownloadIdLastUpdatedUseCase(
        sharedPreferencesRepository: SharedPreferencesRepository
    ): GetWHOHandHygieneBrochureDownloadIdLastUpdatedUseCase = GetWHOHandHygieneBrochureDownloadIdLastUpdatedUseCase(sharedPreferencesRepository)

    @Provides
    fun provideSetWHOHandHygieneBrochureDownloadIdLastUpdatedUseCase(
        sharedPreferencesRepository: SharedPreferencesRepository
    ): SetWHOHandHygieneBrochureDownloadIdLastUpdatedUseCase = SetWHOHandHygieneBrochureDownloadIdLastUpdatedUseCase(sharedPreferencesRepository)

    @Provides
    fun provideGetCountriesCasesDataCasesUseCase(
        covid19CasesRepository: Covid19CasesRepository
    ): GetCountriesCasesDataUseCase = GetCountriesCasesDataUseCase(covid19CasesRepository)

    @Provides
    fun provideSearchCountriesCasesDataCasesUseCase(
        covid19CasesRepository: Covid19CasesRepository
    ): SearchCountriesCasesDataUseCase = SearchCountriesCasesDataUseCase(covid19CasesRepository)

    @Provides
    fun provideGetPreventionTipsUseCase(
        preventionTipsRepository: PreventionTipsRepository
    ): GetPreventionTipsUseCase = GetPreventionTipsUseCase(preventionTipsRepository)

    @Provides
    fun provideRequestNextQuestionUseCase(
        screeningToolRepository: ScreeningToolRepository
    ): RequestNextQuestionUseCase = RequestNextQuestionUseCase(screeningToolRepository)

    @Provides
    fun provideGetDiagnosisUseCase(
        screeningToolRepository: ScreeningToolRepository
    ): GetDiagnosisUseCase = GetDiagnosisUseCase(screeningToolRepository)

//    @Provides
//    fun provideGetCountriesUseCase(
//        countriesRepository: CountriesRepository
//    ): GetCountriesUseCase = GetCountriesUseCase(countriesRepository)

}