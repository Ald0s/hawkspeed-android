package com.vljx.hawkspeed.domain.thirdparty

import com.vljx.hawkspeed.domain.models.world.BoundingBox
import com.vljx.hawkspeed.domain.models.world.Coordinate

class BoundingBoxUtil {
    companion object {
        /**
         * Base taken from https://stackoverflow.com/a/26939834.
         * Modified to use our domain object; Coordinate.
         */
        fun boundingBoxFrom(coordinates: List<Coordinate>): BoundingBox {
            var west = 0.0
            var east = 0.0
            var north = 0.0
            var south = 0.0
            for (lc in coordinates.indices) {
                val loc = coordinates[lc]
                if (lc == 0) {
                    north = loc.latitude
                    south = loc.latitude
                    west = loc.longitude
                    east = loc.longitude
                } else {
                    if (loc.latitude > north) {
                        north = loc.latitude
                    } else if (loc.latitude < south) {
                        south = loc.latitude
                    }
                    if (loc.longitude < west) {
                        west = loc.longitude
                    } else if (loc.longitude > east) {
                        east = loc.longitude
                    }
                }
            }
            return BoundingBox(
                Coordinate(south, west),
                Coordinate(north, east)
            )
        }
    }
}