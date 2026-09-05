# Changelog

All notable changes to Changelist Sorter, a fork and continuation of
[4ch1m/ChangelistOrganizer](https://github.com/4ch1m/ChangelistOrganizer) by Achim Seufert.
Entries below 2.0.0 are from the original project and are retained for provenance.

## [Unreleased]

## [2.0.0]

### Changed
- forked and renamed to "Changelist Sorter"; published under a new plugin ID
  (`io.github.afa_simoes.changelistsorter`) as a separate Marketplace listing
- settings storage is unchanged, so existing Changelist Organizer rules are picked up automatically
- **raised the minimum supported IDE version to 2025.2** (build 252); the plugin no longer
  installs on 2022.2–2025.1
- rebuilt on the IntelliJ Platform Gradle Plugin 2.x, Gradle 9, and a Java 21 toolchain
- rewrote the settings UI with platform components (`ToolbarDecorator`/`TableView`) instead of
  a hand-rolled GUI-Designer form
- `Cancel` in the settings dialog now correctly discards in-progress edits instead of writing
  them straight into persisted state, and `Apply` now re-disables itself after being pressed
- automatic organizing no longer risks re-entering itself through its own change-list listener,
  and no longer shows more than one confirmation dialog per run, or on a VCS callback thread

### Fixed
- **corrected wildcard-to-regex pattern matching**: `?` now matches exactly one character
  (previously it matched zero-or-one, so e.g. `Foo?.txt` incorrectly matched `Foo.txt`), and
  regex metacharacters in a pattern (`( ) [ ] + $ ^ { } |`) are now escaped instead of being
  interpreted as regex syntax
- a file's current changelist is now null-checked before use, instead of being unconditionally
  dereferenced

## [1.10.3]

### Changed
- removed/replaced deprecated function calls 
- dependency updates
- Gradle-wrapper update

## [1.10.2]

### Changed
- dependency updates
- Gradle-wrapper update

## [1.10.1]

### Changed
- various minor improvements (unit tests re-activated; buildfile refactoring)
- dependency updates
- Gradle-wrapper update

## [1.10.0]

### Changed
- dependency updates
- Gradle-wrapper update
- build script refactored
- license change (GNU -> MIT)

## [1.9.0]

### Changed
- updated code, due to API-deprecation warnings
- upgraded dependencies
- Gradle-wrapper update

## [1.8.0]

### Added
- new feature: automatically organize upon additions to changelist

## [1.7.0]

### Changed
- refactorings to get rid of deprecated API-calls/-usage
- raised min-version of compatibility to 201 (202013)
- other minor code improvements
- Gradle-wrapper update

## [1.6.0]

### Fixed
- fixed regex-creation for patterns containing dots

## [1.5.0]

### Changed
- added 'vcs' module-dependency (improving product compatiblity)

## [1.4.0]

### Changed
- migration to Gradle-based plugin/project
- minor code changes/updates

## [1.3.0]

### Changed
- minor code refactorings
- improved/enhanced unit-tests

### Fixed
- small bugfix (don't try to delete an empty changelist if it's the default-changelist)

## [1.2.1]

### Added
- unit-testing for source-code

### Changed
- minor code cosmetics

## [1.2.0]

### Added
* apply organizer-items on current default-changelist only

### Fixed
* bugfixes

## [1.1.0]

### Changed
- organizer-items can be ordered now

### Added
- stop applying items on a file after first match
- remove empty changelists

## [1.0.0]

### Changed
- initial release
