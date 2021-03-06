package it.weMake.covid19Companion.di.modules.repository

import dagger.Binds
import dagger.Module
import it.wemake.covid19Companion.data.impl.*
import it.wemake.covid19Companion.domain.repository.*
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindCovid19CasesRepository(
        covid19CasesRepository: Covid19CasesRepository
    ): ICovid19CasesRepository

    @Singleton
    @Binds
    abstract fun bindSharedPreferencesRepository(
        sharedPreferencesRepository: SharedPreferencesRepository
    ): ISharedPreferencesRepository

    @Singleton
    @Binds
    abstract fun bindPreventionTipsRepository(
        preventionTipsRepository: PreventionTipsRepository
    ): IPreventionTipsRepository

    @Singleton
    @Binds
    abstract fun bindScreeningToolRepository(
        screeningToolRepository: ScreeningToolRepository
    ): IScreeningToolRepository

    @Singleton
    @Binds
    abstract fun bindCountriesRepository(
        countriesRepository: CountriesRepository
    ): ICountriesRepository

    @Singleton
    @Binds
    abstract fun bindWashHandsReminderLocationsRepository(
        washHandsReminderLocationsRepository: WashHandsReminderLocationsRepository
    ): IWashHandsReminderLocationsRepository

    @Singleton
    @Binds
    abstract fun bindAppReleasesRepository(
        appReleasesRepository: AppReleasesRepository
    ): IAppReleasesRepository

    @Singleton
    @Binds
    abstract fun bindSourcesRepository(
        sourcesRepository: SourcesRepository
    ): ISourcesRepository

    @Singleton
    @Binds
    abstract fun bindDevelopmentTeamRepository(
        developmentTeamRepository: DevelopmentTeamRepository
    ): IDevelopmentTeamRepository

}