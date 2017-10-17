package org.cmapi.primitives

/**
 * A rectangular box representing a geospatial area
 */
data class GeoBounds(
        /**
         * The western most longitude of the rectangle
         */
        var west: Double = 0.0,
        /**
         * The eastern most longitude of the rectangle
         */
        var east: Double = 0.0,
        /**
         * The northern most latitude of the rectangle
         */
        var north: Double = 0.0,
        /**
         * The southern most latitude of the rectangle
         */
        var south: Double = 0.0
)


data class GeoPosition(
        /**
         * Latitude value in degrees decimal (i.e. 23.4567) derived from WGS-84
         */
        var latitude: Double = 0.0,
        /**
         * Longitude value in degrees decimal (i.e. 23.4567) derived from WGS-84
         */
        var longitude: Double = 0.0,
        /**
         * A value in meters representing the altitude of the associated position.  This will be interpreted base on the altitudeMode provided in the IGeoAltitudeMode enumeration
         */
        var altitude: Double = 0.0
)

data class GeoPositionGroup(
        /**
         * An ordered list of IGeoPosition objects representing a 1 or more positions with an associated IGeoAltitudeMode to interpret the altitude values.  In the case of a point, a single position will create a single icon, whereas multitple positions will create the same icon at multiple positions to be interpreted as a composite feature. For consistency, and IGeoRenderables use an IGeoPositionGroup even when only containing a single positions
         */
        var positions: List<GeoPosition> = java.util.ArrayList(),
        /**
         * Defines a point in time that something occurred, was created, or was last updated time value as defined by http://tools.ietf.org/html/rfc3339
         */
        var timeStamp: java.util.Date? = null,
        /**
         * Defines one or more periods of time something occurred, or was active.
         */
        var timeSpans: List<IGeoTimeSpan> = java.util.ArrayList()
)
