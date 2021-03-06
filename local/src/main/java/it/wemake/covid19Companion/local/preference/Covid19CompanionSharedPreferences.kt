package it.wemake.covid19Companion.local.preference

import android.content.Context
import android.content.SharedPreferences
import it.wemake.covid19Companion.local.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class Covid19CompanionSharedPreferences constructor(
    context: Context
) {

    private val covid19CompanionAppSharedPref = context.getSharedPreferences(
        COVID_19_COMPANION_SHARED_PREFERENCES, Context.MODE_PRIVATE)

    @ExperimentalCoroutinesApi
    suspend fun getCasesSummaryLastUpdatedFlow(): Flow<Long> =
        callbackFlow {
            offer(covid19CompanionAppSharedPref.getLong(CASES_SUMMARY_LAST_UPDATED, 0))
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreference, key ->
                if (key == CASES_SUMMARY_LAST_UPDATED){
                    offer(sharedPreference.getLong(CASES_SUMMARY_LAST_UPDATED, 0))
                }
            }
            covid19CompanionAppSharedPref.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { covid19CompanionAppSharedPref.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    fun updateCasesSummaryLastUpdated(lastUpdated: Long){
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putLong(CASES_SUMMARY_LAST_UPDATED, lastUpdated)
        editor.apply()
    }

    fun getCasesSummaryLastUpdated(): Long =
        covid19CompanionAppSharedPref.getLong(CASES_SUMMARY_LAST_UPDATED, 0)

    fun getWHOHandHygieneDownloadId(): Long =
        covid19CompanionAppSharedPref.getLong(WHO_HAND_HYGIENE_BROCHURE_DOWNLOAD_ID, 0)

    fun setWHOHandHygieneDownloadId(downloadId: Long) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putLong(WHO_HAND_HYGIENE_BROCHURE_DOWNLOAD_ID, downloadId)
        editor.apply()
    }

    fun getUserCountryIso2(): String =
        covid19CompanionAppSharedPref.getString(USER_COUNTRY_ISO2, "")!!

    fun setUserCountryIso2(userCountryIso2: String) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putString(USER_COUNTRY_ISO2, userCountryIso2)
        editor.apply()
    }

    fun getWashHandsInterval(): Int =
        covid19CompanionAppSharedPref.getInt(WASH_HANDS_INTERVAL, 0)

    fun setWashHandsInterval(interval: Int) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putInt(WASH_HANDS_INTERVAL, interval)
        editor.apply()
    }

    fun getDrinkWaterInterval(): Int =
        covid19CompanionAppSharedPref.getInt(DRINK_WATER_INTERVAL, 0)

    fun setDrinkWaterInterval(interval: Int) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putInt(DRINK_WATER_INTERVAL, interval)
        editor.apply()
    }

    fun getUseCustomNotificationTone(): Boolean =
        covid19CompanionAppSharedPref.getBoolean(USE_CUSTOM_NOTIFICATION_TONE, true)

    fun setUseCustomNotificationTone(useCustomNotificationTone: Boolean) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putBoolean(USE_CUSTOM_NOTIFICATION_TONE, useCustomNotificationTone)
        editor.apply()
    }

    fun getRemindUserToWashHandsWhenArrivedAtLocation(): Boolean =
        covid19CompanionAppSharedPref.getBoolean(REMIND_USER_TO_WASH_HANDS_WHEN_ARRIVED_AT_LOCATION, false)

    fun setRemindUserToWashHandsWhenArrivedAtLocation(useCustomNotificationTone: Boolean) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putBoolean(REMIND_USER_TO_WASH_HANDS_WHEN_ARRIVED_AT_LOCATION, useCustomNotificationTone)
        editor.apply()
    }

    fun getDailyMotivation(): String =
        covid19CompanionAppSharedPref.getString(DAILY_MOTIVATION, DEFAULT_DAILY_MOTIVATION)!!

    fun setDailyMotivation(dailyMotivation: String) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putString(DAILY_MOTIVATION, dailyMotivation)
        editor.apply()
    }

    fun getLatestVersionCode(): Int =
        covid19CompanionAppSharedPref.getInt(LATEST_VERSION_CODE, 1)

    fun setLatestVersionCode(latestVersionCode: Int) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putInt(LATEST_VERSION_CODE, latestVersionCode)
        editor.apply()
    }

    fun getAppUpdateDownloadId(): Long =
        covid19CompanionAppSharedPref.getLong(APP_UPDATE_DOWNLOAD_ID, 0)

    fun setAppUpdateDownloadId(downloadId: Long) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putLong(APP_UPDATE_DOWNLOAD_ID, downloadId)
        editor.apply()
    }

    fun getUsername(): String =
        covid19CompanionAppSharedPref.getString(USERNAME, "Survivor \uD83D\uDCAA\uD83D\uDCAA\uD83D\uDCAA")!!

    fun setUsername(username: String) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putString(USERNAME, username)
        editor.apply()
    }

    fun getHasLongPressedSplashscreen(): Boolean =
        covid19CompanionAppSharedPref.getBoolean(HAS_LONG_PRESSED_SPLASHSCREEN, false)

    fun setHasLongPressedSplashscreen(hasLongPressedSplashscreen: Boolean) {
        val editor = covid19CompanionAppSharedPref.edit()
        editor.putBoolean(HAS_LONG_PRESSED_SPLASHSCREEN, hasLongPressedSplashscreen)
        editor.apply()
    }

}