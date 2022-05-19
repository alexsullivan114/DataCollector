package com.alexsullivan.datacollor

import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableType
import com.alexsullivan.datacollor.database.entities.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class TrackableSerializerTest {
    private val trackables = listOf(
        Trackable(id = "Id1", title = "TTrackable 1", enabled = true, type = TrackableType.BOOLEAN),
        Trackable(id = "Id2", title = "ATrackable 2", enabled = true, type = TrackableType.NUMBER),
        Trackable(id = "Id3", title = "ATrackable 3", enabled = false, type = TrackableType.RATING),
        Trackable(id = "Id4", title = "FTrackable 4", enabled = true, type = TrackableType.BOOLEAN),
        Trackable(id = "Id5", title = "ZTrackable 5", enabled = true, type = TrackableType.NUMBER),
        Trackable(id = "Id6", title = "QTrackable 6", enabled = false, type = TrackableType.RATING),
        Trackable(id = "Id7", title = "QTrackable 7", enabled = true, type = TrackableType.BOOLEAN),
        Trackable(id = "Id8", title = "ATrackable 8", enabled = true, type = TrackableType.BOOLEAN),
    )
    private val entities = listOf(
        TrackableEntity.Boolean(BooleanTrackableEntity("Id1", true, buildDate(0))),
        TrackableEntity.Number(NumberTrackableEntity("Id2", 6, buildDate(0))),
        TrackableEntity.Number(NumberTrackableEntity("Id2", 3, buildDate(1))),
        TrackableEntity.Rating(RatingTrackableEntity("Id3", Rating.MEDIOCRE, buildDate(3))),
        TrackableEntity.Boolean(BooleanTrackableEntity("Id4", true, buildDate(4))),
        TrackableEntity.Number(NumberTrackableEntity("Id5", 8, buildDate(5))),
        TrackableEntity.Rating(RatingTrackableEntity("Id6", Rating.TERRIBLE, buildDate(6))),
        TrackableEntity.Boolean(BooleanTrackableEntity("Id7", true, buildDate(7))),
    )

    private fun buildDate(daysOffset: Int): Date {
        val calendar = GregorianCalendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, daysOffset)
        return calendar.time
    }

    @Test
    fun testSerializing() {
        val serializedString = TrackableSerializer.serialize(entities, trackables)
        val expected = """
            1970-01-01 0:00:00, ATrackable 2, 
        """.trimIndent()
    }
}
