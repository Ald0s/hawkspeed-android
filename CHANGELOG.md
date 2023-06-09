# Changelog

## Version 1.10.00
* Added changelog,
* Rolled out Jetpack Compose instead of Views,
* Improved world socket state setup process(es),
* Changed all mapper base interfaces to support list mappings,
* Redesigned track caching,
* Created world repository for collecting changes to world objects as one unit of work,
* Basic preview dialog that will be used for tracks,

## Version 1.10.05
* Added set/clear ratings use cases & repo functionality,
* Added track comment support, by creating data & domain layer functionality & adding dao
* Added top leaderboard to Track, so top three entries are always available,
* Added basic track preview dialog for world map,
* Added basic registration screen,
* Removed /api/ prefix from service components,
* Added socket type resource error,
* Added race cancellation support,
* Began implementing Track recording and creation screens & functionality.

## Version 1.10.06
* Implemented track draft local data source,
* Settled on dark theme-only for HawkSpeed,
* Implemented the track racing view model & all supporting functionality,
* Wrote instrumented tests for track recording & track racing to prove the logic works,
* Updated changelog to reflect latest details,
* Added track types,
* Added maps style,
* Added various icons,
* Progressed overall UI,
* Updated README.

## Version 1.10.10
* Added Vehicle model and inserted where applicable in race leaderboard results,
* Added Vehicle repository and data sources,
* Added vehicle selection availability to race view model, and as requirement for requesting a new race,
* Handled various race start fail cases,
* Improved world map UIs,
* Added support for installation-unique identifier when requesting join to world,
* Began implementing error management in world socket state,
* Pruned comments.

## Version 1.10.11
* Collapsed multiple types of viewing race tracks into a composable,
* Abstract-friendly way of previewing world objects,
* Changed race+record camera to utilise a 'chase' mode instead of straight down,
* Touched up various animations between camera modes,
* Changed world map standard mode's UI to show world object previews in a modalbottomsheet instead of a modal dialog,
* Changed race & record mode's UI to show their controls in a bottom sheet w/ bottomsheetscaffold,
* Implemented basic controls on both race and record mode.

## Version 1.10.12
* Added support for storing race percent complete, laps complete, average speed,
* Made track type required for creating new track draft, since we want to support multiple track types at some point,
* Creating a new track can now possibly return its path, if server does not need long running verification. Therefore, also moved submitNewTrack from TrackRepository to TrackPathRepository,
* Changed minSdk to 28 due to lack of a newer physical device,
* Started rolling out new workflow for two way screens where forms are involved - attempting to always combine into a single outgoing state,
* Changed all flows & async work to run on IO dispatcher where network/IO is used, added qualifier for providing IO dispatcher from common module.

## Version 1.10.13
* Added separate WorldMapUiState for connecting to game server,
* Moved scrollable from TrackDetail scaffold to details tab screen,
* Decided to integrate device sensors for true rotation instead of co-opting location bearing; changed location access state container to "device aptitude" container to better generalise purpose,
* Added necessary functionality to main activity for determining availability of sensors and added functionality to world service for accelerometer,
* Basic implementation of User's current orientation complete. Solution is buggy and requires low pass filter, but device is now relied on for direction of view,
* Started implementing proper access for configured game settings via cache; may change to preferences,
* Changed GetLeaderboardEntryUseCase to GetCachedLeaderboardEntryUseCase for selecting only from cache, and created another GetLeaderboardEntryUseCase for querying the most up to date version of the race outcome,
* Added RaceRemoteData source for querying current status of a race,
* Added a detail screen for viewing a specific race's detail.