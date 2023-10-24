package com.alexsullivan.datacollor.weather.location_import

import android.content.Context
import com.alexsullivan.datacollor.R
import com.squareup.moshi.Moshi
import javax.inject.Inject

class TakeoutDataManager @Inject constructor(){
    fun importTakeoutData(semanticLocationHistoryMonthString: String) {
        val moshi = Moshi.Builder().add(TimeAdapter()).build()
        val takeoutSemanticLocationHistoryMonth =
            moshi.adapter(TakeoutSemanticLocationHistoryMonth::class.java)
                .fromJson(semanticLocationHistoryMonthString)
        val locationInDegrees =
            takeoutSemanticLocationHistoryMonth?.timelineObjects?.map {
                val location = it.placeVisit?.location ?: return@map it
                val updatedLocation = location.copy(
                    latitudeE7 = location.latitudeE7 / DEGREES_DIVISION_FACTOR,
                    longitudeE7 = location.longitudeE7 / DEGREES_DIVISION_FACTOR
                )
                it.copy(placeVisit = it.placeVisit.copy(location = updatedLocation))
            }
        print("Nice: $locationInDegrees")
    }

    fun triggerImport(context: Context) {
        val text = context.resources.openRawResource(R.raw.nov_2021_takeout).bufferedReader().use { it.readText() }
        importTakeoutData(text)
    }
}

private const val DEGREES_DIVISION_FACTOR = 10000000
