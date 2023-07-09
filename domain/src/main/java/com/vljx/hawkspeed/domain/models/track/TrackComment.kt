package com.vljx.hawkspeed.domain.models.track

import android.annotation.SuppressLint
import com.vljx.hawkspeed.domain.models.user.User
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class TrackComment(
    val commentUid: String,
    val created: Int,
    val text: String,
    val user: User
) {
    /**
     * Convert the result of subtracting the current time from the timestamp for when this comment was created to a pretty string in the format; 12h 34m ago
     * if there are less than 28 days. After 28 days, simply return a date string formatted like 10/08/2023, no need to specify time honestly.
     */
    val createdDurationDescription: String
        @SuppressLint("SimpleDateFormat")
        get() = (System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS).minus(created.toDuration(DurationUnit.SECONDS)))
            .let { sinceCreatedDuration ->
                if(sinceCreatedDuration.inWholeDays >= 28) {
                    // Return a formatted date string.
                    return@let Date(created * 1000L)
                        .let { date ->
                            SimpleDateFormat("dd/MM/yyyy")
                                .format(date)
                        }
                }
                // Return a pretty string.
                sinceCreatedDuration.toComponents { days, hours, minutes, seconds, _ ->
                    if(days > 0) {
                        if(hours > 0) {
                            return@let "${days}d ${hours}h ago"
                        }
                        return@let "${days}d ago"
                    } else if(hours > 0) {
                        if(minutes > 0) {
                            return@let "${hours}h ${minutes}m ago"
                        }
                        return@let "${days}h ago"
                    } else if(minutes > 0) {
                        if(seconds > 0) {
                            return@let "${minutes}m ${seconds}s ago"
                        }
                        return@let "${minutes}m ago"
                    } else if(seconds > 10) {
                        return@let "${seconds}s ago"
                    }
                    return@let "moments ago"
                }
            }
}