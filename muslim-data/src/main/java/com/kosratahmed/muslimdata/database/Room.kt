package com.kosratahmed.muslimdata.database

import android.content.Context
import androidx.room.*
import com.kosratahmed.muslimdata.models.City
import com.kosratahmed.muslimdata.models.Country
import com.kosratahmed.muslimdata.models.CountryAndCity
import com.kosratahmed.muslimdata.models.PrayerTime

@Dao
interface MuslimDataDao {

    @Transaction
    @Query("SELECT * FROM city WHERE city_name LIKE :city")
    fun searchCity(city: String): List<CountryAndCity>

    @Transaction
    @Query("SELECT * FROM city WHERE country_code = :countryCode COLLATE NOCASE and city_name = :city COLLATE NOCASE")
    fun geoCoder(countryCode: String, city: String): CountryAndCity

    @Transaction
    @Query("SELECT * FROM city ORDER BY abs(latitude - :latitude) + abs(longitude - :longitude) LIMIT 1")
    fun geoCoder(latitude: Double, longitude: Double): CountryAndCity

    @Query("SELECT * FROM prayer_time WHERE city = :city")
    fun getPrayerTimes(city: String): List<PrayerTime>
}

@Database(entities = [Country::class, City::class, PrayerTime::class], version = 1)
abstract class MuslimDataDatabase : RoomDatabase() {
    abstract val muslimDataDao: MuslimDataDao

    companion object {

        @Volatile
        private var INSTANCE: MuslimDataDatabase? = null

        fun getInstance(context: Context): MuslimDataDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, MuslimDataDatabase::class.java,
                        "muslim_db.db"
                    )
                        .createFromAsset("database/muslim_db_v1.0.0.db")
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
