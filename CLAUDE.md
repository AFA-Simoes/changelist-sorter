# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

IntelliJ Platform plugin ("Changelist Sorter", a fork/continuation of "Changelist Organizer",
published on the JetBrains Marketplace under a separate listing/ID). It moves modified files
into user-defined changelists based on wildcard patterns. Java 21, Gradle Kotlin DSL,
`org.jetbrains.intellij.platform` Gradle plugin (2.x), targeting IntelliJ 2025.2+.

## Commands

```bash
./gradlew build                              # compile + test + build plugin
./gradlew test                               # run all unit tests (JUnit 5)
./gradlew test --tests "io.github.afa_simoes.changelistsorter.organize.RuleMatcherTest"
./gradlew runIde                             # launch a sandbox IDE with the plugin installed
./gradlew buildPlugin                        # produce distributable zip in build/distributions
./gradlew verifyPluginStructure              # plugin.xml/archive structure checks
./gradlew verifyPluginProjectConfiguration   # sinceBuild/toolchain/platform-version consistency
./gradlew verifyPlugin                       # IntelliJ Plugin Verifier against recommended() IDEs
./gradlew dependencyUpdates                  # report outdated dependencies (pre-release versions rejected)
./gradlew publishPlugin                      # reads the token from the PUBLISH_TOKEN env var
```

No linter is configured.

## Release / versioning

`gradle.properties` is the single source of truth for `pluginVersion`, `pluginName`, `pluginDescription`, `pluginSinceBuild`, `platformVersion`, and `javaVersion` — `build.gradle.kts` reads all of them via `providers.gradleProperty(...)` and patches `plugin.xml` at build time. `plugin.xml` therefore contains no `<version>`, `<description>`, or `<idea-version>` element; do not add them.

Release notes come from `CHANGELOG.md` via the `org.jetbrains.changelog` plugin: the section matching `pluginVersion` is rendered into `<change-notes>`. A version bump means editing `gradle.properties` **and** adding the matching `## [x.y.z]` section to `CHANGELOG.md`, or the build's `getLatest()` lookup breaks.

## Persistence-compatibility constraint (read this before renaming anything in `settings/`)

`ProjectSettingsService` persists project state to `changelistorganizer_project.xml` via
`@State(name = "ChangelistOrganizerProjectSettings", storages = @Storage("changelistorganizer_project.xml"))`.
`XmlSerializer` writes the XML using that `@State` name, the bean's property names, and the
**simple class name** of the list element (`ChangelistOrganizerItem`) — none of that is tied to
the Java package. Renaming `ProjectSettings`/`ChangelistOrganizerItem`, changing a property name,
or changing the `@State`/`@Storage` string literals silently discards every user's configured
rules on upgrade (both this plugin's own prior releases, and the original "Changelist Organizer"
plugin this one forked from, which shares the same file format). If a class genuinely must be
renamed or moved, pin its serialized name with `@Tag("...")` from `com.intellij.util.xmlb.annotations`
first. `SettingsPersistenceTest` locks the serialized XML shape — treat a failure there as a
migration-breaking change, not a fixture to update casually.

## Architecture

Everything funnels through `ChangelistOrganizerService`, a project-level service
(`project.getService(ChangelistOrganizerService.class)` / `ChangelistOrganizerService.getInstance(project)`).
Three call sites trigger `organize()`:

- `action/OrganizeChangesAction` — one class, registered twice in `plugin.xml` under the two
  original action IDs (`ChangesViewToolbarAction` for the toolbar button, `SelectedChangelistPopupAction`
  for the changes-view context menu), so existing keymap customisations survive
- `listener/OrganizeOnChangeListener` — registered declaratively via `<projectListeners>`
  (topic `ChangeListListener`), not an eagerly-run `postStartupActivity`; on `changesAdded` it
  dispatches to the service via `invokeLater` (never runs anything modal on the VCS callback
  thread) if `automaticallyOrganize` is set

`organize()` is plan-then-apply, guarded by an `AtomicBoolean` against reentrancy (applying a
move fires `changesAdded` on the listener above, which can re-enter `organize()` through the
very run that triggered it — `synchronized` would not help, since that callback runs on the same
thread):

