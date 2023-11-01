package com.alexsullivan.datacollor.weather.location_import

import java.time.ZonedDateTime

data class TakeoutSemanticLocationHistoryMonth(val timelineObjects: List<TimelineObject>)

data class TimelineObject(val placeVisit: PlaceVisit?, val activitySegment: ActivitySegment?)

data class PlaceVisit(val location: TakeoutLocation, val duration: TakeoutDuration)

data class TakeoutLocation(val latitudeE7: Double, val longitudeE7: Double)

data class TakeoutDuration(val startTimestamp: ZonedDateTime)

data class ActivitySegment(val startLocation: TakeoutLocation, val duration: ActivityTimestamp)

data class ActivityTimestamp(val endTimestamp: ZonedDateTime)
