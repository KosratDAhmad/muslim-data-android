package com.kosratahmed.muslimdata.repository

import android.content.Context
import com.kosratahmed.muslimdata.database.MuslimDataDatabase
import com.kosratahmed.muslimdata.extensions.formatToDBDate
import com.kosratahmed.muslimdata.extensions.toDate
import com.kosratahmed.muslimdata.models.City
import com.kosratahmed.muslimdata.models.NameOfAllah
import com.kosratahmed.muslimdata.models.UserLocation
import com.kosratahmed.muslimdata.models.prayertime.CalculatedPrayerTime
import com.kosratahmed.muslimdata.models.prayertime.PrayerAttribute
import com.kosratahmed.muslimdata.models.prayertime.PrayerTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class MuslimRepository(context: Context) {
    private val muslimDb = MuslimDataDatabase.getInstance(context)

    suspend fun searchCity(city: String) = withContext(Dispatchers.IO) {
        UserLocation.mapDBLocations(muslimDb.muslimDataDao.searchCity("$city%"))
    }

    suspend fun geoCoder(countryCode: String, city: String) = withContext(Dispatchers.IO) {
        UserLocation.mapDBLocation(muslimDb.muslimDataDao.geoCoder(countryCode, city))
    }

    suspend fun geoCoder(latitude: Double, longitude: Double) = withContext(Dispatchers.IO) {
        UserLocation.mapDBLocation(muslimDb.muslimDataDao.geoCoder(latitude, longitude))
    }

    suspend fun getPrayerTimes(location: UserLocation, date: Date, attribute: PrayerAttribute): PrayerTime {
        return withContext(Dispatchers.IO) {
            val prayerTime: PrayerTime
            if (location.hasFixedPrayerTime) {
                val fixedPrayer = muslimDb.muslimDataDao.getPrayerTimes(
                    location.countryCode,
                    location.cityName,
                    date.formatToDBDate()
                )
                prayerTime = PrayerTime(
                    fixedPrayer.fajr.toDate(),
                    fixedPrayer.sunrise.toDate(),
                    fixedPrayer.dhuhr.toDate(),
                    fixedPrayer.asr.toDate(),
                    fixedPrayer.maghrib.toDate(),
                    fixedPrayer.isha.toDate()
                )
                prayerTime.adjustDST()
            } else {
                prayerTime = CalculatedPrayerTime(attribute).getPrayerTimes(location, date)
            }
            prayerTime.applyOffset(attribute.offset)
            prayerTime
        }
    }

    suspend fun getNamesOfAllah(language: String) = withContext(Dispatchers.IO) {
        NameOfAllah.mapDBNames(muslimDb.muslimDataDao.getNames(language))
    }

    suspend fun getAzkarCategories(language: String) = withContext(Dispatchers.IO) {
        muslimDb.muslimDataDao.getAzkarCategories(language)
    }

    suspend fun getAzkarChapters(language: String) = withContext(Dispatchers.IO) {
        muslimDb.muslimDataDao.getAzkarChapters(language)
    }

    suspend fun getAzkarItems(chapterId: Int, language: String) = withContext(Dispatchers.IO) {
        muslimDb.muslimDataDao.getAzkarItems(chapterId, language)
    }
}