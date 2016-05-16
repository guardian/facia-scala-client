#### 1.6.0

  - Support the marking of collections to indicate that they should have commercial branding.
  
#### 1.5.3

  - Add sbt project that compiles against play-json 2.5.x
  
#### 1.5.2

  - Add homan-square tag to special report card style

#### 1.5.1

  - Changed FAPI object methods with implicit content api parameter to use ContentApiClientLogic instead of GuardianContentClient

#### 1.5.0

  - Update capi client to v.8.2
  - Added ImageReplace to FaciaImage

#### 1.3.0

  - Use hashed strings to check for special report card style

#### 1.2.0

  - Returns an empty list of backfill collections if collection backfill id is invalid
  - Return an empty backfill if backfill type is invalid
  - Returns the api query of a parent backfill collections

#### 1.1.0

  - Add metadata to collections

#### 1.0.0

  __Non backward compatible release__

  - Stop cross compiling, the client is now only available on scala 2.11
  - Remove `backfill` deprecated in 0.67
  - Update to play 2.4.6
  - Update aws to v1.10.58
  - Update capi client to v.7.29

#### 0.70

  - Update aws to v1.10.49
  - Update capi client to v.7.22

#### 0.69

  - Update aws to v1.10.47
  - Update capi client to v.7.16

#### 0.68

  - Change the signature of `FAPI.backfillFromConfig` from ~~`FAPI.backfillFromConfig(collection: Collection)`~~ to `FAPI.backfillFromConfig(collectionConfig: CollectionConfig)`

#### 0.67

  - Add `backfill` in collectiong config. This is meant to replace `apiQuery` in future releases.
  - The method `FAPI.backfill(apiQuery: String, collection: Collection)` has been deprecated, `FAPI.backfillFromConfig(collection: Collection)` should be used instead as it automatically handles the backfill query and returns an empty list in case no backfill is set.


#### 0.66

  - Invalid release, don't use

#### 0.65

  - Add This Is The NHS Tag to Special Report

#### 0.64

  - Upgrade capi client to v7.13

#### 0.63

  - Upgrade capi client to v7.12

#### 0.62

  - Upgrade capi client to v7.11

#### 0.61

  - Upgrade capi client to v7.3

#### 0.60

  - Fix the batching so that it does not request more than 50 at a time in a search query. There is a restriction in CAPI of 50 items in a search query.

#### 0.59

  - Adds a publishedBy to Trail

#### 0.58

  - Adds Collections.hideShowMore

#### 0.57

  - Adds Front.group meta
  - Adds Collections.hideShowMore

#### 0.56

  - Adds a convenience function: ResolvedMetaData.toMap
  - Changes ResolvedMetaData.fromContent to a non-private methods
  - Adds CardStyle.fromContent convenience method for CardStyle

#### 0.55

  - Shows the breaking news kicker even when tonal kickers are supressed

#### 0.54

  - Changes queries to not add editorsPicks to the head of the list of backfill by default


#### 0.53

  - Adds showLivePlayable content meta


#### 0.52

  - Adds "australia-news/series/healthcare-in-detention" tag to resolve to SpecialReport style
