# HawkSpeed (Android App)

## Summary

A realtime, real-world mobile app that allows players to record and upload their own race tracks, then compete with other players for the best time on that track. This is of course not a marketable app, due to its dangerous nature. Currently the app is geographically locked to only work within Australia, I may change this at some stage, or you can. The primary goals for this project are:
* To showcase a complex multi-platform system,
* Consolidate a reliable workflow for employing SocketIO alongside Clean Architecture (work in progress,)
* Rewrite an old personal project of mine with the same name.

### Login / Registration

Each Player requires an account, identified by an email address and authenticated by a password. Once created, accounts could be required to verify their email. Then, they are able to set their profile up; that is, give themselves a friendly username, and select their first vehicle; described next.

<p align="center">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/255379764-7dea510f-9e48-4ada-acef-3e0b1919c7e3.png">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/255379766-85cdafbd-f036-42e2-9ec5-ee89bc5d9d0d.png">
</p>

### Vehicle selection
HawkSpeed requires the User have a vehicle to add to their profile. Eventually, this entity can contain further descriptive detail such as images or upgrades and such; which could form the basis for an exciting yet underground social network. Vehicle selection happens in 5 separate stages of cascading data points. You may find a very minimal version of the dataset used [here](imports/testdata/vehicles.json). The full dataset is private. The data points, in order, are;
1. User selects a Make of Vehicle (Toyota),
2. User selects a Type of Vehicle available therein (Car),
3. User selects a Model of Vehicle available therein (Supra),
4. User selects a Year of Vehicle available therein (1994),
5. User selects a Stock of Vehicle available therein; which is differentiated on the basis of criteria such as motor type, size, induction, transmission type, gears etc. (JZA80 3000 P TT MT6)

<p align="center">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/255379772-580c772b-1f39-4c58-b331-481b89edd21d.png">
    &emsp;&emsp;&emsp;&emsp;
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/255379759-91d4b09d-7daf-4c2c-acbf-9b1d3450e6d3.png">
</p>

### World Map
After logging in, a stateful connection utilising socket IO is launched in the background. This is where the player officially 'joins' the game. This status will last until that stateful connection is broken. Once joined, the player's device will send rapid location updates to the server, receiving in response an approval for their latest reported location, and a list of world objects in proximity to the approved location. Note that HawkSpeed does not utilise the device's reported location prior to server approval.

Alongside receiving world objects via proximity, the Player may also move their viewport, which in itself will trigger requests to collect world objects within those bounds. Below, the map has three race track markers reported. The tracks themselves are not visible because the device has not yet fetched (and therefore cached) those tracks at least once. Tapping a track marker will download the track's path. The menu button will access a navigation drawer with key functionality the User may like to utilise.

<p align="center">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/250266251-24aa8f0d-7e2e-4c7f-8544-8e3110e2cd73.png">
    &emsp;&emsp;&emsp;&emsp;
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/255379765-05cdf482-3001-4adb-af32-2b4d4ce5f80c.png">
</p>

Upon tapping a track from the world map, that track's path will be downloaded and immediately available for preview. The preview modal contains functionality to enter race mode, which will be described later. Tapping the track's information allows the viewing of the track in full detail.

<p align="center">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/255379771-b0447354-ba17-4996-8f3b-9dad3e85cd1d.png">
    &emsp;&emsp;&emsp;&emsp;
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/255379769-ad4c07e1-a226-4c72-87d8-15db375130ea.png">
</p>

### Track Leaderboard / Races

Once a Player successfully completes a race; as judged by the server, they are awarded a non-static position on that track's leaderboard. This, of course, can and will change as soon as other players complete the track and place higher. Irrespective, placements are paged and available in the second tab on the track's detail; while tapping a leaderboard entry will display the details of said successful race attempt. In case you're 🚔, these stats are manually generated; cmonnnn.

<p align="center">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/255379761-13778479-3cc2-482a-a71c-e28a8200f003.png">
    &emsp;&emsp;&emsp;&emsp;
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/255379756-2515c2cf-e5ef-4831-ba9b-5f018134d112.png">
</p>

### World / Racing

If a Player moves within acceptable distance and rotation to a track's start point, the opportunity to race the track will be offered to the player. This can be viewed on the track preview image displayed above. Once tapped, the race screen will become available; which will demand a stricter adherence to the track's start point. Once a race is initiated, this intent will become a request for a new race, sent as soon as the countdown is complete. From the time the race starts, until the server determines a finished, cancelled or disqualified outcome, time is recorded from each update sent and calculations are performed to ensure the player does not drastically deviate from the course. Once the race has an outcome, it is saved and can be viewed from the track's leaderboard.

### Recording

A race track may be recorded (right now) anywhere, as long as the Player has permission to do so (granted by the server) and the track location lands within area of use for EPSG 3112 (Australia). From the world map, players can start the recorder interface. Once recording is started, a track may be driven at any pace. The points recorded are submitted to the server, which validates and normalises the track. Finally, the track is set as verified, and players can find and race it.

As part of the normalisation process, HawkSpeed may optionally utilise the Google Maps Snap-To-Roads API to achieve a cleaner track. This functionality will eventually be available via Celery background work, but for now, only a base implementation is written and tested.

## Android App

This project is the Android app platform. I have designed the app based on the Clean Architecture principles, in a multi-module configuration. I am making significant use of the following packages and extensions:
* Jetpack Compose,
* Flows,
* Dagger Hilt for dependency injection,
* Room,
* Retrofit,
* OkHttp,
* Paging library,
* Timber,
* SocketIO.

## Setup

Ensure you set your Google Maps API key in local.properties!
MAPS_API_KEY=<API KEY HERE>

## Disclaimer

Obviously don't attempt to turn this app into anything more than a cool project. Streets are designed for commuting, not for proving anything. Unless of course, the aforementioned is legal wherever you are; in which case please invite me too.

## Special Thanks

1. https://gpx.studio/ - A great help in the formulation of race tracks and player races.