1. **Plan** (`plan(ProjectSettings, ChangeListManager)`, package-private for testing) — iterates
   `ChangeListManager.getAffectedFiles()`, null-checks `getChangeList(file)` (it's `@Nullable`),
   applies the `onlyApplyItemsOnDefaultChangelist` filter, and asks `RuleMatcher` for matches.
   Returns an `OrganizePlan` (a list of moves), without touching platform state.
2. **Confirm once** — if any planned move asks for confirmation, a single `Messages.showOkCancelDialog`
   summarises all of them (not one dialog per file), always via `invokeAndWait`.
3. **Apply** — every `addChangeList`/`moveChangesTo` in one batch, then the
   `removeEmptyChangelists` sweep.

`RuleMatcher` is the pure, platform-state-free matching core (fully unit-tested in isolation).
It compiles each enabled, non-blank rule's wildcard pattern once at construction (`*` → any run
of characters, `?` → exactly one character, everything else — including regex metacharacters
like `( ) [ ] + $ ^ { } |` and Windows-path backslashes — escaped and matched literally). Item
order is user-controlled (up/down in settings) and matters: `stopApplyingItemsAfterFirstMatch`
returns only the first matching rule instead of every match. A rule matches against the file's
name, or — when `checkFullPath` is set — a caller-supplied path string; `ChangelistOrganizerService`
resolves that to the project-relative forward-slash path via `VfsUtilCore.getRelativePath`,
falling back to the absolute path.

### Settings

`ProjectSettings` (a Lombok `@Data` POJO) is persisted per project by `ProjectSettingsService`, a `PersistentStateComponent` writing `changelistorganizer_project.xml` (see the persistence-compatibility constraint above). Read settings anywhere via `ProjectSettings.storedSettings(project)`, which tolerates both a null service and a null `getState()` by returning defaults. `ProjectSettingsConfigurable` (registered as `projectConfigurable` under the "tools" group) wires `ProjectSettingsPanel` — a `ToolbarDecorator`/`TableView` built in code, no GUI-Designer `.form` file — to that state via `getState()`/`setState(ProjectSettings)`. Both directions deep-copy `ChangelistOrganizerItem`s (its copy constructor exists for exactly this), so the table never aliases live persisted objects: `reset()` loads copies in, `apply()` writes copies out through `ProjectSettingsService.loadState(...)`, and `isModified()` is `!panel.getState().equals(ProjectSettings.storedSettings(project))` (Lombok's generated `equals`) rather than a manually-tracked dirty flag.

### Conventions

- Lombok is `compileOnly` + `annotationProcessor` for both main and test source sets; `@Data`/`@NoArgsConstructor`/`@AllArgsConstructor` are used for the settings/item POJOs.
- All user-visible strings go through `ChangelistOrganizerBundle.message(key, ...)` (extends `com.intellij.DynamicBundle`), backed by `src/main/resources/messages/ChangelistOrganizerBundle.properties`. Icons are typed constants on `ChangelistOrganizerIcons` (e.g. `ChangelistOrganizerIcons.ORGANIZE`), loaded from `resources/icons/`.
- Tests are JUnit 5 + Mockito. `RuleMatcherTest` and most of `ChangelistOrganizerServiceTest` are plain assertions against injected settings/mocks — no platform statics to mock, now that matching logic is pure. `SettingsPersistenceTest` round-trips `ProjectSettings` through `com.intellij.util.xmlb.XmlSerializer` to lock the on-disk XML shape (see the persistence-compatibility constraint above). Note: the IntelliJ Platform Gradle Plugin's sandboxed test runner needs `junit:junit:4.13.2` on the test runtime classpath even though the suite itself is JUnit 5 (it bridges through `org.junit.runners.model.Statement` internally) — that's `testRuntimeOnly("junit:junit:4.13.2")` in `build.gradle.kts`, not an invitation to write JUnit 4 tests.
- Never commit anything for me. It should be always me commiting and pushing changes
