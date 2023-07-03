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