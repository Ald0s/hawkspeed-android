package com.vljx.hawkspeed.view.world

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackWithPath

/**
 * A class responsible for managing all world objects as they're drawn to a google map instance.
 */
class WorldObjectManager(
    private var mGoogleMap: GoogleMap? = null
) {
    // Map a track instance to a pair of marker and polyline. The marker and polyline are nullable objects as tracks can be tracked by this object
    // without the world objects actually being drawn to a map.
    private val trackMap: MutableMap<TrackWithPath, Pair<Marker?, Polyline?>> = mutableMapOf()

    /**
     * Attempt to locate a track with the given marker. This will return null if there is no track associated with this marker. A null result is acceptable,
     * because this means the marker may correspond with another object type.
     */
    fun findTrackWithMarker(marker: Marker): TrackWithPath? {
        trackMap.forEach { (trackWithPath, objectPair) ->
            if(objectPair.first == marker) {
                return trackWithPath
            }
        }
        return null
    }

    /**
     * Update the tracks with paths given the list. This will remove all tracks that are present in trackMap, and not present in the given list. Also,
     * add tracks that are present in tracksWithPaths but not present in trackMap. This will then call the updateDrawnTracks function, which will ensure
     * all tracks and their paths are correctly drawn.
     */
    fun updateTracks(tracksWithPaths: List<TrackWithPath>) {
        // First, remove tracks that are no longer present in trackMap.
        val tracksToRemove: MutableList<TrackWithPath> = mutableListOf()
        trackMap.keys.forEach { trackInMap ->
            if(!tracksWithPaths.contains(trackInMap)) {
                // The new list of tracks does NOT contain this track, which is currently in the map (and maybe drawn.)
                tracksToRemove.add(trackInMap)
            }
        }
        // Remove all those tracks.
        tracksToRemove.forEach { removeTrack(it) }
        // Now, deal with tracks that are NOT already in trackMap at all.
        tracksWithPaths.filter { !trackMap.contains(it) }.forEach { newTrackWithPath ->
            // Simply add it.
            trackMap[newTrackWithPath] = Pair(null, null)
        }
        // Finally, deal with tracks that already exist.
        val tracksToEdit: MutableMap<TrackWithPath, Pair<Marker?, Polyline?>> = mutableMapOf()
        tracksWithPaths.filter { trackMap.contains(it) }.forEach { newTrackWithPath ->
            // Add each combination here, along with the existing value for each, to the tracks to edit map.
            val existingPair: Pair<Marker?, Polyline?> = trackMap[newTrackWithPath]
                ?: throw NotImplementedError("Case where replacing existing track does not have an entry???")
            tracksToEdit[newTrackWithPath] = existingPair
        }
        // Now, for each in tracksToEdit, remove their existing values and replace.
        tracksToEdit.forEach { (trackWithPath, pair) ->
            // Remove the old.
            trackMap.remove(trackWithPath)
            // Put the new.
            trackMap[trackWithPath] = pair
        }
        // Ensure all tracks are correctly drawn.
        updateDrawnTracks()
    }

    /**
     * A function that will set a new google map instance in use, and will draw all existing objects to that map.
     */
    fun setGoogleMap(googleMap: GoogleMap) {
        this.mGoogleMap = googleMap
        // Now, redraw all objects.
        updateDrawnTracks()
    }

    /**
     * A function that will set this manager to a state where no google map is in use, and all world objects are removed.
     */
    fun onDestroyView() {
        // For all objects, we'll simply remove them.
        trackMap.values.forEach { (marker, polyline) ->
            marker?.remove()
            polyline?.remove()
        }
        // Set the google maps instance to null.
        mGoogleMap = null
    }

    /**
     * Update all track objects to ensure they are drawn to the google map, to spec of their
     */
    private fun updateDrawnTracks() {
        // If google map is not available, just return.
        if(mGoogleMap == null) {
            return
        }
        // We will iterate all in the tracks map, and for each key and value, we'll update the corresponding Marker and Polyline accordingly.
        trackMap.forEach { (trackWithPath, objectPair) ->
            var trackMarker: Marker? = objectPair.first
            var trackPath: Polyline? = objectPair.second
            // Handle the track marker.
            if(trackMarker == null) {
                // If marker is null, we want to draw that marker.
                // TODO: extra marker creation options.
                val markerOptions: MarkerOptions = MarkerOptions()
                    .position(LatLng(trackWithPath.track.startPoint.latitude, trackWithPath.track.startPoint.longitude))
                trackMarker = mGoogleMap!!.addMarker(markerOptions)
            } else {
                // The marker is not null. We will just update it.
                // TODO: extra marker editing options.
                trackMarker.position = LatLng(trackWithPath.track.startPoint.latitude, trackWithPath.track.startPoint.longitude)
            }
            // Handle the track path.
            if(trackWithPath.path != null) {
                // If we have been given a track path, decide between creating a new path OR editing our existing one.
                if(trackPath == null) {
                    // We will create a new track path polyline here.
                    // TODO: extra polyline creation options.
                    val polylineOptions: PolylineOptions = PolylineOptions()
                        .addAll(trackWithPath.path!!.points.map { LatLng(it.latitude, it.longitude) })
                    trackPath = mGoogleMap!!.addPolyline(polylineOptions)
                } else {
                    // We will update the existing Polyline.
                    // TODO: extra polyline update options.
                    trackPath.points = trackWithPath.path!!.points.map { LatLng(it.latitude, it.longitude) }
                }
            } else {
                // There is no path. If we have a track path, delete it.
                trackPath?.remove()
            }
            // Set the track marker and polyline as the pair for this instance in track map.
            trackMap[trackWithPath] = Pair(trackMarker, trackPath)
        }
    }

    /**
     * Remove this track from the world object manager. This will first ensure the track is actually in the map, then will remove both the marker
     * and polyline from the google map, and finally will remove the entry entirely.
     */
    private fun removeTrack(trackWithPath: TrackWithPath) {
        if(!trackMap.contains(trackWithPath)) {
            return
        }
        // Get the map objects pair.
        val mapObjectsPair: Pair<Marker?, Polyline?>? = trackMap[trackWithPath]
        // Only execute any further logic if this pair exists.
        mapObjectsPair?.let { (marker, polyline) ->
            // Attempt remove both marker and polyline.
            marker?.remove()
            polyline?.remove()
            // Remove entry from tracks map.
            trackMap.remove(trackWithPath)
        }
    }
}