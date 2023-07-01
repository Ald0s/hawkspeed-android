# HawkSpeed (Android App)

## Summary

This project is still in a very early stage. A realtime, real-world mobile app that allows players to record and upload their own race tracks, then compete with other players for the best time on that track. This is of course not a marketable app, due to its dangerous nature. Currently the app is geographically locked to only work within Australia, I may change this at some stage, or you can. The primary goals for this project are:
* To showcase a complex multi-platform system,
* Consolidate a reliable workflow for employing SocketIO alongside Clean Architecture (work in progress,)
* Rewrite an old personal project of mine with the same name.

### Login / Registration

Each Player requires an account, identified by an email address and authenticated by a password. Once created, accounts could be required to verify their email. Then, they are able to set their profile up; that is, give themselves a friendly username, and a hint on the vehicle they will use.

<p align="center">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/250266174-7c426502-6266-4425-a646-2537bdf7b5cf.png">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/250266242-b13a1aee-3fde-4bb8-a6c2-2b068213038d.png">
</p>

After logging in, a stateful connection utilising socket IO is launched in the background. This is where the player officially 'joins' the game. This status will last until that stateful connection is broken. Once joined, the player's device will send rapid location updates to the server, receiving in response an approval for their latest reported location, and a list of world objects in proximity to the approved location. Note that HawkSpeed does not utilise the device's reported location prior to server approval.

### World Map

Alongside receiving world objects via proximity, the Player may also move their viewport, which in itself will trigger requests to collect world objects within those bounds. Below, the map has three race track markers reported. The tracks themselves are not visible because the device has not yet fetched (and therefore cached) those tracks at least once. Tapping a track marker will download the track's path (note it has appeared in the background of preview image), and display a small preview dialog.

<p align="center">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/250266251-24aa8f0d-7e2e-4c7f-8544-8e3110e2cd73.png">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/250266245-75374088-8118-49fe-9eaf-9517966c4397.png">
</p>

Tapping the 'About' section of this preview will show that track, in detail. This interface allows players to view the track's full leaderboard, leave comments and either a like or dislike rating. 

<p align="center">
    <img width="300px" src="https://user-images.githubusercontent.com/13589397/250266244-c961a59f-d2c2-4f6e-b04c-5ca9b9d0ada3.png">
</p>

### World / Racing

If a Player moves within acceptable distance to a track's start point, the opportunity to race the track will be offered to the player. Once accepted, this intent will become a request for a new race, which will be sent as soon as the countdown is complete. From the time the race starts, until the server determines a finish, cancelled or disqualified outcome, time is recorded from each update sent and calculations are performed to ensure the player does not drastically deviate from the course. Once the race has an outcome, it is saved and can be viewed from the track's leaderboard.

### Recording

A race track may be recorded (right now) anywhere, as long as the Player has permission to do so (granted by the server) and the track location lands within area of use for EPSG 3112 (Australia). From the world map, players can start the recorder interface. Once recording is started, a track may be driven at any pace. The points recorded are submitted to the server, which validates and normalises the track. Finally, the track is set as verified, and players can find and race it.

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
