# HawkSpeed (Android App)

## Summary

This project is still in a very early stage. A realtime, real-world mobile app that allows players to record and upload their own race tracks, then compete with other players for the best time on that track. This is of course not a marketable app, due to its dangerous nature. Currently the app is geographically locked to only work within Australia, I may change this at some stage, or you can. The primary goals for this project are:
* To showcase a complex multi-platform system,
* Consolidate a reliable workflow for employing SocketIO alongside Clean Architecture (work in progress,)
* Rewrite an old personal project of mine with the same name.

### Login / Registration

Each Player requires an account, identified by an email address and authenticated by a password. Each account, for completion, requires a username to be chosen and set. After logging in, a Google Maps view is used to display all race tracks within the device's view. In the background, consistent location updates are taken and sent, via socket, to the server.

### World / Racing

If a Player moves within acceptable distance to a track's start point, the opportunity to race the track will be offered to the player. Once accepted, this intent will be communicated to the server at the same time the start is counted down. From the time the race starts, until the server determines either a finish, cancelled or disqualified outcome, time is recorded from each update sent. Once the race has an outcome, it is saved and can be viewed within the leaderboard by accessing the Race's details interface.

### Recording

A race track may be recorded (right now) anywhere, as long as the Player has permission to do so (granted by the server) and the track location lands within area of use for EPSG 3112. They may select a location and start the recorder interface. At which point, the track may be driven at any pace. The points recorded are submitted to the server, which validates and normalises the track. This is then added as a verified race track to the world and is able to be raced.

## Android App

This project is the Android app platform, at the present time, very few user interfaces are designed. I have designed the app based on the Clean Architecture principles, in a multi-module configuration. I am making significant use of the following packages and extensions:
* Flows,
* Dagger Hilt for dependency injection,
* Room,
* Retrofit,
* OkHttp,
* Paging library,
* Timber,
* SocketIO.

## Disclaimer

Obviously don't attempt to turn this app into anything more than a cool project. Streets are designed for commuting, not for proving anything. Unless of course, the aforementioned is legal wherever you are; in which case please invite me too.

## Special Thanks

1. https://gpx.studio/ - A great help in the formulation of race tracks and player races.
